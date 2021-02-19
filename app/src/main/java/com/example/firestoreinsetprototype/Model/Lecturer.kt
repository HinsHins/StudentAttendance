package com.example.firestoreinsetprototype.Model

class Lecturer (
    override var id:String="",
    override var name:String="",
    var position:String="",
    var department:String=""
) : ModelBase