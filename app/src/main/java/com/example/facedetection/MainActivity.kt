package com.example.facedetection

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.view.MotionEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.example.facedetection.MainActivity.Global.Companion.abbrDataLog
import com.example.facedetection.MainActivity.Global.Companion.abbrFaceDetectionLog
import com.example.facedetection.MainActivity.Global.Companion.abbrTransferredFile
import com.example.facedetection.MainActivity.Global.Companion.dateFormatter
import com.example.facedetection.MainActivity.Global.Companion.dateStr
import com.example.facedetection.MainActivity.Global.Companion.debugFlag
import com.example.facedetection.MainActivity.Global.Companion.emarthUrl
import com.example.facedetection.MainActivity.Global.Companion.holdDays
import com.example.facedetection.MainActivity.Global.Companion.storageType
import com.example.facedetection.MainActivity.Global.Companion.userEmail
import com.example.facedetection.camera.CameraManager
import com.example.facedetection.camera.bgCameraManager
import com.example.facedetection.databinding.ActivityMainBinding
import com.example.facedetection.fileuploader.MyAPI
import com.example.facedetection.fileuploader.UploadRequestBody
import com.example.facedetection.fileuploader.UploadResponse
import com.example.facedetection.fileuploader.getFileName
import com.example.facedetection.utils.BaseActivity
import com.example.facedetection.utils.FileUtils
import com.example.facedetection.utils.SingletonContext
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class MainActivity : BaseActivity(), UploadRequestBody.UploadCallback {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var cameraManager: CameraManager
    private lateinit var bg_cameraManager: bgCameraManager
    private lateinit var videoView: VideoView
    private lateinit var webView: WebView
    private var file: FileUtils
    private var selectedFileUri: Uri? = null

    private val REQUIRED_PERMISSIONS_29 = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
//    private val REQUIRED_PERMISSIONS_30 = arrayOf(
//        android.Manifest.permission.CAMERA,
//        android.Manifest.permission.READ_EXTERNAL_STORAGE,
//        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
//    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val REQUIRED_PERMISSIONS_33 = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VIDEO,
    )


    val handler = Handler(Looper.getMainLooper())
    val timer = object : Runnable {
        override fun run() {
            binding.buttonControl.isVisible = false
            handler.postDelayed(this, 7000)
        }
    }

    class Global : Application() {
        companion object {
            @JvmField
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            val userEmail = auth.currentUser?.email

            var emarthUrl: String =
                "https://emadtech.jp/wp-content/uploads/2019/06/HeatStroke_v2.mp4"
            // set EMarth download URL to above currentUrl global variable

            // 1: Internal Storage, 2: External Storage(Download folder)
            val storageType: String = "1"
            val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val dateStr = LocalDateTime.now().format(dateFormatter)
            val abbrFaceDetectionLog = "Log_"
            val abbrTransferredFile = "Done_"
            val abbrDataLog = "DataLog"
            val holdDays = 5
            val debugFlag = false
        }
    }

    init {
        file = FileUtils(abbrFaceDetectionLog + dateStr + ".txt", storageType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        supportActionBar?.hide()

        cameraManager = CameraManager(
            this,
            binding.viewCameraPreview,
            binding.viewGraphicOverlay,
            this
        )

        bg_cameraManager = bgCameraManager(
            this,
            this
        )

        videoView = binding.videoView
        webView = binding.webView

        //askCameraPermission()
        askAllPermissions()

        buttonClick()
        settingsPage()
        repeatVideoFile()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        binding.buttonControl.isVisible = true
        return super.dispatchTouchEvent(ev)
    }

    private fun allPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            return REQUIRED_PERMISSIONS_33.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            return REQUIRED_PERMISSIONS_29.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun askAllPermissions() {
        if (allPermissionGranted()) {
            screenSetting()
            detectAndPlayStart()
        } else {
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= 33) {
                REQUIRED_PERMISSIONS_33
            } else {
                REQUIRED_PERMISSIONS_29
            }
            ActivityCompat.requestPermissions(this, permissionsToRequest, 0)
        }
    }


    // permission for array of required functions (CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE etc)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (allPermissionGranted()) {
                screenSetting()
                detectAndPlayStart()
        } else {
            Toast.makeText(
                this,
                "One of Permissions (Camera, File access) was denied.",
                Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun buttonClick() {
        binding.apply {
            buttonStopCamera.setOnClickListener {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                screenSetting()
                detectAndPlayStop()
            }

            buttonStartCamera.setOnClickListener {
                screenSetting()
                detectAndPlayStart()
            }
        }
    }

    private fun repeatVideoFile() {
        videoView.setOnPreparedListener {
            it.isLooping = true
        }
    }

    private fun settingsPage() {
        val secondPage = binding.buttonChangePage
        secondPage.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)

            if (binding.buttonStopCamera.isVisible == true) {
                detectAndPlayStop()
                binding.buttonStopCamera.isVisible = false
                binding.buttonStartCamera.isVisible = true
            }
        }
    }

    private fun screenSetting() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val execMode = sharedPref?.getString("listPreference", "2")

        // hide all views
        binding.viewGraphicOverlay.isVisible = false
        binding.viewCameraPreview.isVisible = false
        webView.isVisible = false
        videoView.isVisible = false
        binding.counterText.isVisible = false

        when (execMode) {
            "2" -> { // video play: local file
                videoView.isVisible = true
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            }

            "3" -> { // video play: YouTube
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                webView.isVisible = true
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            }

            "4" -> { // background log file check
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                binding.counterText.isVisible = true
            }

            "1" -> { // face detection camera test
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                binding.viewGraphicOverlay.isVisible = true
                binding.viewCameraPreview.isVisible = true
            }
        }
    }

    private fun getDetectionFile() {
        binding.counterText.text = file.read()
    }

    private fun detectAndPlayStart() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val execMode = sharedPref?.getString("listPreference", "2")
        val cameraSide = sharedPref?.getString("list2Preference", "1") ?: "1"
        val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
        val dd = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd")).toInt()

        handler.post(timer)

        if(dd % holdDays == 0){
            deleteFile()
            clearCache()
        }

        if(file.isFirstRun()) {
            if(debugFlag) {
                Toast.makeText(
                    this,
                    "First run for today: " + file.isFirstRun(),
                    Toast.LENGTH_SHORT
                ).show()
            }
            transferFile()
        }

        file.save(dateAndTime + ", ")
        file.save("LaunchApp, Mode=" + execMode + cameraSide + ", ")
        file.save(userEmail)
        file.save("\n")

        when (execMode) {
            "2" -> { // video play: local file
                playVideoFile()
                bg_cameraManager.cameraStart(cameraSide)
            }

            "3" -> { // video play: YouTube
                playVideoUrl()
                bg_cameraManager.cameraStart(cameraSide)
            }

            "4" -> { // background log file check
                //playVideoFile()
                binding.counterText.text = getString(R.string.logmode_msg)
                bg_cameraManager.cameraStart(cameraSide)
            }

            "1" -> { // face detection camera test
                cameraManager.cameraStart(cameraSide)
            }
        }

        binding.buttonStopCamera.isVisible = true
        binding.buttonStartCamera.isVisible = false
    }

    private fun detectAndPlayStop() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val execMode = sharedPref?.getString("listPreference", "2")

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        handler.removeCallbacks(timer)

        when (execMode) {
            "2" -> { // video play: local file
                stopVideoFile()
                bg_cameraManager.cameraStop()
            }

            "3" -> { // video play: YouTube
                webView.onPause()
                bg_cameraManager.cameraStop()
            }

            "4" -> { // background log file check
                getDetectionFile()
                bg_cameraManager.cameraStop()
            }

            "1" -> { // face detection test
                cameraManager.cameraStop()
            }
        }

        binding.buttonStopCamera.isVisible = false
        binding.buttonStartCamera.isVisible = true
    }

    private fun playVideoFile() {

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var videoFile = sharedPref?.getString("videoPreference1", "")

        val videoLoc =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/"
        val downloadedfilename: String = emarthUrl.substring(emarthUrl.lastIndexOf('/') + 1)
        val defaultFile = downloadedfilename

        val directory = File(videoLoc)
        //Log.i("TAGY", "${directory}")

        var filecnt = 0
        val files = directory.listFiles()?.filter { it.isFile }
        files?.forEach { file ->
            println(file.name)
            if (videoFile == file.name) {
                ++filecnt
            }
        }

        if (filecnt != 0) {
            if(debugFlag) {
                Toast.makeText(
                    this,
                    "Playing " + videoFile,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            videoFile = defaultFile
            if(debugFlag){
                Toast.makeText(
                    this,
                    "File does not exist. Using " + defaultFile,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        videoView.setVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + videoFile)
        videoView.start()
    }

    private fun stopVideoFile() {

        // video download upon stop action
        /*
        val downloadButton = findViewById<Button>(R.id.downloadbtn)
        val downloader = AndroidDownloader(this)
        downloadButton.setOnClickListener {
            downloader.downloadFile(emarthUrl)
            Toast.makeText(this, "Download Started", Toast.LENGTH_LONG).show()
        }
        */

        if (videoView.isPlaying) {
            videoView.pause()
            //downloadButton.visibility = View.VISIBLE
        }
    }

    private fun playVideoUrl() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultURL = "https://www.youtube.com/watch?v=yt7OM515Y58"
        //val defaultURL = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/2MSyHu9bMPo?si=Y5midxW256Ybn7Qg\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay=\"1\"; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>"

        var videoURL = sharedPref?.getString("videoPreference2", defaultURL) ?: defaultURL

        if (videoURL == "") {
            videoURL = defaultURL
            //Log.i("TAGY", "${videoURL}")
        }

        /* to enable YouTube functions
        webView.apply {
            settings.javaScriptEnabled = true
            webChromeClient = WebChromeClient()
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true
        }.loadData(videoURL,"text/html","utf-8")
        */

        webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
        }

        webView.loadUrl(videoURL)
        webView.onResume()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onProgressUpdate(percentage: Int) {
        //do nothing
        //binding.progressBar.progress = percentage
    }

    private fun clearCache(){
        val transferLog = FileUtils(abbrDataLog + ".txt", "2")
        val ctx = SingletonContext.applicationContext()

        val cacheLoc = ctx.cacheDir
        val logList = cacheLoc.list()?.filter{ it.startsWith(abbrFaceDetectionLog) }
        val doneList = cacheLoc.list()?.filter{ it.startsWith(abbrTransferredFile) }

        // delete face detection log file in cache
        logList?.forEach {
            val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
            val deleteFilePath = File(cacheLoc, it)
            val result = deleteFilePath.delete()
            if(result){
                transferLog.save(dateAndTime + ", ")
                transferLog.save("file was deleted: " + deleteFilePath.toString() + "\n")
            }
        }
        // delete transferred file in cache
        doneList?.forEach {
            val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
            val deleteFilePath = File(cacheLoc, it)
            val result = deleteFilePath.delete()
            if(result){
                transferLog.save(dateAndTime + ", ")
                transferLog.save("file was deleted: " + deleteFilePath.toString() + "\n")
            }
        }
    }

    private fun deleteFile(){
        val transferLog = FileUtils(abbrDataLog + ".txt", "2")
        val ctx = SingletonContext.applicationContext()
        val fileLoc = ctx.filesDir
        val fileList = fileLoc.list()?.filter{ it.startsWith(abbrTransferredFile) }

        // delete transferred file in local folder
        fileList?.forEach {
            val yyyymmdd = it.substring(abbrTransferredFile.length, abbrTransferredFile.length + 8)
            val elapsedDays = ChronoUnit.DAYS.between(LocalDate.parse(yyyymmdd, dateFormatter), LocalDate.parse(dateStr, dateFormatter))

            if(elapsedDays > holdDays){
                val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
                val deleteFilePath = File(fileLoc, it)
                val result = deleteFilePath.delete()
                if(result){
                    transferLog.save(dateAndTime + ", ")
                    transferLog.save("file was deleted: " + deleteFilePath.toString() + "\n")
                }
                if(debugFlag) {
                    Toast.makeText(
                        this,
                        "データ・クリーンアップが完了しました",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun transferFile() {
        val transferLog = FileUtils(abbrDataLog + ".txt", "2")
        val ctx = SingletonContext.applicationContext()
        val fileLoc = ctx.filesDir
        val fileList = fileLoc.list()?.filter { it.startsWith(abbrFaceDetectionLog) }

        fileList?.forEach {
            val filePath = File(fileLoc, it)
            val newFileName = it.replace(abbrFaceDetectionLog, abbrTransferredFile)
            val newFilePath = File(fileLoc, newFileName)
            selectedFileUri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                filePath
            )

            // Obtain the ParcelFileDescriptor
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(selectedFileUri!!, "r", null)

            try {
                // Use the ParcelFileDescriptor
                val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
                val file = File(cacheDir, contentResolver.getFileName(selectedFileUri!!))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                // Upload the file
                //binding.progressBar.progress = 0
                val body = UploadRequestBody(file, "image", this)
                MyAPI().uploadFile(
                    MultipartBody.Part.createFormData(
                        "image",
                        file.name,
                        body
                    ),
                    RequestBody.create(MediaType.parse("multipart/form-data"), "json")
                ).enqueue(object : Callback<UploadResponse> {
                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        //binding.layoutRoot.snackbar(t.message!!)
                        //binding.progressBar.progress = 0
                        val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
                        transferLog.save(dateAndTime + ", ")
                        transferLog.save("transfer file=" + file.toString() + ": NG\n")
                    }

                    override fun onResponse(
                        call: Call<UploadResponse>,
                        response: Response<UploadResponse>
                    ) {
                        response.body()?.let {
                            //binding.layoutRoot.snackbar(it.message)
                            //binding.progressBar.progress = 100
                            val dateAndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))
                            transferLog.save(dateAndTime + ", ")
                            transferLog.save("transfer file=" + file.toString() + ": OK\n")
                            filePath.renameTo(newFilePath)
                            transferLog.save(dateAndTime + ", ")
                            transferLog.save("file was renamed to " + newFileName + "\n")
                        }
                    }
                })

            } finally {
                // Ensure that the ParcelFileDescriptor is closed
                try {
                    parcelFileDescriptor?.close()
                } catch (e: IOException) {
                    // Handle the exception if needed
                    e.printStackTrace()
                }
            }
        }
    }

}

