package com.example.marcin.servicesapp

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import java.io.IOException


class MusicPlayer() : Service(), MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private val START_ACTION = "com.example.marcin.servicesapp.startmusic"
    private val STOP_ACTION = "com.example.marcin.servicesapp.stopmusic"
    private val NOTIFICATION_ID = 111

    lateinit var mediaPlayer : MediaPlayer

    private var songList : List<Song>? = null

    inner class BinderMusicPlayer() : Binder() {
        fun getService() : Service = this@MusicPlayer
    }

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
    }

    override fun onBind(p0: Intent?): IBinder = BinderMusicPlayer()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            STOP_ACTION -> {
                stopPlayingMusic()
                startForeground(NOTIFICATION_ID,
                        getNotificationWithStartButton(songList!![0].artist, songList!![0].title))
            }
            START_ACTION -> {
                startPlayingMusic()
                startForeground(NOTIFICATION_ID,
                        getNotificationWithStopButton(songList!![0].artist, songList!![0].title))
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun prepareMediaPlayer(songs: List<Song>) {
        songList = songs
        mediaPlayer.reset()
        val currentlyPlayedSong = songList!![0]
        val currentSongId = currentlyPlayedSong.id
        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongId)
        try {
            mediaPlayer.setDataSource(applicationContext, trackUri)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        mediaPlayer.prepareAsync()
        startForeground(NOTIFICATION_ID,
                getNotificationWithStopButton(songList!![0].artist, songList!![0].title))
    }

    fun stopPlayingMusic(){
        mediaPlayer.pause()
    }

    fun startPlayingMusic(){
        mediaPlayer.start()
    }

    private fun initMediaPlayer(){
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicPlayer)
            setOnErrorListener(this@MusicPlayer)
            setOnCompletionListener(this@MusicPlayer)
        }
    }

    private fun getNotificationWithStopButton(title: String, text: String): Notification = Notification.Builder(this)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle(title)
            .setContentText(text)
            .addAction(android.R.drawable.ic_media_pause, "Pause",
                    getPendingIntent(applicationContext, STOP_ACTION))
            .build()

    private fun getNotificationWithStartButton(title: String, text: String): Notification = Notification.Builder(this)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentTitle(title)
            .setContentText(text)
            .addAction(android.R.drawable.ic_media_play, "Play",
                    getPendingIntent(applicationContext, START_ACTION))
            .build()


    private fun getPendingIntent(context: Context, action: String): PendingIntent{
        val intent = Intent(context, MusicPlayer::class.java)
        when(action) {
            START_ACTION -> {
                intent.action = START_ACTION
                return PendingIntent.getService(context, 0, intent, 10)
            }
            STOP_ACTION -> {
                intent.action = STOP_ACTION
                return PendingIntent.getService(context, 0, intent, 10)
            }
        }
        return PendingIntent.getService(context, 0, intent, 0)
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) = Unit

    override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int) = false

    override fun onPrepared(mediaPlayer: MediaPlayer) = mediaPlayer.start()
}