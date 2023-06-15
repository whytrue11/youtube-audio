package com.whytrue.youtubeaudio.callbacks;

import android.media.MediaPlayer;

import com.whytrue.youtubeaudio.AudioQueue;

public interface PlayingListener {
  void onPlaying(MediaPlayer mediaPlayer, AudioQueue queue);
}
