package fi.oamk.chat

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.name
    private lateinit var messages: ArrayList<String>;
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private  var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Firebase.database.reference
        auth = Firebase.auth

        messages = arrayListOf<String>()

        messageText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                addMessage()
                return@OnKeyListener true
            }
            false
        })

        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    val messagesFromFirebase =
                        (snapshot.value as HashMap<Int, ArrayList<String>>).get("messages")
                    messages.clear()
                    messagesFromFirebase?.forEach {
                        if (it != null) messages.add(it)
                    }
                    messageList.adapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Chat", error.toString())
            }
        }
        database.addValueEventListener(messageListener)

        messageList.layoutManager = LinearLayoutManager(this)
        messageList.adapter = MyAdapter(messages)

    }

    override fun onStart() {
        super.onStart()

        loginDialog()
    }

    fun loginDialog() {
        val builder = AlertDialog.Builder(this)

        with(builder) {
            setTitle("Login")
            val linearLayout: LinearLayout = LinearLayout(this@MainActivity)
            linearLayout.orientation = LinearLayout.VERTICAL

            val inputEmail: EditText = EditText(this@MainActivity)
            inputEmail.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            inputEmail.hint = "Enter email"
            linearLayout.addView(inputEmail)

            val inputPw: EditText = EditText(this@MainActivity)
            inputPw.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_PASSWORD
            inputPw.hint = "Enter password"
            linearLayout.addView(inputPw)
            builder.setView(linearLayout)

            builder.setPositiveButton("OK") { dialog, which ->
                login(inputEmail.text.toString(), inputPw.text.toString())

            }.show()
        }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    currentUser= auth.currentUser

                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }
            }
    }


    fun addUserDialog() {
        val builder = AlertDialog.Builder(this)

        with(builder) {
            setTitle("Register")
            val linearLayout: LinearLayout = LinearLayout(this@MainActivity)
            linearLayout.orientation = LinearLayout.VERTICAL

            val inputEmail: EditText = EditText(this@MainActivity)
            inputEmail.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            inputEmail.hint = "Enter email"
            linearLayout.addView(inputEmail)
            //builder.setView(inputEmail)
            val inputPw: EditText = EditText(this@MainActivity)
            inputPw.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_PASSWORD
            inputPw.hint = "Enter password"
            linearLayout.addView(inputPw)
            builder.setView(linearLayout)

            builder.setPositiveButton("OK") { dialog, which ->
                createUser(inputEmail.text.toString(), inputPw.text.toString())

            }.show()
        }
    }

    fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    currentUser = auth.currentUser
                    Toast.makeText(
                        baseContext, "Registration completed.",
                        Toast.LENGTH_SHORT
                    ).show();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
    }


    fun addMessage() {
        val newMessage = messageText.text.toString()
        messages.add(newMessage)

        //val key = database.child("messages").push().key.toString()
        //database.child("messages").child(key).setValue(it)

        database.child("messages").setValue(messages)
        messageText.setText("")

        closeKeyBoard()

    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}

