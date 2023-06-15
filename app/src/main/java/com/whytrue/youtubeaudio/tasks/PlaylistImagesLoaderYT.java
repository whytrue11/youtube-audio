package com.whytrue.youtubeaudio.tasks;

import static com.whytrue.youtubeaudio.utils.Constants.API_KEY;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.whytrue.youtubeaudio.utils.Utils;
import com.whytrue.youtubeaudio.entities.PlaylistMeta;

import java.io.IOException;
import java.util.List;

public class PlaylistImagesLoaderYT extends AsyncTask<Void, Void, Void> {
  private static String LOG_TAG = "PlaylistImagesLoaderYT";
  private com.google.api.services.youtube.YouTube service;
  private List<PlaylistMeta> playlistsMeta;
  private Utils.ImageQuality imageQuality;

  public PlaylistImagesLoaderYT(GoogleAccountCredential credential, List<PlaylistMeta> playlistsMeta,
                                Utils.ImageQuality imageQuality) {
    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    this.service = new com.google.api.services.youtube.YouTube.Builder(
            transport, jsonFactory, credential)
            .setApplicationName("YouTube Audio")
            .build();
    this.playlistsMeta = playlistsMeta;
    this.imageQuality = imageQuality;
  }

  @Override
  protected Void doInBackground(Void... params) {
    try {
      getDataFromApi();
    }
    catch (Exception e) {
      e.printStackTrace();
      cancel(true);
    }
    return null;
  }

  @Override
  protected void onPostExecute(Void unused) {
    super.onPostExecute(unused);
    Log.i(LOG_TAG, "Images load finish");
  }

  void getDataFromApi() throws IOException {
    Log.i(LOG_TAG, "Start load images");

    StringBuilder IDs = new StringBuilder();
    for (PlaylistMeta meta: playlistsMeta) {
      IDs.append(meta.getFirstAudioId() == null ? "": meta.getFirstAudioId()).append(",");
    }
    IDs.deleteCharAt(IDs.length() - 1);

    List<Video> resultsInfo = service.videos().list("snippet")
            .setKey(API_KEY)
            .setId(IDs.toString())
            .execute()
            .getItems();

    int i = 0;
    for (Video info : resultsInfo) {
      Thumbnail image = Utils.imageLoad(info.getSnippet().getThumbnails(), imageQuality);
      PlaylistMeta temp = playlistsMeta.get(i++);
      temp.setImageURI(image.getUrl());
      temp.setWidth(image.getWidth());
      temp.setHeight(image.getHeight());
    }
  }
}
