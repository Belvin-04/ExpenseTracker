package com.belvin.expensetracker

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.add_trip.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class AddTrip : AppCompatActivity() {
    lateinit var cur: Cursor
    lateinit var db: SQLiteDatabase
    val tripList = mutableListOf<String>()
    val cv = ContentValues()
    var currentTripId = 0
    var addingUser = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_trip)

        db = ExpenseTrackerDB(this).readableDatabase

        addTripBtn.setOnClickListener {
            val b = it as Button
            if(b.text == "Add Trip")
            {
                startActivity(Intent(this, AddTripDetails::class.java))
            }
            else
            {
                startActivity(Intent(this, GroupHome::class.java))
            }

        }

        if(tripExists())
        {
            addTripBtn.setText("Current Trip")
        }

        cur = db.rawQuery("SELECT * FROM Trips WHERE CurrentTrip = ?", arrayOf("0"))

        while (cur.moveToNext())
        {
            var memberNameString = ""
            val memberNameCur = db.rawQuery("SELECT Name FROM Users WHERE TripId = ?", arrayOf(cur.getString(0)))

            while(memberNameCur.moveToNext())
            {
                memberNameString += memberNameCur.getString(0)+", "
                if(memberNameCur.isLast)
                {
                    memberNameString = memberNameString.substring(0..memberNameString.length-3)
                }
            }

            tripList.add("Trip Id: ${cur.getInt(0)} \n" +
                    "Trip to: ${cur.getString(1)} \n" +
                    "Members Name: (${memberNameString}) \n"+
                    "Total Members: ${cur.getInt(2)} \n"+
                    "Trip Budget: ${cur.getInt(3)} \n")
        }

        prevTrips.adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,tripList)

        prevTrips.setOnItemClickListener { parent, view, position, id ->
            SessionEssentials.CURRENT_TRIP_ID = tripList[position].split("\n")[0].substring(8).trim().toInt()
            startActivity(Intent(this,GroupHome::class.java))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.personmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.personBtn -> {
                startActivity(Intent(this, Signin::class.java))
            }

            R.id.importBtn -> {
                import()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun tripExists():Boolean
    {
        cur = db.rawQuery("SELECT Id FROM Trips WHERE CurrentTrip = ?", arrayOf("1"))
        if(cur.moveToFirst())
        {
            SessionEssentials.CURRENT_TRIP_ID = cur.getInt(0)
            SessionEssentials.TEMP_TRIP_ID = SessionEssentials.CURRENT_TRIP_ID
            return true
        }
        return false
    }


    private fun import() {



        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val f = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/ExpenseTracker/data.json"
            )
            val reader = FileReader(f)
            val breader = BufferedReader(reader)
            val stringBuilder = StringBuilder()
            var line = breader.readLine()
            while (line != null) {
                stringBuilder.append(line).append("\n")
                line = breader.readLine()
            }
            breader.close()
            val response = stringBuilder.toString()
            val gson = Gson()
            val data = gson.fromJson(response, TripDetails::class.java)

            cv.clear()
            cv.put("TripName", data!!.trip.name)
            cv.put("TotalTripMembers", data.trip.totalMembers.toString())
            cv.put("TripBudget", data.trip.budget.toString())
            cv.put("CurrentTrip", "1")
            db.insert("Trips", null, cv)

            cur = db.rawQuery("SELECT * FROM Trips", null)
            if (cur.moveToLast()) {
                currentTripId = cur.getInt(0)
                SessionEssentials.CURRENT_TRIP_ID = currentTripId
            }
            cv.clear()

            for (member in data.members) {
                cv.put("Name", member.name)
                cv.put("TripId", currentTripId)
                db.insert("Users", null, cv)
                cv.clear()
            }

            for (expense in data.expense) {
                cur = db.rawQuery(
                    "SELECT Id FROM Users WHERE TripId = ? AND Name = ?",
                    arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString(), expense.user)
                )
                if (cur.moveToFirst()) {
                    addingUser = cur.getInt(0)
                }
                cv.put("Userid", addingUser.toString())
                cv.put("TripId", SessionEssentials.CURRENT_TRIP_ID)
                cv.put("Price", expense.amt.toString())
                cv.put("Description", expense.desc)
                db.insert("Expense", null, cv)
                cv.clear()
            }

            for (member in data.members) {
                cur = db.rawQuery(
                    "SELECT Id FROM Users WHERE TripId = ? AND Name = ?",
                    arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString(), member.name)
                )
                if (cur.moveToFirst()) {
                    addingUser = cur.getInt(0)
                }
                val cur1 = db.rawQuery(
                    "SELECT SUM(Price) FROM Expense WHERE TripId = ? AND Userid = ?",
                    arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString(), addingUser.toString())
                )
                if (cur1.moveToFirst()) {
                    cv.put("Id", addingUser)
                    cv.put("Total", cur1.getString(0))
                } else {
                    cv.put("Id", addingUser)
                    cv.put("Total", 0)
                }
                db.insert("LimitAndLogged", null, cv)
                cv.clear()
            }
            startActivity(Intent(this, GroupHome::class.java))
            Toast.makeText(this, "Trip data imported successfully...!", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                123
            )
        }
    }
}