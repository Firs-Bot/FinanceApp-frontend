package com.example.catatkeuangan.models

import com.google.gson.annotations.SerializedName

data class Transaction(
    @SerializedName("id")         val id: Int = 0,
    @SerializedName("user_id")    val userId: Int = 0,
    @SerializedName("judul")      val judul: String = "",
    @SerializedName("jumlah")     val jumlah: Double = 0.0,
    @SerializedName("kategori")   val kategori: String = "",
    @SerializedName("tipe")       val tipe: String = "",
    @SerializedName("catatan")    val catatan: String = "",
    @SerializedName("tanggal")    val tanggal: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)

data class User(
    @SerializedName("id")    val id: Int = 0,
    @SerializedName("nama")  val nama: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("token") val token: String = ""
)

data class ApiResponse<T>(
    @SerializedName("status")  val status: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("data")    val data: T? = null
)

data class Summary(
    @SerializedName("total_pemasukan")   val totalPemasukan: Double = 0.0,
    @SerializedName("total_pengeluaran") val totalPengeluaran: Double = 0.0,
    @SerializedName("saldo")             val saldo: Double = 0.0
)

data class KategoriStat(
    @SerializedName("kategori")           val kategori: String = "",
    @SerializedName("total")              val total: Double = 0.0,
    @SerializedName("jumlah_transaksi")   val jumlahTransaksi: Int = 0,
    @SerializedName("persentase")         val persentase: Double = 0.0
)

data class StatisticsData(
    @SerializedName("total_pengeluaran")    val totalPengeluaran: Double = 0.0,
    @SerializedName("total_pemasukan")      val totalPemasukan: Double = 0.0,
    @SerializedName("kategori_pengeluaran") val kategoriPengeluaran: List<KategoriStat> = emptyList(),
    @SerializedName("kategori_pemasukan")   val kategoriPemasukan: List<KategoriStat> = emptyList()
)

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("nama")     val nama: String,
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class TransactionRequest(
    @SerializedName("user_id")  val userId: Int,
    @SerializedName("judul")    val judul: String,
    @SerializedName("jumlah")   val jumlah: Double,
    @SerializedName("kategori") val kategori: String,
    @SerializedName("tipe")     val tipe: String,
    @SerializedName("catatan")  val catatan: String,
    @SerializedName("tanggal")  val tanggal: String
)
