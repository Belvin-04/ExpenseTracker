package com.belvin.expensetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.graph_representation.*
import java.util.*

class GraphRepresentation : AppCompatActivity() {

    val currentUserId = SessionEssentials.CURRENT_USER_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph_representation)

        val Expenses = mutableListOf<Float>()
        val Desc = mutableListOf<String>()

        val db = ExpenseTrackerDB(applicationContext).readableDatabase

        val cur = db.rawQuery("SELECT * FROM Expense WHERE Userid = ?", arrayOf(currentUserId.toString()))

        while(cur.moveToNext())
        {
            Expenses.add(cur.getInt(5).toFloat())
            Desc.add(cur.getString(6))
        }

        val entries = ArrayList<PieEntry>()

        for(i in 0 until Expenses.size)
        {
            entries.add(PieEntry(Expenses[i],Desc[i]))
        }

        val dataSet = PieDataSet(entries,"Expenses")
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS.toList())

        val pieData = PieData(dataSet)

        chart.data = pieData
        chart.animateY(1000)
        chart.invalidate()


    }
}