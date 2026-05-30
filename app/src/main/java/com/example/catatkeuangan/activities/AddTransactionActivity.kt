package com.example.catatkeuangan.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.catatkeuangan.databinding.ActivityAddTransactionBinding
import com.example.catatkeuangan.models.ApiResponse
import com.example.catatkeuangan.models.Transaction
import com.example.catatkeuangan.models.TransactionRequest
import com.example.catatkeuangan.network.RetrofitClient
import com.example.catatkeuangan.utils.FormatHelper
import com.example.catatkeuangan.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var session: SessionManager

    private var isEdit       = false
    private var editId       = 0
    private var selectedDate = FormatHelper.getTodayDate()

    private val kategoriList = listOf(
        "Makanan", "Transportasi", "Belanja", "Hiburan",
        "Kesehatan", "Pendidikan", "Gaji", "Investasi", "Lainnya"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isEdit = intent.hasExtra("edit_id")
        editId = intent.getIntExtra("edit_id", 0)
        supportActionBar?.title = if (isEdit) "Edit Transaksi" else "Tambah Transaksi"

        setupSpinner()
        setupDatePicker()
        if (isEdit) prefill()

        binding.btnSimpan.setOnClickListener { save() }
    }

    private fun setupSpinner() {
        binding.spinnerKategori.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, kategoriList
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupDatePicker() {
        binding.tvTanggal.text = FormatHelper.formatDate(selectedDate)
        binding.btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
                binding.tvTanggal.text = FormatHelper.formatDate(selectedDate)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun prefill() {
        binding.etJudul.setText(intent.getStringExtra("judul"))
        binding.etJumlah.setText(intent.getDoubleExtra("jumlah", 0.0).toString())
        binding.etCatatan.setText(intent.getStringExtra("catatan"))
        selectedDate = intent.getStringExtra("tanggal") ?: selectedDate
        binding.tvTanggal.text = FormatHelper.formatDate(selectedDate)

        if (intent.getStringExtra("tipe") == "pemasukan") binding.rbPemasukan.isChecked = true
        else binding.rbPengeluaran.isChecked = true

        val kat = intent.getStringExtra("kategori") ?: ""
        val idx = kategoriList.indexOfFirst { it.equals(kat, true) }
        if (idx >= 0) binding.spinnerKategori.setSelection(idx)
    }

    private fun save() {
        val judul   = binding.etJudul.text.toString().trim()
        val jmlStr  = binding.etJumlah.text.toString().trim()
        val catatan = binding.etCatatan.text.toString().trim()
        val kat     = binding.spinnerKategori.selectedItem.toString()
        val tipe    = if (binding.rbPemasukan.isChecked) "pemasukan" else "pengeluaran"

        if (judul.isEmpty()) { binding.etJudul.error = "Wajib diisi"; return }
        if (jmlStr.isEmpty()) { binding.etJumlah.error = "Wajib diisi"; return }
        val jumlah = jmlStr.toDoubleOrNull() ?: run { binding.etJumlah.error = "Format salah"; return }

        val req = TransactionRequest(session.getUserId(), judul, jumlah, kat, tipe, catatan, selectedDate)
        setLoading(true)

        val call: Call<ApiResponse<Transaction>> =
            if (isEdit) RetrofitClient.instance.updateTransaction(editId, req)
            else        RetrofitClient.instance.createTransaction(req)

        call.enqueue(object : Callback<ApiResponse<Transaction>> {
            override fun onResponse(c: Call<ApiResponse<Transaction>>, res: Response<ApiResponse<Transaction>>) {
                setLoading(false)
                if (res.isSuccessful && res.body()?.status == "success") {
                    Toast.makeText(this@AddTransactionActivity, "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddTransactionActivity, res.body()?.message ?: "Gagal", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(c: Call<ApiResponse<Transaction>>, t: Throwable) {
                setLoading(false)
                Toast.makeText(this@AddTransactionActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setLoading(b: Boolean) {
        binding.progressBar.visibility = if (b) View.VISIBLE else View.GONE
        binding.btnSimpan.isEnabled = !b
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
