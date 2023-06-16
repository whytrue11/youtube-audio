package com.whytrue.youtubeaudio.callbacks;

import android.media.MediaPlayer;

import androidx.media3.exoplayer.ExoPlayer;

import com.whytrue.youtubeaudio.AudioQueue;

public interface PlayingListener {
  void onPlaying(ExoPlayer mediaPlayer, AudioQueue queue);
}
