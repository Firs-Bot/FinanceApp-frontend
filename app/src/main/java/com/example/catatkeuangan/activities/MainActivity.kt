package com.example.catatkeuangan.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catatkeuangan.R
import com.example.catatkeuangan.adapters.TransactionAdapter
import com.example.catatkeuangan.databinding.ActivityMainBinding
import com.example.catatkeuangan.models.ApiResponse
import com.example.catatkeuangan.models.Summary
import com.example.catatkeuangan.models.Transaction
import com.example.catatkeuangan.network.RetrofitClient
import com.example.catatkeuangan.utils.FormatHelper
import com.example.catatkeuangan.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: TransactionAdapter
    private val txnList = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        setSupportActionBar(binding.toolbar)

        adapter = TransactionAdapter(txnList,
            onClick = { txn ->
                Intent(this, DetailTransactionActivity::class.java).also {
                    it.putExtra("id",       txn.id)
                    it.putExtra("judul",    txn.judul)
                    it.putExtra("jumlah",   txn.jumlah)
                    it.putExtra("kategori", txn.kategori)
                    it.putExtra("tipe",     txn.tipe)
                    it.putExtra("catatan",  txn.catatan)
                    it.putExtra("tanggal",  txn.tanggal)
                    startActivity(it)
                }
            },
            onDelete = { txn, pos -> confirmDelete(txn, pos) }
        )

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onResume() { super.onResume(); loadData() }

    private fun loadData() {
        val uid = session.getUserId()
        loadSummary(uid)
        loadTransactions(uid)
    }

    private fun loadSummary(uid: Int) {
        RetrofitClient.instance.getSummary(uid).enqueue(object : Callback<ApiResponse<Summary>> {
            override fun onResponse(call: Call<ApiResponse<Summary>>, res: Response<ApiResponse<Summary>>) {
                if (res.isSuccessful && res.body()?.status == "success") {
                    val s = res.body()?.data ?: return
                    binding.tvNamaUser.text    = "Halo, ${session.getUserName()} 👋"
                    binding.tvSaldo.text       = FormatHelper.formatRupiah(s.saldo)
                    binding.tvPemasukan.text   = FormatHelper.formatRupiah(s.totalPemasukan)
                    binding.tvPengeluaran.text = FormatHelper.formatRupiah(s.totalPengeluaran)
                }
            }
            override fun onFailure(call: Call<ApiResponse<Summary>>, t: Throwable) {}
        })
    }

    private fun loadTransactions(uid: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility     = View.GONE
        RetrofitClient.instance.getTransactions(uid).enqueue(object : Callback<ApiResponse<List<Transaction>>> {
            override fun onResponse(call: Call<ApiResponse<List<Transaction>>>, res: Response<ApiResponse<List<Transaction>>>) {
                binding.progressBar.visibility = View.GONE
                if (res.isSuccessful && res.body()?.status == "success") {
                    val data = res.body()?.data ?: emptyList()
                    if (data.isEmpty()) binding.tvEmpty.visibility = View.VISIBLE
                    else adapter.updateData(data)
                }
            }
            override fun onFailure(call: Call<ApiResponse<List<Transaction>>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Gagal memuat: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmDelete(txn: Transaction, pos: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Hapus \"${txn.judul}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                RetrofitClient.instance.deleteTransaction(txn.id)
                    .enqueue(object : Callback<ApiResponse<Any>> {
                        override fun onResponse(call: Call<ApiResponse<Any>>, res: Response<ApiResponse<Any>>) {
                            if (res.isSuccessful && res.body()?.status == "success") {
                                adapter.removeAt(pos)
                                loadSummary(session.getUserId())
                                Toast.makeText(this@MainActivity, "Dihapus", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                            Toast.makeText(this@MainActivity, "Gagal hapus", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Batal", null).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu); return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_statistics -> {
                startActivity(Intent(this, StatisticsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                AlertDialog.Builder(this).setTitle("Logout").setMessage("Yakin keluar?")
                    .setPositiveButton("Ya") { _, _ ->
                        session.logout()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }.setNegativeButton("Tidak", null).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
