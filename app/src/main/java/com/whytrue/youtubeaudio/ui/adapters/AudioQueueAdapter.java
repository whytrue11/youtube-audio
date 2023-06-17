package com.whytrue.youtubeaudio.ui.adapters;

import static com.whytrue.youtubeaudio.utils.Constants.HEIGHT_RATIO;
import static com.whytrue.youtubeaudio.utils.Constants.WIDTH_RATIO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.whytrue.youtubeaudio.callbacks.ClickAudioListener;
import com.whytrue.youtubeaudio.callbacks.MusicPlayerGetter;
import com.whytrue.youtubeaudio.utils.playlist.PlaylistController;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.entities.Audio;

import java.util.List;

public class AudioQueueAdapter extends RecyclerView.Adapter<AudioQueueAdapter.ViewHolder> {
  private List<Audio> audioItems;
  private Context context;
  private MusicPlayerGetter musicPlayerGetter;
  private ClickAudioListener clickAudioListener;
  private boolean clickable = true;

  public AudioQueueAdapter(List<Audio> audioItems, MusicPlayerGetter musicPlayerGetter, ClickAudioListener clickAudioListener) {
    this.audioItems = audioItems;
    this.musicPlayerGetter = musicPlayerGetter;
    this.clickAudioListener = clickAudioListener;
  }

  @SuppressLint("NotifyDataSetChanged")
  public void setAudioItems(List<Audio> audioItems) {
    this.audioItems = audioItems;
    notifyDataSetChanged();
  }

  @SuppressLint("NotifyDataSetChanged")
  public void setClickable(boolean clickable) {
    if (this.clickable == clickable) return;
    this.clickable = clickable;
    notifyDataSetChanged();
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    context = recyclerView.getContext();
  }

  @Override
  public AudioQueueAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue_audio, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(AudioQueueAdapter.ViewHolder holder, int position) {
    Audio audioItem = audioItems.get(position);
    holder.titleView.setText(audioItem.getTitle());
    holder.channelView.setText(audioItem.getChannel());
    holder.durationView.setText(audioItem.getDuration());
    holder.button.setOnClickListener(v -> PlaylistController.
            showAudioOptionBottomDialog(context, audioItem, musicPlayerGetter.getMusicService()));
    holder.itemView.setOnClickListener(v -> clickAudioListener.onClick(audioItems, position));
    holder.itemView.setClickable(clickable);

    Picasso.get()
            .load(audioItem.getImageURI())
            .centerCrop()
            .resize(Math.toIntExact(audioItem.getWidth()),
                    (int) ((float) audioItem.getWidth() * HEIGHT_RATIO / WIDTH_RATIO))
            .placeholder(R.drawable.outline_music_note_24)
            .noFade()
            .into(holder.imageView);/*, new Callback() {

              @Override
              public void onSuccess() {
                holder.imageView.setAlpha(0f);
                holder.imageView.animate().setDuration(200).alpha(1f).start();
              }

              @Override
              public void onError(Exception e) {
              }
            });*/
  }

  @Override
  public int getItemCount() {
    return audioItems.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView imageView;
    final TextView titleView, channelView, durationView;
    final ImageButton button;

    ViewHolder(View view) {
      super(view);
      imageView = view.findViewById(R.id.item_queue_image);
      titleView = view.findViewById(R.id.item_queue_title);
      channelView = view.findViewById(R.id.item_queue_channel);
      durationView = view.findViewById(R.id.item_queue_duration);
      button = view.findViewById(R.id.item_queue_button);
    }
  }
}
