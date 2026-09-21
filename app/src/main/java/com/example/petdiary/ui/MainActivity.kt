package com.example.petdiary.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petdiary.data.AppDatabase
import com.example.petdiary.databinding.ActivityMainBinding
import com.example.petdiary.repository.PetRepository
import com.example.petdiary.viewmodel.PetViewModel
import com.example.petdiary.viewmodel.PetViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import com.example.petdiary.repository.ReminderRepository
import com.example.petdiary.repository.TrackingRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: PetViewModel
    private lateinit var adapter: PetAdapter

    private fun showNotifications(notifications: List<String>) {
        if (notifications.isEmpty()) {
            Toast.makeText(this, "Нет новых уведомлений", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Уведомления")
            .setItems(notifications.toTypedArray()) { _, _ -> }
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Мои питомцы"

        val database = AppDatabase.getDatabase(this)
        val petRepository = PetRepository(database.petDao(), database.diaryEntryDao())
        val trackingRepository = TrackingRepository(database.diaryEntryDao())
        val reminderRepository = ReminderRepository(database.petDao(), trackingRepository)

        viewModel = ViewModelProvider(this, PetViewModelFactory(petRepository))[PetViewModel::class.java]

        adapter = PetAdapter(
            onItemClick = { pet ->
                val intent = Intent(this, PetDetailActivity::class.java)
                intent.putExtra("petId", pet.id)
                intent.putExtra("petName", pet.name)
                startActivity(intent)
            },
            onEditClick = { pet ->
                val intent = Intent(this, AddEditPetActivity::class.java)
                intent.putExtra("petId", pet.id)
                startActivity(intent)
            },
            onDeleteClick = { pet -> viewModel.deletePet(pet) }
        )

        binding.recyclerViewPets.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.recyclerViewPets.adapter = adapter

        viewModel.allPets.observe(this) { pets ->
            adapter.submitList(pets)
            binding.textEmpty.visibility = if (pets.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.textDate.text = SimpleDateFormat("Сегодня, d MMMM", Locale("ru")).format(Date())

        binding.fabAddPet.setOnClickListener {
            startActivity(Intent(this, AddEditPetActivity::class.java))
        }

        binding.iconBell.setOnClickListener {
            lifecycleScope.launch {
                val notifications = reminderRepository.getNotifications()
                showNotifications(notifications)
            }
        }
    }
}
