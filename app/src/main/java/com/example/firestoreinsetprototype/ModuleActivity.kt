package com.example.firestoreinsetprototype

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.ModuleRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Lecturer
import com.example.firestoreinsetprototype.Model.Module
import com.example.firestoreinsetprototype.Model.Programme
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.synthetic.main.activity_module.*
import java.util.*
import kotlin.collections.ArrayList

class ModuleActivity : AppCompatActivity() {

    private val modules = ArrayList<Module>()

    private val lecturers = ArrayList<Lecturer>()
    private val lecturersString =ArrayList<String>()
    private var selectedLecturer:Lecturer? = null

    private val programmes = ArrayList<Programme>()
    private val programmesString =ArrayList<String>()
    private var selectedProgramme:Programme? = null

    @ServerTimestamp
    var Fdate = Date()
    var fyear:Int = 0
    var fmonth:Int = 0
    var fday:Int = 0


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
            Fdate = Date(fyear-1900,fmonth,fday)
          //  var year = module_year_et.text.toString().trim()
           // var level = module_level_et.text.toString().trim()
           // var credit = module_credit_et.text.toString().trim()
            var lecturer = selectedLecturer
            var programme = selectedProgramme

            //if (id != "" && name != "" && year != "" && level != "" && credit != "" && lecturer != null) {
            if (id != "" && name != "" && lecturer != null && programme != null) {
                //var module = Module(id.toInt(),name, year.toInt(), level.toInt(), credit.toInt(),lecturer)
                var module = Module(id,name,Timestamp(Fdate),lecturer.id.toString(),lecturer.name,programme.id.toString(),programme.name)
                Log.d("Module", "$module")
                hideKeyboard()
                clearInput()
                writeModule(module)
                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(this, "Please fill all fields before insert", Toast.LENGTH_SHORT).show()
        }
        val moduleRef = fb.collection("modules")
        moduleRef.get()
            .addOnSuccessListener { result ->
                for(document in result){
                    Log.d("Module", "${document.id} => ${document.data}")
                    var module = document.toObject(Module::class.java)
                    Log.d("Module", "$modules")
                    modules.add(module)
                }
                Log.d("load Module", "$modules")
                moduleAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("", "Error getting document",exception)
            }

        val lecture_spinner: Spinner = findViewById(R.id.lecturerSpinner)
        val arrayadapter_lecturer = ArrayAdapter(this,android.R.layout.simple_spinner_item,lecturersString)
        arrayadapter_lecturer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            lecture_spinner.adapter = arrayadapter_lecturer
        lecture_spinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedLecturer = lecturers[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        val programme_spinner: Spinner = findViewById(R.id.programmeSpinner)
        val arrayadapter_programme = ArrayAdapter(this,android.R.layout.simple_spinner_item,programmesString)
        arrayadapter_programme.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        programme_spinner.adapter = arrayadapter_programme
        programme_spinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedProgramme = programmes[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }



        val lecturerRef = fb.collection("lecturers")
        lecturerRef.get()
            .addOnSuccessListener { result->
                for (document in result){
                    Log.d("Lecturer", "${document.id} => ${document.data}")
                    var lecturer = document.toObject(Lecturer::class.java)
                    Log.d("Lecturer object", "$lecturers")
                    lecturers.add(lecturer)
                    lecturersString.add(lecturer.name)
                }
                Log.d("load Lecturer", "$lecturersString")
                arrayadapter_lecturer.notifyDataSetChanged()

            }

        val programmeRef = fb.collection("programmes")
        programmeRef.get()
            .addOnSuccessListener { result->
                for (document in result){
                    Log.d("Programme", "${document.id} => ${document.data}")
                    var programme = document.toObject(Programme::class.java)
                    Log.d("Programme object", "$programmes")
                    programmes.add(programme)
                    programmesString.add(programme.name)
                }
                Log.d("load Programme", "$programmesString")
                arrayadapter_programme.notifyDataSetChanged()

            }

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

        startDate_button.setOnClickListener {
            DatePickerFragment()
                .show(supportFragmentManager, "datePicker")
        }

    }

    private fun writeModule(module: Module) {
        val moduleCollection = fb.collection("modules")
        realTimeUpdate(moduleCollection)
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
        val moduleCollection = fb.collection("modules")
        realTimeUpdate(moduleCollection)
        moduleCollection.document(module.id.toString()).delete()
            .addOnSuccessListener {
                Log.d("", "Module successfully deleted! ")
            }
            .addOnFailureListener {e ->
                Log.w("", "Error deleting document",e )
            }
    }

    private fun realTimeUpdate(moduleRef: CollectionReference) {
        moduleRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Fail", "Listen failed.", e)
                return@addSnapshotListener
            }

            val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
                "Local"
            else
                "Server"
            Log.d("snapshot", snapshot.toString())
            if (snapshot != null) {
                for (dc in snapshot.documentChanges) {
                    val doc = dc.document.toObject(Module::class.java)
                    Log.d("dc.type", dc.type.toString())
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if(hasModule(modules,doc) == null) {
                                modules.add(doc)
                                Log.d("adding module", doc.toString())
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            hasModule(modules,doc).let { lecturer ->
                                val index = modules.indexOf(lecturer)
                                modules[index] = doc
                                Log.d("modify module", doc.toString())
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            hasModule(modules,doc).let { lecturer ->
                                modules.remove(lecturer)
                                Log.d("removing module", doc.toString())
                            }
                        }
                    }
                }
                Log.d("RealTimeUpdate", "$modules")
                moduleAdapter.notifyDataSetChanged()
            } else {
                android.util.Log.d("null", "$source data: null")
            }
        }
    }

    private fun hasModule(arr: ArrayList<Module>, module: Module): Module? {
        for(index in 0 until arr.size){
            if(module.id == arr[index].id)
                return arr[index]
        }
        return null

    }


}