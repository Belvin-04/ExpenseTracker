package com.belvin.expensetracker

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.add_update_expense.*
import java.text.SimpleDateFormat
import java.util.*

class AddUpdateExpense : AppCompatActivity() {

  val currentUserId = SessionEssentials.CURRENT_USER_ID

  val c = Calendar.getInstance()
  val sdf = SimpleDateFormat("dd/MM/yyyy")
  val dateSet = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
    c.set(Calendar.YEAR,year)
    c.set(Calendar.MONTH,month)
    c.set(Calendar.DAY_OF_MONTH,dayOfMonth)
    date.setText(sdf.format(c.time).toString())

  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.add_update_expense)

    val db = ExpenseTrackerDB(applicationContext).readableDatabase

    operation.text = SessionEssentials.OPERATION

    date.setText(sdf.format(c.time).toString())
    date.setOnClickListener { DatePickerDialog(this,dateSet,c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show() }

    add_expense.setOnClickListener {

      val day = date.text.toString().substring(0,2)
      val month =  date.text.toString().substring(3,5)
      val year =  date.text.toString().substring(6,10)

      val cv = ContentValues()
      cv.put("Userid",currentUserId)
      cv.put("Day",day)
      cv.put("Month",month)
      cv.put("Year",year)
      cv.put("Price",a_expense.text.toString())
      cv.put("Description",a_description.text.toString())

      db.insert("Expense",null,cv)

      updateTotal(db)

      Toast.makeText(applicationContext,"Expense Added Succesfully..!",Toast.LENGTH_SHORT).show()
     resetFields()
    }

    if (operation.text.toString() == "Edit Expense")
    {
      updateDeleteGroup.visibility = View.VISIBLE
      add_expense.visibility = View.GONE

      date.setText(intent.getStringExtra("E_Date"))
      a_expense.setText(intent.getStringExtra("E_Price"))
      a_description.setText(intent.getStringExtra("E_Desc"))

      update_expense.setOnClickListener {

        val cv = ContentValues()
        val day = date.text.toString().substring(0,2)
        val month =  date.text.toString().substring(3,5)
        val year =  date.text.toString().substring(6,10)

        cv.put("Day",day)
        cv.put("Month",month)
        cv.put("Year",year)
        cv.put("Price",a_expense.text.toString())
        cv.put("Description",a_description.text.toString())

        db.update("Expense",cv,"Id = ? AND Userid = ?", arrayOf(intent.getStringExtra("E_Id"),currentUserId.toString()))


        Toast.makeText(this,"Expense Updated Successfully..!",Toast.LENGTH_SHORT).show()
        resetFields()
        SessionEssentials.OPERATION = "Add Expense"
        startActivity(Intent(this,Statement::class.java))
        updateTotal(db)
      }

      delete_expense.setOnClickListener {

        db.delete("Expense","Id = ? AND Userid = ?", arrayOf(intent.getStringExtra("E_Id"),currentUserId.toString()))
        Toast.makeText(this,"Expense Deleted Successfully..!",Toast.LENGTH_SHORT).show()
        resetFields()
        SessionEssentials.OPERATION = "Add Expense"
        startActivity(Intent(this,Statement::class.java))
        updateTotal(db)

      }

    }

  }

  fun updateTotal(db:SQLiteDatabase)
  {
    val cv = ContentValues()
    val cur = db.rawQuery("SELECT SUM(Price) FROM Expense WHERE Userid = ?", arrayOf(currentUserId.toString()))
    if(cur.moveToFirst())
    {
      cv.clear()
      cv.put("Total",cur.getInt(0))
      db.update("LimitAndLogged",cv,"Id = ?", arrayOf(currentUserId.toString()))
    }
  }

  fun resetFields()
  {
    date.setText(sdf.format(c.time).toString())
    a_expense.setText("")
    a_description.setText("")
  }

}