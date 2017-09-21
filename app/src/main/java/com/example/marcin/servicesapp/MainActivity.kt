package com.example.marcin.servicesapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var musicPlayerInstance : MusicPlayer? = null
    private var isMusicPlayerInstanceReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setListeners(){
        startButton.setOnClickListener {
            val intent = Intent(this, MusicPlayer::class.java)
            applicationContext.startService(intent)
            applicationContext.bindService(intent, initServiceConnection(), Context.BIND_AUTO_CREATE )
        }

        stopButton.setOnClickListener {
            musicPlayerInstance?.stopPlayingMusic()
        }
    }

    private fun startPlayingMusic(){
        val musicProvider = MusicProvider()
        val songList = musicProvider.retrieveDeviceSongList(baseContext)
        textView.text = songList[0].title
        musicPlayerInstance?.prepareMediaPlayer(songList)
    }

    private fun initServiceConnection() : ServiceConnection{
        return object : ServiceConnection{
            override fun onServiceDisconnected(p0: ComponentName?) {
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
                musicPlayerInstance = (binder as MusicPlayer.BinderMusicPlayer).getService() as MusicPlayer
                isMusicPlayerInstanceReady = true
                startPlayingMusic()
            }
        }
    }

}
