package com.belvin.expensetracker

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
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

        GrpOperation.setText(SessionEssentials.GRP_OPERATION)

        if(GrpOperation.text.toString() == "Edit Expense")
        {
            GrpExpenseAddBtn.visibility = View.GONE
            editGrp.visibility = View.VISIBLE

            val name = intent.extras?.getString("Name")
            val price = intent.extras?.getString("Price")
            val desc = intent.extras?.getString("Desc")

            val nameIndex = TripMembers.indexOf(name)

            val tempName = TripMembers[0]
            TripMembers[0] = TripMembers[nameIndex]
            TripMembers[nameIndex] = tempName

            GrpDescription.setText(desc)
            GrpPrice.setText(price)
        }

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
            Toast.makeText(applicationContext, "Expense Added Successfully", Toast.LENGTH_SHORT).show()
            updateTotal()
        }

        GrpExpenseUpdateBtn.setOnClickListener {
            cur = db.rawQuery("SELECT Id FROM Users WHERE TripId = ? AND Name = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString(),MemberSpinner.selectedItem.toString()))
            if(cur.moveToFirst())
            {
                addingUser = cur.getInt(0)
            }
            val cv = ContentValues()
            cv.put("Userid",addingUser.toString())
            cv.put("Tripid",SessionEssentials.CURRENT_TRIP_ID)
            cv.put("Price",GrpPrice.text.toString())
            cv.put("Description",GrpDescription.text.toString())
            db.update("Expense",cv,"Id = ?", arrayOf(intent.extras?.getString("Id")))

            val addingUserNameCur = db.rawQuery("SELECT Name FROM Users WHERE Id = ? AND TripId = ?", arrayOf(addingUser.toString(),SessionEssentials.CURRENT_TRIP_ID.toString()))
            val oldUserNameIdCur= db.rawQuery("SELECT Id FROM Users WHERE Name = ? AND TripId = ?", arrayOf(intent.extras?.getString("Name"),SessionEssentials.CURRENT_TRIP_ID.toString()))

            var oldUserNameId = ""
            if(oldUserNameIdCur.moveToNext())
            {
                oldUserNameId = oldUserNameIdCur.getString(0)
            }
            var addingUserName = ""

            if(addingUserNameCur.moveToNext())
            {
                addingUserName = addingUserNameCur.getString(0)
            }

            val oldPriceCur = db.rawQuery("SELECT Total FROM LimitAndLogged WHERE Id = ?", arrayOf(oldUserNameId))
            var oldTotal = ""
            if(oldPriceCur.moveToNext())
            {
                oldTotal = oldPriceCur.getString(0)
            }

            val oldPrice = intent.extras?.getString("Price")

            var newPrice = (oldTotal.toInt() - oldPrice!!.toInt())

            if(intent.extras?.getString("Name") != addingUserName)
            {
                val cv1 = ContentValues()
                cv1.put("Total",newPrice)
                db.update("LimitAndLogged",cv1,"Id = ?", arrayOf(oldUserNameId))
            }

            Toast.makeText(applicationContext, "Expense Updated Successfully", Toast.LENGTH_SHORT).show()
            updateTotal()
            startActivity(Intent(this,GroupStatement::class.java))
        }

        GrpExpenseDeleteBtn.setOnClickListener {

            cur = db.rawQuery("SELECT Id FROM Users WHERE TripId = ? AND Name = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString(),MemberSpinner.selectedItem.toString()))
            if(cur.moveToFirst())
            {
                addingUser = cur.getInt(0)
            }
            db.delete("Expense","Id = ?", arrayOf(intent.extras?.getString("Id")))
            Toast.makeText(applicationContext, "Expense Deleted Successfully", Toast.LENGTH_SHORT).show()
            updateTotal()
            startActivity(Intent(this,GroupStatement::class.java))
        }

    }

    private fun updateTotal()
    {
        cur = db.rawQuery("SELECT SUM(Price) FROM Expense WHERE Userid = ? AND TripId = ?", arrayOf(addingUser.toString(),SessionEssentials.CURRENT_TRIP_ID.toString()))
        var total = 0
        if(cur.moveToFirst())
        {
            total = cur.getInt(0)
        }
        val totalCur = db.rawQuery("SELECT SUM(Price) FROM Expense WHERE TripId = ?", arrayOf(SessionEssentials.CURRENT_TRIP_ID.toString()))
        if(totalCur.moveToNext())
        {
            SessionEssentials.EXPENSE_MADE = totalCur.getInt(0)
        }
        val cv = ContentValues()
        cv.put("Total",total)

        db.update("LimitAndLogged",cv,"Id = ?", arrayOf(addingUser.toString()))

        GrpPrice.setText("")
        GrpDescription.setText("")
    }
}