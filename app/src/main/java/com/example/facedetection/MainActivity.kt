package com.example.facedetection

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.example.facedetection.MainActivity.Global.Companion.emarthUrl
import com.example.facedetection.camera.CameraManager
import com.example.facedetection.camera.bgCameraManager
import com.example.facedetection.databinding.ActivityMainBinding
import com.example.facedetection.utils.FileUtils
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var cameraManager: CameraManager
    private lateinit var bg_cameraManager: bgCameraManager
    private lateinit var videoView: VideoView
    private lateinit var webView: WebView
    private var videoId = R.raw.sample0001
    private var file: FileUtils
    private val REQUIRED_PERMISSIONS_29 = arrayOf(
        android.Manifest.permission.CAMERA
        ,android.Manifest.permission.READ_EXTERNAL_STORAGE
        ,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUIRED_PERMISSIONS_30 = arrayOf(
        android.Manifest.permission.CAMERA
        ,android.Manifest.permission.READ_EXTERNAL_STORAGE
        ,android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        //,android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )

    public class Global : Application() {
        companion object {
            @JvmField
            var emarthUrl: String = "https://www.soumu.go.jp/main_content/000487279.mp4"
            // set EMarth download URL to above currentUrl global variable
            // sample video https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_10mb.mp4
            // sampel video2 https://www.soumu.go.jp/main_content/000487279.mp4
        }
    }


    init {
        val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        file = FileUtils("Log_${dateStr}.txt")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
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
        askAllPermission()
        buttonClick()
        settingsPage()
        repeatVideoFile()
    }

    private fun allPermissionGranted(): Boolean{
        if(Build.VERSION.SDK_INT >= 30){
            return REQUIRED_PERMISSIONS_30.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }else{
            return REQUIRED_PERMISSIONS_29.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun askAllPermission() {
        if (allPermissionGranted()) {
            screenSetting()
            detectAndPlayStart()
        } else {
            if(Build.VERSION.SDK_INT >= 30) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS_30, 0)
            }else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS_29, 0)
            }
        }
    }

    // permission for array of required functions (CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE etc)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && allPermissionGranted()) {
            screenSetting()
            detectAndPlayStart()
        } else {
            Toast.makeText(
                this,
                "One of Permissions (Camera, Write/Read external storage Denied!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun buttonClick() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

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
        videoView.setOnCompletionListener {
            videoView.setVideoPath("android.resource://" + packageName + "/" + videoId)
            videoView.seekTo(0)
            videoView.start()
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
                binding.rotatebtn.isVisible = false
                binding.downloadbtn.isVisible = false
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
        binding.rotatebtn.isVisible = false
        binding.downloadbtn.isVisible = false

        when (execMode) {
            "1" -> { // video play: local file
                videoView.isVisible = true
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                binding.rotatebtn.isVisible = false
                binding.downloadbtn.isVisible = true
            }

            "2" -> { // video play: YouTube
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                webView.isVisible = true
            }

            "3" -> { // background log file check
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                binding.counterText.isVisible = true
            }

            "4" -> { // face detection test
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                binding.viewGraphicOverlay.isVisible = true
                binding.viewCameraPreview.isVisible = true
            }
        }
    }

    private fun getDetectionFile() {
        binding.counterText.text = file.readFile()
    }

    private fun detectAndPlayStart() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val execMode = sharedPref?.getString("listPreference", "2")
        val cameraSide = sharedPref?.getString("list2Preference", "1") ?: "1"
        val dateAndTime =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"))

        file.saveFile(dateAndTime)
        file.saveFile(", mode=")
        file.saveFile(execMode + cameraSide)
        file.saveFile("\n")

        //file.savePublicFiles(execMode + cameraSide)

        when (execMode) {
            "1" -> { // video play: local file
                playVideoFile()
                bg_cameraManager.cameraStart(cameraSide)
            }

            "2" -> { // video play: YouTube
                playVideoUrl()
                bg_cameraManager.cameraStart(cameraSide)
            }

            "3" -> { // background log file check
                //playVideoFile()
                binding.counterText.text = getString(R.string.logmode_msg)
                bg_cameraManager.cameraStart(cameraSide)
            }

            "4" -> { // face detection test
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

        when (execMode) {
            "1" -> { // video play: local file
                stopVideoFile()
                bg_cameraManager.cameraStop()
                binding.rotatebtn.isVisible = false
            }

            "2" -> { // video play: YouTube
                webView.onPause()
                bg_cameraManager.cameraStop()
            }

            "3" -> { // background log file check
                getDetectionFile()
                bg_cameraManager.cameraStop()
            }

            "4" -> { // face detection test
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
            Toast.makeText(
                this,
                "Playing " + videoFile,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            videoFile = defaultFile
            Toast.makeText(
                this,
                "File does not exist. Using " + defaultFile,
                Toast.LENGTH_SHORT
            ).show()
        }

        videoView.setVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + videoFile)
        videoView.start()

        val hidebtn = findViewById<Button>(R.id.downloadbtn)
        hidebtn.isVisible = false

    }

    private fun stopVideoFile() {

        // video download upon stop action
        val downloadButton = findViewById<Button>(R.id.downloadbtn)
        val downloader = AndroidDownloader(this)
        downloadButton.setOnClickListener {
            downloader.downloadFile(emarthUrl)
            Toast.makeText(this, "Download Started", Toast.LENGTH_LONG).show()
        }

        if (videoView.isPlaying) {
            videoView.pause()
            downloadButton.visibility = View.VISIBLE
        }
    }

    private fun playVideoUrl() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val defaultURL = "https://youtu.be/yt7OM515Y58?si=zFOvrjxrwzzvR1kK"
        //val defaultURL = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/2MSyHu9bMPo?si=Y5midxW256Ybn7Qg\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay=\"1\"; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>"

        var videoURL = sharedPref?.getString("videoPreference2", defaultURL) ?: defaultURL

        if (videoURL == "") {
            videoURL = defaultURL
            Log.i("TAGY", "${videoURL}")
        }

        webView.apply {
            settings.javaScriptEnabled = true
            webChromeClient = WebChromeClient()
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.loadsImagesAutomatically = true
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true
        }.loadData(videoURL,"text/html","utf-8")
        webView.settings.javaScriptEnabled=true
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

}

