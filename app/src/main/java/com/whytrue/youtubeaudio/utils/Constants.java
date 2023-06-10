package com.whytrue.youtubeaudio.utils;

import com.whytrue.youtubeaudio.ui.activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {
  public static final int WIDTH_RATIO = 16;
  public static final int HEIGHT_RATIO = 9;
  public static final String START_YOUTUBE_URL = "https://youtu.be/";

  public static String API_KEY;

  private static final String PROPERTIES_FILENAME = "youtube.properties";
  static {
    Properties properties = new Properties();
    try (InputStream in = MainActivity.class.getResourceAsStream("/" + PROPERTIES_FILENAME)) {
      properties.load(in);

      API_KEY = properties.getProperty("youtube.apikey");
      assert API_KEY != null : "Invalid property name - API key";
    }
    catch (IOException | NullPointerException e) {
      System.err.println("There was an error reading '" + PROPERTIES_FILENAME + "': " + e.getCause()
              + " : " + e.getMessage());
      System.exit(1);
    }
  }
}
