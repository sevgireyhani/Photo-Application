package com.example.photoapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_upload.*
import java.util.*
import java.util.jar.Manifest

class UploadActivity : AppCompatActivity() {

    var selectedPicture : Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

    }

    fun imageViewClicked(view : View) {

      if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        } else {
          val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
          startActivityForResult(intent,2)
      }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if(requestCode == 1){
            if (grantResults.size > 0 && grantResults [0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            selectedPicture = data.data


            try {

                if (selectedPicture != null) {

                    if (Build.VERSION.SDK_INT >= 28) {

                        val source = ImageDecoder.createSource(contentResolver, selectedPicture!!)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        uploadimageView.setImageBitmap(bitmap)


                    } else {
                        val bitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPicture)
                        uploadimageView.setImageBitmap(bitmap)
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    fun uploadClicked(view: View) {

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val storage = FirebaseStorage.getInstance()
        val reference = storage.reference
        val imagesReference = reference.child("images").child(imageName)

        if (selectedPicture != null){
            imagesReference.putFile(selectedPicture!!).addOnSuccessListener {taskSnapshot ->
            val uploadPictureReference = FirebaseStorage.getInstance().reference.child("images").child(imageName)
            uploadPictureReference.downloadUrl.addOnSuccessListener {uri ->
                val downloadUrl = uri.toString()
                println(downloadUrl)

                val postMap = hashMapOf<String,Any>()


                postMap["downloadUrl"] = downloadUrl
                postMap["userEmail"] = auth.currentUser!!.email.toString()
                postMap["comment"] = uploadCommentText.text.toString()
                postMap["date"] = Timestamp.now()


                db.collection("Posts").add(postMap).addOnCompleteListener { task ->
                    if (task.isComplete && task.isSuccessful){
                        //back
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(applicationContext,exception.localizedMessage.toString(),Toast.LENGTH_LONG).show()
                }




                }
                }
            }

            }
        }




