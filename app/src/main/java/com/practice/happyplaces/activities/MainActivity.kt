package com.practice.happyplaces.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.practice.happyplaces.database.DatabaseHandler
import com.practice.happyplaces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding : ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent  = Intent(this@MainActivity, AddHappyPlaceActivity :: class.java)
            startActivity(intent)
        }
        getHappyPlacesListFromLocalDB()
    }

    private fun getHappyPlacesListFromLocalDB(){

        val dbHandler = DatabaseHandler(this)
        val getHappyPlacesList = dbHandler.getHappyPlacesList()

        if (getHappyPlacesList.size>0){

            for (i in getHappyPlacesList){
                Log.e("Title", i.title)
                Log.e("Description", i.description)
                Log.e("Date", i.date)
            }
        }
    }
}