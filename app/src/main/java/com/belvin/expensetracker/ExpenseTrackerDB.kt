package com.belvin.expensetracker

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseTrackerDB(context : Context) : SQLiteOpenHelper(context,"ExpenseTracker",null,1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE Users(Id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL,TripId Integer ,Name VARCHAR(50), Phone VARCHAR(50), Password VARCHAR(50));")
        db?.execSQL("CREATE TABLE Expense(Id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,Userid INTEGER,TripId INTEGER ,Day VARCHAR(50),Month VARCHAR(50),Year VARCHAR(50), Price INTEGER,Description VARCHAR(50));")
        db?.execSQL("CREATE TABLE LimitAndLogged(Id INTEGER,ELimit INTEGER,Total INTEGER,Logged VARCHAR(50));")
        db?.execSQL("CREATE TABLE Trips(Id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,TripName VARCHAR(50),TotalTripMembers INTEGER,TripBudget INTEGER,CurrentTrip Integer);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}