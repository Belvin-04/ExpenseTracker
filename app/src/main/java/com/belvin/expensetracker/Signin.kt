package com.belvin.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login.*

class Signin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val db = ExpenseTrackerDB(applicationContext).readableDatabase

        login.setOnClickListener {
            val args = arrayOf(phone.text.toString(),encrypt(pass.text.toString()))
            val cur = db.rawQuery("SELECT * FROM Users WHERE Phone = ? AND Password = ?",args)

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

    private fun encrypt(password:String):String
    {
        return AESCrypt.encrypt("com.packageName.applicationName", password)
    }

    private fun resetFields()
    {
        phone.setText("")
        pass.setText("")
    }
}