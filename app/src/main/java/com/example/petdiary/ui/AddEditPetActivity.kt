package com.example.petdiary.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.petdiary.data.AppDatabase
import com.example.petdiary.data.Pet
import com.example.petdiary.databinding.ActivityAddEditPetBinding
import com.example.petdiary.repository.PetRepository
import com.example.petdiary.viewmodel.PetViewModel
import com.example.petdiary.viewmodel.PetViewModelFactory
import kotlinx.coroutines.launch

class AddEditPetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPetBinding
    private lateinit var viewModel: PetViewModel
    private var petId: Long = -1L
    private var existingPet: Pet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPetBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val database = AppDatabase.getDatabase(this)
        val repository = PetRepository(database.petDao(), database.diaryEntryDao())
        viewModel = ViewModelProvider(this, PetViewModelFactory(repository))[PetViewModel::class.java]

        petId = intent.getLongExtra("petId", -1L)

        if (petId != -1L) {
            binding.toolbar.title = "Редактировать питомца"
            lifecycleScope.launch {
                existingPet = repository.getPetById(petId)
                existingPet?.let { pet ->
                    binding.editName.setText(pet.name)
                    binding.editSpecies.setText(pet.species)
                    binding.editBreed.setText(pet.breed)
                    binding.editBirthDate.setText(pet.birthDate)
                    binding.editNotes.setText(pet.notes)
                }
            }
        } else {
            binding.toolbar.title = "Новый питомец"
        }

        binding.buttonSave.setOnClickListener { savePet() }
    }

    private fun savePet() {
        val name = binding.editName.text.toString().trim()
        val species = binding.editSpecies.text.toString().trim()
        val breed = binding.editBreed.text.toString().trim()
        val birthDate = binding.editBirthDate.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        if (name.isEmpty()) {
            binding.editName.error = "Введите имя питомца"
            return
        }

        if (petId != -1L && existingPet != null) {
            viewModel.updatePet(
                existingPet!!.copy(
                    name = name,
                    species = species,
                    breed = breed,
                    birthDate = birthDate,
                    notes = notes
                )
            )
        } else {
            viewModel.insertPet(
                Pet(name = name, species = species, breed = breed, birthDate = birthDate, notes = notes)
            )
        }
        finish()
    }
}
