package com.whytrue.youtubeaudio.callbacks;

import android.media.MediaPlayer;

import com.whytrue.youtubeaudio.AudioQueue;

public interface RewindListener {
  void onRewinding(MediaPlayer mediaPlayer);
}
