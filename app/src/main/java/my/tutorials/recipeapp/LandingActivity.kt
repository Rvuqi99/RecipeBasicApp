package my.tutorials.recipeapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class LandingActivity : AppCompatActivity(){

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        sharedPreferences = getSharedPreferences("loggedIn", Context.MODE_PRIVATE)

        val loggedIn = sharedPreferences.getString("loggedIn", null)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if(loggedIn == "1"){
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
            } else {
                val i = Intent(this, LoginActivity::class.java)
                startActivity(i)
            }
        }, 3000)
    }
}