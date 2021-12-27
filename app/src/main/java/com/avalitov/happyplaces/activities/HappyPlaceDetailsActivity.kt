package com.avalitov.happyplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.avalitov.happyplaces.R
import com.avalitov.happyplaces.models.HappyPlaceModel

lateinit var tbHappyPlaceDetails : Toolbar
private lateinit var ivPlaceImage : ImageView
lateinit var tvDescription : TextView
lateinit var tvLocation : TextView

class HappyPlaceDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_details)

        tbHappyPlaceDetails = findViewById(R.id.tb_happy_place_details)
        ivPlaceImage = findViewById(R.id.iv_details_image)
        tvDescription = findViewById(R.id.tv_details_description)
        tvLocation = findViewById(R.id.tv_details_location)

        var happyPlaceDetailModel : HappyPlaceModel? = null

        /**
         * First, we get the object (HappyPlaceModel) from intent's serialized extra
         */
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            happyPlaceDetailModel =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                        as HappyPlaceModel
        }

        /**
         * And if we got the model, we set the Activity fields
         * accordingly to model's data
         */
        if(happyPlaceDetailModel != null) {
            setSupportActionBar(tbHappyPlaceDetails)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = happyPlaceDetailModel.title

            tbHappyPlaceDetails.setNavigationOnClickListener {
                onBackPressed()
            }

            ivPlaceImage.setImageURI(Uri.parse(happyPlaceDetailModel.imagePath))
            tvDescription.text = happyPlaceDetailModel.description
            tvLocation.text = happyPlaceDetailModel.location
        }

    }
}