package com.avalitov.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avalitov.happyplaces.R
import com.avalitov.happyplaces.adapters.HappyPlacesAdapter
import com.avalitov.happyplaces.database.DatabaseHandler
import com.avalitov.happyplaces.models.HappyPlaceModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

lateinit var rvHappyPlacesList : RecyclerView
lateinit var tvNoPlacesFound : TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fabHappyPlace : FloatingActionButton = findViewById(R.id.fab_happy_place)
        rvHappyPlacesList = findViewById(R.id.rv_happy_places_list)
        tvNoPlacesFound = findViewById(R.id.tv_no_places_found)

        fabHappyPlace.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getHappyPlacesListFromLocalDB()
    }


    private fun setupHappyPlacesRecyclerView(happyPlacesList: ArrayList<HappyPlaceModel>){
        rvHappyPlacesList.layoutManager = LinearLayoutManager(this)
        rvHappyPlacesList.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
        rvHappyPlacesList.adapter = placesAdapter
    }


    private fun getHappyPlacesListFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val happyPlacesList : ArrayList<HappyPlaceModel> = dbHandler.getAllPlacesList()

        if(happyPlacesList.isNotEmpty()) {
                rvHappyPlacesList.visibility = View.VISIBLE
                tvNoPlacesFound.visibility = View.GONE
                setupHappyPlacesRecyclerView(happyPlacesList)
        } else {
            rvHappyPlacesList.visibility = View.GONE
            tvNoPlacesFound.visibility = View.VISIBLE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                getHappyPlacesListFromLocalDB()
            } else {
                Log.e("Activity", "Cancelled or Back pressed")
            }
        }
    }


    companion object {
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    }
}