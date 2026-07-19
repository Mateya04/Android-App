package com.example.data

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation

// Helper class for UI that needs borrower and loan info together
data class LoanWithBorrower(
    @Embedded val loan: Loan,
    @Relation(
        parentColumn = "borrowerId",
        entityColumn = "id"
    )
    val borrower: Borrower
)

data class BorrowerWithLoans(
    @Embedded val borrower: Borrower,
    @Relation(
        parentColumn = "id",
        entityColumn = "borrowerId"
    )
    val loans: List<Loan>
)
