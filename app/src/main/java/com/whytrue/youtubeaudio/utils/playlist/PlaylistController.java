package com.whytrue.youtubeaudio.utils.playlist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.entities.Audio;
import com.whytrue.youtubeaudio.entities.PlaylistMeta;
import com.whytrue.youtubeaudio.services.MusicService;
import com.whytrue.youtubeaudio.tasks.AudioDataLoaderYT;
import com.whytrue.youtubeaudio.ui.adapters.AudioQueueAdapter;
import com.whytrue.youtubeaudio.ui.adapters.PlaylistMetaAdapter;
import com.whytrue.youtubeaudio.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;

public class PlaylistController {
  public static void loadPlaylistData(Context context, GoogleAccountCredential credential,
                                      String playlistName, ProgressDialog progressDialog,
                                      AudioQueueAdapter queueAdapter, RecyclerView recyclerView, TextView text) throws IOException {
    PlaylistLoader playlistLoader = null;
    try {
      playlistLoader = PlaylistLoader.getInstance(context.getFilesDir().getPath());
    }
    catch (IOException e) {
      e.printStackTrace();
      Toast.makeText(context,
              context.getResources().getString(R.string.error),
              Toast.LENGTH_SHORT).show();
      return;
    }

    new AudioDataLoaderYT(context, credential, playlistLoader.getPlaylist(playlistName),
            progressDialog, queueAdapter, recyclerView, text, Constants.QUEUE_IMAGE_QUALITY)
            .execute();
  }

  public static void showAddPlaylistDialog(Context context, PlaylistMetaAdapter adapter, TextView text) {
    View view = LayoutInflater.from(context).inflate(R.layout.dialog_playlist_add, null);
    AlertDialog dialog = new AlertDialog.Builder(context).create();
    dialog.setView(view);

    view.findViewById(R.id.playlist_add_dialog_ok).setOnClickListener(v ->
            readNameAndAddPlaylist(view, context, dialog, adapter, text));

    dialog.show();
  }

  public static void showChoosePlaylistDialog(Context context, Audio audio) {
    //TODO: (опционально) отображать определённое кол-во элементов
    PlaylistLoader playlistLoader = null;
    try {
      playlistLoader = PlaylistLoader.getInstance(context.getFilesDir().getPath());
    }
    catch (IOException e) {
      e.printStackTrace();
      Toast.makeText(context,
              context.getResources().getString(R.string.error),
              Toast.LENGTH_SHORT).show();
      return;
    }
    String[] names = playlistLoader
            .getPlaylistsMeta().stream().map(PlaylistMeta::getName)
            .toArray(String[]::new);

    if (names.length == 0) {
      Toast.makeText(context, R.string.first_need_to_create_playlist, Toast.LENGTH_SHORT).show();
      return;
    }

    View view = LayoutInflater.from(context).inflate(R.layout.dialog_playlist_choose, null);
    androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
    dialog.setView(view);

    PlaylistLoader finalPlaylistLoader = playlistLoader; //for lambda
    ListView playlistNames = view.findViewById(R.id.playlist_choose_list_id);
    playlistNames.setOnItemClickListener((parent, v, position, id) ->
            addAudioInPlaylist(position, finalPlaylistLoader, names, audio, context, dialog));

    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, names);
    playlistNames.setAdapter(adapter);

    dialog.show();
  }

  public static void showAudioOptionBottomDialog(Context context, Audio audio, MusicService musicService) {
    BottomSheetDialog dialog = new BottomSheetDialog(context);
    dialog.setContentView(R.layout.bottom_sheet_audio_option);

    LinearLayout playNext = dialog.findViewById(R.id.main_bottom_sheet_playnext_id);
    LinearLayout addToEnd = dialog.findViewById(R.id.main_bottom_sheet_addtoend_id);
    LinearLayout addToPlaylist = dialog.findViewById(R.id.main_bottom_sheet_addtoplaylist_id);

    playNext.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
        musicService.addNextAudio(audio);
      }
    });

    addToEnd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
        musicService.addAudio(audio);
      }
    });

    addToPlaylist.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
        PlaylistController.showChoosePlaylistDialog(context, audio);
      }
    });

    dialog.show();
  }

  private static void addAudioInPlaylist(int position, PlaylistLoader playlistLoader, String[] names, Audio audio, Context context, androidx.appcompat.app.AlertDialog dialog) {
    try {
      playlistLoader.addAudio(names[position], audio);
    }
    catch (IOException e) {
      e.printStackTrace();
      Toast.makeText(context,
              context.getResources().getString(R.string.error),
              Toast.LENGTH_SHORT).show();
    }
    dialog.dismiss();
  }

  private static void readNameAndAddPlaylist(View view, Context context, AlertDialog dialog, PlaylistMetaAdapter adapter, TextView text) {
    EditText editText = view.findViewById(R.id.playlist_add_dialog_name_id);
    String playlistName = editText.getText().toString();

    if (playlistName.isEmpty()) {
      Toast.makeText(context, R.string.empty_name_not_allowed, Toast.LENGTH_SHORT).show();
      dialog.dismiss();
      return;
    }

    try {
      PlaylistLoader playlistLoader = PlaylistLoader.getInstance(context.getFilesDir().getPath());
      if (playlistLoader.getPlaylistsMeta().stream().anyMatch(meta -> meta.getName().equals(playlistName))) {
        Toast.makeText(context,
                R.string.playlist_exist,
                Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        return;
      }

      PlaylistMeta newPlaylist = new PlaylistMeta(0, playlistName, null, null, 1L, 1L);
      playlistLoader.getPlaylistsMeta().add(newPlaylist);
      playlistLoader.savePlaylist(playlistName, new ArrayList<>(1));

      adapter.addPlaylistItem(newPlaylist);
      text.setText("");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    Toast.makeText(context,
            context.getResources().getString(R.string.playlist_and_mark)
                    + playlistName
                    + context.getResources().getString(R.string.mark_and_added),
            Toast.LENGTH_SHORT).show();
    dialog.dismiss();
  }
}
