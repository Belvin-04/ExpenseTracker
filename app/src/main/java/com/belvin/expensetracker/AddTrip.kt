package com.belvin.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.add_trip.*

class AddTrip : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_trip)

        addTripBtn.setOnClickListener {
            startActivity(Intent(this,AddTripDetails::class.java))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.personmenu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            R.id.personBtn -> {
                startActivity(Intent(this,Signin::class.java))
            }
        }

        return super.onOptionsItemSelected(item)
    }
}