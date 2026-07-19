package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Users
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserByIdFlow(id: Int): Flow<User?>

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<User>>

    @Delete
    suspend fun deleteUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    // Borrowers
    @Query("SELECT * FROM borrowers WHERE userId = :userId ORDER BY fullName ASC")
    fun getBorrowersByUser(userId: Int): Flow<List<Borrower>>

    @Query("SELECT * FROM borrowers WHERE userId = :userId AND phoneNumber = :phone LIMIT 1")
    suspend fun getBorrowerByPhone(userId: Int, phone: String): Borrower?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrower(borrower: Borrower): Long
    
    @Update
    suspend fun updateBorrower(borrower: Borrower)

    @Delete
    suspend fun deleteBorrower(borrower: Borrower)

    // Loans
    @Transaction
    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY dateIssued DESC")
    fun getLoansWithBorrowers(userId: Int): Flow<List<LoanWithBorrower>>

    @Transaction
    @Query("SELECT * FROM loans WHERE userId = :userId AND isPaid = 0 ORDER BY dueDate ASC")
    fun getActiveLoansWithBorrowers(userId: Int): Flow<List<LoanWithBorrower>>
    
    @Transaction
    @Query("SELECT * FROM loans WHERE id = :loanId LIMIT 1")
    fun getLoanWithBorrowerById(loanId: Int): Flow<LoanWithBorrower?>

    @Query("SELECT * FROM loans WHERE borrowerId = :borrowerId")
    suspend fun getLoansForBorrower(borrowerId: Int): List<Loan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan): Long

    @Update
    suspend fun updateLoan(loan: Loan)

    @Delete
    suspend fun deleteLoan(loan: Loan)

    // Payments
    @Query("SELECT * FROM payments WHERE loanId = :loanId ORDER BY date DESC")
    fun getPaymentsForLoan(loanId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY date DESC LIMIT 20")
    fun getRecentPayments(userId: Int): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)

    // Settings
    @Query("SELECT * FROM settings WHERE userId = :userId LIMIT 1")
    fun getUserSettings(userId: Int): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettings)
    
    @Update
    suspend fun updateSettings(settings: UserSettings)
}
