package com.example.firestoreinsetprototype

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.LecturerRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Lecturer
import com.example.firestoreinsetprototype.Model.Student
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_lecturer.*

class LecturerActivity : AppCompatActivity() {
    private val lecturers = ArrayList<Lecturer>()
    var fb = FirebaseFirestore.getInstance()
    lateinit var lecturerAdapter: LecturerRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lecturer)

        val linearLayoutManager = LinearLayoutManager(this)
        lecturer_recyclerview.layoutManager = linearLayoutManager
        lecturer_recyclerview.adapter = LecturerRecyclerViewAdaptor(lecturers)
        lecturerAdapter = (lecturer_recyclerview.adapter as LecturerRecyclerViewAdaptor)
        lecturerAdapter.onItemClickListener =
            object : LecturerRecyclerViewAdaptor.OnItemClickListener {
                override fun onClick(v: View, position: Int) {
                    Log.d("onClick", "$position")
                }

                override fun onLongClick(v: View, position: Int): Boolean {
                    Log.d("OnItemLongClickListener", "long pressed at $position")
                    presentDeleteAlert(lecturers[position])
                    return true
                }
            }

        lecturer_insert.setOnClickListener {
            var id = lecturer_id_et.text.toString().trim()
            var name = lecturer_name_et.text.toString().trim()
            var position = position_et.text.toString().trim()
            var department = lecturer_department_et.text.toString().trim()

            if (id != "" && name != "" && position != "" && department != "") {
                var lecturer = Lecturer(id, name, position, department)
                Log.d("Lecturer", "$lecturer")
                hideKeyboard()
                clearInputs()
                writeLecturer(lecturer)
                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
            }else
                Toast.makeText(this, "Please fill all fields before insert", Toast.LENGTH_SHORT).show()
        }

        val lecturerRef = fb.collection("lecturers")
        lecturerRef.get()
            .addOnSuccessListener { result ->
                for(document in result){
                    Log.d("Lecturer", "${document.id} => ${document.data}")
                    var lecturer = document.toObject(Lecturer::class.java)
                    Log.d("Lecturer", "$lecturer")
                    lecturers.add(lecturer)
                }
                Log.d("load Lecturer", "$lecturers")
                lecturerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("", "Error getting document",exception)
            }
    }

    override fun onStop() {
        super.onStop()
        clearInputs()
        lecturerAdapter.clear()
    }

    private fun presentDeleteAlert(lecturer: Lecturer) {
        val dialog =  AlertDialog.Builder(this)
        dialog.setCancelable(true).setMessage("Are you sure to delete Lecturer ID : ${lecturer.id}").setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            deleteLecturer(lecturer)
        }).setNegativeButton("No", DialogInterface.OnClickListener{ dialog, _ ->
            dialog.cancel()
        }).show()
    }

    private fun deleteLecturer(lecturer: Lecturer) {
        val lecturerCollection = fb.collection("lecturers")
        realTimeUpdate(lecturerCollection)
        lecturerCollection.document(lecturer.id.toString()).delete().addOnSuccessListener {
            Log.d("", "Lecturer successfully deleted! ")
        }
            .addOnFailureListener { e->
                Log.w("", "Error deleting document",e )
            }
    }

    private fun writeLecturer(lecturer: Lecturer) {
        val lecturerCollection = fb.collection("lecturers")
        realTimeUpdate(lecturerCollection)
        lecturerCollection.document(lecturer.id.toString())
            .set(lecturer)
            .addOnSuccessListener {
                Log.d("", "Lecturer successfully written!")
            }
            .addOnFailureListener { e->
                Log.w("", "Error writing document",e )
            }
    }

    private fun realTimeUpdate(lecturerRef: CollectionReference) {
        lecturerRef.addSnapshotListener { snapshot, e ->
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
                    val doc = dc.document.toObject(Lecturer::class.java)
                    Log.d("dc.type", dc.type.toString())
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if(hasLecturer(lecturers,doc) == null) {
                                lecturers.add(doc)
                                Log.d("adding lecturer", doc.toString())
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            hasLecturer(lecturers,doc).let { lecturer ->
                                val index = lecturers.indexOf(lecturer)
                                lecturers[index] = doc
                                Log.d("modify lecturer", doc.toString())
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            hasLecturer(lecturers,doc).let { lecturer ->
                                lecturers.remove(lecturer)
                                Log.d("removing lecturer", doc.toString())
                            }
                        }
                    }
                }
                Log.d("RealTimeUpdate", "$lecturers")
                lecturerAdapter.notifyDataSetChanged()
            } else {
                android.util.Log.d("null", "$source data: null")
            }
        }

    }

    private fun hasLecturer(arr: ArrayList<Lecturer>, lecturer: Lecturer): Lecturer? {
        for(index in 0 until arr.size){
            if(lecturer.id == arr[index].id)
                return arr[index]
        }
        return null
    }

    private fun clearInputs() {
        lecturer_id_et.text.clear()
        lecturer_name_et.text.clear()
        position_et.text.clear()
        lecturer_department_et.text.clear()
    }
}
