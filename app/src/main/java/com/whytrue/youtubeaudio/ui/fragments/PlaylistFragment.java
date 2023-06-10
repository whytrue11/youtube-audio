package com.whytrue.youtubeaudio.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.entities.PlaylistMeta;
import com.whytrue.youtubeaudio.services.MusicService;
import com.whytrue.youtubeaudio.ui.activities.MainActivity;
import com.whytrue.youtubeaudio.ui.adapters.AudioQueueAdapter;
import com.whytrue.youtubeaudio.ui.adapters.PlaylistMetaAdapter;
import com.whytrue.youtubeaudio.utils.playlist.PlaylistController;
import com.whytrue.youtubeaudio.utils.playlist.PlaylistLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {
  private ProgressDialog progressDialog;
  private RecyclerView recyclerView;
  private PlaylistMetaAdapter playlistAdapter;
  private AudioQueueAdapter queueAdapter;
  private MusicService musicService;
  private ActionBar actionBar;
  private CharSequence prevTitle;

  public PlaylistFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    progressDialog = new ProgressDialog(getContext());
    progressDialog.setCancelable(false);
    progressDialog.setMessage("Calling YouTube Data API ...");

    actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    actionBar.setHomeAsUpIndicator(R.drawable.round_arrow_back_24);
    setHasOptionsMenu(true);

    //Show action bar logo
    /*actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayUseLogoEnabled(true);
    actionBar.setLogo(R.drawable.baseline_music_video_24);*/

    //actionBar.setHomeButtonEnabled(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_playlist, container, false);

    initRecyclerView(view);

    Button addPlaylistButton = view.findViewById(R.id.playlist_add_playlist);
    addPlaylistButton.setOnClickListener(v -> PlaylistController.
            showAddPlaylistDialog(getContext(), playlistAdapter));

    return view;
  }

  @Override
  public void onPause() {
    super.onPause();
    returnBack();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case (android.R.id.home):
        returnBack();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  private void returnBack() {
    recyclerView.setAdapter(playlistAdapter);
    if (prevTitle != null) actionBar.setTitle(prevTitle);
    actionBar.setDisplayHomeAsUpEnabled(false);
  }

  private void initRecyclerView(View view) {
    recyclerView = view.findViewById(R.id.playlist_list);

    if (playlistAdapter == null || playlistAdapter.getItemCount() == 0) {
      playlistAdapter = new PlaylistMetaAdapter(new ArrayList<>(),
              (playlists, playlistPos) -> showPlaylistAudios(playlists, playlistPos));
      recyclerView.setAdapter(playlistAdapter);
    }
    updatePlaylistAdapterData();

    if (playlistAdapter.getItemCount() == 0) {
      TextView text = view.findViewById(R.id.playlist_error_text_id);
      text.setText(R.string.playlist_absence);
    }
  }

  private void showPlaylistAudios(List<PlaylistMeta> playlists, int playlistPos) {
    queueAdapter = new AudioQueueAdapter(new ArrayList<>(), this::getMusicService,
            (audios, audioPos) -> {
              getMusicService().replacePlaylist(audios);
              getMusicService().goToAudio(audios.get(audioPos));
            }
    );
    String playlistName = playlists.get(playlistPos).getName();
    try {
      PlaylistController.loadPlaylistData(getContext(), null,
              playlistName, progressDialog, queueAdapter, recyclerView);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    prevTitle = actionBar.getTitle();
    actionBar.setTitle(playlistName);
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  private void updatePlaylistAdapterData() {
    //TODO: добавить лист (addAll)
    try {
      List<PlaylistMeta> playlistsMeta = PlaylistLoader.getInstance(getContext().getFilesDir().getPath()).getPlaylistsMeta();
      for (int i = playlistAdapter.getItemCount(); i < playlistsMeta.size(); i++) {
        playlistAdapter.addPlaylistItem(playlistsMeta.get(i));
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private MusicService getMusicService() {
    return musicService = musicService == null ? ((MainActivity) getActivity()).getMusicService() : musicService;
  }
}