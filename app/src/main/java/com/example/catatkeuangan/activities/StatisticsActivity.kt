package com.example.catatkeuangan.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.catatkeuangan.R
import com.example.catatkeuangan.databinding.ActivityStatisticsBinding
import com.example.catatkeuangan.models.ApiResponse
import com.example.catatkeuangan.models.KategoriStat
import com.example.catatkeuangan.models.StatisticsData
import com.example.catatkeuangan.network.RetrofitClient
import com.example.catatkeuangan.utils.FormatHelper
import com.example.catatkeuangan.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var session: SessionManager

    private val calendar = Calendar.getInstance()
    private var selectedMonth = calendar.get(Calendar.MONTH) + 1
    private var selectedYear  = calendar.get(Calendar.YEAR)
    private var isSpinnerReady = false

    // Warna pengeluaran — merah/oranye
    private val COLORS_OUT = intArrayOf(
        0xFFEF5350.toInt(), 0xFFFF7043.toInt(), 0xFFFFCA28.toInt(),
        0xFF9CCC65.toInt(), 0xFF26C6DA.toInt(), 0xFF42A5F5.toInt(),
        0xFFAB47BC.toInt(), 0xFFEC407A.toInt(), 0xFF8D6E63.toInt(), 0xFF78909C.toInt()
    )

    // Warna pemasukan — hijau/teal
    private val COLORS_IN = intArrayOf(
        0xFF43A047.toInt(), 0xFF00ACC1.toInt(), 0xFF7CB342.toInt(),
        0xFF039BE5.toInt(), 0xFF00897B.toInt(), 0xFF3949AB.toInt(),
        0xFF8E24AA.toInt(), 0xFFFB8C00.toInt(), 0xFF6D4C41.toInt(), 0xFF546E7A.toInt()
    )

    private var activeTab = "pemasukan"
    private var cachedData: StatisticsData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true); title = "Statistik" }

        setupSpinners()
        setupTabs()
        loadStatistics()
    }

    // ── Spinner bulan & tahun ────────────────────────────────
    private fun setupSpinners() {
        val months = listOf("Semua Bulan","Januari","Februari","Maret","April","Mei","Juni",
            "Juli","Agustus","September","Oktober","November","Desember")
        binding.spinnerMonth.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, months).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerMonth.setSelection(selectedMonth)

        val now   = Calendar.getInstance().get(Calendar.YEAR)
        val years = (now downTo now - 4).map { it.toString() }
        binding.spinnerYear.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, years).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerYear.setSelection(0)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (!isSpinnerReady) return
                selectedMonth = binding.spinnerMonth.selectedItemPosition
                selectedYear  = years[binding.spinnerYear.selectedItemPosition].toInt()
                loadStatistics()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        binding.spinnerMonth.onItemSelectedListener = listener
        binding.spinnerYear.onItemSelectedListener  = listener
        isSpinnerReady = true
    }

    // ── Tab switcher ─────────────────────────────────────────
    private fun setupTabs() {
        binding.btnTabOut.setOnClickListener { switchTab("pengeluaran") }
        binding.btnTabIn.setOnClickListener  { switchTab("pemasukan") }
        switchTab("pemasukan")
    }

    private fun switchTab(tab: String) {
        activeTab = tab
        val isOut = tab == "pengeluaran"

        // Button style
        binding.btnTabOut.setBackgroundColor(
            if (isOut) Color.parseColor("#EF5350") else Color.parseColor("#E0E0E0"))
        binding.btnTabOut.setTextColor(
            if (isOut) Color.WHITE else Color.parseColor("#757575"))

        binding.btnTabIn.setBackgroundColor(
            if (!isOut) Color.parseColor("#43A047") else Color.parseColor("#E0E0E0"))
        binding.btnTabIn.setTextColor(
            if (!isOut) Color.WHITE else Color.parseColor("#757575"))

        // Show/hide content panels
        binding.panelOut.visibility = if (isOut)  View.VISIBLE else View.GONE
        binding.panelIn.visibility  = if (!isOut) View.VISIBLE else View.GONE

        // Re-render if data already loaded
        cachedData?.let { renderTab(it) }
    }

    // ── Load dari API ────────────────────────────────────────
    private fun loadStatistics() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollContent.visibility = View.GONE

        RetrofitClient.instance.getStatistics(session.getUserId(), selectedMonth, selectedYear)
            .enqueue(object : Callback<ApiResponse<StatisticsData>> {
                override fun onResponse(call: Call<ApiResponse<StatisticsData>>,
                                        res: Response<ApiResponse<StatisticsData>>) {
                    binding.progressBar.visibility = View.GONE
                    val data = res.body()?.takeIf { it.status == "success" }?.data
                    if (data != null) {
                        cachedData = data
                        renderAll(data)
                    } else showEmpty()
                }
                override fun onFailure(call: Call<ApiResponse<StatisticsData>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@StatisticsActivity, "Gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                    showEmpty()
                }
            })
    }

    // ── Render semua ─────────────────────────────────────────
    private fun renderAll(data: StatisticsData) {
        val hasOut = data.kategoriPengeluaran.isNotEmpty()
        val hasIn  = data.kategoriPemasukan.isNotEmpty()
        if (!hasOut && !hasIn) { showEmpty(); return }

        binding.scrollContent.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE

        // Summary header
        binding.tvTotalIncome.text  = FormatHelper.formatRupiah(data.totalPemasukan)
        binding.tvTotalExpense.text = FormatHelper.formatRupiah(data.totalPengeluaran)
        binding.tvSaldo.text        = FormatHelper.formatRupiah(data.totalPemasukan - data.totalPengeluaran)

        renderTab(data)
    }

    private fun renderTab(data: StatisticsData) {
        if (activeTab == "pengeluaran") {
            renderSection(
                list      = data.kategoriPengeluaran,
                total     = data.totalPengeluaran,
                colors    = COLORS_OUT,
                pieChart  = binding.pieChartOut,
                container = binding.llCategoriesOut,
                tvEmpty   = binding.tvEmptyOut
            )
        } else {
            renderSection(
                list      = data.kategoriPemasukan,
                total     = data.totalPemasukan,
                colors    = COLORS_IN,
                pieChart  = binding.pieChartIn,
                container = binding.llCategoriesIn,
                tvEmpty   = binding.tvEmptyIn
            )
        }
    }

    private fun renderSection(
        list: List<KategoriStat>,
        total: Double,
        colors: IntArray,
        pieChart: com.example.catatkeuangan.views.PieChartView,
        container: android.widget.LinearLayout,
        tvEmpty: android.widget.TextView
    ) {
        if (list.isEmpty()) {
            pieChart.visibility  = View.GONE
            container.visibility = View.GONE
            tvEmpty.visibility   = View.VISIBLE
            return
        }
        pieChart.visibility  = View.VISIBLE
        container.visibility = View.VISIBLE
        tvEmpty.visibility   = View.GONE

        pieChart.setData(
            list.map { it.persentase.toFloat() }.toFloatArray(),
            list.map { it.kategori }.toTypedArray(),
            list.mapIndexed { i, _ -> colors[i % colors.size] }.toIntArray()
        )

        container.removeAllViews()
        list.forEachIndexed { i, stat ->
            val color = colors[i % colors.size]
            val row   = layoutInflater.inflate(R.layout.item_category_stat, container, false)
            row.findViewById<View>(R.id.viewColor).setBackgroundColor(color)
            row.findViewById<android.widget.TextView>(R.id.tvPercent).apply {
                text = "${stat.persentase.toInt()}%"
                setBackgroundColor(color)
            }
            row.findViewById<android.widget.TextView>(R.id.tvCategoryName).text = stat.kategori
            row.findViewById<android.widget.TextView>(R.id.tvAmount).text =
                FormatHelper.formatRupiah(stat.total)
            container.addView(row)
        }
    }

    private fun showEmpty() {
        binding.scrollContent.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
