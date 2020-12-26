package com.belvin.expensetracker

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.group_statement.*

class GroupStatement : AppCompatActivity() {

    lateinit var db:SQLiteDatabase
    lateinit var cur:Cursor
    val ExpenseData = mutableListOf<String>()
    var check = 0
    var dividedExpense = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_statement)

        db = ExpenseTrackerDB(applicationContext).readableDatabase
        normalStatement()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.grpstatement,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            R.id.grpGraph -> {
                startActivity(Intent(this,GraphRepresentation::class.java).putExtra("AccountType","Group"))
            }

            R.id.splitbill -> {
                ExpenseData.clear()
                toggleList()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun toggleList()
    {
        if(check==0)
        {
            splitExpense.visibility = View.VISIBLE
            dividedStatement()
            check = 1
        }
        else
        {
            splitExpense.visibility = View.GONE
            normalStatement()
            check = 0
        }
    }

    private fun normalStatement()
    {
        cur = db.rawQuery("SELECT * FROM Expense WHERE TripId = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString()))

        while (cur.moveToNext())
        {
            var name = ""
            val nameCur = db.rawQuery("SELECT Name FROM Users WHERE Id = ?",arrayOf(cur.getString(1)))
            if(nameCur.moveToFirst())
            {
                name = nameCur.getString(0)
            }

            ExpenseData.add( "\nId: "+cur.getInt(0).toString()+"\n"+
                    "Name: " +name+"\n"+
                    "Price: "+cur.getInt(6).toString()+" Rs.\n"+
                    "Description: "+cur.getString(7)+"\n")
        }
        val myadapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,ExpenseData)
        grpStatement.adapter = myadapter
        grpStatement.setOnItemClickListener { parent, view, position, id ->
            val str = parent.getItemAtPosition(position).toString().split("\n")
            val id = str[1].substring(4)
            val name = str[2].substring(6)
            val price = str[3].substring(7,str[3].length-4)
            val desc = str[4].substring(13)

            SessionEssentials.GRP_OPERATION = "Edit Expense"
            startActivity(Intent(this,AddUpdateGrp::class.java)
                .putExtra("Id",id)
                .putExtra("Name",name)
                .putExtra("Price",price)
                .putExtra("Desc",desc))
        }
    }

    private fun dividedStatement()
    {
        var payAmt = 0
        var recAmt = 0
        cur = db.rawQuery("SELECT TotalTripMembers FROM Trips WHERE CurrentTrip = ?", arrayOf("1"))
        if(cur.moveToNext())
        {
            dividedExpense = SessionEssentials.EXPENSE_MADE / cur.getInt(0)
        }

        splitExpense.setText("Divided Amount : $dividedExpense Rs.")
        cur = db.rawQuery("SELECT * FROM LimitAndLogged WHERE Id IN (SELECT Id FROM Users WHERE TripId = ?)", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString()))

        var name = ""

        while(cur.moveToNext())
        {
            val cur1 = db.rawQuery("SELECT Name FROM Users WHERE Id = ?", arrayOf(cur.getString(0)))
            if(cur1.moveToNext())
            {
                name = cur1.getString(0)
            }

            if(cur.getInt(2)>dividedExpense)
            {
                recAmt = cur.getInt(2) - dividedExpense
                payAmt = 0
            }
            else
            {
                payAmt = dividedExpense - cur.getInt(2)
                recAmt = 0
            }

            ExpenseData.add( "\nId: "+cur.getInt(0).toString()+"\n"+
                    "Name: "+name+"\n"+
                    "Total Paid: "+cur.getInt(2).toString()+" Rs.\n"+
                    "Amount to Pay: "+payAmt+" Rs."+"\n"+
                    "Amount to Receive: "+recAmt+" Rs."+"\n")
        }
        val myadapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,ExpenseData)
        grpStatement.adapter = myadapter
        grpStatement.setOnItemClickListener(null)
    }
}