package com.example.firestoreinsetprototype

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.util.TimeUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.ModuleRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Lecturer
import com.example.firestoreinsetprototype.Model.ModelBase
import com.example.firestoreinsetprototype.Model.Module
import com.example.firestoreinsetprototype.Model.Programme
import com.example.firestoreinsetprototype.Util.DateUtil
import com.example.firestoreinsetprototype.Util.SpinnerUtil
import com.example.firestoreinsetprototype.Util.realTimeUpdate
import com.example.firestoreinsetprototype.Util.retrieveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.synthetic.main.activity_module.*
import java.sql.Time
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ModuleActivity : AppCompatActivity() {

    private val modulesPath = FirestoreCollectionPath.MODULES_PATH
    private val lecturersPath = FirestoreCollectionPath.LECTURERS_PATH
    private val programmesPath = FirestoreCollectionPath.PROGRAMMES_PATH

    private val modules = ArrayList<Module>()
    private val lecturers = ArrayList<Lecturer>()
    private val programmes = ArrayList<Programme>()
//    private val lecturersString =ArrayList<String>()
    private var selectedLecturer:Lecturer? = null
//    private val programmesString =ArrayList<String>()
    private var selectedProgramme:Programme? = null

    @ServerTimestamp
    var Fdate = Date()
    var Ftime = Date()
    var fyear:Int = 0
    var fmonth:Int = 0
    var fday:Int = 0
    var fhour:Int = 0
    var fminute:Int = 0


    var fb = FirebaseFirestore.getInstance()
    lateinit var moduleAdapter: ModuleRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module)

        val linearLayoutManager = LinearLayoutManager(this)
        module_recyclerView.layoutManager = linearLayoutManager
        module_recyclerView.adapter = ModuleRecyclerViewAdaptor(modules)
        moduleAdapter = (module_recyclerView.adapter as ModuleRecyclerViewAdaptor)
        moduleAdapter.onItemClickListener =
            object : ModuleRecyclerViewAdaptor.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    Log.d("onClick", "$position")
                }

                override fun onLongClick(v: View, position: Int): Boolean {
                    Log.d("OnItemLongClickListener", "long pressed at $position")
                    presentDeleteAlert(modules[position])
                    return true
                }
            }

        module_insert.setOnClickListener {
            var id = module_id_et.text.toString().trim()
            var name = module_name_et.text.toString().trim()
            var numOfWeek = number_of_week_et.text.toString().toInt()
            Fdate = Date(DateUtil.toYearFrom(datePickerYear = fyear),fmonth,fday)
            Ftime= Date(0,0,0,fhour,fminute)
          //  var year = module_year_et.text.toString().trim()
           // var level = module_level_et.text.toString().trim()
           // var credit = module_credit_et.text.toString().trim()
            var lecturer = selectedLecturer
            var programme = selectedProgramme

            //if (id != "" && name != "" && year != "" && level != "" && credit != "" && lecturer != null) {
            if (id != "" && name != "" && lecturer != null && programme != null) {
                //var module = Module(id.toInt(),name, year.toInt(), level.toInt(), credit.toInt(),lecturer)
                var module =
                    Module(id,name,Timestamp(Fdate),lecturer.id.toString(),lecturer.name,
                        programme.id.toString(), programme.name,numOfWeek,Timestamp(Ftime))


                Log.d("Module", "$module")
                hideKeyboard()
                clearInput()
                writeModule(module)
            } else
                Toast.makeText(this, "Please fill all fields before insert", Toast.LENGTH_SHORT).show()
        }

        retrieveModules()
        retrieveLecturers()
        retrieveProgrammes()

        class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                // Use the current date as the default date in the picker
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)


                // Create a new instance of DatePickerDialog and return it
                return DatePickerDialog(this@ModuleActivity, this, year, month, day)
            }

            override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
                // Do something with the date chosen by the user
                fyear = year
                fmonth = month
                fday = day
                Log.d("Year", "$fyear")
                Log.d("Month", "$fmonth")
                Log.d("Day", "$fday")
                var selectedDate = findViewById<TextView>(R.id.selectedDate_tv)
                selectedDate.text = fday.toString() + "-" + (fmonth+1).toString() + "-" + fyear.toString()
            }
        }

        class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                // Use the current time as the default values for the picker
                val c = Calendar.getInstance()
                val hour = c.get(Calendar.HOUR_OF_DAY)
                val minute = c.get(Calendar.MINUTE)

                // Create a new instance of TimePickerDialog and return it
                return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
            }

            override fun onTimeSet(view: TimePicker, hour: Int, minute: Int) {
                // Do something with the time chosen by the user
                fhour = hour
                fminute = minute
                Log.d("Hour", "$fhour")
                Log.d("Minute", "$fminute")
                var selectedTime = findViewById<TextView>(R.id.selectedTime_tv)
                selectedTime.text = fhour.toString() + "-" + fminute.toString()
            }
        }

        startDate_button.setOnClickListener {
            DatePickerFragment()
                .show(supportFragmentManager, "datePicker")
        }

        startTime_button.setOnClickListener{
            TimePickerFragment()
                .show(supportFragmentManager, "timePicker")
        }

    }


    private fun retrieveModules() {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.retrieveData(modules as ArrayList<ModelBase>, Module::class.java) {
            moduleAdapter.notifyDataSetChanged()
        }
    }

    private fun retrieveLecturers() {
        val lecturersCollection = fb.collection(lecturersPath)
        lecturersCollection.retrieveData(lecturers as ArrayList<ModelBase>, Lecturer::class.java) {
            val lecturersString = lecturers.map { it.name } as ArrayList<String>
            val lectureSpinner: Spinner = findViewById(R.id.lecturerSpinner)
            val lecturerAdapter =  SpinnerUtil.setupSpinner(this,lectureSpinner,lecturersString){
                selectedLecturer = lecturers[it]
            }
            lecturerAdapter.notifyDataSetChanged()
        }
    }

    private fun retrieveProgrammes() {
        val programmesCollection = fb.collection(programmesPath)
        programmesCollection.retrieveData(programmes as ArrayList<ModelBase>, Programme::class.java) {
            val programmesString = programmes.map { it.name } as ArrayList<String>
            val programmeSpinner: Spinner = findViewById(R.id.programmeSpinner)
            val programmeAdapter = SpinnerUtil.setupSpinner(this,programmeSpinner,programmesString) {
                selectedProgramme = programmes[it]
            }
            programmeAdapter.notifyDataSetChanged()
        }
    }

    private fun writeModule(module: Module) {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.realTimeUpdate(modules as ArrayList<ModelBase>,Module::class.java) {
            moduleAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
        }
        moduleCollection.document(module.id.toString())
            .set(module)
            .addOnSuccessListener {
                Log.d("", "Module successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("", "Error writing document", e)
            }
    }

    override fun onStop() {
        super.onStop()
        clearInput()
        moduleAdapter.clear()
    }

    private fun clearInput() {
        module_id_et.text.clear()
        module_name_et.text.clear()
        //module_year_et.text.clear()
        //module_level_et.text.clear()
        //module_credit_et.text.clear()
    }

    private fun presentDeleteAlert(module: Module) {
        val dialog =  AlertDialog.Builder(this)
        dialog.setCancelable(true).setMessage("Are you sure to delete module ID : ${module.id}").setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            deleteModule(module)
        }).setNegativeButton("No", DialogInterface.OnClickListener{ dialog, _ ->
            dialog.cancel()
        }).show()
    }

    private fun deleteModule(module: Module) {
        val moduleCollection = fb.collection(modulesPath)
        moduleCollection.realTimeUpdate(modules as ArrayList<ModelBase>,Module::class.java) {
            moduleAdapter.notifyDataSetChanged()
            Toast.makeText(this, "delete successful", Toast.LENGTH_SHORT).show()
        }
        moduleCollection.document(module.id.toString()).delete()
            .addOnSuccessListener {
                Log.d("", "Module successfully deleted! ")
            }
            .addOnFailureListener {e ->
                Log.w("", "Error deleting document",e )
            }
    }

}