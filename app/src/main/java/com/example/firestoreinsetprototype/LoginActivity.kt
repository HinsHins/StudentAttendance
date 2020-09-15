package com.example.firestoreinsetprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        login_button.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            val email = email_login_ed.text.toString().trim()
            val password = _password_login_ed.text.toString().trim()
            if(email=="")
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            else if (password == "")
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            else{
                signIn(email,password)
            }
        }

        register_button.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }



    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        Log.d("user","$user")
    }

    fun signIn(email:String,password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Success", "signInWithEmail:success")
                    val user = auth.currentUser
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Fail", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    // ...
                }

                // ...
            }
    }
}