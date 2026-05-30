package com.example.catatkeuangan.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.catatkeuangan.databinding.ActivityLoginBinding
import com.example.catatkeuangan.models.ApiResponse
import com.example.catatkeuangan.models.LoginRequest
import com.example.catatkeuangan.models.User
import com.example.catatkeuangan.network.RetrofitClient
import com.example.catatkeuangan.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        if (session.isLoggedIn()) { toMain(); return }

        binding.btnLogin.setOnClickListener { doLogin() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun doLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass  = binding.etPassword.text.toString().trim()
        if (email.isEmpty()) { binding.etEmail.error = "Email wajib diisi"; return }
        if (pass.isEmpty())  { binding.etPassword.error = "Password wajib diisi"; return }

        setLoading(true)
        RetrofitClient.instance.login(LoginRequest(email, pass))
            .enqueue(object : Callback<ApiResponse<User>> {
                override fun onResponse(call: Call<ApiResponse<User>>, res: Response<ApiResponse<User>>) {
                    setLoading(false)
                    val body = res.body()
                    if (res.isSuccessful && body?.status == "success" && body.data != null) {
                        session.saveUser(body.data)
                        toMain()
                    } else {
                        toast(body?.message ?: "Login gagal")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                    setLoading(false); toast("Koneksi gagal: ${t.message}")
                }
            })
    }

    private fun setLoading(b: Boolean) {
        binding.progressBar.visibility = if (b) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !b
    }

    private fun toMain() {
        startActivity(Intent(this, MainActivity::class.java)); finish()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
