package com.example.ffmpegdemo

import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var outPutFile: String? = null
    var filePath: Uri? = null
    var filePathSecond: Uri? = null
    var photoUri: Uri? = null
    var AudioUri: Uri? = null
    var progressDialog: ProgressDialog? = null
    private val REQUEST_TAKE_GALLERY_VIDEO = 110
    private val REQUEST_TAKE_GALLERY_VIDEO_2 = 115
    private val REQUEST_TAKE_GALLERY_PHOTO = 120
    private val REQUEST_TAKE_GALLERY_AUDIO = 130
    private var ffmpeg: FFmpeg? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressDialog = ProgressDialog(this)
        findViewById<View>(R.id.video_player).setOnClickListener {
            val intent = Intent(this@MainActivity, VideoEditPlayer::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.selectVideo).setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO)
        }
        findViewById<View>(R.id.start).setOnClickListener {
            if (filePath != null) {
                executeCutVideoCommand(1 , 10, filePath!!)
            }
        }
        findViewById<View>(R.id.background).setOnClickListener {
            if (filePath != null && photoUri != null) {
                executeBackgroundCommand(filePath!!, photoUri!!)
            }
        }
        findViewById<View>(R.id.selectPhoto).setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_PHOTO)
        }
        findViewById<View>(R.id.speed).setOnClickListener {
            if (filePath != null) {
                executeSpeedCommand(.9f, filePath!!)
            }
        }
        findViewById<View>(R.id.rotate).setOnClickListener {
            if (filePath != null) {
                executeRotateCommand(3, filePath!!)
            }
        }
        findViewById<View>(R.id.selectAudio).setOnClickListener {
            val intent = Intent()
            intent.type = "audio/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_AUDIO)
        }
        findViewById<View>(R.id.setAudio).setOnClickListener {
            if (filePath != null && AudioUri != null) {
                executeChangeMusicCommand(filePath!!, AudioUri!!)
            }
        }
        findViewById<View>(R.id.mute).setOnClickListener {
            if (filePath != null) {
                executeMuteCommand(filePath!!)
            }
        }
        findViewById<View>(R.id.volume).setOnClickListener {
            if (filePath != null) {
                executeVolumeCommand(filePath!!, .5f)
            }
        }
        findViewById<View>(R.id.audio_trim).setOnClickListener {
            if (AudioUri != null) {
                executeCutAudioCommand(1 * 1000, 10 * 1000, AudioUri!!)
            }
        }
        findViewById<View>(R.id.marge_video).setOnClickListener { if (filePath != null && filePathSecond != null) executeMargeVideoCommand(filePath!!, filePathSecond!!) }
        findViewById<View>(R.id.selectVideo_2).setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO_2)
        }
        findViewById<View>(R.id.ratio_video).setOnClickListener { if (filePath != null) executeRatioCommand(filePath!!, 2, 5) }
        findViewById<View>(R.id.filter_video).setOnClickListener {
            if (filePath != null) {
                executeFilterCommand(filePath!!)
            }
        }
        loadFFMpegBinary()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
            val uri = data!!.data
            if (uri != null) {
                filePath = uri
            }
        }
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO_2) {
            val uri = data!!.data
            if (uri != null) {
                filePathSecond = uri
            }
        }
        if (requestCode == REQUEST_TAKE_GALLERY_PHOTO) {
            val uri = data!!.data
            if (uri != null) {
                photoUri = uri
            }
        }
        if (requestCode == REQUEST_TAKE_GALLERY_AUDIO) {
            val uri = data!!.data
            if (uri != null) {
                AudioUri = uri
            }
        }
    }

    private fun executeBackgroundCommand(filePath: Uri, photoUri: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "set_background_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        val yourRealPathImage = getPath(this@MainActivity, photoUri)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        /* Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);*/outPutFile = dest.absolutePath
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        val complexCommand = arrayOf("-i", yourRealPathImage, "-i", yourRealPath
                , "-filter_complex", "overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2", outPutFile)
        //   String[] complexCommand = {"-i", "" + yourRealPath, "-i", "" +yourRealPathImage, "-filter_complex", "overlay=10:main_h-overlay_h-10",outPutFile};
        execFFmpegBinary(complexCommand)
    }

    // video trim
    private fun executeCutVideoCommand(startMs: Int, endMs: Int, filePath: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "cut_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        /* Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);*/outPutFile = dest.absolutePath
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        val complexCommand = arrayOf("-ss",
                "" + startMs ,
                "-y",
                "-i",
                yourRealPath,
                "-t",
                "" + (endMs - startMs) ,
                "-vcodec",
                "mpeg4",
                "-b:v",
                "2097152",
                "-b:a",
                "48000",
                "-ac",
                "2",
                "-ar",
                "22050",
                outPutFile)
       // execFFmpegBinary(complexCommand)
        execFFmpegBinary(getFfmpegEncodingArgs(yourRealPath,startMs ,(endMs - startMs) ,outPutFile))

    }

    private fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                            split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                              selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
                column
        )
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                    null)
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                ffmpeg = FFmpeg.getInstance(this)
            }
            ffmpeg!!.loadBinary(object : LoadBinaryResponseHandler() {
                override fun onFailure() {}
                override fun onSuccess() {}
            })
        } catch (e: FFmpegNotSupportedException) {
        } catch (e: Exception) {
        }
    }

    private fun execFFmpegBinary(command: Array<String?>) {
        progressDialog?.show()
        try {
            ffmpeg?.execute(command, object : ExecuteBinaryResponseHandler() {
                override fun onFailure(s: String) {
                     Log.d("@a", "FAILED with output : " + s);
                }

                override fun onSuccess(s: String) {
                        Log.d("@a", "SUCCESS with output : " + s);
                    Toast.makeText(this@MainActivity, "SUCCESS$outPutFile", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(s: String) {
                    val start: Int = s.indexOf("time=")
                    val end: Int = s.indexOf(" bitrate")
                    if (start != -1 && end != -1) {
                        val duration: String = s.substring(start + 5, end)
                        if (duration !== "") {
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                                sdf.timeZone = TimeZone.getTimeZone("UTC")
                                Log.d("@a", "onProgress: $s"+sdf.parse("1970-01-01 $duration").time/System.currentTimeMillis())

                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                override fun onStart() {
                    progressDialog!!.setMessage("Processing...")
                    progressDialog!!.show()
                }

                override fun onFinish() {
                    Log.d("@a", "onFinish with output : " );

                    progressDialog!!.dismiss()
                }
            })
        } catch (e: FFmpegCommandAlreadyRunningException) {
            Toast.makeText(this@MainActivity, "EXCEPTION", Toast.LENGTH_SHORT).show()

            // do nothing for now
        }
    }

    private fun executeSpeedCommand(speed: Float, filePath: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "speed_change_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        outPutFile = dest.absolutePath
        val complexCommand = arrayOf("-y", "-i", yourRealPath, "-filter_complex",
                "[0:v]setpts=" + (2.5 - speed) + "*PTS[v];[0:a]atempo=" + speed + "[a]", "-map", "[v]", "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", outPutFile)
        execFFmpegBinary(complexCommand)
    }

    private fun getFfmpegEncodingArgs(inputFilePath: String?, startTimeSec: Int?, endTimeSec: Int?,
                                      destinationFilePath: String?): Array<String?> {
        val command: MutableList<String> = LinkedList()

        // If the output exists, overwrite it
        command.add("-y")
        // Input file
        command.add("-i")
        inputFilePath?.let { command.add(it) }
        command.add("-i")
        command.add("/storage/emulated/0/xpk/asset/kxp_11.mp3")
        if (startTimeSec != null && startTimeSec != 0) {
            // Start time offset
            command.add("-ss")
            command.add(startTimeSec.toString())
        }
        if (startTimeSec != null && endTimeSec != null) {
            val subDurationSec = endTimeSec - startTimeSec
            //  if (fullDurationSec != subDurationSec) {
            // Duration of media file
            command.add("-t")
            command.add(subDurationSec.toString())
            //  }
        }
        // music
        command.add("-c:a")
        command.add("copy")
        // command.add("aac");
        command.add("-map")
        command.add("0:v:0")
        command.add("-map")
        command.add("1:a:0")
        command.add("-shortest")
        //speed change
        command.add("-filter:v")
        command.add("setpts=0.5*PTS")
     /*   // flip v
        command.add("-vf")
        command.add("vflip")
        command.add("-c:a")
        command.add("copy")
         // flip h
        command.add("-vf")
        command.add("hflip")
        command.add("-c:a")
        command.add("copy")*/

        destinationFilePath?.let { command.add(it) }
        return command.toTypedArray()
    }


    private fun executeRotateCommand(rotate: Int, filePath: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "speed_rotation_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        outPutFile = dest.absolutePath
        var complexCommand: Array<String?>? = null
        complexCommand = if (rotate == 3) {
            arrayOf("-i", yourRealPath, "-filter:v", "transpose=2,transpose=2", outPutFile)
        } else {
            arrayOf("-i", yourRealPath, "-filter:v", "transpose=$rotate", outPutFile)
        }
        execFFmpegBinary(complexCommand)
    }

    private fun executeChangeMusicCommand(filePath: Uri, audioUri: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "audio_change_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        val yourRealPathAudio = getPath(this@MainActivity, audioUri)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        outPutFile = dest.absolutePath
        val complexCommand = arrayOf("-i", yourRealPath, "-i", yourRealPathAudio, "-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0", "-shortest", outPutFile)
        execFFmpegBinary(complexCommand)
    }

    private fun executeMuteCommand(filePath: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "mute_change_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        outPutFile = dest.absolutePath
        val complexCommand = arrayOf("-i", yourRealPath, "-vcodec", "copy", "-an", outPutFile)
        execFFmpegBinary(complexCommand)
    }

    private fun executeVolumeCommand(filePath: Uri, volume: Float) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "volume_change_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        outPutFile = dest.absolutePath
        val complexCommand = arrayOf("-i", yourRealPath, "-filter:a", "volume=$volume", outPutFile)
        execFFmpegBinary(complexCommand)
    }

    private fun executeCutAudioCommand(startMs: Int, endMs: Int, filePath: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "cut_video"
        val fileExtn = ".mp3"
        val yourRealPath = getPath(this@MainActivity, filePath)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        /* Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);*/outPutFile = dest.absolutePath
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        val complexCommand = arrayOf("-ss",
                "" + startMs / 1000,
                "-y",
                "-i",
                yourRealPath,
                "-t",
                "" + (endMs - startMs) / 1000,
                "-vcodec",
                "mpeg4",
                "-b:v",
                "2097152",
                "-b:a",
                "48000",
                "-ac",
                "2",
                "-ar",
                "22050",
                outPutFile)
        execFFmpegBinary(complexCommand)
    }

    private fun executeMargeVideoCommand(filePath: Uri, filePathSecond: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "marge_change_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        val yourRealPath2 = getPath(this@MainActivity, filePathSecond)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        outPutFile = dest.absolutePath
        val complexCommand = arrayOf("-y", "-i", yourRealPath, "-i", yourRealPath2, "-strict", "experimental", "-filter_complex",
                "[0:v]scale=iw*min(1920/iw\\,1080/ih):ih*min(1920/iw\\,1080/ih), pad=1920:1080:(1920-iw*min(1920/iw\\,1080/ih))/2:(1080-ih*min(1920/iw\\,1080/ih))/2,setsar=1:1[v0];[1:v] scale=iw*min(1920/iw\\,1080/ih):ih*min(1920/iw\\,1080/ih), pad=1920:1080:(1920-iw*min(1920/iw\\,1080/ih))/2:(1080-ih*min(1920/iw\\,1080/ih))/2,setsar=1:1[v1];[v0][0:a][v1][1:a] concat=n=2:v=1:a=1",
                "-ab", "48000", "-ac", "2", "-ar", "22050", "-s", "1920x1080", "-vcodec", "libx264", "-crf", "27",
                "-q", "4", "-preset", "ultrafast", outPutFile)
        execFFmpegBinary(complexCommand)
    }

    private fun executeRatioCommand(filePath: Uri, w: Int, h: Int) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "ratio_change_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        outPutFile = dest.absolutePath
        //  String complexCommand[] = {"-i", yourRealPath,"-r", "15", "-aspect" ,""+w+":"+""+h ,"-strict" ,"-2",outPutFile};
        val complexCommand = arrayOf("-i", yourRealPath, "-lavf", "[0:v]scale=1920*2:1080*2,boxblur=luma_radius=min(h,w)/20:luma_power=1:chroma_radius=min(cw,ch)/20:chroma_power=1[bg];[0:v]scale=-1:1080[ov];[bg][ov]overlay=(W-w)/2:(H-h)/2,crop=w=1920:h=1080", outPutFile)
        execFFmpegBinary(complexCommand)
    }

    private fun executeFilterCommand(filePath: Uri) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "filter_change_video"
        val fileExtn = ".mp4"
        val yourRealPath = getPath(this@MainActivity, filePath)
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        outPutFile = dest.absolutePath
        //  String complexCommand[] = {"-i", yourRealPath,"-r", "15", "-aspect" ,""+w+":"+""+h ,"-strict" ,"-2",outPutFile};
        val complexCommand = arrayOf("-i", yourRealPath, "-vf", "split [main][tmp]; [tmp] lutyuv=", "y=val*5", " [tmp2]; [main][tmp2] overlay", outPutFile)
        execFFmpegBinary(complexCommand)
    }
}