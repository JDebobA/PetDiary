package com.example.petdiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petdiary.data.AppDatabase
import com.example.petdiary.data.EventType
import com.example.petdiary.databinding.ActivityPetDetailBinding
import com.example.petdiary.repository.PetRepository
import com.example.petdiary.repository.TrackingRepository
import com.example.petdiary.viewmodel.PetViewModel
import com.example.petdiary.viewmodel.PetViewModelFactory
import kotlinx.coroutines.launch

class PetDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPetDetailBinding
    private lateinit var viewModel: PetViewModel
    private lateinit var adapter: DiaryAdapter
    private var petId: Long = -1L

    private lateinit var trackingRepository: TrackingRepository
    private lateinit var repository: PetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        petId = intent.getLongExtra("petId", -1L)
        binding.toolbar.title = intent.getStringExtra("petName") ?: "Питомец"

        val database = AppDatabase.getDatabase(this)
        repository = PetRepository(database.petDao(), database.diaryEntryDao())
        viewModel = ViewModelProvider(this, PetViewModelFactory(repository))[PetViewModel::class.java]
        trackingRepository = TrackingRepository(database.diaryEntryDao())

        adapter = DiaryAdapter(
            onEditClick = { entry ->
                val intent = Intent(this, AddEditDiaryActivity::class.java)
                intent.putExtra("petId", petId)
                intent.putExtra("entryId", entry.id)
                startActivity(intent)
            },
            onDeleteClick = { entry -> viewModel.deleteEntry(entry) }
        )

        binding.recyclerViewEntries.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewEntries.adapter = adapter

        viewModel.getEntriesForPet(petId).observe(this) { entries ->
            adapter.submitList(entries)
            binding.textEmptyEntries.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddEntry.setOnClickListener {
            val intent = Intent(this, AddEditDiaryActivity::class.java)
            intent.putExtra("petId", petId)
            startActivity(intent)
        }

        binding.iconDeletePet.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Удалить питомца?")
                .setMessage("Все записи этого питомца тоже будут удалены.")
                .setPositiveButton("Удалить") { _, _ ->
                    lifecycleScope.launch {
                        val pet = repository.getPetById(petId)
                        if (pet != null) repository.deletePet(pet)
                        finish()
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        loadWeeklyStats()
    }

    private fun loadWeeklyStats() {
        lifecycleScope.launch {
            val stats = trackingRepository.getWeeklyStats(petId)
            binding.textFeedingCount.text = (stats[EventType.FEEDING] ?: 0).toString()
            binding.textWalkCount.text = (stats[EventType.WALK] ?: 0).toString()
            binding.textMedicineCount.text = (stats[EventType.MEDICINE] ?: 0).toString()
        }
    }
}