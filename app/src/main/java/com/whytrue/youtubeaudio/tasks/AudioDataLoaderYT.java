package com.whytrue.youtubeaudio.tasks;

import static com.whytrue.youtubeaudio.utils.Constants.API_KEY;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.entities.Audio;
import com.whytrue.youtubeaudio.ui.adapters.AudioQueueAdapter;
import com.whytrue.youtubeaudio.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioDataLoaderYT extends AsyncTask<Void, Void, List<Audio>> {
  private static String LOG_TAG = "AudioDataLoaderYT";
  private com.google.api.services.youtube.YouTube service;
  private Context context;
  private List<String> audiosID;
  private ProgressDialog progressDialog;
  private AudioQueueAdapter adapter;
  private RecyclerView recyclerView;
  private TextView text;
  private Utils.ImageQuality imageQuality;

  public AudioDataLoaderYT(Context context, GoogleAccountCredential credential, List<String> audiosID, ProgressDialog progressDialog,
                           AudioQueueAdapter adapter, RecyclerView recyclerView, TextView text, Utils.ImageQuality imageQuality) {
    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    this.service = new com.google.api.services.youtube.YouTube.Builder(
            transport, jsonFactory, credential)
            .setApplicationName("YouTube Audio")
            .build();
    this.context = context;
    this.audiosID = audiosID;
    this.progressDialog = progressDialog;
    this.adapter = adapter;
    this.recyclerView = recyclerView;
    this.text = text;
    this.imageQuality = imageQuality;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    progressDialog.show();
  }

  @Override
  protected List<Audio> doInBackground(Void... params) {
    try {
      return getDataFromApi();
    }
    catch (Exception e) {
      e.printStackTrace();
      cancel(true);
    }
    return null;
  }

  @Override
  protected void onPostExecute(List<Audio> output) {
    super.onPostExecute(output);
    if (!output.isEmpty()) {
      adapter.setAudioItems(output);
      recyclerView.setAdapter(adapter);
    }
    else if (text != null) {
      text.setText(context.getResources().getString(R.string.empty_playlist));
      adapter.setAudioItems(output);
      recyclerView.setAdapter(adapter);
    }
    progressDialog.dismiss();
  }

  @Override
  protected void onCancelled() {
    super.onCancelled();
    progressDialog.dismiss();
    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
  }

  List<Audio> getDataFromApi() throws IOException {
    List<Audio> audiosInfo = new ArrayList<>();

    StringBuilder IDs = new StringBuilder();
    for (String audio : audiosID) {
      IDs.append(audio).append(",");
    }
    IDs.deleteCharAt(IDs.length() - 1);

    List<Video> resultsInfo = service.videos().list("snippet,statistics,contentDetails")
            .setKey(API_KEY)
            .setId(IDs.toString())
            .execute()
            .getItems();

    for (Video info : resultsInfo) {
      Thumbnail image = Utils.imageLoad(info.getSnippet().getThumbnails(), imageQuality);

      Audio audioInfo = new Audio(
              info.getId(),
              info.getSnippet().getTitle(),
              info.getSnippet().getChannelTitle(),
              info.getStatistics().getViewCount(),
              info.getSnippet().getPublishedAt().getValue(),
              image.getUrl(),
              image.getWidth(),
              image.getHeight(),
              Utils.youtubeDurationFormatting(info.getContentDetails().getDuration()));
      audiosInfo.add(audioInfo);
    }
    return audiosInfo;
  }
}
