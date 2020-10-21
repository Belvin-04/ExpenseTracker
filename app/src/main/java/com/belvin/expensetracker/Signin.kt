package com.belvin.expensetracker

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login.*

class Signin : AppCompatActivity() {

    lateinit var db:SQLiteDatabase
    lateinit var cur: Cursor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        db = ExpenseTrackerDB(applicationContext).readableDatabase

        login.setOnClickListener {
            val args = arrayOf(phone.text.toString(),encrypt(pass.text.toString()))
            cur = db.rawQuery("SELECT * FROM Users WHERE Phone = ? AND Password = ?",args)

            if(cur.moveToFirst())
            {
                resetFields()
                SessionEssentials.CURRENT_USER_ID = cur.getInt(0)
                startActivity(Intent(this,Home::class.java))
            }
            else
            {
                resetFields()
                Toast.makeText(applicationContext,"Incorrect Details..!",Toast.LENGTH_SHORT).show()
            }
        }

        newUser.setOnClickListener {
            resetFields()
            startActivity(Intent(this,Signup::class.java)) }

        forgot.setOnClickListener {
            resetFields()
            startActivity(Intent(this,ForgotPassword::class.java)) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.groupmenu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            R.id.grpBtn -> {

                if(tripExists())
                {
                    startActivity(Intent(this,GroupHome::class.java))
                }
                else
                {
                    startActivity(Intent(this,AddTrip::class.java))
                }

            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun encrypt(password:String):String
    {
        return AESCrypt.encrypt("com.packageName.applicationName", password)
    }

    private fun tripExists():Boolean
    {
        cur = db.rawQuery("SELECT Id FROM Trips WHERE CurrentTrip = ?", arrayOf("1"))
        if(cur.moveToFirst())
        {
            SessionEssentials.CURRENT_TRIP_ID = cur.getInt(0)
            return true
        }
        return false
    }

    private fun resetFields()
    {
        phone.setText("")
        pass.setText("")
    }
}