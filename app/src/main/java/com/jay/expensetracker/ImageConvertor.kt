package com.jay.expensetracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.webkit.MimeTypeMap
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Locale
import java.util.UUID

class ImageConvertor {
    companion object {
        //        it'll take the Image Uri as input and give the what is the format of the image
        fun getImageFormat(context: Context, imageUri: Uri): String {
            val extension = getFileExtension(context, imageUri)

            // Check the file extension and return the corresponding image format
            return if (extension != null) {
                when (extension.lowercase(Locale.getDefault())) {
                    "jpg", "jpeg" -> ".JPEG"
                    "png" -> ".PNG"
                    "gif" -> ".GIF"
                    else -> "Unknown"
                }
            } else "Unknown"

            // If the extension is not recognized or the URI is invalid, return "Unknown"
        }

        //      This function will take image Uri as input and give the extension of that image URI
        private fun getFileExtension(context: Context, uri: Uri): String? {
            // Use MimeTypeMap to get the file extension
            var extension: String? = null
            if (uri.scheme == "content") {
                val mime = MimeTypeMap.getSingleton()
                extension = mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
            } else if (uri.scheme == "file") {
                val path = uri.lastPathSegment
                extension = path!!.substring(path.lastIndexOf(".") + 1)
            }
            return extension
        }

        //        This function will take the type of the image and create a unique path for the storing the image
        fun getUniqueImagePath(context: Context, imageType: String): String {
            // Get the current timestamp
            val timeStamp = System.currentTimeMillis()

            // Generate a random UUID
            val uniqueId = UUID.randomUUID().toString()

            // Define the directory where you want to store the images
            val storageDir = File(context.getExternalFilesDir(null), "Bill Images")

            // Create the directory if it doesn't exist
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            // Create a unique file name for the image using the timestamp and UUID
            val imageFileName = "IMG_${timeStamp}_$uniqueId$imageType"

            // Combine the directory and file name to create the full path
            val imageFile = File(storageDir, imageFileName)

            // Return the absolute path of the image file
            return imageFile.absolutePath
        }

        //        This functin will take the image of uri and path of the image and store the image that taken as parameter
        fun handleImage(context: Context, imageUri: Uri, destinationPath: String) {
            try {
                // Copy the image to external storage
                val destinationFile = File(destinationPath)
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val outputStream: OutputStream = FileOutputStream(destinationFile)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream!!.read(buffer).also { bytesRead = it } > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                inputStream.close()
                outputStream.close()


            } catch (e: IOException) {
                Toast.makeText(context, "Select Photo again", Toast.LENGTH_SHORT).show()
            }
        }

        // this function will Encode an image to a base64 string
        fun encodeImageToBase64(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            // Compress the image to JPEG format with 100% quality (you can adjust the quality)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(imageBytes, Base64.DEFAULT)
        }

        // Function to decode Base64 string and convert it to a Bitmap(Image)
        fun decodeBase64ToBitmap(base64String: String): Bitmap? {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        }

        fun bitmapToImageUri(context: Context, bitmap: Bitmap): Uri? {
            // Define a file path where the image will be saved
            val imagePath = File(context.externalCacheDir, "image.jpg")

            try {
                // Create an OutputStream to write the bitmap to the file
                val outputStream: OutputStream = FileOutputStream(imagePath)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                // Return a Uri from the saved file
                return Uri.fromFile(imagePath)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
    }
}