package com.example.firestoreinsetprototype.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.sql.Time
import java.util.*

class Attendance (
    override var id:String="",
    override var name:String="",
    var studentId:String="",
    var status:Boolean=false,
    var date:Timestamp? = null,
    var time: Timestamp? = null,
    var lecturerId:String="",
    var moduleId:String="",
    var moduleName:String="",
    var lesson_type:String="",
    var week:Int=0
):ModelBase

