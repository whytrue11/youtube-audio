package com.whytrue.youtubeaudio.utils.playlist;

import com.whytrue.youtubeaudio.entities.Audio;
import com.whytrue.youtubeaudio.entities.PlaylistMeta;
import com.whytrue.youtubeaudio.tasks.PlaylistImagesLoaderYT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PlaylistLoader {
  private static final String PREFIX = "playlist_";
  private static final String CUR_PLAYLIST_FILENAME = "cur_playlist";
  private static final String EMPTY_STRING = "empty";
  private static PlaylistLoader instance;
  private final String path;
  private List<PlaylistMeta> playlistsMeta;
  private List<String> curPlaylist;


  private PlaylistLoader(String path) throws IOException {
    this.path = path;

    extractPlaylistsMeta();
    extractCurPlaylist();
  }

  public static PlaylistLoader getInstance(String path) throws IOException {
    if (instance == null) {
      instance = new PlaylistLoader(path);
    }
    return instance;
  }

  public List<PlaylistMeta> getPlaylistsMeta() {
    return playlistsMeta;
  }

  public List<String> getCurPlaylist() {
    return curPlaylist;
  }

  public List<String> extractPlaylistByName(String name) throws IOException {
    return extractPlaylist(path + "/" + PREFIX + name);
  }

  public void saveCurPlaylist() throws IOException {
    savePlaylist(curPlaylist, path + "/" + CUR_PLAYLIST_FILENAME);
  }

  public void savePlaylist(String name, List<String> playlist) throws IOException {
    savePlaylist(playlist, path + "/" + PREFIX + name);
  }

  public void addAudio(String playlistName, Audio audio) throws IOException {
    for (PlaylistMeta meta: playlistsMeta) {
      if (meta.getName().equals(playlistName)) {
        meta.setCount(meta.getCount() + 1);

        if (meta.getCount() == 1) {
          meta.setImageURI(audio.getImageURI());
          meta.setWidth(audio.getWidth());
          meta.setHeight(audio.getHeight());
        }
        break;
      }
    }

    File tempFile = new File(path + "/temp");
    File inputFile = new File(path + "/" + PREFIX + playlistName);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
         BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
      String temp;

      //count
      writer.write(Integer.toString(Integer.parseInt(reader.readLine()) + 1));
      writer.newLine();

      //first ID
      temp = reader.readLine();
      if (temp.equals(EMPTY_STRING)) {
        writer.write(audio.getId());
      }
      else {
        writer.write(temp);
      }
      writer.newLine();

      //playlist
      temp = reader.readLine();
      if (temp.equals(EMPTY_STRING)) {
        writer.write(audio.getId());
      }
      else {
        writer.write(temp);
        writer.write("," + audio.getId());
      }
    }
    inputFile.delete();
    tempFile.renameTo(inputFile);
  }

  private void extractPlaylistsMeta() throws IOException {
    File dir = new File(path);
    File[] files = dir.listFiles();
    playlistsMeta = new ArrayList<>(files.length);
    String fileName = null;
    for (File file : files) {
      fileName = file.getName();
      if (!fileName.startsWith(PREFIX)) {
        continue;
      }
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        PlaylistMeta meta = new PlaylistMeta(
                Integer.parseInt(reader.readLine()),
                fileName.replace(PREFIX, ""),
                reader.readLine(),
                null, 1L, 1L);
        playlistsMeta.add(meta);
      }
    }
    new PlaylistImagesLoaderYT(null, playlistsMeta).execute();
  }

  private void extractCurPlaylist() throws IOException {
    File curPlaylistFile = new File(path + "/" + CUR_PLAYLIST_FILENAME);
    if (curPlaylistFile.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
        curPlaylist = Arrays.asList(reader.readLine().split(","));
      }
    }
  }

  private static List<String> extractPlaylist(String path) throws IOException {
    List<String> playlist;
    try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
      reader.readLine(); //count
      reader.readLine(); //first ID

      String line;
      playlist = (line = reader.readLine()) != null ? Arrays.asList(line.split(",")) : new ArrayList<>();
    }
    return playlist;
  }

  private static void savePlaylist(List<String> playlist, String path) throws IOException {
    File file = new File(path);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write(Integer.toString(playlist.size()));
      writer.newLine();
      writer.write(playlist.isEmpty() ? EMPTY_STRING : playlist.get(0));
      writer.newLine();
      for (String audioId : playlist) {
        writer.write(audioId + ",");
      }

      if (playlist.isEmpty()) {
        writer.write(EMPTY_STRING);
      }
    }
  }
}
