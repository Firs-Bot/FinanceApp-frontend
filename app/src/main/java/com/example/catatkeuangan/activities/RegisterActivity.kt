package com.example.catatkeuangan.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.catatkeuangan.databinding.ActivityRegisterBinding
import com.example.catatkeuangan.models.ApiResponse
import com.example.catatkeuangan.models.RegisterRequest
import com.example.catatkeuangan.models.User
import com.example.catatkeuangan.network.RetrofitClient
import com.example.catatkeuangan.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        binding.btnRegister.setOnClickListener { doRegister() }
        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun doRegister() {
        val nama  = binding.etNama.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass  = binding.etPassword.text.toString().trim()
        val conf  = binding.etConfirmPassword.text.toString().trim()

        if (nama.isEmpty())  { binding.etNama.error = "Nama wajib diisi"; return }
        if (email.isEmpty()) { binding.etEmail.error = "Email wajib diisi"; return }
        if (pass.isEmpty())  { binding.etPassword.error = "Password wajib diisi"; return }
        if (pass != conf)    { binding.etConfirmPassword.error = "Password tidak cocok"; return }

        setLoading(true)
        RetrofitClient.instance.register(RegisterRequest(nama, email, pass))
            .enqueue(object : Callback<ApiResponse<User>> {
                override fun onResponse(call: Call<ApiResponse<User>>, res: Response<ApiResponse<User>>) {
                    setLoading(false)
                    val body = res.body()
                    if (res.isSuccessful && body?.status == "success" && body.data != null) {
                        session.saveUser(body.data)
                        Toast.makeText(this@RegisterActivity, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finishAffinity()
                    } else {
                        Toast.makeText(this@RegisterActivity, body?.message ?: "Gagal", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                    setLoading(false)
                    Toast.makeText(this@RegisterActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setLoading(b: Boolean) {
        binding.progressBar.visibility = if (b) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !b
    }
}
