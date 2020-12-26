package com.belvin.expensetracker

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.group_home.*
import java.io.File

class GroupHome : AppCompatActivity() {

    lateinit var db:SQLiteDatabase
    lateinit var cur:Cursor
    lateinit var trip:Trip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_home)

        add.setOnClickListener {
            SessionEssentials.GRP_OPERATION = "Add Expense"
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

            R.id.exportBtn -> {
                export()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun export()
    {
        val memberList = ArrayList<Member>()
        val expenseList = ArrayList<Expense>()
        val expenseCur = db.rawQuery("SELECT Userid,Price,Description FROM Expense WHERE TripId = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString()))
        val tripCur = db.rawQuery("SELECT TripName,TotalTripMembers,TripBudget FROM Trips WHERE CurrentTrip = ?", arrayOf("1"))
        val userCur = db.rawQuery("SELECT Name FROM Users WHERE TripId = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString()))

        if(tripCur.moveToFirst())
        {
            trip = Trip(tripCur.getString(0),tripCur.getInt(1),tripCur.getInt(2))
        }

        while (expenseCur.moveToNext())
        {
            val expUserCur = db.rawQuery("SELECT Name FROM Users WHERE TripId = ? AND Id = ?",
                arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString(),expenseCur.getString(0)))
            var name = ""
            if(expUserCur.moveToFirst())
            {
                name = expUserCur.getString(0)
            }
            expenseList.add(Expense(name,expenseCur.getInt(1),expenseCur.getString(2)))
        }
        while (userCur.moveToNext())
        {
            memberList.add(Member(userCur.getString(0)))
        }

        val tripDetails = TripDetails(trip,memberList,expenseList)
        val gson = Gson()
        val data = gson.toJson(tripDetails)

        val f = File(Environment.getExternalStorageDirectory().toString()+"/ExpenseTracker/data.json")
        f.createNewFile()
        f.writeText(data)

        Toast.makeText(this, "Data exported successfully..!", Toast.LENGTH_SHORT).show()
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