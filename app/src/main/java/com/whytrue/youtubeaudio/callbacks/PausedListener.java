package com.whytrue.youtubeaudio.callbacks;

public interface PausedListener {
  /*default void changeButtonImage(ImageButton button) {
    button.setImageResource(R.drawable.round_play_arrow_24);
  };*/
  void onPaused();
}
