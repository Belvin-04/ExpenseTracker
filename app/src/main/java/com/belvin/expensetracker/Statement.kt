package com.belvin.expensetracker

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.statement.*

class Statement : AppCompatActivity(){

    val currentUserId = SessionEssentials.CURRENT_USER_ID


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statement)


        val db = ExpenseTrackerDB(applicationContext).readableDatabase
        val cur = db.rawQuery("SELECT * FROM Expense WHERE Userid = ?", arrayOf(currentUserId.toString()))


        displayData(cur)

        statementList.setOnItemClickListener { parent, view, position, id ->
            val str = parent.getItemAtPosition(position).toString().split("\n")
            val id = str[1].substring(4)
            val date = str[2].substring(6)
            val price = str[3].substring(7,str[3].length-4)
            val desc = str[4].substring(13)

            SessionEssentials.OPERATION = "Edit Expense"
            startActivity(Intent(this,AddUpdateExpense::class.java)
                .putExtra("E_Id",id)
                .putExtra("E_Date",date)
                .putExtra("E_Price",price)
                .putExtra("E_Desc",desc))
        }

        filter_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val cur = db.rawQuery("SELECT * FROM Expense WHERE Userid = ? AND (Id LIKE '%$s%' OR Price LIKE '%$s%' OR Description LIKE '%$s%' OR Day LIKE '%$s%' OR Month LIKE '%$s%' OR Year LIKE '%$s%')", arrayOf(currentUserId.toString()))
                displayData(cur)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.graph -> startActivity(Intent(this,GraphRepresentation::class.java).putExtra("AccountType","Personal"))
        }
        return super.onOptionsItemSelected(item)
    }

    fun displayData(cur:Cursor)
    {

        val expenseValues = mutableListOf<String>()
        while(cur.moveToNext())
        {
            expenseValues.add( "\nId: "+cur.getInt(0).toString()+"\n"+
                    "Date: "+cur.getString(3)+"/"+ cur.getString(4)+"/"+ cur.getString(5)+"\n"+
                    "Price: "+cur.getInt(6).toString()+" Rs.\n"+
                    "Description: "+cur.getString(7)+"\n")
        }

        val myadapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,expenseValues)

        statementList.adapter = myadapter
    }
}