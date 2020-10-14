package com.belvin.expensetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.home.*

class Home : AppCompatActivity() {

  private val currentUserId = SessionEssentials.CURRENT_USER_ID
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.home)
    add.setOnClickListener {
      SessionEssentials.OPERATION = "Add Expense"
      startActivity(Intent(this,AddUpdateExpense::class.java))
    }
    view.setOnClickListener { startActivity(Intent(this,Statement::class.java)) }
    change.setOnClickListener { startActivity(Intent(this,ChangeLimit::class.java)) }

  }

  override fun onResume() {
    super.onResume()
    val db = ExpenseTrackerDB(applicationContext).readableDatabase
    var cur = db.rawQuery("SELECT * FROM LimitAndLogged WHERE Id = ?", arrayOf(currentUserId.toString()))
    if(cur.moveToFirst())
    {
      limit.setText(cur.getInt(1).toString()+" Rs.")
      expense.setText(cur.getInt(2).toString()+" Rs.")
      savings.text = (limit.text.toString().substring(0,limit.text.toString().length-4).toInt() - expense.text.toString().substring(0,expense.text.toString().length-4).toInt()).toString()+" Rs."
    }
    cur = db.rawQuery("SELECT Name FROM Users WHERE Id = ?", arrayOf(currentUserId.toString()))

    if(cur.moveToFirst())
    {
      name.setText("Welcome, "+cur.getString(0))
    }
  }
}