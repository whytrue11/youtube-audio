package com.whytrue.youtubeaudio.ui.activities;

import static com.whytrue.youtubeaudio.utils.Constants.HEIGHT_RATIO;
import static com.whytrue.youtubeaudio.utils.Constants.WIDTH_RATIO;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;
import com.whytrue.youtubeaudio.AudioQueue;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.callbacks.ChangeCurAudioListener;
import com.whytrue.youtubeaudio.callbacks.PausedListener;
import com.whytrue.youtubeaudio.callbacks.PlayingListener;
import com.whytrue.youtubeaudio.entities.Audio;
import com.whytrue.youtubeaudio.services.MusicService;
import com.whytrue.youtubeaudio.utils.playlist.PlaylistLoader;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
  private static final String LOG_TAG = "MainActivity";

  //Music Service
  private MusicService musicService;
  private ServiceConnection musicServiceConnection;
  private Intent musicServiceIntent;
  private boolean musicServiceBound;

  public MusicService getMusicService() {
    return musicService;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Log.i(LOG_TAG, "Create");

    BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.sheet));
    bottomSheetBehavior.setHideable(false);
    bottomSheetBehavior.setPeekHeight(200);
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

    initBottomNavigation();
    initYoutubeDl();
    initPlaylists();
    initMusicService();

    findViewById(R.id.player_bar).setVisibility(View.GONE);
    findViewById(R.id.player_bar).setOnClickListener(v ->
            showBottomDialog());
  }

  @Override
  public void onStop() {
    super.onStop();
    if (!musicServiceBound) return;
    unbindService(musicServiceConnection);
    musicServiceBound = false;
  }

  private void initBottomNavigation() {
    BottomNavigationView navView = findViewById(R.id.bottom_navigation);
    NavController navController = ((NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.fragment_container)).getNavController();
    NavigationUI.setupWithNavController(navView, navController);
  }

  private void initYoutubeDl() {
    try {
      YoutubeDL.getInstance().init(this);
    }
    catch (YoutubeDLException e) {
      Log.e(LOG_TAG, "Failed to initialize youtube-dl");
      e.printStackTrace();
    }
  }

  private void initPlaylists() {
    try {
      PlaylistLoader.getInstance(getFilesDir().getPath());
    }
    catch (IOException e) {
      Log.e(LOG_TAG,"IOException: " + e.getCause().toString());
      e.printStackTrace();
    }
  }

  private void initMusicService() {
    musicServiceConnection = new ServiceConnection() {
      public void onServiceConnected(ComponentName name, IBinder binder) {
        Log.i(LOG_TAG, "On music service connected");
        musicService = ((MusicService.MusicServiceBinder) binder).getService();

        musicService.addPlayingListener(new PlayingListener() {
          @Override
          public void onPlaying(MediaPlayer mediaPlayer, AudioQueue queue) {
            startPlayerBar(mediaPlayer, queue);
          }
        });

        musicService.addPausedListener(new PausedListener() {
          @Override
          public void onPaused() {
            ImageButton playButton = findViewById(R.id.player_bar_play_button_id);
            playButton.setImageResource(R.drawable.round_play_arrow_24);
          }
        });

        musicService.addChangeListener(new ChangeCurAudioListener() {
          @Override
          public void onChange(AudioQueue queue) {
            updatePlayerBarData(queue);
          }
        });

        ImageButton playPlayerBarButton = findViewById(R.id.player_bar_play_button_id);
        ImageButton prevPlayerBarButton = findViewById(R.id.player_bar_prev_button_id);
        ImageButton nextPlayerBarButton = findViewById(R.id.player_bar_next_button_id);

        playPlayerBarButton.setOnClickListener(v -> musicService.playOrPause());
        prevPlayerBarButton.setOnClickListener(v -> musicService.playPrev());
        nextPlayerBarButton.setOnClickListener(v -> musicService.playNext());

        musicServiceBound = true;
      }

      public void onServiceDisconnected(ComponentName name) {
        Log.i(LOG_TAG, "On music service disconnected");
        musicServiceBound = false;
      }
    };
    musicServiceIntent = new Intent(this, MusicService.class);
    bindService(musicServiceIntent, musicServiceConnection, BIND_AUTO_CREATE);
  }

  private void startPlayerBar(MediaPlayer mediaPlayer, AudioQueue queue) {
    ImageButton playButton = findViewById(R.id.player_bar_play_button_id);
    playButton.setImageResource(R.drawable.baseline_pause_24);
    LinearProgressIndicator progressIndicator = findViewById(R.id.player_bar_progress_id);
    progressIndicator.setMax(mediaPlayer.getDuration());

    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!mediaPlayer.isPlaying()) return;

        progressIndicator.setProgress(mediaPlayer.getCurrentPosition());
        new Handler().postDelayed(this, 500);
      }
    });
    if (findViewById(R.id.player_bar).getVisibility() == View.VISIBLE) {
      return;
    }

    findViewById(R.id.player_bar).setVisibility(View.VISIBLE);
    updatePlayerBarData(queue);
  }

  private void updatePlayerBarData(AudioQueue queue) {
    LinearProgressIndicator progressIndicator = findViewById(R.id.player_bar_progress_id);
    TextView title = findViewById(R.id.player_bar_audio_title_id);
    TextView channel = findViewById(R.id.player_bar_channel_id);
    ImageView image = findViewById(R.id.player_bar_image_id);

    Audio curAudio = queue.getCurrentAudio();
    title.setText(curAudio.getTitle());
    channel.setText(curAudio.getChannel());
    progressIndicator.setProgress(0);
    Picasso.get()
            .load(curAudio.getImageURI())
            .centerCrop()
            .resize(Math.toIntExact(curAudio.getWidth()),
                    (int) ((float) curAudio.getWidth() * HEIGHT_RATIO / WIDTH_RATIO))
            .placeholder(R.drawable.outline_music_note_24)
            .noFade()
            .into(image);
  }

  private void showBottomDialog() {
    BottomSheetDialog dialog = new BottomSheetDialog(this);
    dialog.setContentView(R.layout.player_bottom_sheet);

    dialog.show();
  }
}
