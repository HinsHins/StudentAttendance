package com.example.firestoreinsetprototype

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.LecturerRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath.USER_PATH
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Lecturer
import com.example.firestoreinsetprototype.Model.ModelBase
import com.example.firestoreinsetprototype.Model.Programme
import com.example.firestoreinsetprototype.Model.User
import com.example.firestoreinsetprototype.Util.SpinnerUtil
import com.example.firestoreinsetprototype.Util.realTimeUpdate
import com.example.firestoreinsetprototype.Util.retrieveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_lecturer.*

class LecturerActivity : AppCompatActivity() {

    private val lecturersPath = FirestoreCollectionPath.LECTURERS_PATH
    private val lecturers = ArrayList<Lecturer>()
    private val users = ArrayList<User>()
    private var selectedUser:User? = null


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
            var email = selectedUser?.email

            if (id != "" && name != "" && email != null && position != "" && department != "") {
                var lecturer = Lecturer(id, name,email,position, department)
                Log.d("Lecturer", "$lecturer")
                hideKeyboard()
                clearInputs()
                writeLecturer(lecturer)
                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
            }else
                Toast.makeText(this, "Please fill all fields before insert", Toast.LENGTH_SHORT).show()
        }
        retrieveLecturers()
        retrieveUsers()
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
        val lecturerCollection = fb.collection(lecturersPath)
        lecturerCollection.realTimeUpdate(lecturers as ArrayList<ModelBase>, Lecturer::class.java) {
            lecturerAdapter.notifyDataSetChanged()
        }
        lecturerCollection.document(lecturer.id.toString()).delete().addOnSuccessListener {
            Log.d("", "Lecturer successfully deleted! ")
        }
            .addOnFailureListener { e->
                Log.w("", "Error deleting document",e )
            }
    }

    private fun retrieveLecturers() {
        val lecturersCollection = fb.collection(lecturersPath)
        lecturersCollection.retrieveData(lecturers as ArrayList<ModelBase>, Lecturer::class.java) {
            lecturerAdapter.notifyDataSetChanged()
        }
    }


    private fun writeLecturer(lecturer: Lecturer) {
        val lecturerCollection = fb.collection(lecturersPath)
        lecturerCollection.realTimeUpdate(lecturers as ArrayList<ModelBase>, Lecturer::class.java) {
            lecturerAdapter.notifyDataSetChanged()
        }
        lecturerCollection.document(lecturer.id.toString())
            .set(lecturer)
            .addOnSuccessListener {
                Log.d("", "Lecturer successfully written!")
            }
            .addOnFailureListener { e->
                Log.w("", "Error writing document",e )
            }
    }

    private fun retrieveUsers(){
        val userCollection = fb.collection(USER_PATH)
        userCollection.retrieveData(users as ArrayList<ModelBase>, User::class.java) {
            val usersString = users.map { it.email } as ArrayList<String>
            val emailSpinner: Spinner = findViewById(R.id.email_Spinner)
            val userAdapter =  SpinnerUtil.setupSpinner(
                this,
                emailSpinner,
                usersString
            ){
                selectedUser = users[it]
            }
            userAdapter.notifyDataSetChanged()
        }
    }

    private fun clearInputs() {
        lecturer_id_et.text.clear()
        lecturer_name_et.text.clear()
        position_et.text.clear()
        lecturer_department_et.text.clear()
    }


}
