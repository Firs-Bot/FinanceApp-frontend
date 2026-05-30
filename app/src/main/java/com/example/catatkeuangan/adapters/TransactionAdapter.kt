package com.example.catatkeuangan.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.catatkeuangan.R
import com.example.catatkeuangan.databinding.ItemTransactionBinding
import com.example.catatkeuangan.models.Transaction
import com.example.catatkeuangan.utils.FormatHelper

class TransactionAdapter(
    private val list: MutableList<Transaction>,
    private val onClick: (Transaction) -> Unit,
    private val onDelete: (Transaction, Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.VH>() {

    inner class VH(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(t: Transaction, pos: Int) {
            b.tvEmoji.text    = FormatHelper.getCategoryEmoji(t.kategori)
            b.tvJudul.text    = t.judul
            b.tvKategori.text = t.kategori
            b.tvTanggal.text  = FormatHelper.formatDate(t.tanggal)

            val formatted = FormatHelper.formatRupiah(t.jumlah)
            if (t.tipe == "pemasukan") {
                b.tvJumlah.text = "+ $formatted"
                b.tvJumlah.setTextColor(ContextCompat.getColor(b.root.context, R.color.color_income))
            } else {
                b.tvJumlah.text = "- $formatted"
                b.tvJumlah.setTextColor(ContextCompat.getColor(b.root.context, R.color.color_expense))
            }

            b.root.setOnClickListener { onClick(t) }
            b.btnDelete.setOnClickListener { onDelete(t, pos) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(list[position], position)
    override fun getItemCount() = list.size

    fun updateData(newList: List<Transaction>) {
        list.clear(); list.addAll(newList); notifyDataSetChanged()
    }

    fun removeAt(pos: Int) {
        if (pos < list.size) { list.removeAt(pos); notifyItemRemoved(pos) }
    }
}
