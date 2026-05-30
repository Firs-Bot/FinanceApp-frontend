package com.example.catatkeuangan.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.catatkeuangan.R
import com.example.catatkeuangan.databinding.ActivityDetailTransactionBinding
import com.example.catatkeuangan.utils.FormatHelper

class DetailTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Transaksi"

        val id       = intent.getIntExtra("id", 0)
        val judul    = intent.getStringExtra("judul")    ?: ""
        val jumlah   = intent.getDoubleExtra("jumlah",  0.0)
        val kategori = intent.getStringExtra("kategori") ?: ""
        val tipe     = intent.getStringExtra("tipe")     ?: ""
        val catatan  = intent.getStringExtra("catatan")  ?: ""
        val tanggal  = intent.getStringExtra("tanggal")  ?: ""

        binding.tvEmoji.text    = FormatHelper.getCategoryEmoji(kategori)
        binding.tvJudul.text    = judul
        binding.tvKategori.text = kategori
        binding.tvTanggal.text  = FormatHelper.formatDate(tanggal)
        binding.tvCatatan.text  = catatan.ifEmpty { "-" }
        binding.tvTipe.text     = if (tipe == "pemasukan") "Pemasukan" else "Pengeluaran"

        val formatted = FormatHelper.formatRupiah(jumlah)
        if (tipe == "pemasukan") {
            binding.tvJumlah.text = "+ $formatted"
            binding.tvJumlah.setTextColor(ContextCompat.getColor(this, R.color.color_income))
            binding.cardTipe.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_income_bg))
        } else {
            binding.tvJumlah.text = "- $formatted"
            binding.tvJumlah.setTextColor(ContextCompat.getColor(this, R.color.color_expense))
            binding.cardTipe.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_expense_bg))
        }

        binding.btnEdit.setOnClickListener {
            Intent(this, AddTransactionActivity::class.java).also {
                it.putExtra("edit_id",  id)
                it.putExtra("judul",    judul)
                it.putExtra("jumlah",   jumlah)
                it.putExtra("kategori", kategori)
                it.putExtra("tipe",     tipe)
                it.putExtra("catatan",  catatan)
                it.putExtra("tanggal",  tanggal)
                startActivity(it)
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
