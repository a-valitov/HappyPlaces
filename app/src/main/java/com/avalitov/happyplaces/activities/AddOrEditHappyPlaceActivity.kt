package com.avalitov.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.avalitov.happyplaces.R
import com.avalitov.happyplaces.database.DatabaseHandler
import com.avalitov.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


lateinit var toolbarAddPlace : Toolbar
lateinit var etTitle : EditText
lateinit var etDescription: EditText
lateinit var etLocation: EditText
lateinit var etDate : EditText
lateinit var tvAddImage : TextView
private lateinit var ivPlaceImage : ImageView
lateinit var btnSave : Button

class AddOrEditHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0

    private var mHappyPlaceDetails : HappyPlaceModel? = null // Current HP that we work with

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_or_edit_happy_place)

        etTitle = findViewById(R.id.et_title)
        etDescription = findViewById(R.id.et_description)
        etLocation = findViewById(R.id.et_location)

        etDate = findViewById(R.id.et_date)
        etDate.setOnClickListener(this)

        tvAddImage = findViewById(R.id.tv_add_image)
        tvAddImage.setOnClickListener(this)

        ivPlaceImage = findViewById(R.id.iv_place_image_add)

        btnSave = findViewById(R.id.btn_save)
        btnSave.setOnClickListener(this)

        toolbarAddPlace = findViewById(R.id.tb_add_place)
        setSupportActionBar(toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        /**
         * If some data (HappyPlaceModel) has been passed here via intent,
         * that means user wants to EDIT the HP item
         */
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = intent.getParcelableExtra(
                MainActivity.EXTRA_PLACE_DETAILS)
                    as HappyPlaceModel?
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()
        }

        //updateDateInView()

        if(mHappyPlaceDetails != null) {
            /**
             * That means we already have an HP, so it's editing mode
             * Setting up the fields of HP that we edit
             */
            setupHappyPlaceForEditing()
        }

    }

    private fun setupHappyPlaceForEditing() {
        supportActionBar?.title = "Edit Happy Place"

        etTitle.setText(mHappyPlaceDetails!!.title)
        etDescription.setText(mHappyPlaceDetails!!.description)
        etDate.setText(mHappyPlaceDetails!!.date)
        etLocation.setText(mHappyPlaceDetails!!.location)
        mLatitude = mHappyPlaceDetails!!.latitude
        mLongitude = mHappyPlaceDetails!!.longitude

        saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.imagePath)
        ivPlaceImage.setImageURI(saveImageToInternalStorage)

        btnSave.text = "UPDATE"
    }

    override fun onClick(v: View?) {
        when(v!!.id) {

            R.id.et_date -> {
                DatePickerDialog(
                        this, dateSetListener,
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery",
                        "Capture photo from Camera")
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            R.id.btn_save -> {
                when {
                    etTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter the title", Toast.LENGTH_SHORT).show()
                    }
                    etDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter the description", Toast.LENGTH_SHORT).show()
                    }
                    etLocation.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter the description", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please select the image", Toast.LENGTH_SHORT).show()
                    } else -> {
                        // all fields are OK and we can save the Place
                        val happyPlace = HappyPlaceModel(
                                id = if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                                // 0 is a default number, SQLite is going to autoincrement it anyway
                                title = etTitle.text.toString(),
                                imagePath = saveImageToInternalStorage.toString(),
                                description = etDescription.text.toString(),
                                date = etDate.text.toString(),
                                location = etLocation.text.toString(),
                                latitude = mLatitude,
                                longitude = mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)

                        /**
                         * If there is an HP in mHappyPlaceDetails, we update.
                         * Otherwise we create a new HP in database.
                         */
                        val saveHappyPlaceResult =
                                if (mHappyPlaceDetails == null){
                                    dbHandler.addPlace(happyPlace)
                                } else {
                                    dbHandler.updatePlace(happyPlace).toLong()
                                }
                        if (saveHappyPlaceResult > 0) {
                            setResult(Activity.RESULT_OK)
                            finish()    // the Activity may be closed
                        }
                    }
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_CODE -> {
                    if (data != null) {
                        val contentURI = data.data
                        try {
                            val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                            saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                            Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")

                            ivPlaceImage.setImageBitmap(selectedImageBitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(this@AddOrEditHappyPlaceActivity,
                                    "Failed to load an image from Gallery", Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }
                }
                CAMERA_CODE -> {
                    val thumbnail : Bitmap = data?.extras?.get("data") as Bitmap // !! -> ?

                    saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                    Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")

                    ivPlaceImage.setImageBitmap(thumbnail)
                }
            }
        }
    }

    //is written with third-party library DEXTER which helps to manage Permissions
    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA_CODE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }

    //is written with third-party library DEXTER which helps to manage Permissions
    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent = Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY_CODE)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("It looks like you've turned off permission " +
                "required for this feature. It can be enabled under the Application Settings")
                .setPositiveButton("GO TO SETTINGS ") {
                    _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }.setNegativeButton("Cancel"){dialog, _ ->
                    dialog.dismiss()
                }.show()
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        etDate.setText(sdf.format(cal.time))
    }

    //saves the bitmap and returns its URI address on a device
    private fun saveImageToInternalStorage(bitmap: Bitmap) : Uri {
        val wrapper = ContextWrapper(applicationContext)
        // Private mode means other apps cannot access the directory
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        // creating an image file with a unique user identifier an a name
        file = File(file, "${UUID.randomUUID()}.jpg")

        // writing the image file via output stream
        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush() // guarantees that bytes previously written to the stream are passed to the OS
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY_CODE = 1
        private const val CAMERA_CODE = 2

        // a local directory on a device where images are stored
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

}