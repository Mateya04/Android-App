package com.example

import android.app.Application
import android.content.Context
import com.example.data.AppDatabase
import com.example.data.AppRepository

class LoanApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database.appDao()) }
    val sharedPreferences by lazy { getSharedPreferences("loan_prefs", Context.MODE_PRIVATE) }
}
