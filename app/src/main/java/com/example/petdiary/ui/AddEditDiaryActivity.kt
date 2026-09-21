package com.example.petdiary.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.petdiary.data.AppDatabase
import com.example.petdiary.data.DiaryEntry
import com.example.petdiary.databinding.ActivityAddEditDiaryBinding
import com.example.petdiary.repository.PetRepository
import com.example.petdiary.viewmodel.PetViewModel
import com.example.petdiary.viewmodel.PetViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.petdiary.data.EventType

class AddEditDiaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditDiaryBinding
    private lateinit var viewModel: PetViewModel
    private var petId: Long = -1L
    private var entryId: Long = -1L
    private var existingEntry: DiaryEntry? = null

    private var selectedType: EventType = EventType.OTHER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditDiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)         // 1. setContentView

        setSupportActionBar(binding.toolbar) // 2. setSupportActionBar
        binding.toolbar.title = "Новая запись" // или "Редактировать запись"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val database = AppDatabase.getDatabase(this)
        val repository = PetRepository(database.petDao(), database.diaryEntryDao())
        viewModel = ViewModelProvider(this, PetViewModelFactory(repository))[PetViewModel::class.java]

        petId = intent.getLongExtra("petId", -1L)
        entryId = intent.getLongExtra("entryId", -1L)

        val today = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        binding.editDate.setText(today)

        if (entryId != -1L) {
            binding.toolbar.title = "Редактировать запись"
            viewModel.getEntriesForPet(petId).observe(this) { entries ->
                if (existingEntry == null) {
                    val entry = entries.find { it.id == entryId }
                    if (entry != null) {
                        existingEntry = entry
                        binding.editDate.setText(entry.date)
                        binding.editTitle.setText(entry.title)
                        binding.editDescription.setText(entry.description)
                        entry.weight?.let { binding.editWeight.setText(it.toString()) }

                        // Восстанавливаем выбранный тип
                        val type = runCatching { EventType.valueOf(entry.eventType) }
                            .getOrNull() ?: EventType.OTHER
                        selectType(type)
                    }
                }
            }
        } else {
            binding.toolbar.title = "Новая запись"
        }

        binding.buttonSave.setOnClickListener { saveEntry() }
        binding.iconFeeding.setOnClickListener { selectType(EventType.FEEDING) }
        binding.iconWalk.setOnClickListener { selectType(EventType.WALK) }
        binding.iconMedicine.setOnClickListener { selectType(EventType.MEDICINE) }
        binding.iconCheckup.setOnClickListener { selectType(EventType.CHECKUP) }

    }

    private fun saveEntry() {
        val date = binding.editDate.text.toString().trim()
        val entryTitle = binding.editTitle.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()
        val weightText = binding.editWeight.text.toString().trim()
        val weight = weightText.toDoubleOrNull()

        if (entryTitle.isEmpty()) {
            binding.editTitle.error = "Введите заголовок записи"
            return
        }

        if (entryId != -1L && existingEntry != null) {
            // Редактирование существующей записи
            viewModel.updateEntry(
                existingEntry!!.copy(
                    date = date,
                    title = entryTitle,
                    description = description,
                    weight = weight,
                    eventType = selectedType.name
                )
            )
        } else {
            // Новая запись
            viewModel.insertEntry(
                DiaryEntry(
                    petId = petId,
                    date = date,
                    title = entryTitle,
                    description = description,
                    weight = weight,
                    eventType = selectedType.name
                )
            )
        }
        finish()
    }

    private fun selectType(type: EventType) {
        selectedType = type
        // Подсветка выбранного
        binding.iconFeeding.alpha = if (type == EventType.FEEDING) 1f else 0.4f
        binding.iconWalk.alpha = if (type == EventType.WALK) 1f else 0.4f
        binding.iconMedicine.alpha = if (type == EventType.MEDICINE) 1f else 0.4f
        binding.iconCheckup.alpha = if (type == EventType.CHECKUP) 1f else 0.4f
    }


}
