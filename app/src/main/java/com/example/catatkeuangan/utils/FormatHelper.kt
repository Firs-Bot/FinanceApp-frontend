package com.example.catatkeuangan.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatHelper {

    fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount)
    }

    fun formatDate(dateStr: String): String {
        return try {
            val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            output.format(input.parse(dateStr) ?: return dateStr)
        } catch (e: Exception) { dateStr }
    }

    fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun getCategoryEmoji(kategori: String): String = when (kategori.lowercase()) {
        "makanan"      -> "🍽️"
        "transportasi" -> "🚗"
        "belanja"      -> "🛍️"
        "hiburan"      -> "🎮"
        "kesehatan"    -> "💊"
        "pendidikan"   -> "📚"
        "gaji"         -> "💼"
        "investasi"    -> "📈"
        else           -> "💰"
    }
}
