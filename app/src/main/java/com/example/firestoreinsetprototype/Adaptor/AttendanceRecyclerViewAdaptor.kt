package com.example.firestoreinsetprototype.Adaptor

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestoreinsetprototype.Extension.inflate
import com.example.firestoreinsetprototype.Model.Attendance
import com.example.firestoreinsetprototype.R
import kotlinx.android.synthetic.main.student_item.view.*
class AttendanceRecyclerViewAdaptor(attendances: ArrayList<Attendance>) : RecyclerView.Adapter<AttendanceRecyclerViewAdaptor.AttendanceHolder>() {

     interface OnItemClickListener {
        fun onClick(v: View,position : Int)
        fun onLongClick(v: View,position : Int) : Boolean
    }

    val attendances : ArrayList<Attendance> = attendances
    var onItemClickListener : OnItemClickListener? = null

    //create item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceHolder {
        //without Extension
//        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.student_item,parent,false)
        //With Extension
        val view = parent.inflate(R.layout.timetable_item)
        return AttendanceHolder(view)
    }

    //bind data to view
    override fun onBindViewHolder(holder: AttendanceHolder, position: Int) {
        holder.view.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onClick(it,position)
        })
        holder.view.setOnLongClickListener(View.OnLongClickListener {
            val isClickable = onItemClickListener?.onLongClick(it,position) ?: true
            return@OnLongClickListener(isClickable)
        })
        holder.bindAttendance(attendances[position])
    }

    override fun getItemCount(): Int {
        return attendances.size
    }

    fun clear(){
        attendances.clear()
        onItemClickListener = null
    }

    //list item
    class AttendanceHolder(v: View) : RecyclerView.ViewHolder(v) {
        var view: View = v
        private var attendance: Attendance? = null

        fun bindAttendance(attendance: Attendance) {
            this.attendance = attendance
//            view.country_tv.text = student.country
        }
    }
}



