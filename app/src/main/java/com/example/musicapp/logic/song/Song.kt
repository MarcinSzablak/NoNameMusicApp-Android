package com.example.musicapp.logic.song

import android.net.Uri

data class Song(
    val uri: Uri,
    val parentUri: Uri,
    val title: String?,
    val author: String?,
    val format: String?,
    val number: Int,
    val length: Int,
    var timePlayed: Int = 0,
)