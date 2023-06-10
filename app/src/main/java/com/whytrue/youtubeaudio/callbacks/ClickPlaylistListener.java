package com.whytrue.youtubeaudio.callbacks;

import com.whytrue.youtubeaudio.entities.Audio;
import com.whytrue.youtubeaudio.entities.PlaylistMeta;

import java.util.List;

public interface ClickPlaylistListener {
  void onClick(List<PlaylistMeta> playlists, int pos);
}
