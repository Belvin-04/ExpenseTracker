package com.belvin.expensetracker

data class Expense(val user:String,val amt:Int,val desc:String)
data class Trip(val name:String,val totalMembers:Int,val budget:Int)
data class Member(val name:String)
data class TripDetails(val trip:Trip, val members:List<Member>,val expense: List<Expense>)
