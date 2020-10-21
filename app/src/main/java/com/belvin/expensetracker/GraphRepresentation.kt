package com.belvin.expensetracker

import android.database.Cursor
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
    lateinit var cur:Cursor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph_representation)

        val Expenses = mutableListOf<Float>()
        val Desc = mutableListOf<String>()

        val db = ExpenseTrackerDB(applicationContext).readableDatabase

        val graphdata = intent.extras?.getString("AccountType")

        if(graphdata == "Personal")
        {
            cur = db.rawQuery("SELECT * FROM Expense WHERE Userid = ?", arrayOf(currentUserId.toString()))
        }
        else
        {
            cur = db.rawQuery("SELECT * FROM Expense WHERE TripId = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString()))
        }


        while(cur.moveToNext())
        {
            Expenses.add(cur.getInt(6).toFloat())
            Desc.add(cur.getString(7))
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