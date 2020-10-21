package com.belvin.expensetracker

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.group_home.*

class GroupHome : AppCompatActivity() {

    lateinit var db:SQLiteDatabase
    lateinit var cur:Cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_home)

        add.setOnClickListener {
            startActivity(Intent(this,AddUpdateGrp::class.java))
        }
        view.setOnClickListener {
            startActivity(Intent(this,GroupStatement::class.java))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.endtripmenu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.endTrip -> {
                AlertDialog.Builder(this)
                    .setTitle("End Trip..!")
                    .setMessage("Clicking on Ok will end this trip...")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        val cv = ContentValues()
                        cv.put("CurrentTrip", "0")
                        db.update("Trips", cv, "CurrentTrip = ?", arrayOf("1"))
                        SessionEssentials.CURRENT_TRIP_ID = 0
                        Toast.makeText(applicationContext, "Hope you enjoyed your trip..!!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Signin::class.java))
                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    }).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        db = ExpenseTrackerDB(applicationContext).readableDatabase
        cur = db.rawQuery("SELECT SUM(Price) FROM Expense WHERE TripId = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString()))

        var expense = ""
        var savings = ""
        var limit = ""

        if(cur.moveToFirst())
        {
            if(cur.getString(0) != null)
            {
                SessionEssentials.EXPENSE_MADE = cur.getInt(0)
                expense = cur.getString(0)+" Rs."
            }
            else
            {
                SessionEssentials.EXPENSE_MADE = 0
                expense = "0 Rs."
            }
        }

        cur = db.rawQuery("SELECT TripBudget FROM Trips WHERE CurrentTrip = ?", arrayOf("1"))
        if(cur.moveToFirst())
        {
            limit = cur.getString(0)+" Rs."

        }

        savings = (limit.substring(0,limit.length-4).toInt() - expense.substring(0,expense.length-4).toInt()).toString()+" Rs."

        cur = db.rawQuery("SELECT TripName FROM Trips WHERE CurrentTrip = ?", arrayOf("1"))
        if(cur.moveToFirst())
        {
            Grpname.setText("Welcome To "+cur.getString(0))
        }

        Grpexpense.setText(expense)
        Grplimit.setText(limit)
        Grpsavings.setText(savings)
        super.onResume()
    }
}