package fi.oamk.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.name
    //private lateinit var messages: ArrayList<String>;
    private lateinit var messages: ArrayList<Message>;
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private  var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Firebase.database.reference
        auth = Firebase.auth
        //currentUser = auth.currentUser

        //messages = arrayListOf<String>()
        messages = arrayListOf<Message>()

        messageText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                addMessage()
                return@OnKeyListener true
            }
            false
        })

        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    //val messagesFromFirebase =
                    //    (snapshot.value as HashMap<Int, ArrayList<String>>).get("messages")

                    val messagesFromFirebase =
                        (snapshot.value as HashMap<Int, ArrayList<Message>>).get("messages")


                    messages.clear()

                    if (messagesFromFirebase != null) {
                        for (i in 0..messagesFromFirebase.size-1) {
                            //val hashMap = (messagesFromFirebase as ArrayList).get(i) as HashMap<Int, String>
                            if (messagesFromFirebase.get(i) != null) {
                                val hashMap: HashMap<Int, String> =
                                     messagesFromFirebase.get(i) as HashMap<Int, String>

                                val author = hashMap["author"]
                                val text = hashMap["message"]
                                val time = hashMap["time"]
                                val message = Message(text!!, author, time!!)
                                messages.add(message)

                            }
                        }
                    }


                    //val media: MediaPlayer? = MediaPlayer.create(ToneGenerator.)


                    //media!!.start()

                    //}

                    /*
                    messagesFromFirebase?.forEach {
                        if (it != null)  {

                        }
                            //messages.add(it)
                    }*/
                    messageList.adapter?.notifyDataSetChanged()
                    messageList.smoothScrollToPosition(messageList.adapter!!.itemCount- 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, error.toString())
            }
        }
        database.addValueEventListener(messageListener)

        messageList.layoutManager = LinearLayoutManager(this)
        messageList.adapter = MyAdapter(messages)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.settings -> {
            this.showSettings()
            true
        } else  -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        currentUser = auth.currentUser
        if (currentUser == null) loginDialog()
    }


    fun showSettings() {
        //val editText = findViewById<EditText>(R.id.editText)
        //val message = editText.text.toString()
        //val intent = Intent(this, DisplayMessageActivity::class.java).apply {
        //    putExtra(EXTRA_MESSAGE, message)
        //}
        val intent = Intent(this, Settings::class.java).apply {

            putExtra("currentUser",currentUser)

        }
        startActivity(intent)
    }
    /*
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("currentUser",currentUser)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentUser = savedInstanceState.getParcelable("currentUser")
    }
    */

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

            builder.setPositiveButton("OK") { _, _ ->
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

            builder.setPositiveButton("OK") { _, _ ->
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
        //val newMessage = messageText.text.toString()
        val formatter: DateTimeFormatter  = DateTimeFormatter . ofPattern ("dd.MM.yyyy HH:mm")
        val newMessage: Message = Message(messageText.text.toString(),
            currentUser?.email.toString(),
            formatter.format(LocalDateTime.now()))
        messages.add(newMessage)

        //val key = database.child("messages").push().key.toString()
        //database.child("messages").child(key).setValue(it)

        database.child("messages").setValue(messages)
        messageText.setText("")

        closeKeyBoard()
        messageList.smoothScrollToPosition(messageList.adapter!!.itemCount- 1);
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


}

