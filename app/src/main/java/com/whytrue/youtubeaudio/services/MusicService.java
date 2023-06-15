package com.whytrue.youtubeaudio.services;

import static com.whytrue.youtubeaudio.utils.Constants.START_YOUTUBE_URL;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.whytrue.youtubeaudio.AudioQueue;
import com.whytrue.youtubeaudio.callbacks.ChangeCurAudioListener;
import com.whytrue.youtubeaudio.callbacks.ChangeQueueListener;
import com.whytrue.youtubeaudio.callbacks.InitListener;
import com.whytrue.youtubeaudio.callbacks.PausedListener;
import com.whytrue.youtubeaudio.callbacks.PlayingListener;
import com.whytrue.youtubeaudio.callbacks.RewindListener;
import com.whytrue.youtubeaudio.entities.Audio;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MusicService
        extends Service
        implements OnCompletionListener, OnPreparedListener, OnErrorListener {
  private final static String LOG_TAG = "MusicService";
  private final static String WIFI_LOCK_TAG = "YA_WIFI_LOCK";

  private final IBinder binder = new MusicServiceBinder();
  private final AudioQueue currentPlaylist = new AudioQueue(true, "AudioQueue");

  private MediaPlayer mediaPlayer;

  private State state = State.NO_ACTION;
  ExtractAudioURL extractAudioURLTask;

  WifiLock wifiLock;
  AudioManager mAudioManager;
  NotificationManager mNotificationManager;

  private List<PlayingListener> playingListeners = new ArrayList<>();
  private List<PausedListener> pausedListeners = new ArrayList<>();
  private List<ChangeCurAudioListener> changeCurAudioListeners = new ArrayList<>();
  private List<ChangeQueueListener> changeQueueListeners = new ArrayList<>();
  private RewindListener rewindListener;

  //Callbacks
  public void init(InitListener initListener) {
    initListener.init(currentPlaylist);
  }

  public void addPlayingListener(PlayingListener playingListener) {
    this.playingListeners.add(playingListener);
  }

  public void addPausedListener(PausedListener pausedListener) {
    this.pausedListeners.add(pausedListener);
  }

  public void addChangeListener(ChangeCurAudioListener changeCurAudioListener) {
    this.changeCurAudioListeners.add(changeCurAudioListener);
  }

  public void addChangeQueueListener(ChangeQueueListener changeQueueListener) {
    this.changeQueueListeners.add(changeQueueListener);
  }

  public void setRewindListener(RewindListener rewindListener) {
    this.rewindListener = rewindListener;
  }

  public void removeChangeListener(ChangeCurAudioListener changeCurAudioListener) {
    this.changeCurAudioListeners.remove(changeCurAudioListener);
  }

  public void removeChangeQueueListener(ChangeQueueListener changeQueueListener) {
    this.changeQueueListeners.remove(changeQueueListener);
  }

  //Current playlist
  public void replacePlaylist(List<Audio> playlist) {
    if (!currentPlaylist.equalPlaylist(playlist)) {
      currentPlaylist.clear();
      currentPlaylist.addAll(playlist);
      stop();

      for (ChangeQueueListener listener: changeQueueListeners) {
        listener.onChange(0, currentPlaylist.size());
      }
    }
  }

  public void goToAudio(Audio audio) {
    if (currentPlaylist.getCurrentAudio().equals(audio)) {
      playOrPause();
      return;
    }

    int newAudioIndex = currentPlaylist.indexOf(audio);
    if (newAudioIndex != -1) {
      currentPlaylist.goTo(newAudioIndex);
    }
    else {
      Log.i(LOG_TAG, "Audio doesn't contains in playlist");
    }
    play();
    for (ChangeCurAudioListener listener: changeCurAudioListeners) {
      listener.onChange(currentPlaylist);
    }
  }

  public void addAllAudios(Collection<? extends Audio> c) {
    currentPlaylist.addAll(c);
    for (ChangeQueueListener listener: changeQueueListeners) {
      listener.onChange(currentPlaylist.size() - c.size(), c.size());
    }
  }

  public void addAudio(Audio audio) {
    currentPlaylist.add(audio);
    for (ChangeQueueListener listener: changeQueueListeners) {
      listener.onChange(currentPlaylist.size() - 1, 1);
    }
  }

  public void addNextAudio(Audio audio) {
    currentPlaylist.addNext(audio);
    for (ChangeQueueListener listener: changeQueueListeners) {
      listener.onChange(currentPlaylist.getCurrentIndex() + 1, 1);
    }
  }

  //Player
  public void playOrPause() {
    if (state == State.PAUSED || state == State.STOPPED || state == State.PAUSE_ON_PREPARING || state == State.NO_ACTION) {
      play();
    }
    else if (state == State.PLAYING) {
      pause();
    }
  }

  public void playSeparated() {
    if (state == State.PAUSED || state == State.STOPPED || state == State.PAUSE_ON_PREPARING || state == State.NO_ACTION) {
      play();
    }
  }

  public boolean pauseSeparated() {
    if (state == State.PLAYING) {
      pause();
      return true;
    }
    return false;
  }

  public void playNext() {
    if (currentPlaylist.goToNext()) {
      state = State.PREPARING;
      play();
      for (ChangeCurAudioListener listener: changeCurAudioListeners) {
        listener.onChange(currentPlaylist);
      }
    }
  }

  public void playPrev() {
    if (currentPlaylist.goToPrev()) {
      state = State.PREPARING;
      play();
      for (ChangeCurAudioListener listener: changeCurAudioListeners) {
        listener.onChange(currentPlaylist);
      }
    }
  }

  public void rewindTo(int time) {
    if (state != State.PLAYING && state != State.PAUSED) return;

    if (time >= 0 && mediaPlayer.getDuration() >= time) mediaPlayer.seekTo(time);
    else if (mediaPlayer.getDuration() < time) mediaPlayer.seekTo(mediaPlayer.getDuration());
    if (rewindListener != null) rewindListener.onRewinding(mediaPlayer);
  }

  public void rewindOn(int time) {
    if (state != State.PLAYING && state != State.PAUSED) return;

    if (time != 0
            && mediaPlayer.getCurrentPosition() + time < mediaPlayer.getDuration()
            && mediaPlayer.getCurrentPosition() + time >= 0) {
      mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + time);
    }
    else if (mediaPlayer.getCurrentPosition() + time < 0) mediaPlayer.seekTo(0);
    if (rewindListener != null) rewindListener.onRewinding(mediaPlayer);
  }

  //private Player
  private void play() {
    if (currentPlaylist.size() == 0) return;
    if (state == State.PAUSED) {
      state = State.PLAYING;
      mediaPlayer.start();

      for (PlayingListener listener: playingListeners) {
        listener.onPlaying(mediaPlayer, currentPlaylist);
      }
    }
    else if (state == State.PAUSE_ON_PREPARING) {
      state = State.PREPARING;
    }
    else {
      state = State.PREPARING;
      if (extractAudioURLTask != null) {
        extractAudioURLTask.cancel(true);
      }
      createMediaPlayerIfNeeded();
      extractAudioURLTask = new ExtractAudioURL(currentPlaylist.getCurrentAudio().getId());
      try {
        extractAudioURLTask.execute();
      }
      catch (Exception e) {
        Toast.makeText(getApplicationContext(), "ERROR: " + e.getCause(), Toast.LENGTH_SHORT).show();
      }
    }
    wifiLock.acquire();
  }

  private void pause() {
    if (currentPlaylist.size() == 0) return;
    if (state == State.PLAYING) {
      state = State.PAUSED;
      mediaPlayer.pause();
      releaseResources(false);

      for (PausedListener listener: pausedListeners) {
        listener.onPaused();
      }
    }
    else if (state == State.PREPARING) {
      state = State.PAUSE_ON_PREPARING;
      for (PausedListener listener: pausedListeners) {
        listener.onPaused();
      }
    }
  }

  private void stop() {
    if (mediaPlayer != null) {
      state = State.STOPPED;
      mediaPlayer.reset();
      releaseResources(false);
    }
  }

  //Resources
  private void createMediaPlayerIfNeeded() {
    if (mediaPlayer == null) {
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

      mediaPlayer.setOnPreparedListener(this);
      mediaPlayer.setOnCompletionListener(this);
      mediaPlayer.setOnErrorListener(this);

      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mediaPlayer.getAudioSessionId();
    }
    else {
      mediaPlayer.reset();
    }
  }

  private void releaseResources(boolean releaseMediaPlayer) {
    stopForeground(true);
    if (releaseMediaPlayer && mediaPlayer != null) {
      mediaPlayer.reset();
      mediaPlayer.release();
      mediaPlayer = null;
    }
    if (wifiLock.isHeld()) wifiLock.release();
  }

  //MediaPlayer methods
  public void onCompletion(MediaPlayer player) {
    playNext();
  }

  public void onPrepared(MediaPlayer player) {
    if (state == State.PAUSE_ON_PREPARING) {
      state = State.PAUSED;
      return;
    }

    state = State.PLAYING;
    mediaPlayer.start();
    for (PlayingListener listener: playingListeners) {
      listener.onPlaying(mediaPlayer, currentPlaylist);
    }
  }

  public boolean onError(MediaPlayer mp, int what, int extra) {
    Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
            Toast.LENGTH_SHORT).show();
    Log.e(LOG_TAG, "Error: what=" + what + ", extra=" + extra);
    state = State.STOPPED;
    releaseResources(true);
    //giveUpAudioFocus();
    return true; // true indicates we handled the error
  }

  //Service
  @Override
  public void onCreate() {
    Log.i(LOG_TAG, "Create");
    wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
            .createWifiLock(WifiManager.WIFI_MODE_FULL, WIFI_LOCK_TAG);
    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
  }

  @Override
  public void onDestroy() {
    Log.i(LOG_TAG, "Destroy");
    state = State.STOPPED;
    releaseResources(true);
    //giveUpAudioFocus();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    Log.i(LOG_TAG, "Bind");
    return binder;
  }

  //Utils
  public class MusicServiceBinder extends Binder {
    public MusicService getService() {
      return MusicService.this;
    }
  }

  private enum State {
    NO_ACTION,
    PLAYING,
    PAUSED,
    STOPPED,
    PREPARING,
    PAUSE_ON_PREPARING
  }

  private class ExtractAudioURL extends AsyncTask<Void, Void, Void> {
    private String videoURL;

    public ExtractAudioURL(String videoURL) {
      this.videoURL = videoURL;
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        mediaPlayer.reset();
        mediaPlayer.setDataSource(extractAudioURL(videoURL));
        mediaPlayer.prepareAsync();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    private String extractAudioURL(String id)
            throws YoutubeDL.CanceledException, YoutubeDLException, InterruptedException {
      Log.i(LOG_TAG, "START extract audio");
      YoutubeDLRequest request = new YoutubeDLRequest(START_YOUTUBE_URL + id);
      request.addOption("--extract-audio");
      VideoInfo streamInfo = YoutubeDL.getInstance().getInfo(request);
      Log.i(LOG_TAG, "FINISH extract audio");
      return streamInfo.getUrl();
    }
  }

  /*void processRewindRequest() {
    if (state == State.Playing || state == State.Paused)
      mediaPlayer.seekTo(0);
  }
  */

  /*void giveUpAudioFocus() {
    if (focus == AudioFocus.Focused && mAudioFocusHelper != null
            && mAudioFocusHelper.abandonFocus())
      focus = AudioFocus.NoFocusNoDuck;
  }
  */

  /*
  void tryToGetAudioFocus() {
    if (focus != AudioFocus.Focused && mAudioFocusHelper != null
            && mAudioFocusHelper.requestFocus())
      focus = AudioFocus.Focused;
  }
  */

  /** Updates the notification. */

  /*void updateNotification(String text) {
    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
            new Intent(getApplicationContext(), MainActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT);
    mNotificationBuilder.setContentText(text)
            .setContentIntent(pi);
    mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
  }*/
  /**
   * Configures service as a foreground service. A foreground service is a service that's doing
   * something the user is actively aware of (such as playing music), and must appear to the
   * user as a notification. That's why we create the notification here.
   */

  /*void setUpAsForeground(String text) {
    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
            new Intent(getApplicationContext(), MainActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT);
    // Build the notification object.
    mNotificationBuilder = new Notification.Builder(getApplicationContext())
            .setSmallIcon(R.drawable.ic_stat_playing)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
            .setContentTitle("RandomMusicPlayer")
            .setContentText(text)
            .setContentIntent(pi)
            .setOngoing(true);
    startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
  }*/

  /*public void onGainedAudioFocus() {
    Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
    focus = AudioFocus.Focused;
    // restart media player with new focus settings
    if (state == State.Playing)
      configAndStartMediaPlayer();
  }
  public void onLostAudioFocus(boolean canDuck) {
    Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
            "no duck"), Toast.LENGTH_SHORT).show();
    focus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
    // start/restart/pause media player with new focus settings
    if (mediaPlayer != null && mediaPlayer.isPlaying())
      configAndStartMediaPlayer();
  }
  */
}