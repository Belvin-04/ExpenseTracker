package com.belvin.expensetracker

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.add_trip_details.*

class AddTripDetails : AppCompatActivity() {

    var memberCount = 1
    var currentTripId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_trip_details)

        val db = ExpenseTrackerDB(applicationContext).readableDatabase

        AddTripDetailsBtn.setOnClickListener {
            val cv = ContentValues()
            if(tripName.text.toString().isNotEmpty() || tripSize.text.toString().isNotEmpty() || tripBudget.text.toString().isNotEmpty())
            {
                cv.put("TripName",tripName.text.toString())
                cv.put("TotalTripMembers",tripSize.text.toString())
                cv.put("TripBudget",tripBudget.text.toString())
                cv.put("CurrentTrip","1")
                db.insert("Trips",null,cv)
                Toast.makeText(applicationContext, "Trip added..!!", Toast.LENGTH_SHORT).show()
                AddTripDetailsBtn.isEnabled = false
                memberGrp.visibility = View.VISIBLE

                val cur = db.rawQuery("SELECT * FROM Trips",null)
                if(cur.moveToLast())
                {
                    currentTripId = cur.getInt(0)
                    SessionEssentials.CURRENT_TRIP_ID = currentTripId
                }
            }
            else
            {
                Toast.makeText(applicationContext, "Please Enter a All Details...!!", Toast.LENGTH_SHORT).show()
            }

        }

        AddMemberBtn.setOnClickListener {
            if(tripMemberName.text.toString().isEmpty())
            {
                Toast.makeText(applicationContext, "Please Enter a Member Name...!!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val cv = ContentValues()
                cv.put("Name",tripMemberName.text.toString())
                cv.put("TripId",currentTripId)
                db.insert("Users",null,cv)
                val cur = db.rawQuery("SELECT Id FROM Users WHERE TripId = ?", arrayOf(currentTripId.toString()))
                if(cur.moveToLast())
                {
                    val cv2 = ContentValues()
                    cv2.put("Id",cur.getString(0))
                    cv2.put("Total",0)
                    db.insert("LimitAndLogged",null,cv2)

                }
                tripMemberName.setText("")
                tripMemberName.setHint((++memberCount).toString()+". Member Name")
                Toast.makeText(applicationContext, "Member added successfully..!", Toast.LENGTH_SHORT).show()
                if(memberCount > tripSize.text.toString().toInt())
                {
                    startActivity(Intent(this,GroupHome::class.java))
                }
            }
        }
    }
}