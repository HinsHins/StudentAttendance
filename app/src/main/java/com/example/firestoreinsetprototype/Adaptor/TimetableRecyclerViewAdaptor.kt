package com.example.firestoreinsetprototype.Adaptor

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestoreinsetprototype.Extension.inflate
import com.example.firestoreinsetprototype.Model.Attendance
import com.example.firestoreinsetprototype.Model.Timetable
import com.example.firestoreinsetprototype.R
import kotlinx.android.synthetic.main.module_item.view.*
import kotlinx.android.synthetic.main.student_item.view.*
import kotlinx.android.synthetic.main.timetable_item.view.*

class TimetableRecyclerViewAdaptor(timetables: ArrayList<Timetable>) : RecyclerView.Adapter<TimetableRecyclerViewAdaptor.TimetableHolder>() {

     interface OnItemClickListener {
        fun onClick(v: View,position : Int)
        fun onLongClick(v: View,position : Int) : Boolean
    }

    val timetables : ArrayList<Timetable> = timetables
    var onItemClickListener : OnItemClickListener? = null

    //create item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableHolder {
        //without Extension
//        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.student_item,parent,false)
        //With Extension
        val view = parent.inflate(R.layout.timetable_item)
        return TimetableHolder(view)
    }

    //bind data to view
    override fun onBindViewHolder(holder: TimetableHolder, position: Int) {
        holder.view.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onClick(it,position)
        })
        holder.view.setOnLongClickListener(View.OnLongClickListener {
            val isClickable = onItemClickListener?.onLongClick(it,position) ?: true
            return@OnLongClickListener(isClickable)
        })
        holder.bindTimetable(timetables[position])
    }

    override fun getItemCount(): Int {
        return timetables.size
    }

    fun clear(){
        timetables.clear()
        onItemClickListener = null
    }

    //list item
    class TimetableHolder(v: View) : RecyclerView.ViewHolder(v) {
        var view: View = v
        private var timetable: Timetable? = null

        fun bindTimetable(timetable: Timetable) {
            this.timetable = timetable
            view.lesson_name_tv.text = timetable.moduleName
            view.startDate_tv.text = timetable.date.toString()
            view.time_tv.text = timetable.time.toString()

        }
    }
}



