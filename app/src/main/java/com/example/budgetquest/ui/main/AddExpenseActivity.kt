package com.example.budgetquest.ui.main

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.budgetquest.data.database.Category
import com.example.budgetquest.data.database.Expense
import com.example.budgetquest.data.repository.BudgetRepository
import com.example.budgetquest.databinding.ActivityAddExpenseBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var repository: BudgetRepository
    private var userId = -1

    private var selectedDate = ""
    private var selectedStartTime = ""
    private var selectedEndTime = ""
    private var photoPath: String? = null
    private var categories = listOf<Category>()
    private var cameraImageUri: Uri? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val TAG = "AddExpenseActivity"
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        Log.d(TAG, "Camera result: $success")
        if (success && cameraImageUri != null) {
            val it = null
            binding.ivPhotoPreview.setImageURI(it)
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.cardPhotoPreview.visibility = View.VISIBLE
            photoPath = cameraImageUri.toString()
            Toast.makeText(this, "Photo captured!", Toast.LENGTH_SHORT).show()
        } else {
            Log.d(TAG, "Camera cancelled or failed")
        }
    }

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            Log.d(TAG, "Gallery image selected: $it")
            binding.ivPhotoPreview.setImageURI(it)
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.cardPhotoPreview.visibility = View.VISIBLE
            photoPath = it.toString()
            Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Log.d(TAG, "Camera permission granted")
            openCamera()
        } else {
            Toast.makeText(
                this,
                "Camera permission is needed to take photos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = BudgetRepository(this)
        val prefs = getSharedPreferences("BudgetQuestPrefs", MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        Log.d(TAG, "AddExpenseActivity started for userId: $userId")

        setupDatePicker()
        setupTimePickers()
        setupSeekBar()
        setupPhotoButtons()
        loadCategories()
        setupSaveButton()
    }

    private fun setupDatePicker() {
        binding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                    binding.btnSelectDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePickers() {
        binding.btnStartTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    selectedStartTime = String.format("%02d:%02d", hour, minute)
                    binding.btnStartTime.text = "Start: $selectedStartTime"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        binding.btnEndTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    selectedEndTime = String.format("%02d:%02d", hour, minute)
                    binding.btnEndTime.text = "End: $selectedEndTime"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupSeekBar() {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        binding.seekBarGoal.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    binding.tvSeekBarValue.text = format.format(progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
    }

    private fun setupPhotoButtons() {
        binding.btnCamera.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openCamera()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                ) -> {
                    Toast.makeText(
                        this,
                        "Camera permission needed for photos",
                        Toast.LENGTH_LONG
                    ).show()
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "com.example.budgetquest.fileprovider",
                photoFile
            )
            Log.d(TAG, "Opening camera with URI: $cameraImageUri")
            cameraLauncher.launch(cameraImageUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}")
            Toast.makeText(
                this,
                "Error opening camera: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        storageDir?.mkdirs()
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).also {
            photoPath = it.absolutePath
            Log.d(TAG, "Created image file: ${it.absolutePath}")
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val cats = repository.getCategoriesOnce(userId).toMutableList()

            if (cats.isEmpty()) {
                val defaults = listOf(
                    "Food & Dining", "Transportation", "Shopping",
                    "Entertainment", "Bills & Utilities",
                    "Healthcare", "Education", "Other"
                )
                defaults.forEach { name ->
                    val cat = Category(userId = userId, name = name)
                    repository.insertCategory(cat)
                }
                categories = repository.getCategoriesOnce(userId)
            } else {
                categories = cats
            }

            runOnUiThread {
                val adapter = ArrayAdapter(
                    this@AddExpenseActivity,
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name }
                )
                adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
                binding.spinnerCategory.adapter = adapter
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveExpense.setOnClickListener {
            val amountText = binding.etAmount.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (amountText.isEmpty()) {
                binding.etAmount.error = "Please enter an amount"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                binding.etDescription.error = "Please enter a description"
                return@setOnClickListener
            }
            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedStartTime.isEmpty()) {
                Toast.makeText(this, "Please select a start time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedEndTime.isEmpty()) {
                Toast.makeText(this, "Please select an end time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categories.isEmpty()) {
                Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.etAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            val selectedCategoryIndex = binding.spinnerCategory.selectedItemPosition
            val categoryId = if (categories.isNotEmpty())
                categories[selectedCategoryIndex].id else 0
            val isIncome = binding.switchIncome.isChecked

            val expense = Expense(
                userId = userId,
                categoryId = categoryId,
                amount = amount,
                description = description,
                date = selectedDate,
                startTime = selectedStartTime,
                endTime = selectedEndTime,
                photoPath = photoPath,
                isIncome = isIncome
            )

            lifecycleScope.launch {
                repository.insertExpense(expense)
                Log.d(TAG, "Expense saved: $description, amount: $amount")

                // Award 10 points and update streak
                val user = repository.getUserById(userId)
                user?.let {
                    val today = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(Date())

                    // Calculate new streak
                    val newStreak = if (it.lastLoginDate == today) {
                        it.streak
                    } else {
                        it.streak + 1
                    }

                    // Calculate new level (every 100 points = new level)
                    val newPoints = it.points + 10
                    val newLevel = (newPoints / 100) + 1

                    val updatedUser = it.copy(
                        points = newPoints,
                        level = newLevel,
                        streak = newStreak,
                        lastLoginDate = today
                    )
                    repository.updateUser(updatedUser)
                    Log.d(TAG, "User updated: points=$newPoints, level=$newLevel, streak=$newStreak")
                }

                runOnUiThread {
                    Toast.makeText(
                        this@AddExpenseActivity,
                        "Saved! +10 points earned",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }
}