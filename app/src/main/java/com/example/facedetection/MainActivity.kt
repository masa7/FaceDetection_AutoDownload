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
import android.util.Log
import android.view.MotionEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.widget.VideoView
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


    private val REQUIRED_PERMISSIONS = mutableListOf<String>().apply {
        add(android.Manifest.permission.CAMERA)

        // Add storage permissions for Android 8 (Oreo) to 12 (S)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Add media permissions for Android 13 (TIRAMISU) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.READ_MEDIA_IMAGES)
            add(android.Manifest.permission.READ_MEDIA_VIDEO)
        }
    }.toTypedArray()


    private fun allPermissionGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun askAllPermissions() {
        if (allPermissionGranted()) {
            screenSetting()
            detectAndPlayStart()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 0)
        }
    }

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
                    "One of the permissions (Camera, File access) was denied.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


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

        askAllPermissions()
        buttonClick()
        settingsPage()
        repeatVideoFile()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        binding.buttonControl.isVisible = true
        return super.dispatchTouchEvent(ev)
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
                playVideoFromPreferences()
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
        //Toa

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

        if (videoView.isPlaying) {
            videoView.pause()
            //downloadButton.visibility = View.VISIBLE
        }
    }

    private fun playVideoFromPreferences() {

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultIframeUrl = "https://www.youtube.com/embed/Qghjl2tJsoo"

        val videoUrl = sharedPref.getString("videoPreference2", null)

        val iframeUrl = when {
            !videoUrl.isNullOrEmpty() -> convertToEmbedUrl(videoUrl) ?: defaultIframeUrl
            videoUrl == null -> defaultIframeUrl
            else -> defaultIframeUrl
        }

        Log.d("VideoPlayback", "iframeUrl: $iframeUrl")
        playVideoUrl(iframeUrl)
    }

    private fun convertToEmbedUrl(youtubeUrl: String): String? {
        val videoId = extractVideoId(youtubeUrl) ?: return null
        return "https://www.youtube.com/embed/$videoId"
    }

    private fun extractVideoId(youtubeUrl: String): String? {
        val regex = """(?:https?://)?(?:www\.)?(?:youtube\.com/(?:[^/]+/.*|(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\.be/)([^?&]{11})""".toRegex()
        val matchResult = regex.find(youtubeUrl)
        return matchResult?.groups?.get(1)?.value
    }

    private fun playVideoUrl(iframeUrl: String) {
        // Create an HTML string to load in the WebView
        val html = """
        <html>
            <body style="margin:0;padding:0;">
                <iframe width="100%" height="100%"
                        src="$iframeUrl?autoplay=1&mute=0"
                        frameborder="0" allowfullscreen>
                </iframe>
            </body>
        </html>
    """.trimIndent()

        webView.apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = WebViewClient()
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            loadData(html, "text/html", "UTF-8")
        }

        webView.onResume()
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

