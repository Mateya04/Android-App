package com.example.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double, currencySymbol: String = "K"): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    val formatted = format.format(amount)
    // Replace default currency symbol with ours
    return formatted.replace("$", currencySymbol)
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
