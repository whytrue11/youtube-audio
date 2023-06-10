package com.whytrue.youtubeaudio.callbacks;

import com.whytrue.youtubeaudio.entities.Audio;

import java.util.List;

public interface ClickAudioListener {
  void onClick(List<Audio> audios, int pos);
}
