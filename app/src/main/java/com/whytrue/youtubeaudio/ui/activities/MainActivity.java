package com.whytrue.youtubeaudio.ui.activities;

import static com.whytrue.youtubeaudio.utils.Constants.HEIGHT_RATIO;
import static com.whytrue.youtubeaudio.utils.Constants.WIDTH_RATIO;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;
import com.whytrue.youtubeaudio.AudioQueue;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.callbacks.ChangeCurAudioListener;
import com.whytrue.youtubeaudio.callbacks.PausedListener;
import com.whytrue.youtubeaudio.callbacks.PlayingListener;
import com.whytrue.youtubeaudio.callbacks.RewindListener;
import com.whytrue.youtubeaudio.entities.Audio;
import com.whytrue.youtubeaudio.services.MusicService;
import com.whytrue.youtubeaudio.utils.Constants;
import com.whytrue.youtubeaudio.utils.playlist.PlaylistController;
import com.whytrue.youtubeaudio.utils.playlist.PlaylistLoader;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
  private static final String LOG_TAG = "MainActivity";

  //PlayerBar
  private FrameLayout playerBarView;
  private LinearLayout playerBarExtensionView;
  private BottomSheetBehavior bottomSheetBehavior;
  boolean isPlayerBarStarted = false;
  //--Fields
  ImageButton playPlayerBarButton;
  LinearProgressIndicator progressIndicator;
  TextView titlePlayerBar;
  TextView channelPlayerBar;
  ImageView imagePlayerBar;
  ImageButton extensionPlayPlayerBarButton;
  SeekBar extensionProgressIndicator;
  TextView extensionAllTime;
  TextView extensionCutTime;
  TextView extensionTitle;
  TextView extensionChannel;

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

    initPlayerBar();
    initBottomNavigation();
    initYoutubeDl();
    initPlaylists();
    initMusicService();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (!musicServiceBound) return;
    unbindService(musicServiceConnection);
    musicServiceBound = false;
  }

  private void initPlayerBar() {
    playerBarView = findViewById(R.id.player_bar_view);
    playerBarExtensionView = findViewById(R.id.player_bar_extension_view);

    playPlayerBarButton = findViewById(R.id.player_bar_play_button);
    progressIndicator = findViewById(R.id.player_bar_progress);
    titlePlayerBar = findViewById(R.id.player_bar_audio_title);
    channelPlayerBar = findViewById(R.id.player_bar_channel);
    imagePlayerBar = findViewById(R.id.player_bar_image);

    extensionPlayPlayerBarButton = findViewById(R.id.player_bar_extension_play_button);
    extensionProgressIndicator = findViewById(R.id.player_bar_extension_progress);
    extensionAllTime = findViewById(R.id.player_bar_extension_alltime);
    extensionCutTime = findViewById(R.id.player_bar_extension_curtime);
    extensionTitle = findViewById(R.id.player_bar_extension_audio_title);
    extensionChannel = findViewById(R.id.player_bar_extension_channel);

    playerBarView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
      }
    });
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      playerBarView.setRenderEffect(RenderEffect.createBlurEffect(
              50, 50, Shader.TileMode.CLAMP));
    }
    else {
      playerBarView.setAlpha(0.5f);
    }

    playerBarExtensionView.setAlpha(0);

    bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.slide_up_panel));
    bottomSheetBehavior.setPeekHeight((int) getResources().getDimension(R.dimen.player_bar_height));
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    bottomSheetBehavior.setHideable(false);
    bottomSheetBehavior.setDraggable(false);

    bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        playerBarView.setAlpha(1 - slideOffset);
        playerBarExtensionView.setAlpha(slideOffset);
      }
    });

    ImageButton prevPlayerBarButton = findViewById(R.id.player_bar_prev_button);
    ImageButton nextPlayerBarButton = findViewById(R.id.player_bar_next_button);

    playPlayerBarButton.setOnClickListener(v -> musicService.playOrPause());
    prevPlayerBarButton.setOnClickListener(v -> musicService.playPrev());
    nextPlayerBarButton.setOnClickListener(v -> musicService.playNext());
    playerBarView.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

    //Extension
    ImageButton extensionPrevPlayerBarButton = findViewById(R.id.player_bar_extension_prev_button);
    ImageButton extensionNextPlayerBarButton = findViewById(R.id.player_bar_extension_next_button);
    ImageButton extensionRewindBackPlayerBarButton = findViewById(R.id.player_bar_extension_rewind_back_button);
    ImageButton extensionRewindForwardPlayerBarButton = findViewById(R.id.player_bar_extension_rewind_forward_button);
    ImageButton extensionOptionPLayerBarButton = findViewById(R.id.player_bar_extension_audio_option_button);
    ImageButton extensionWrapPlayerBarButton = findViewById(R.id.player_bar_extension_wrap_button);

    extensionPlayPlayerBarButton.setOnClickListener(v -> musicService.playOrPause());
    extensionPrevPlayerBarButton.setOnClickListener(v -> musicService.playPrev());
    extensionNextPlayerBarButton.setOnClickListener(v -> musicService.playNext());
    extensionRewindForwardPlayerBarButton.setOnClickListener(v -> musicService.rewindOn(Constants.REWIND_TIME));
    extensionRewindBackPlayerBarButton.setOnClickListener(v -> musicService.rewindOn(-Constants.REWIND_TIME));
    extensionOptionPLayerBarButton.setOnClickListener(v ->
            musicService.init(queue -> PlaylistController
                    .showAudioOptionBottomDialog(MainActivity.this, queue.getCurrentAudio(), musicService)));
    extensionProgressIndicator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      boolean isPaused;
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          musicService.rewindTo(progress);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        isPaused = musicService.pauseSeparated();
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        if (isPaused) {
          musicService.playSeparated();
        }
      }
    });
    extensionWrapPlayerBarButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
          bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
      }
    });
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
      Log.e(LOG_TAG, "IOException: " + e.getCause().toString());
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
          public void onPlaying(ExoPlayer mediaPlayer, AudioQueue queue) {
            startPlayerBar(mediaPlayer, queue);
          }
        });

        musicService.addPausedListener(new PausedListener() {
          @Override
          public void onPaused() {
            pausePlayerBar();
          }
        });

        musicService.addChangeListener(new ChangeCurAudioListener() {
          @Override
          public void onChange(AudioQueue queue) {
            updatePlayerBarData(queue);
            progressIndicator.setProgress(0);
            extensionProgressIndicator.setProgress(0);
            extensionCutTime.setText("0:00");
          }
        });

        musicService.setRewindListener(new RewindListener() {
          @Override
          public void onRewinding(ExoPlayer mediaPlayer) {
            updatePlayerBarProgressIndicator(mediaPlayer);
          }
        });

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

  private void pausePlayerBar() {
    playPlayerBarButton.setImageResource(R.drawable.round_play_arrow_24);

    //Extension
    extensionPlayPlayerBarButton.setImageResource(R.drawable.round_play_arrow_32);
  }

  private void startPlayerBar(ExoPlayer mediaPlayer, AudioQueue queue) {
    if (!isPlayerBarStarted) {
      playerBarView.setOnTouchListener(null);
      bottomSheetBehavior.setDraggable(true);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        playerBarView.setRenderEffect(null);
      }
      else {
        playerBarView.setAlpha(1);
      }
      isPlayerBarStarted = true;
    }

    playPlayerBarButton.setImageResource(R.drawable.round_pause_24);
    progressIndicator.setMax((int) mediaPlayer.getDuration());

    //Extension
    extensionPlayPlayerBarButton.setImageResource(R.drawable.round_pause_32);
    extensionProgressIndicator.setMax((int) mediaPlayer.getDuration());
    extensionAllTime.setText(convertTimeToString((int) mediaPlayer.getDuration()));

    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (!mediaPlayer.isPlaying()) return;

        progressIndicator.setProgress((int) mediaPlayer.getCurrentPosition());
        extensionProgressIndicator.setProgress((int) mediaPlayer.getCurrentPosition());
        extensionCutTime.setText(convertTimeToString((int) mediaPlayer.getCurrentPosition()));

        new Handler().postDelayed(this, 500);
      }
    });

    updatePlayerBarData(queue);
  }

  private void updatePlayerBarData(AudioQueue queue) {
    Audio curAudio = queue.getCurrentAudio();
    titlePlayerBar.setText(curAudio.getTitle());
    channelPlayerBar.setText(curAudio.getChannel());
    Picasso.get()
            .load(curAudio.getImageURI())
            .centerCrop()
            .resize(Math.toIntExact(curAudio.getWidth()),
                    (int) ((float) curAudio.getWidth() * HEIGHT_RATIO / WIDTH_RATIO))
            .placeholder(R.drawable.outline_music_note_24)
            .noFade()
            .into(imagePlayerBar);

    //Extension
    extensionTitle.setText(curAudio.getTitle());
    extensionChannel.setText(curAudio.getChannel());
  }

  private void updatePlayerBarProgressIndicator(ExoPlayer mediaPlayer) {
    progressIndicator.setProgress((int) mediaPlayer.getCurrentPosition());

    //Extension
    extensionProgressIndicator.setProgress((int) mediaPlayer.getCurrentPosition());
    extensionCutTime.setText(convertTimeToString((int) mediaPlayer.getCurrentPosition()));
  }

  private static String convertTimeToString(int millis) {
    StringBuilder stringBuilder = new StringBuilder();

    long days = TimeUnit.MILLISECONDS.toDays(millis);
    if (days != 0) stringBuilder.append(days).append(":");

    long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
    if (hours == 0 && days != 0) stringBuilder.append("00:");
    else if (hours != 0 && hours < 10 && days != 0) stringBuilder.append("0").append(hours).append(":");
    else if (hours != 0) stringBuilder.append(hours).append(":");

    long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
    if (minutes == 0) {
      if (hours != 0) stringBuilder.append("00:");
      else stringBuilder.append("0:");
    }
    else if (minutes < 10) stringBuilder.append("0").append(minutes).append(":");
    else stringBuilder.append(minutes).append(":");

    long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
    if (seconds < 10) stringBuilder.append("0").append(seconds);
    else stringBuilder.append(seconds);

    return stringBuilder.toString();
  }
}
