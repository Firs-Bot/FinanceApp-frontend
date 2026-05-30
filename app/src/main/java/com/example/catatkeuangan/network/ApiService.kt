package com.example.catatkeuangan.network

import com.example.catatkeuangan.models.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ============================================================
// GANTI URL INI DENGAN IP SERVER ANDA
//   Emulator Android Studio  → http://10.0.2.2/financeapp/
//   HP fisik (WiFi sama)     → http://192.168.x.x/financeapp/
// ============================================================
const val BASE_URL = "http://192.168.1.7/financeapp/"

interface ApiService {

    // AUTH
    @POST("api/auth/login.php")
    fun login(@Body request: LoginRequest): Call<ApiResponse<User>>

    @POST("api/auth/register.php")
    fun register(@Body request: RegisterRequest): Call<ApiResponse<User>>

    // TRANSAKSI
    @GET("api/transactions/read.php")
    fun getTransactions(@Query("user_id") userId: Int): Call<ApiResponse<List<Transaction>>>

    @POST("api/transactions/create.php")
    fun createTransaction(@Body request: TransactionRequest): Call<ApiResponse<Transaction>>

    @PUT("api/transactions/update.php")
    fun updateTransaction(
        @Query("id") id: Int,
        @Body request: TransactionRequest
    ): Call<ApiResponse<Transaction>>

    @DELETE("api/transactions/delete.php")
    fun deleteTransaction(@Query("id") id: Int): Call<ApiResponse<Any>>

    @GET("api/transactions/summary.php")
    fun getSummary(@Query("user_id") userId: Int): Call<ApiResponse<Summary>>

    // STATISTIK (baru)
    @GET("api/transactions/statistics.php")
    fun getStatistics(
        @Query("user_id") userId: Int,
        @Query("month")   month: Int = 0,
        @Query("year")    year: Int = 0
    ): Call<ApiResponse<StatisticsData>>
}

object RetrofitClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
