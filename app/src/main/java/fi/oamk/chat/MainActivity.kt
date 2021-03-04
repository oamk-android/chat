package fi.oamk.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var messages: ArrayList<String>;
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Firebase.database.reference


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
