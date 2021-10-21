package com.avalitov.happyplaces

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.text.SimpleDateFormat
import java.util.*


lateinit var toolbarAddPlace : Toolbar
lateinit var etDate : EditText
lateinit var tvAddImage : TextView

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        toolbarAddPlace = findViewById(R.id.tb_add_place)
        setSupportActionBar(toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()
        }

        etDate = findViewById(R.id.et_date)
        etDate.setOnClickListener(this)

        tvAddImage = findViewById(R.id.tv_add_image)
        tvAddImage.setOnClickListener(this)



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
                        1 -> Toast.makeText(
                                this@AddHappyPlaceActivity,
                                "Camera selection coming soon...",
                                Toast.LENGTH_SHORT).show()
                    }
                }
                pictureDialog.show()
            }
        }
    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                //Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    Toast.makeText(this@AddHappyPlaceActivity,
                    "Storage READ/WRITE permissions are granted. Now you can select image from Gallery",
                    Toast.LENGTH_SHORT).show()
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


}