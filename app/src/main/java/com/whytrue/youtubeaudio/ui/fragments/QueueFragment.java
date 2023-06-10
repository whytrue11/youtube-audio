package com.whytrue.youtubeaudio.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.whytrue.youtubeaudio.AudioQueue;
import com.whytrue.youtubeaudio.callbacks.ChangeCurAudioListener;
import com.whytrue.youtubeaudio.callbacks.ChangeQueueListener;
import com.whytrue.youtubeaudio.callbacks.InitListener;
import com.whytrue.youtubeaudio.ui.activities.MainActivity;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.services.MusicService;
import com.whytrue.youtubeaudio.ui.adapters.AudioQueueAdapter;

public class QueueFragment extends Fragment {
  private static final String LOG_TAG = "QueueFragment";

  //Recycler view
  private RecyclerView queueRecyclerView;
  private AudioQueueAdapter queueAdapter;

  ChangeQueueListener changeQueueListener;
  ChangeCurAudioListener changeCurAudioListener;
  private ConstraintLayout currentAudioLayout;
  private Drawable startBackground;

  private MusicService musicService;

  public QueueFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_queue, container, false);

    initRecyclerView(view);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    getMusicService().init((queue -> {
      scrollToCurrentAudio(queue);
      selectCurrentAudio(queue);
    }));

    changeCurAudioListener = this::selectCurrentAudio;
    changeQueueListener = this::updateQueue;
    if (getMusicService() != null) {
      getMusicService().addChangeListener(changeCurAudioListener);
      getMusicService().addChangeQueueListener(changeQueueListener);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (currentAudioLayout != null) currentAudioLayout.setBackground(startBackground);
    if (getMusicService() != null) {
      getMusicService().removeChangeListener(changeCurAudioListener);
      getMusicService().removeChangeQueueListener(changeQueueListener);
    }
    changeCurAudioListener = null;
    changeQueueListener = null;
  }

  private void initRecyclerView(View view) {
    queueRecyclerView = view.findViewById(R.id.queue_list);

    if (getMusicService() == null) {
      TextView text = view.findViewById(R.id.queue_error_text);
      text.setText(R.string.player_not_active);
      return;
    }

    getMusicService().init(new InitListener() {
      @Override
      public void init(AudioQueue queue) {
        if (queue.getCurrentIndex() == -1) {
          TextView text = view.findViewById(R.id.queue_error_text);
          text.setText(R.string.player_not_active);
          return;
        }

        if (queueAdapter == null) {
          TextView text = view.findViewById(R.id.queue_error_text);
          text.setText("");
          queueAdapter = new AudioQueueAdapter(null, () -> getMusicService(),
                  ((audios, pos) ->
                          getMusicService().goToAudio(audios.get(pos)))
          );
          queueAdapter.setAudioItems(queue.getAll());
        }
        queueRecyclerView.setAdapter(queueAdapter);

        scrollToCurrentAudio(queue);
      }
    });

  }

  private MusicService getMusicService() {
    return musicService = musicService == null ? ((MainActivity) getActivity()).getMusicService() : musicService;
  }

  private void scrollToCurrentAudio(AudioQueue queue) {
    if (queueRecyclerView == null || queueAdapter == null) return;

    ((LinearLayoutManager) queueRecyclerView.getLayoutManager())
            .scrollToPositionWithOffset(queue.getCurrentIndex(), 0);

  }

  private void selectCurrentAudio(AudioQueue queue) {
    if (queueRecyclerView == null || queueAdapter == null) return;

    if (currentAudioLayout != null) {
      currentAudioLayout.setBackground(startBackground);
    }

    AudioQueueAdapter.ViewHolder viewHolder = (AudioQueueAdapter.ViewHolder) queueRecyclerView
            .findViewHolderForLayoutPosition(queue.getCurrentIndex());
    currentAudioLayout = viewHolder.itemView.findViewById(R.id.item_queue_layout);
    startBackground = currentAudioLayout.getBackground();
    currentAudioLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_grey));
  }

  private void updateQueue(int startInd, int size) {
    queueAdapter.notifyItemRangeInserted(startInd, size);
  }
}
