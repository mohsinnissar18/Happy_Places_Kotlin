package com.practice.happyplaces.activities

import android.Manifest
import android.app.Activity
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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.practice.happyplaces.R
import com.practice.happyplaces.database.DatabaseHandler
import com.practice.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.practice.happyplaces.models.HappyPlaceModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var cal  = Calendar.getInstance()
    private lateinit var dateSetListener : DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage : Uri? = null
    private var mLatitude: Double = 0.0 // A variable which will hold the latitude value.
    private var mLongitude: Double = 0.0 // A variable which will hold the longitude value.


    var binding : ActivityAddHappyPlaceBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace)
        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{ view, year, monthOfYear, dayOfMonth ->

            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)

    }

    private fun updateDateInView (){

        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(
            myFormat, Locale.getDefault()
        )
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    override fun onClick(v: View?) {
        when(v!!.id){

            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity, dateSetListener,

                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image ->{

                val pictureDialog = AlertDialog.Builder(this@AddHappyPlaceActivity)
                pictureDialog.setTitle("Select Action")

                val pictureDialogItems = arrayOf("Select photo from Gallery",
                    "Capture Photo from Camera")

                pictureDialog.setItems(pictureDialogItems){
                    dialog, which ->
                    when(which){

                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()

                    }
                }
                pictureDialog.show()
            }
            R.id.btn_save -> {


                when {
                    binding?.etTitle?.text?.isNullOrEmpty() == true -> {
                        Toast.makeText(this, "Please Enter TITLE", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text?.isNullOrEmpty() == true -> {
                        Toast.makeText(this, "Please Enter DESCRIPTION", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etLocation?.text?.isNullOrEmpty() == true -> {
                        Toast.makeText(this, "Please Enter LOCATION", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please add IMAGE", Toast.LENGTH_SHORT).show()

                    }
                    else -> {
                        //Assigning all the values to data model class
                        val happyPLaceModel = HappyPlaceModel(
                            0,
                            binding?.etTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding?.etDescription?.text.toString(),
                            binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )

                        val dbHandler = DatabaseHandler(this)
                        val addHappyPlace = dbHandler.addHappyPlace(happyPLaceModel)

                        if (addHappyPlace > 0){
                            Toast.makeText(this, "The Happy Place Details are inserted SUCCESSFULLY", Toast.LENGTH_SHORT).show()
                            finish()
                        }



                    }
                }
            }

        }
    }
    private fun choosePhotoFromGallery(){

        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_MEDIA_IMAGES
        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                if (report!!.areAllPermissionsGranted()){

                    val galleryIntent = Intent (
                        Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions : MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread()
            .check()
    }
    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA

        ).withListener(object: MultiplePermissionsListener{
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                if (report!!.areAllPermissionsGranted()){

                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions : MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }
        }).onSameThread()
            .check()

    }
    private fun showRationaleDialogForPermissions(){

        AlertDialog.Builder(this).setMessage("Please enable permissions for this function to work")
            .setPositiveButton("GO TO SETTINGS") {
                _, _ ->
                try {

                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e : ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){
                dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GALLERY && resultCode == Activity.RESULT_OK){
            if (data != null){
                val contentURI = data.data

                try {

                    val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                    Log.e("Saved Image :" ,"Path :: $saveImageToInternalStorage")
                    binding?.ivPlaceImage!!.setImageBitmap(selectedImageBitmap)

                }catch (e : IOException){
                    e.printStackTrace()
                    Toast.makeText(this@AddHappyPlaceActivity, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }else if (requestCode == CAMERA && resultCode == Activity.RESULT_OK){

            val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap
            saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
            Log.e("Saved Image :" ,"Path :: $saveImageToInternalStorage")
            binding?.ivPlaceImage!!.setImageBitmap(thumbnail)

        }else if (resultCode == Activity.RESULT_CANCELED){
            Log.e("CANCELLED", "Cancelled")
        }
    }

    private fun saveImageToInternalStorage (bitmap : Bitmap) : Uri {

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File (file, "${UUID.randomUUID()}.jpg")

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)

    }

    companion object {
        private const val GALLERY = 1
        private  const val CAMERA = 2
        private  const val IMAGE_DIRECTORY = "HappyPlacesImages"

    }
}