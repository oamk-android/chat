package fi.oamk.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() {

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //val upArrow: Drawable = resources.getDrawable(R.drawable.ic_arrow_back_black_24dp,null)

        supportActionBar?.apply {
            title="Settings"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            ///setHomeAsUpIndicator(upArrow)
        }

        val currentUser = intent.getParcelableExtra<FirebaseUser>("currentUser")
        email.text = currentUser.email
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun signOut(view: View) {
        FirebaseAuth.getInstance().signOut()
        email.text = ""
    }
}
