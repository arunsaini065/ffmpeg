package com.example.ffmpegdemo

import android.content.Context
import android.graphics.SurfaceTexture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import kotlinx.android.synthetic.main.activity_video_edit_player.*
import android.media.MediaPlayer
import android.view.Surface
import android.net.Uri
import android.widget.Toast
import java.io.IOException
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat


class VideoEditPlayer : AppCompatActivity(),TextureView.SurfaceTextureListener{
    private var mediaPlayer: MediaPlayer? = null
    private val rotateVideo= arrayOf(90,180,270,0)
    private var counter=0
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        Toast.makeText(this,"Please wait..........",Toast.LENGTH_LONG).show()
        val surface = Surface(surface)
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.setDataSource(this , Uri.parse("https://www.radiantmediaplayer.com/media/bbb-360p.mp4"))
            mediaPlayer?.setSurface(surface)
            mediaPlayer?.isLooping = true
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener(MediaPlayer.OnPreparedListener { mediaPlayer -> mediaPlayer.start() })
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit_player)
        video_player.surfaceTextureListener=this
        rotate.setOnClickListener {
            video_player.rotation=rotateVideo[counter++].toFloat()
            if(counter>3){
                counter=0
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mediaPlayer != null){
            mediaPlayer?.stop();
            mediaPlayer?.release();
            mediaPlayer = null;
        }
    }
}
