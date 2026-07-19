package com.example.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: AppRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val firebaseManager = com.example.data.FirebaseManager()
    // Auth State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    // Settings State
    private val _userSettings = MutableStateFlow<UserSettings?>(null)
    val userSettings = _userSettings.asStateFlow()

    init {
        val savedUserId = sharedPreferences.getInt("logged_in_user_id", -2)
        if (savedUserId != -2) {
            viewModelScope.launch {
                if (savedUserId == -1) {
                    val adminUser = User(id = -1, fullName = "Administrator", username = "admin", password = "")
                    _currentUser.value = adminUser
                } else {
                    val user = repository.getUserByIdFlow(savedUserId).firstOrNull()
                    if (user != null) {
                        _currentUser.value = user
                        loadSettings(user.id)
                    }
                }
            }
        }
    }

    fun login(username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (username == "admin" && pass == "admin123") {
                val adminUser = User(id = -1, fullName = "Administrator", username = "admin", password = "")
                _currentUser.value = adminUser
                sharedPreferences.edit().putInt("logged_in_user_id", adminUser.id).apply()
                onResult(true, "Success")
                return@launch
            }
            
            var user = repository.getUserByUsername(username)
            if (user == null) {
                // Try fetching from cloud
                val cloudUser = firebaseManager.getUserByUsername(username)
                if (cloudUser != null) {
                    repository.insertUser(cloudUser)
                    firebaseManager.syncDataToLocal(cloudUser.id, repository)
                    user = cloudUser
                }
            }
            
            if (user == null) {
                onResult(false, "User not found")
            } else if (user.password == pass) {
                // Background sync on login
                firebaseManager.syncDataToLocal(user.id, repository)
                
                _currentUser.value = user
                sharedPreferences.edit().putInt("logged_in_user_id", user.id).apply()
                loadSettings(user.id)
                onResult(true, "Success")
            } else {
                onResult(false, "Invalid password")
            }
        }
    }

    fun register(fullName: String, username: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByUsername(username)
            if (existing != null) {
                onResult(false, "Username already exists")
                return@launch
            }
            val newUser = User(
                fullName = fullName,
                username = username,
                password = pass
            )
            val userId = repository.insertUser(newUser)
            val finalUser = newUser.copy(id = userId.toInt())
            // Insert default settings
            repository.insertSettings(UserSettings(userId = userId.toInt()))
            _currentUser.value = finalUser
            sharedPreferences.edit().putInt("logged_in_user_id", userId.toInt()).apply()
            
            // Save to Firestore
            firebaseManager.saveUser(finalUser)
            
            loadSettings(userId.toInt())
            onResult(true, "Registration successful")
        }
    }
    
    fun logout() {
        _currentUser.value = null
        _userSettings.value = null
        sharedPreferences.edit().remove("logged_in_user_id").apply()
    }
    
    fun changePassword(newPass: String) {
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                val updated = user.copy(password = newPass)
                repository.updateUser(updated)
                _currentUser.value = updated
            }
        }
    }

    fun updateSettings(currency: String, r1: Double, r2: Double, r3: Double, r4: Double, theme: String) {
        _currentUser.value?.let { user ->
            viewModelScope.launch {
                val settings = UserSettings(user.id, currency, r1, r2, r3, r4, theme)
                repository.updateSettings(settings)
                _userSettings.value = settings
                firebaseManager.saveSettings(user.id, settings)
            }
        }
    }

    private fun loadSettings(userId: Int) {
        viewModelScope.launch {
            repository.getUserSettings(userId).collect {
                _userSettings.value = it
            }
        }
    }
    
    // Data Flows
    val allUsers = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBorrowers = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getBorrowersByUser(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLoans = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getLoansWithBorrowers(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLoans = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getActiveLoansWithBorrowers(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val recentPayments = _currentUser.flatMapLatest { user ->
        if (user == null) emptyFlow() else repository.getRecentPayments(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun addLoan(
        phone: String,
        fullName: String,
        nationalId: String,
        address: String,
        notes: String,
        amount: Double,
        durationWeeks: Int,
        interestRate: Double,
        dateIssued: Long,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            // Find or create borrower
            var borrower = repository.getBorrowerByPhone(user.id, phone)
            var borrowerId = borrower?.id
            if (borrower == null) {
                val newBorrower = Borrower(
                    userId = user.id,
                    fullName = fullName,
                    phoneNumber = phone,
                    nationalId = nationalId,
                    address = address,
                    notes = notes
                )
                borrowerId = repository.insertBorrower(newBorrower).toInt()
                val finalBorrower = newBorrower.copy(id = borrowerId)
                firebaseManager.saveBorrower(user.id, finalBorrower)
            }
            
            // Calculate total
            val interestAmount = amount * (interestRate / 100.0)
            val totalRepayment = amount + interestAmount
            val dueDate = dateIssued + (durationWeeks * 7L * 24 * 60 * 60 * 1000)
            
            val newLoan = Loan(
                userId = user.id,
                borrowerId = borrowerId!!,
                amount = amount,
                interestRate = interestRate,
                durationWeeks = durationWeeks,
                dateIssued = dateIssued,
                dueDate = dueDate,
                totalRepayment = totalRepayment,
                amountPaid = 0.0,
                isPaid = false
            )
            val loanId = repository.insertLoan(newLoan).toInt()
            val finalLoan = newLoan.copy(id = loanId)
            
            // Sync to cloud
            firebaseManager.saveLoan(user.id, finalLoan)
            
            onSuccess()
        }
    }

    fun addPayment(loanId: Int, amount: Double, method: String, notes: String, date: Long, onSuccess: () -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val payment = Payment(userId = user.id, loanId = loanId, amount = amount, method = method, notes = notes, date = date)
            repository.insertPayment(payment)
            
            // Sync to cloud
            val savedPayment = repository.getPaymentsForLoan(loanId).firstOrNull()?.firstOrNull() ?: payment
            firebaseManager.savePayment(user.id, savedPayment)
            
            // Update loan
            repository.getLoanWithBorrowerById(loanId).firstOrNull()?.let { lw ->
                val loan = lw.loan
                val newPaid = loan.amountPaid + amount
                val isPaid = newPaid >= loan.totalRepayment
                val updatedLoan = loan.copy(amountPaid = newPaid, isPaid = isPaid)
                repository.updateLoan(updatedLoan)
                firebaseManager.saveLoan(user.id, updatedLoan)
            }
            onSuccess()
        }
    }
    
    fun deleteBorrower(borrower: Borrower, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val loans = repository.getLoansForBorrower(borrower.id)
            if (loans.any { !it.isPaid }) {
                onResult(false, "Cannot delete borrower with active loans")
            } else {
                repository.deleteBorrower(borrower)
                onResult(true, "Borrower deleted")
            }
        }
    }
    
    fun deleteLoan(loan: Loan) {
        viewModelScope.launch { repository.deleteLoan(loan) }
    }
    
    fun deletePayment(payment: Payment) {
        viewModelScope.launch { 
            repository.deletePayment(payment)
            // also revert loan amountPaid
            repository.getLoanWithBorrowerById(payment.loanId).firstOrNull()?.let { lw ->
                val loan = lw.loan
                val newPaid = loan.amountPaid - payment.amount
                repository.updateLoan(loan.copy(amountPaid = newPaid, isPaid = newPaid >= loan.totalRepayment))
            }
        }
    }
    
    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }
    
    fun markLoanPaid(loan: Loan) {
        viewModelScope.launch {
            repository.updateLoan(loan.copy(amountPaid = loan.totalRepayment, isPaid = true))
        }
    }
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
