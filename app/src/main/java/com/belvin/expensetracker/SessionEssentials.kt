package com.belvin.expensetracker


data class Person(val name:String, var amt:Int, val toList:MutableList<String>, val fromList:MutableList<String>,val pAmt:Int,val rAmt:Int,val totalPaid:Int)

class SessionEssentials {

   companion object{
       var CURRENT_USER_ID = 0
       var OPERATION = ""
       var GRP_OPERATION = ""
       var OTP = ""
       var CURRENT_TRIP_ID = 0
       var TEMP_TRIP_ID = 0
       var EXPENSE_MADE = 0

   }

}