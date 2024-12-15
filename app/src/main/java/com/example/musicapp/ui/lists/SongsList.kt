package com.example.musicapp.ui.lists

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.musicapp.R
import com.example.musicapp.logic.mediaPlayer.AppExoPlayer
import com.example.musicapp.logic.song.Song
import com.example.musicapp.logic.song.getSongs
import com.example.musicapp.logic.song.getSongsFromDatabaseWithUri

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SongsList(
    listUri: List<String>,
    searchText: MutableState<String>,
    //album list is needed to get images to notification
    albumsList: List<Any>,
){
    val context = LocalContext.current

    val discList = remember { mutableStateListOf<MutableList<Song>>()}

    LaunchedEffect(listUri) {
        if (listUri.isNotEmpty()){
            for (i in 0..< listUri.count()){
                discList.add(mutableStateListOf())
                discList[i].addAll(getSongsFromDatabaseWithUri(context, listUri[i].toUri()))
                discList[i].sortBy { song -> song.number }
            }
            for (i in 0..< listUri.count()){
                val songsFromDirectory: SnapshotStateList<Song> = mutableStateListOf()
                getSongs(
                    uri = listUri[i].toUri(),
                    context = context,
                    songsList = songsFromDirectory
                ).await()
                discList[i].clear()
                discList[i].addAll(songsFromDirectory)
                discList[i].sortBy { song -> song.number }
            }
        }
    }

    val filteredDisc = discList.map { disc ->
        disc.filter { song: Song ->
            song.title!!.contains(searchText.value, ignoreCase = true)
        }.sortedBy { it.number }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        for (disc in filteredDisc){
            InfoHeader(
                songsList = disc,
                discIndex = filteredDisc.indexOf(disc)
            )
            HeaderOfDisc()
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surface,
                thickness = 2.dp
            )
            for (song in disc){
                SongItem(
                    song = song,
                    songsList = disc,
                    albumsList = albumsList,
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale", "CoroutineCreationDuringComposition")
@Composable
private fun SongItem(
    song: Song,
    songsList: List<Song>,
    albumsList: List<Any>
){
    val actuallyPlayingSong = remember { AppExoPlayer.currentSong }
    val isPlaying = remember { mutableStateOf(false) }

    LaunchedEffect(actuallyPlayingSong.value) {
        isPlaying.value = song == AppExoPlayer.currentSong.value
    }

    val context = LocalContext.current

    val minutes = (song.length / 1000) / 60
    val seconds = (song.length / 1000) % 60

    val songDurationFormated = String.format("%d:%02d", minutes, seconds)

    Button(
        contentPadding = PaddingValues(5.dp, 0.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.tertiary,
            containerColor =
                if(isPlaying.value) MaterialTheme.colorScheme.onTertiary
                else MaterialTheme.colorScheme.background,
        ),
        onClick = {
            AppExoPlayer.setPlaylist(
                songPlaylist = songsList,
                albumsList = albumsList,
            )
            AppExoPlayer.playMusic()
            AppExoPlayer.setPlaylistToSelectedSong(song, songsList)
            },
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
        ){
            Text(
                text = song.number.toString(),
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1f)
            )
            Text(
                text = song.title ?: "",
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(9f)

            )
            Text(
                text = song.timePlayed.toString(),
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
            )
            Text(
                text = songDurationFormated,
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(3f)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun InfoHeader(
    songsList: List<Song>,
    discIndex: Int,
){
    val songsCount = songsList.count()
    var albumLength: Int = 0
    for(song in songsList){
        albumLength += song.length
    }
    val minutes = (albumLength/ 1000) / 60
    val seconds = (albumLength / 1000) % 60

    val albumLengthString = String.format("%d:%02d", minutes, seconds)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 5.dp)
    ){
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier =  Modifier
                .fillMaxWidth()
                .padding(0.dp, 4.dp)
        ) {
            Text(
                text = "Disk ${discIndex+1}",
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp, 0.dp)
            )
            Text(
                text = "Time: $albumLengthString",
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
            )
            Text(
                text = "Songs: $songsCount",
                color = MaterialTheme.colorScheme.surface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp, 0.dp)
            )
        }
    }
}

@Composable
private fun HeaderOfDisc(){
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp, 0.dp)
    ){
        Text(
            text = "#",
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1f)
        )
        Text(
            text = "Title:",
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(8f)

        )
        Text(
            text = "Play Count:",
            color = MaterialTheme.colorScheme.surface,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(7f)
        )
        Icon(
            painter = painterResource(id = R.drawable.baseline_clock_24),
            contentDescription = "Time",
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .weight(1f)
        )
    }
}