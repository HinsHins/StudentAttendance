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
import com.example.firestoreinsetprototype.Adaptor.StudentRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Student
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_student.*
import java.security.cert.Extension

class StudentActivity : AppCompatActivity() {
    private val students = ArrayList<Student>()
    val fb = FirebaseFirestore.getInstance()
    lateinit var studentAdapter : StudentRecyclerViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        val linearLayoutManager = LinearLayoutManager(this)
        student_recyclerView.layoutManager = linearLayoutManager
        student_recyclerView.adapter = StudentRecyclerViewAdaptor(students)
        studentAdapter = (student_recyclerView.adapter as StudentRecyclerViewAdaptor)
        studentAdapter.onItemClickListener = object : StudentRecyclerViewAdaptor.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("onClick", "$position")
            }

            override fun onLongClick(v: View, position: Int) : Boolean {
                Log.d("OnItemLongClickListener", "long pressed at $position")
                presentDeleteAlert(students[position])
                return true
            }
        }
//        student_list_view.setOnItemLongClickListener {  parent, view, position, id ->
//            Log.d("OnItemLongClickListener", "long pressed at $position and id $id and the view $view")
//            presentDeleteAlert(students[position])
//            return@setOnItemLongClickListener(true)
//        }

        student_insert.setOnClickListener {
            var id = sid_et.text.toString().trim()
            var name = sname_et.text.toString().trim()
            var email = semail_et.text.toString().trim()
            var programme = sprogramme_et.text.toString().trim()
            var country = scountry_et.text.toString().trim()

            if (id != "" && name != "" && email != "" && programme != "" && country != "") {
                var student = Student(id.toInt(), name, email, programme, country)
                Log.d("Student", "$student")
                hideKeyboard()
                clearInputs()
                writeStudent(student)
                Toast.makeText(this@StudentActivity, "Insert successful", Toast.LENGTH_SHORT)
                    .show()
            } else
                Toast.makeText(
                    this@StudentActivity,
                    "Please fill all fields before insert",
                    Toast.LENGTH_SHORT
                ).show()
        }

        val studentRef = fb.collection("students")
        studentRef.get()
            .addOnSuccessListener { result->
                for(document in result){
                    Log.d("Student", "${document.id} => ${document.data}")
                    var student = document.toObject(Student::class.java)
                    Log.d("Student","$student")
                    students.add(student)
                }
                Log.d("load Student", "$students")
                studentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("", "Error getting documents: ", exception)
            }

    }

    override fun onStop() {
        super.onStop()
        clearInputs()
        studentAdapter.clear()
    }

    private fun clearInputs(){
        sid_et.text.clear()
        sname_et.text.clear()
        semail_et.text.clear()
        sprogramme_et.text.clear()
        scountry_et.text.clear()
    }

    private fun writeStudent(student: Student) {
        val studentCollection = fb.collection("students")
        realTimeUpdate(studentCollection)
        studentCollection.document(student.id.toString())
            .set(student)
            .addOnSuccessListener {
                Log.d("", "Student successfully written!")
            }
            .addOnFailureListener {e->
                Log.w("", "Error writing document", e)
            }
    }


    private fun presentDeleteAlert(student: Student){
        val dialog =  AlertDialog.Builder(this)
        dialog.setCancelable(true).setMessage("Are you sure to delete Student ID : ${student.id}").setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
            deleteStudent(student)
        }).setNegativeButton("No", DialogInterface.OnClickListener{ dialog , _ ->
            dialog.cancel()
        }).show()
    }

    private fun deleteStudent(student: Student) {
        val studentCollection = fb.collection("students")
        realTimeUpdate(studentCollection)
        studentCollection.document(student.id.toString()).delete().addOnSuccessListener {
            Log.d("", "Student successfully deleted!")
        }
            .addOnFailureListener {e->
                Log.w("", "Error deleting document", e)
            }
    }


    private fun realTimeUpdate(studentRef:CollectionReference){
        studentRef.addSnapshotListener { snapshot, e ->
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
                    val doc = dc.document.toObject(Student::class.java)
                    Log.d("dc.type", dc.type.toString())
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if(hasStudent(students,doc) == null) {
                                students.add(doc)
                                Log.d("adding student", doc.toString())
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            hasStudent(students,doc).let { student ->
                                val index = students.indexOf(student)
                                students[index] = doc
                                Log.d("modify student", doc.toString())
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            hasStudent(students,doc).let { student ->
                                students.remove(student)
                                Log.d("removing student", doc.toString())
                            }
                        }
                    }
                }
                Log.d("RealTimeUpdate", "$students")
                studentAdapter.notifyDataSetChanged()
            } else {
                android.util.Log.d("null", "$source data: null")
            }
        }

    }

    /* ArrayList<Student> -> listview dataset
       Student -> from firebase
    */
    private fun hasStudent(arr : ArrayList<Student> , student: Student): Student? {
        for(index in 0 until arr.size) {
            if(student.id == arr[index].id) {
                return arr[index]
            }
        }
        return null
    }
}