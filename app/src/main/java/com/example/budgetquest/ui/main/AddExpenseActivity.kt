package com.example.budgetquest.ui.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

    // Stores selected values
    private var selectedDate = ""
    private var selectedStartTime = ""
    private var selectedEndTime = ""
    private var photoPath: String? = null
    private var categories = listOf<Category>()
    private var cameraImageUri: Uri? = null

    // Camera launcher — opens camera and waits for result
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let {
                binding.ivPhotoPreview.setImageURI(it)
                binding.ivPhotoPreview.visibility = android.view.View.VISIBLE
                photoPath = it.toString()
            }
        }
    }

    // Gallery launcher — opens gallery and waits for result
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            binding.ivPhotoPreview.setImageURI(it)
            binding.ivPhotoPreview.visibility = android.view.View.VISIBLE
            photoPath = it.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = BudgetRepository(this)
        val prefs = getSharedPreferences("BudgetQuestPrefs", MODE_PRIVATE)
        userId = prefs.getInt("userId", -1)

        setupDatePicker()
        setupTimePickers()
        setupSeekBar()
        setupPhotoButtons()
        loadCategories()
        setupSaveButton()
    }

    // Opens a calendar date picker dialog
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

    // Opens time picker dialogs for start and end time
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

    // SeekBar updates the displayed value as user slides it
    private fun setupSeekBar() {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        binding.seekBarGoal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSeekBarValue.text = format.format(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // Camera and gallery buttons
    private fun setupPhotoButtons() {
        binding.btnCamera.setOnClickListener {
            val photoFile = createImageFile()
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "com.example.budgetquest.fileprovider",
                photoFile
            )
            cameraLauncher.launch(cameraImageUri)
        }

        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

    // Creates a file in the Pictures folder to store the camera photo
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).also {
            photoPath = it.absolutePath
        }
    }

    // Loads categories from database into the spinner dropdown
    private fun loadCategories() {
        lifecycleScope.launch {
            val cats = repository.getCategoriesOnce(userId).toMutableList()

            // Add default categories if none exist yet
            if (cats.isEmpty()) {
                val defaults = listOf(
                    "Food & Dining", "Transportation", "Shopping",
                    "Entertainment", "Bills & Utilities", "Healthcare", "Education", "Other"
                )
                defaults.forEach { name ->
                    val cat = com.example.budgetquest.data.database.Category(
                        userId = userId, name = name
                    )
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
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveExpense.setOnClickListener {
            val amountText = binding.etAmount.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            // Validate all fields
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
            val categoryId = categories[selectedCategoryIndex].id
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

                // Award points for logging a transaction
                val user = repository.getUserById(userId)
                user?.let {
                    val updatedUser = it.copy(points = it.points + 10)
                    repository.updateUser(updatedUser)
                }

                runOnUiThread {
                    Toast.makeText(
                        this@AddExpenseActivity,
                        "Expense saved! +10 points",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }
}