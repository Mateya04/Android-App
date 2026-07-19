package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val username: String,
    val password: String
)

@Entity(tableName = "borrowers")
data class Borrower(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val fullName: String,
    val phoneNumber: String,
    val nationalId: String = "",
    val address: String = "",
    val notes: String = ""
)

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val borrowerId: Int,
    val amount: Double,
    val interestRate: Double,
    val durationWeeks: Int,
    val dateIssued: Long,
    val dueDate: Long,
    val totalRepayment: Double,
    val amountPaid: Double = 0.0,
    val isPaid: Boolean = false,
    val notes: String = ""
) {
    val remainingBalance: Double get() = totalRepayment - amountPaid
}

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val loanId: Int,
    val amount: Double,
    val date: Long,
    val method: String,
    val notes: String = ""
)

@Entity(tableName = "settings")
data class UserSettings(
    @PrimaryKey val userId: Int,
    val currency: String = "K",
    val rate1Week: Double = 10.0,
    val rate2Weeks: Double = 20.0,
    val rate3Weeks: Double = 30.0,
    val rate4Weeks: Double = 35.0,
    val theme: String = "system" // system, light, dark
)
