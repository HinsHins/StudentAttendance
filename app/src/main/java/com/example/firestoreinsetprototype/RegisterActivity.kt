package com.example.firestoreinsetprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        submit_button.setOnClickListener {
            val email = register_email_ed.text.toString().trim()
            val password = register_password_ed.text.toString().trim()
            val confirmPassword = register_confirm_password_ed.text.toString().trim()

            if(email=="")
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            else if(password=="")
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            else if(password!=confirmPassword)
                Toast.makeText(this, "Password does not match ,please try again", Toast.LENGTH_SHORT).show()
            else{
                registerAccount(email,password)
            }

        }
    }

    fun registerAccount(email:String,password:String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Success", "createUserWithEmail:success")
                    val user = auth.currentUser
                    val i = Intent(this,MainActivity::class.java)
                    startActivity(i)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Fail", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }

                // ...
            }
    }
}