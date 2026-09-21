# PetDiary — README для верстки/кнопок

Бэкенд уже готов: база данных, все CRUD-функции, статистика, уведомления.
Твоя задача — вызвать нужную функцию в нужном месте (`onClick`, `onCreate` и т.д.).
Ничего из этого сейчас никуда не подключено специально, чтобы не мешать вёрстке.

Архитектура (стандартный Room + Repository, тебе для работы это знать не обязательно,
но если будет непонятно — вот путь данных):

```
Activity/Fragment (UI) → Repository → Dao → Room (SQLite)
```

---

## 1. Как получить доступ к базе

В каждой Activity сначала получаешь `AppDatabase`, потом создаёшь нужный репозиторий:

```kotlin
val database = AppDatabase.getDatabase(this)          // this = Activity/Context

val petRepository = PetRepository(database.petDao(), database.diaryEntryDao())
val trackingRepository = TrackingRepository(database.diaryEntryDao())
val reminderRepository = ReminderRepository(database.petDao(), trackingRepository)
val authRepository = AuthRepository(database.userDao())   // только если будет экран входа
```

Это уже сделано в `MainActivity`, `PetDetailActivity`, `AddEditPetActivity`,
`AddEditDiaryActivity` — смотри их `onCreate()` как пример.

**Важно:** почти все функции — `suspend`. Их нельзя вызвать просто так из `onClick`,
нужна корутина:

```kotlin
lifecycleScope.launch {
    val result = repository.someFunction(...)
    // тут уже можно обновлять UI (TextView, Toast и т.д.)
}
```

Либо через `ViewModel` (`viewModelScope.launch { ... }`) — так уже сделано для
питомцев и записей дневника в `PetViewModel`, бери оттуда пример.

---

## 2. Экран 1 — Главная

Файл: `MainActivity.kt`. Уже показывает список питомцев (`viewModel.allPets`).

Что можно довесить:

| Кнопка / элемент        | Функция                                       | Где лежит            |
| ----------------------- | --------------------------------------------- | -------------------- |
| Иконка колокольчика 🔔  | `reminderRepository.getNotifications()`       | `ReminderRepository` |
| Клик по питомцу в ленте | уже работает → открывает `PetDetailActivity`  | —                    |
| `+` в ленте питомцев    | уже работает → открывает `AddEditPetActivity` | —                    |

Пример для колокольчика:

```kotlin
binding.iconBell.setOnClickListener {
    lifecycleScope.launch {
        val notifications = reminderRepository.getNotifications()
        // notifications - это List<String>, например:
        // ["Барсик: сегодня ещё не было прогулки"]
        // покажи их как хочешь - Toast, диалог, выпадающий список
    }
}
```

---

## 3. Экран 2 — Профиль питомца

Файл: `PetDetailActivity.kt`. Уже показывает историю событий питомца.

Блок "Статистика за неделю" (Кормлений: X, Прогулок: X, Лекарств: X) — с макета:

```kotlin
lifecycleScope.launch {
    val stats = trackingRepository.getWeeklyStats(petId)
    // stats - это Map<EventType, Int>
    binding.textFeedingCount.text = stats[EventType.FEEDING].toString()
    binding.textWalkCount.text = stats[EventType.WALK].toString()
    binding.textMedicineCount.text = stats[EventType.MEDICINE].toString()
}
```

Другие полезные функции для этого экрана:

```kotlin
// сколько дней с последнего осмотра
val days = trackingRepository.daysSinceLastEvent(petId, EventType.CHECKUP)
// null, если осмотра ещё не было ни разу

// последнее событие конкретного типа
val lastWalk = trackingRepository.getLastEventOfType(petId, EventType.WALK)

// сколько всего было кормлений за всё время
val totalFeedings = trackingRepository.getEventCountByType(petId, EventType.FEEDING)
```

---

## 4. Экран 3 — Новое событие

Файл: `AddEditDiaryActivity.kt`. Уже сохраняет событие через
`viewModel.insertEntry(...)`.

Единственное, чего не хватает во вьюхе — выбора **типа** события
(Кормление / Прогулка / Лекарство / Осмотр). Когда добавишь на макете
4 кнопки/иконки выбора типа, при сохранении передай выбранный тип:

```kotlin
viewModel.insertEntry(
    DiaryEntry(
        petId = petId,
        date = date,
        title = entryTitle,
        description = description,
        weight = weight,
        eventType = selectedType.name   // например EventType.FEEDING.name
    )
)
```

`selectedType` — это переменная `EventType`, которую ты выставляешь при клике
на иконку типа, например:

```kotlin
var selectedType = EventType.OTHER   // дефолт

binding.iconFeeding.setOnClickListener { selectedType = EventType.FEEDING }
binding.iconWalk.setOnClickListener { selectedType = EventType.WALK }
binding.iconMedicine.setOnClickListener { selectedType = EventType.MEDICINE }
binding.iconCheckup.setOnClickListener { selectedType = EventType.CHECKUP }
```

Если строки на UI ("Кормление", "Прогулка"...) — бери их не руками,
а через `EventType.FEEDING.displayName()` и т.д., чтобы не дублировать текст.

---

## 5. Регистрация / вход (если вообще будет)

Это на макетах не было чётко решено — если в итоге решите, что дневник
просто локальный (один пользователь на телефон, без логина), то файлы
`User.kt`, `UserDao.kt`, `AuthRepository.kt` можно вообще не трогать.

Если экран входа всё-таки будет:

```kotlin
// Регистрация
binding.buttonSignUp.setOnClickListener {
    lifecycleScope.launch {
        val result = authRepository.registration(
            binding.editUsername.text.toString(),
            binding.editPassword.text.toString()
        )
        if (result.success) {
            // result.userId - сохрани куда-нибудь (SharedPreferences),
            // чтобы понимать, кто сейчас залогинен
        } else {
            // result.message - показать ошибку, например Toast
        }
    }
}

// Вход
binding.buttonLogIn.setOnClickListener {
    lifecycleScope.launch {
        val result = authRepository.login(
            binding.editUsername.text.toString(),
            binding.editPassword.text.toString()
        )
        // тот же result.success / result.message / result.userId
    }
}
```

---

## 6. Шпаргалка по всем функциям

### PetRepository (питомцы)

- `insertPet(pet: Pet): Long`
- `updatePet(pet: Pet)`
- `deletePet(pet: Pet)`
- `getPetById(id: Long): Pet?`
- `allPets: LiveData<List<Pet>>` — список для главного экрана

### PetRepository (события дневника)

- `insertEntry(entry: DiaryEntry): Long`
- `updateEntry(entry: DiaryEntry)`
- `deleteEntry(entry: DiaryEntry)`
- `getEntriesForPet(petId): LiveData<List<DiaryEntry>>`

### TrackingRepository (статистика)

- `getWeeklyStats(petId): Map<EventType, Int>`
- `getEventCountByType(petId, type): Int`
- `getLastEventOfType(petId, type): DiaryEntry?`
- `daysSinceLastEvent(petId, type): Int?`

### ReminderRepository (уведомления)

- `getNotifications(): List<String>`

### AuthRepository (если понадобится)

- `registration(username, password): AuthResult`
- `login(username, password): AuthResult`
  - `AuthResult` = `{ success: Boolean, message: String, userId: Long? }`

---
