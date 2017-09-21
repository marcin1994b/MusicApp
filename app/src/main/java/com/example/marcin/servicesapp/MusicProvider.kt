package com.example.marcin.servicesapp

import android.content.Context
import android.provider.BaseColumns._ID
import android.provider.MediaStore.Audio.AudioColumns.*
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns.TITLE
import java.util.*

/**
 * Created by Marcin on 21.09.2017.
 */
class MusicProvider {

    fun retrieveDeviceSongList(context: Context) : List<Song> {
        var songList = mutableListOf<Song>()
        val musicResolver = context.contentResolver
        val musicCursor = musicResolver.query(EXTERNAL_CONTENT_URI, null, null, null, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val titleColumn = musicCursor.getColumnIndex(TITLE)
            val idColumn = musicCursor.getColumnIndex(_ID)
            val artistColumn = musicCursor.getColumnIndex(ARTIST)
            val albumCoverColumn = musicCursor.getColumnIndex(ALBUM_ID)
            val albumNameColumn = musicCursor.getColumnIndex(ALBUM)
            val durationColumn = musicCursor.getColumnIndex(DURATION)
            do {
                val thisId = musicCursor.getLong(idColumn)
                val songTitle = musicCursor.getString(titleColumn)
                val songArtist = musicCursor.getString(artistColumn)
                val songAlbum = musicCursor.getString(albumNameColumn)
                val songDuration = musicCursor.getLong(durationColumn)
                songList.add(Song(thisId, songTitle, songArtist, songAlbum, songDuration.toInt()))
            } while (musicCursor.moveToNext())
        }
        musicCursor?.close()
        Collections.sort<Song>(songList) { lhs, rhs -> lhs.title.compareTo(rhs.title) }
        return songList
    }
}