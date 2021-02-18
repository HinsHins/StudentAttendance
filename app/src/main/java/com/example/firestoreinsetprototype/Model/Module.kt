package com.example.firestoreinsetprototype.Model

import com.google.firebase.Timestamp

class Module(
    var id:String="",
    var name:String="",
   // var year:Int=0,
   // var level:Int=0,
    //var credit:Int=0,
    var startDate:Timestamp?=null,
    var lecturerId:String="",
    var lecturerName:String="",
    var programmeId:String="",
    var programmeName:String=""

)