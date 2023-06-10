package com.whytrue.youtubeaudio.callbacks;

import android.media.MediaPlayer;

import com.whytrue.youtubeaudio.AudioQueue;

public interface PlayingListener {
  /*default void changeButtonImage(ImageButton button) {
    button.setImageResource(R.drawable.baseline_pause_24);
  };*/
  //void drawTimeline();
  void onPlaying(MediaPlayer mediaPlayer, AudioQueue queue);
}
