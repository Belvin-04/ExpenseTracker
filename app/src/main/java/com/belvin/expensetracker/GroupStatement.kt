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

    val listOfReceivers = mutableListOf<Person>()
    val listOfGivers = mutableListOf<Person>()
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
            totalExpense.visibility = View.VISIBLE
            dividedStatement()
            check = 1
        }
        else
        {
            splitExpense.visibility = View.GONE
            totalExpense.visibility = View.GONE
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
        totalExpense.setText("Total Amount : ${SessionEssentials.EXPENSE_MADE} Rs.")
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

            if(payAmt!=0)
            {
                listOfGivers.add(Person(name,payAmt, mutableListOf(), mutableListOf(),payAmt,recAmt,cur.getInt(2)))
            }
            else
            {
                listOfReceivers.add(Person(name,recAmt, mutableListOf(), mutableListOf(),payAmt,recAmt,cur.getInt(2)))
            }
        }
        val list = calculate(listOfGivers,listOfReceivers)

        for(item in list)
        {

            var s = "\n"

            if(item.toList.isNotEmpty())
            {
                for(i in item.toList)
                {
                    s = s + i + "\n"
                }
            }
            if(item.fromList.isNotEmpty())
            {
                for(i in item.fromList)
                {
                    s = s + i + "\n"
                }
            }

            if(item.pAmt == 0)
            {
                ExpenseData.add(
                    "Name: "+item.name+"\n"+
                            "Total Paid: "+item.totalPaid+" Rs.\n"+
                            "Amount to Receive: "+item.rAmt+" Rs."+"\n"+
                            s)
            }
            else
            {
                ExpenseData.add(
                    "Name: "+item.name+"\n"+
                            "Total Paid: "+item.totalPaid+" Rs.\n"+
                            "Amount to Pay: "+item.pAmt+" Rs."+"\n"+
                            s)
            }
        }

        val myadapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,ExpenseData)
        grpStatement.adapter = myadapter
        grpStatement.setOnItemClickListener(null)
    }

    private fun calculate(listOfGivers: MutableList<Person>, listOfReceivers: MutableList<Person>):List<Person> {

        for(receiver in listOfReceivers)
        {
            for(giver in listOfGivers)
            {
                if((receiver.amt <= giver.amt) && (receiver.amt != 0))
                {
                    receiver.fromList.add("Receive from ${giver.name}: ${receiver.amt} Rs.")
                    giver.toList.add("Pay to ${receiver.name}: ${receiver.amt} Rs.")
                    giver.amt -= receiver.amt
                    receiver.amt = 0
                    continue
                }
                else if((receiver.amt > giver.amt) && (giver.amt != 0))
                {
                    receiver.fromList.add("Receive from ${giver.name}: ${giver.amt} Rs.")
                    giver.toList.add("Pay to ${receiver.name}: ${giver.amt} Rs.")
                    receiver.amt -= giver.amt
                    giver.amt = 0
                    continue
                }
            }
        }

        return (listOfGivers+listOfReceivers)
    }
}