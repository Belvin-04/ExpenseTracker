package com.belvin.expensetracker

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.forgot_password.*

class ForgotPassword : AppCompatActivity() {

    lateinit var otp:String
    lateinit var msg:String
    val SMS_PERMISSION_CODE = 1

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent?.action)
            {
                val msg = Telephony.Sms.Intents.getMessagesFromIntent(intent)[0].displayMessageBody.substring(0..5)
                otpText.setText(msg)

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)

        val db = ExpenseTrackerDB(applicationContext).readableDatabase

        otp = (100000..999999).random().toString()

        SessionEssentials.OTP  = otp

        msg = "$otp is your Expense Tracker One-Time-Password for resetting your Main Password."

        confirm.setOnClickListener {

            val cur = db.rawQuery("SELECT Name FROM Users WHERE Phone = ?", arrayOf(forgot_phone.text.toString()))
            if(cur.moveToFirst())
            {
                sendOTP(forgot_phone.text.toString(),msg)
                otpGrp.visibility = View.VISIBLE
                Toast.makeText(this, "OTP will arrive in a few seconds...", Toast.LENGTH_SHORT).show()
                confirm.isEnabled = false
            }
            else
            {
                Toast.makeText(this,"Entered Phone Number is Not Registered",Toast.LENGTH_SHORT).show()
            }
        }

        otp_confirm.setOnClickListener {

            val enteredOtp = otpText.text.toString()
            if(enteredOtp == SessionEssentials.OTP)
            {
                Toast.makeText(this,"Otp Matched...",Toast.LENGTH_SHORT).show()
                passGrp.visibility = View.VISIBLE
            }
            else
            {
                Toast.makeText(this,"Invalid OTP..!!",Toast.LENGTH_SHORT).show()
            }
        }

        resend.setOnClickListener {
            otp = (100000..999999).random().toString()
            SessionEssentials.OTP  = otp
            msg = "$otp is your Expense Tracker One-Time-Password for resetting your Main Password."
            sendOTP(forgot_phone.text.toString(),msg)
            Toast.makeText(this, "OTP will arrive in a few seconds...", Toast.LENGTH_SHORT).show()
        }

        confirmPass.setOnClickListener {

            if(newPass.text.toString() == newConfirmPass.text.toString())
            {
                val cv = ContentValues()
                cv.put("Password",encrypt(newConfirmPass.text.toString()))

                db.update("Users",cv,"Phone = ?", arrayOf(forgot_phone.text.toString()))

                Toast.makeText(applicationContext,"Password Changed Successfully..!",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,Signin::class.java))
            }
            else
            {
                Toast.makeText(this,"Password doesn't Match..!",Toast.LENGTH_SHORT).show()
            }

        }

    }

    override fun onStart() {
        super.onStart()

        registerReceiver(receiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    fun sendOTP(num:String,msg:String)
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) +
            ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS) +
            ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
            val smsmanager = SmsManager.getDefault()
            smsmanager.sendTextMessage(num,"Expense Tracker",msg,null,null)

        }
        else
        {
            requestSmsPermission()
        }


    }


    private fun requestSmsPermission()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_SMS) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECEIVE_SMS))
        {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This permission is needed by the application to send, receive and detect OTP")
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->

                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS),SMS_PERMISSION_CODE)
                })
                .setNegativeButton("CANCEL",DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                }).show()
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS),SMS_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if(requestCode == SMS_PERMISSION_CODE)
        {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun encrypt(password:String):String
    {
        return AESCrypt.encrypt("com.packageName.applicationName", password)
    }
}