package com.belvin.expensetracker

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseTrackerDB(context : Context) : SQLiteOpenHelper(context,"ExpenseTracker",null,1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE Users(Id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, Name VARCHAR(50), Phone VARCHAR(50), Password VARCHAR(50));")
        db?.execSQL("CREATE TABLE Expense(Id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,Userid INTEGER, Day VARCHAR(50),Month VARCHAR(50),Year VARCHAR(50), Price INTEGER,Description VARCHAR(50));")
        db?.execSQL("CREATE TABLE LimitAndLogged(Id INTEGER,ELimit INTEGER,Total INTEGER,Logged VARCHAR(50));")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}