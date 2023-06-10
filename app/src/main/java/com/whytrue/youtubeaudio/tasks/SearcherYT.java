package com.whytrue.youtubeaudio.tasks;

import static com.whytrue.youtubeaudio.utils.Constants.API_KEY;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.utils.Utils;
import com.whytrue.youtubeaudio.entities.Audio;
import com.whytrue.youtubeaudio.ui.adapters.AudioHomeAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearcherYT extends AsyncTask<Void, Void, List<Audio>> {
  private static final long PAGE_SIZE = 10L;
  private static String nextPageToken;
  private static String prevQuery;
  private com.google.api.services.youtube.YouTube service;
  private Context context;
  private String searchQuery;
  private ProgressDialog progressDialog;
  private AudioHomeAdapter adapter;
  private TextView errorTextView;
  private boolean update;

  public SearcherYT(Context context, GoogleAccountCredential credential, String searchQuery, ProgressDialog progressDialog,
                    AudioHomeAdapter adapter, TextView errorTextView, boolean update) {
    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    this.service = new com.google.api.services.youtube.YouTube.Builder(
            transport, jsonFactory, credential)
            .setApplicationName("YouTube Audio")
            .build();
    this.context = context;
    this.searchQuery = searchQuery;
    this.progressDialog = progressDialog;
    this.adapter = adapter;
    this.errorTextView = errorTextView;
    this.update = update;
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
      return null;
    }
  }

  @Override
  protected void onPostExecute(List<Audio> output) {
    super.onPostExecute(output);
    if (!output.isEmpty()) {
      if (update) {
        adapter.addAudioItems(output);
      }
      else {
        adapter.setAudioItems(output);
      }
    }
    progressDialog.dismiss();
  }

  @Override
  protected void onCancelled() {
    super.onCancelled();
    progressDialog.dismiss();
    if (adapter.getItemCount() == 0) errorTextView.setText(context.getResources().getString(R.string.error));
    else Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
  }

  private List<Audio> getDataFromApi() throws IOException {
    List<Audio> audiosInfo = new ArrayList<>();

    SearchListResponse searchListResponse = null;
    if (update) {
      searchListResponse = service.search().list("id")
              .setKey(API_KEY)
              .setMaxResults(PAGE_SIZE)
              .setQ(prevQuery)
              .setPageToken(nextPageToken)
              .setType("video")
              .execute();
    }
    else {
      searchListResponse = service.search().list("id")
              .setKey(API_KEY)
              .setMaxResults(PAGE_SIZE)
              .setQ(searchQuery)
              .setType("video")
              .execute();
      prevQuery = searchQuery;
    }
    nextPageToken = searchListResponse.getNextPageToken();

    List<SearchResult> searchResults = searchListResponse.getItems();

    StringBuilder IDs = new StringBuilder();
    for (SearchResult searchResult: searchResults) {
      IDs.append(searchResult.getId().getVideoId()).append(",");
    }
    IDs.deleteCharAt(IDs.length() - 1);

    List<Video> resultsInfo = service.videos().list("snippet,statistics,contentDetails")
            .setKey(API_KEY)
            .setId(IDs.toString())
            .execute()
            .getItems();

    for (Video info : resultsInfo) {
      if (info.getContentDetails().getDuration().equals("P0D")) {
        continue;
      }

      Thumbnail image = Utils.imageLoad(info.getSnippet().getThumbnails());

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
