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
import com.example.firestoreinsetprototype.Adaptor.StudentAdaptor
import com.example.firestoreinsetprototype.Adaptor.ProgrammeRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Programme
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_programme.*
import kotlinx.android.synthetic.main.activity_student.*
import kotlinx.android.synthetic.main.activity_student.student_recyclerView
import java.security.cert.Extension

class ProgrammeActivity : AppCompatActivity() {
    private val programmes = ArrayList<Programme>()
    val fb = FirebaseFirestore.getInstance()
    lateinit var programmeAdapter : ProgrammeRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_programme)

        val linearLayoutManager = LinearLayoutManager(this)
        programme_recyclerView.layoutManager = linearLayoutManager
        programme_recyclerView.adapter = ProgrammeRecyclerViewAdaptor(programmes)
        programmeAdapter = (programme_recyclerView.adapter as ProgrammeRecyclerViewAdaptor)
        programmeAdapter.onItemClickListener = object : ProgrammeRecyclerViewAdaptor.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("onClick", "$position")
            }

            override fun onLongClick(v: View, position: Int) : Boolean {
                Log.d("OnItemLongClickListener", "long pressed at $position")
                presentDeleteAlert(programmes[position])
                return true
            }
        }
//        student_list_view.setOnItemLongClickListener {  parent, view, position, id ->
//            Log.d("OnItemLongClickListener", "long pressed at $position and id $id and the view $view")
//            presentDeleteAlert(students[position])
//            return@setOnItemLongClickListener(true)
//        }

        programme_insert.setOnClickListener {
            var id = programme_id_et.text.toString().trim()
            var name = programme_name_et.text.toString().trim()

            if (id != "" && name != "" ) {
                var programme = Programme(id, name)
                Log.d("Programme", "$programme")
                hideKeyboard()
                clearInputs()
                writeProgramme(programme)
                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT)
                    .show()
            } else
                Toast.makeText(
                    this,
                    "Please fill all fields before insert",
                    Toast.LENGTH_SHORT
                ).show()
        }

        val programmeRef = fb.collection("programmes")
        programmeRef.get()
            .addOnSuccessListener { result->
                for(document in result){
                    Log.d("Programme", "${document.id} => ${document.data}")
                    var programme = document.toObject(Programme::class.java)
                    Log.d("Programme","$programme")
                    programmes.add(programme)
                }
                Log.d("load Programme", "$programmes")
                programmeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("", "Error getting documents: ", exception)
            }

    }

    override fun onStop() {
        super.onStop()
        clearInputs()
        programmeAdapter.clear()
    }

    private fun clearInputs(){
        programme_id_et.text.clear()
        programme_name_et.text.clear()
    }

    private fun writeProgramme(programme: Programme) {
        val programmeCollection = fb.collection("programmes")
        realTimeUpdate(programmeCollection)
        programmeCollection.document(programme.id.toString())
            .set(programme)
            .addOnSuccessListener {
                Log.d("", "Programme successfully written!")
            }
            .addOnFailureListener {e->
                Log.w("", "Error writing document", e)
            }
    }


    private fun presentDeleteAlert(programme: Programme){
        val dialog =  AlertDialog.Builder(this)
        dialog.setCancelable(true).setMessage("Are you sure to delete Programme ID : ${programme.id}").setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            deleteProgramme(programme)
        }).setNegativeButton("No", DialogInterface.OnClickListener{ dialog , _ ->
            dialog.cancel()
        }).show()
    }

    private fun deleteProgramme(programme: Programme) {
        val programmeCollection = fb.collection("programme")
        realTimeUpdate(programmeCollection)
        programmeCollection.document(programme.id.toString()).delete().addOnSuccessListener {
            Log.d("", "Programme successfully deleted!")
        }
            .addOnFailureListener {e->
                Log.w("", "Error deleting document", e)
            }
    }


    private fun realTimeUpdate(programmeRef:CollectionReference){
        programmeRef.addSnapshotListener { snapshot, e ->
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
                    val doc = dc.document.toObject(Programme::class.java)
                    Log.d("dc.type", dc.type.toString())
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if(hasProgramme(programmes,doc) == null) {
                                programmes.add(doc)
                                Log.d("adding programme", doc.toString())
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            hasProgramme(programmes,doc).let { programme ->
                                val index = programmes.indexOf(programme)
                                programmes[index] = doc
                                Log.d("modify programme", doc.toString())
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            hasProgramme(programmes,doc).let { programme ->
                                programmes.remove(programme)
                                Log.d("removing programme", doc.toString())
                            }
                        }
                    }
                }
                Log.d("RealTimeUpdate", "$programmes")
                programmeAdapter.notifyDataSetChanged()
            } else {
                android.util.Log.d("null", "$source data: null")
            }
        }

    }

    /* ArrayList<Student> -> listview dataset
       Student -> from firebase
    */
    private fun hasProgramme(arr : ArrayList<Programme> , programme: Programme): Programme? {
        for(index in 0 until arr.size) {
            if(programme.id == arr[index].id) {
                return arr[index]
            }
        }
        return null
    }
}