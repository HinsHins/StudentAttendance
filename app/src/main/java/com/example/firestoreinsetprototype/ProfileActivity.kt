package com.example.firestoreinsetprototype

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.StudentRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Adaptor.TimetableRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Model.*
import com.example.firestoreinsetprototype.Util.retrieveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_student.*

class ProfileActivity : AppCompatActivity() {

    private val attendancesPath = FirestoreCollectionPath.ATTENDANCES_PATH
    private val lecturersPath = FirestoreCollectionPath.LECTURERS_PATH
    private val modulesPath = FirestoreCollectionPath.MODULES_PATH

    private val attendances = ArrayList<Attendance>()
    private val lecturers = ArrayList<Lecturer>()
    private val modules = ArrayList<Module>()
    private var modulesIds = ArrayList<String>()


    private val timetables = ArrayList<Timetable>()
    private val fb = FirebaseFirestore.getInstance()
    private var currentUserEmail = ""
    private var currentLecturer = Lecturer()

    lateinit var timetableAdapter: TimetableRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        currentUserEmail = intent.getStringExtra("email").toString()

        val linearLayoutManager = LinearLayoutManager(this)
        timetable_recyclerView.layoutManager = linearLayoutManager
        timetable_recyclerView.adapter = TimetableRecyclerViewAdaptor(timetables)
        timetableAdapter = (timetable_recyclerView.adapter as TimetableRecyclerViewAdaptor)
        timetableAdapter.onItemClickListener =
            object : TimetableRecyclerViewAdaptor.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    Log.d("onClick", "$position")
                }

                override fun onLongClick(v: View, position: Int): Boolean {
                    return true
                }
            }
        retrieveModules()


    }

    private fun retrieveLecturers() {
        val lecturersCollection = fb.collection(lecturersPath)
        lecturersCollection.retrieveData(lecturers as ArrayList<ModelBase>, Lecturer::class.java) {
            currentLecturer = lecturers.filter {
                it.email == currentUserEmail
            }.first()
            retrieveTimetables()
        }
    }

    private fun retrieveModules() {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.retrieveData(modules as ArrayList<ModelBase>, Module::class.java) {
            retrieveLecturers()
        }
    }

    private fun retrieveTimetables() {
        val attendanceCollection = fb.collection(attendancesPath)
        attendanceCollection.retrieveData(
            attendances as ArrayList<ModelBase>,
            Attendance::class.java
        ) {

            modulesIds = attendances.filter {
                currentLecturer.id == it.lecturerId
            }.distinctBy {
                it.moduleId
            }.map {
                it.moduleId
            } as ArrayList<String>
            var currentModules =
                findMatchedModules(modules, modulesIds)
            Log.d("Targeted Modules", currentModules.toString())
            timetableAdapter.notifyDataSetChanged()
        }
    }

    fun findMatchedModules(modules: ArrayList<Module>, ids: ArrayList<String>): ArrayList<Module> {
        var result = ArrayList<Module>()
        for (module in modules) {
            for (id in ids) {
                if (id == module.id) {
                    result.add(module)
                }
            }
        }
        return result
    }
}