package fi.oamk.chat

import com.google.firebase.auth.FirebaseUser
import java.util.*

data class Message(val message: String, val author: FirebaseUser, val time: Date) {
}