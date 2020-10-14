package com.belvin.expensetracker

import android.content.ContentValues
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.change_limit.*

class ChangeLimit : AppCompatActivity() {
    val currentUserId = SessionEssentials.CURRENT_USER_ID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_limit)
        val db = ExpenseTrackerDB(applicationContext).readableDatabase

        val cur = db.rawQuery("SELECT ELimit FROM LimitAndLogged WHERE Id = ?", arrayOf(currentUserId.toString()))
        if(cur.moveToFirst())
        {
            currentLimit.setText(cur.getInt(0).toString()+" Rs.")
        }

        changeLimit.setOnClickListener {
            val cv = ContentValues()
            cv.put("ELimit",newLimit.text.toString())
            db.update("LimitAndLogged",cv,"Id = ?", arrayOf(currentUserId.toString()))
            Toast.makeText(applicationContext,"Limit Changed Successfully..!",Toast.LENGTH_SHORT).show()
            currentLimit.setText(newLimit.text.toString()+" Rs.")
            newLimit.setText("")
        }
    }
}