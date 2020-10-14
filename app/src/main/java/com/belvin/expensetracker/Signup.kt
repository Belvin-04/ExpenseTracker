package com.belvin.expensetracker

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.signup.*
import java.util.*

class Signup : AppCompatActivity() {
    lateinit var db:SQLiteDatabase
    val c = Calendar.getInstance()
    val date = ""+c.get(Calendar.MONTH)+"/"+c.get(Calendar.YEAR)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        db = ExpenseTrackerDB(applicationContext).readableDatabase

        signup.setOnClickListener {
            if(!userExists())
            {
                if(signup_pass.text.toString() == confirm_pass.text.toString())
                {
                    val cv = ContentValues()
                    cv.put("Name",name.text.toString())
                    cv.put("Phone",phone.text.toString())
                    cv.put("Password",encrypt(confirm_pass.text.toString()))
                    db.insert("Users",null,cv)

                    val cur = db.rawQuery("SELECT Id FROM Users WHERE Phone = ?", arrayOf(phone.text.toString()))

                    if(cur.moveToFirst())
                    {
                        cv.clear()
                        cv.put("Id",cur.getInt(0))
                        cv.put("ELimit",500)
                        cv.put("Total",0)
                        cv.put("Logged",date)
                        db.insert("LimitAndLogged",null,cv)
                        Toast.makeText(applicationContext,"Registration Successful..!",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,Signin::class.java))
                    }
                }
                else
                {
                    Toast.makeText(applicationContext,"Password doesn't Match..!",Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(this,"Phone Number is already Registered",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun userExists(): Boolean{

        val rec = db.rawQuery("SELECT * FROM Users WHERE Phone = ?", arrayOf(phone.text.toString()))

        if(rec.moveToFirst())
        {
            return true
        }
        return false
    }

    private fun encrypt(password:String):String
    {
        return AESCrypt.encrypt("com.packageName.applicationName", password)
    }
}