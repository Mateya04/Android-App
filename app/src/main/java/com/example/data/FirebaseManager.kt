package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseManager {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveUser(user: User) {
        try {
            db.collection("users").document(user.id.toString()).set(user).await()
            Log.d("FirebaseManager", "User ${user.id} saved to Firestore")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving user", e)
        }
    }

    suspend fun saveBorrower(userId: Int, borrower: Borrower) {
        try {
            db.collection("users").document(userId.toString())
                .collection("borrowers").document(borrower.id.toString()).set(borrower).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving borrower", e)
        }
    }

    suspend fun saveLoan(userId: Int, loan: Loan) {
        try {
            db.collection("users").document(userId.toString())
                .collection("loans").document(loan.id.toString()).set(loan).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving loan", e)
        }
    }

    suspend fun savePayment(userId: Int, payment: Payment) {
        try {
            db.collection("users").document(userId.toString())
                .collection("payments").document(payment.id.toString()).set(payment).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving payment", e)
        }
    }

    suspend fun saveSettings(userId: Int, settings: UserSettings) {
        try {
            db.collection("users").document(userId.toString())
                .collection("settings").document("user_settings").set(settings).await()
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error saving settings", e)
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        try {
            val querySnapshot = db.collection("users").whereEqualTo("username", username).get().await()
            if (!querySnapshot.isEmpty) {
                return querySnapshot.documents[0].toObject(User::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error getting user", e)
        }
        return null
    }

    suspend fun syncDataToLocal(userId: Int, repository: AppRepository) {
        try {
            val userDoc = db.collection("users").document(userId.toString())
            
            // fetch settings
            try {
                val settingsSnap = userDoc.collection("settings").document("user_settings").get().await()
                if (settingsSnap.exists()) {
                    settingsSnap.toObject(UserSettings::class.java)?.let { repository.insertSettings(it) }
                }
            } catch (e: Exception) {
                Log.e("FirebaseManager", "Error syncing settings", e)
            }
            
            // fetch borrowers
            val borrowers = userDoc.collection("borrowers").get().await().toObjects(Borrower::class.java)
            borrowers.forEach { repository.insertBorrower(it) }

            // fetch loans
            val loans = userDoc.collection("loans").get().await().toObjects(Loan::class.java)
            loans.forEach { repository.insertLoan(it) }

            // fetch payments
            val payments = userDoc.collection("payments").get().await().toObjects(Payment::class.java)
            payments.forEach { repository.insertPayment(it) }

            Log.d("FirebaseManager", "Data synced from cloud for user $userId")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error syncing data from cloud", e)
        }
    }
}
