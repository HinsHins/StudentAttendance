package com.example.firestoreinsetprototype

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Adapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.ModuleRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Extension.hideKeyboard
import com.example.firestoreinsetprototype.Model.Lecturer
import com.example.firestoreinsetprototype.Model.Module
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_module.*

class ModuleActivity : AppCompatActivity() {

    private val modules = ArrayList<Module>()
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
            var year = module_year_et.text.toString().trim()
            var level = module_level_et.text.toString().trim()
            var credit = module_credit_et.text.toString().trim()

            if (id != "" && name != "" && year != "" && level != "" && credit != "") {
                var module = Module(id.toInt(), name, year.toInt(), level.toInt(), credit.toInt())
                Log.d("Module", "$module")
                hideKeyboard()
                clearInput()
                writeModule(module)
                Toast.makeText(this, "Insert successful", Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(this, "Please fill all fields before insert", Toast.LENGTH_SHORT).show()
        }

        val ModuleRef = fb.collection("modules")
        ModuleRef.get()
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
        module_year_et.text.clear()
        module_level_et.text.clear()
        module_credit_et.text.clear()
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