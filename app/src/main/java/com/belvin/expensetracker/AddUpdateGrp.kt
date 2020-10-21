package com.belvin.expensetracker

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.add_update_grp.*

class AddUpdateGrp : AppCompatActivity() {

    lateinit var db:SQLiteDatabase
    lateinit var cur:Cursor
    var addingUser = 0
    val TripMembers = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_update_grp)

        db = ExpenseTrackerDB(applicationContext).readableDatabase

        cur = db.rawQuery("SELECT Name FROM Users WHERE TripId = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString()))

        while (cur.moveToNext())
        {
            TripMembers.add(cur.getString(0))
        }
        val myadapter = ArrayAdapter(this,android.R.layout.simple_dropdown_item_1line,TripMembers)
        MemberSpinner.adapter = myadapter

        GrpExpenseAddBtn.setOnClickListener {

            cur = db.rawQuery("SELECT Id FROM Users WHERE TripId = ? AND Name = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString(),MemberSpinner.selectedItem.toString()))
            if(cur.moveToFirst())
            {
                addingUser = cur.getInt(0)
            }

            val cv = ContentValues()
            cv.put("Userid",addingUser.toString())
            cv.put("TripId",SessionEssentials.CURRENT_TRIP_ID)
            cv.put("Price",GrpPrice.text.toString())
            cv.put("Description",GrpDescription.text.toString())

            db.insert("Expense",null,cv)


            cur = db.rawQuery("SELECT SUM(Price) FROM Expense WHERE Userid = ? AND TripId = ?", arrayOf(addingUser.toString(),SessionEssentials.CURRENT_TRIP_ID.toString()))
            var total = 0
            if(cur.moveToFirst())
            {
                total = cur.getInt(0)
            }
            cv.clear()
            cv.put("Total",total)

            db.update("LimitAndLogged",cv,"Id = ?", arrayOf(addingUser.toString()))

            Toast.makeText(applicationContext, "Expense Added Successfully", Toast.LENGTH_SHORT).show()
            GrpPrice.setText("")
            GrpDescription.setText("")

        }

    }
}