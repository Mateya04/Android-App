package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    
    // Auth
    fun getAllUsers() = appDao.getAllUsers()
    suspend fun deleteUser(user: User) = appDao.deleteUser(user)
    suspend fun getUserByUsername(username: String) = appDao.getUserByUsername(username)
    fun getUserByIdFlow(id: Int) = appDao.getUserByIdFlow(id)
    suspend fun insertUser(user: User) = appDao.insertUser(user)
    suspend fun updateUser(user: User) = appDao.updateUser(user)
    
    // Settings
    fun getUserSettings(userId: Int) = appDao.getUserSettings(userId)
    suspend fun insertSettings(settings: UserSettings) = appDao.insertSettings(settings)
    suspend fun updateSettings(settings: UserSettings) = appDao.updateSettings(settings)
    
    // Borrowers
    fun getBorrowersByUser(userId: Int) = appDao.getBorrowersByUser(userId)
    suspend fun getBorrowerByPhone(userId: Int, phone: String) = appDao.getBorrowerByPhone(userId, phone)
    suspend fun insertBorrower(borrower: Borrower) = appDao.insertBorrower(borrower)
    suspend fun updateBorrower(borrower: Borrower) = appDao.updateBorrower(borrower)
    suspend fun deleteBorrower(borrower: Borrower) = appDao.deleteBorrower(borrower)
    suspend fun getLoansForBorrower(borrowerId: Int) = appDao.getLoansForBorrower(borrowerId)
    
    // Loans
    fun getLoansWithBorrowers(userId: Int) = appDao.getLoansWithBorrowers(userId)
    fun getActiveLoansWithBorrowers(userId: Int) = appDao.getActiveLoansWithBorrowers(userId)
    fun getLoanWithBorrowerById(loanId: Int) = appDao.getLoanWithBorrowerById(loanId)
    suspend fun insertLoan(loan: Loan) = appDao.insertLoan(loan)
    suspend fun updateLoan(loan: Loan) = appDao.updateLoan(loan)
    suspend fun deleteLoan(loan: Loan) = appDao.deleteLoan(loan)
    
    // Payments
    fun getPaymentsForLoan(loanId: Int) = appDao.getPaymentsForLoan(loanId)
    fun getRecentPayments(userId: Int) = appDao.getRecentPayments(userId)
    suspend fun insertPayment(payment: Payment) {
        appDao.insertPayment(payment)
        // Whenever we insert a payment, we should update the loan's amount paid
        // But let's do this in the ViewModel to avoid complex logic here, or do it inside a transaction.
    }
    suspend fun deletePayment(payment: Payment) = appDao.deletePayment(payment)
}
