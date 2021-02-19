package com.example.firestoreinsetprototype.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.sql.Time
import java.util.*

class Attendance (
    var studentId:String="",
    var status:Boolean=false,
    var date:Timestamp? = null,
    var time: Timestamp? = null,
    var moduleId:String="",
    var lesson_type:String="",
    var week:Int=0
        )

