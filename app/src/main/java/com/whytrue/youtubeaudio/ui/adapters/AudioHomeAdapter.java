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

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.whytrue.youtubeaudio.callbacks.MusicPlayerGetter;
import com.whytrue.youtubeaudio.callbacks.ClickAudioListener;
import com.whytrue.youtubeaudio.utils.playlist.PlaylistController;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.entities.Audio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AudioHomeAdapter extends RecyclerView.Adapter<AudioHomeAdapter.ViewHolder> {
  private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

  private List<Audio> audioItems;
  private Context context;
  private final MusicPlayerGetter musicPlayerGetter;
  private ClickAudioListener clickAudioListener;

  public AudioHomeAdapter(List<Audio> audioItems, MusicPlayerGetter musicPlayerGetter, ClickAudioListener clickAudioListener) {
    this.audioItems = audioItems;
    this.musicPlayerGetter = musicPlayerGetter;
    this.clickAudioListener = clickAudioListener;
  }

  @SuppressLint("NotifyDataSetChanged")
  public void setAudioItems(List<Audio> audioItems) {
    this.audioItems = audioItems;
    notifyDataSetChanged();
  }

  @SuppressLint("NotifyItemRangeInserted")
  public void addAudioItems(List<Audio> audioItems) {
    this.audioItems.addAll(audioItems);
    notifyItemRangeInserted(this.audioItems.size() - audioItems.size(), audioItems.size());
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    context = recyclerView.getContext();
  }

  @Override
  public AudioHomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_audio, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(AudioHomeAdapter.ViewHolder holder, int position) {
    Audio audioItem = audioItems.get(position);
    holder.titleView.setText(audioItem.getTitle());
    holder.descriptionView.setText(audioItem.getChannel() + " - "
            + audioItem.getViews() + " views - "
            + FORMATTER.format(new Date(audioItem.getPublicationDate())));
    holder.durationView.setText(audioItem.getDuration());
    holder.button.setOnClickListener(v -> PlaylistController.
            showAudioOptionBottomDialog(context, audioItem, musicPlayerGetter.getMusicService()));
    holder.itemView.setOnClickListener(v -> clickAudioListener.onClick(audioItems, position));

    Picasso.get()
            .load(audioItem.getImageURI())
            .centerCrop()
            .resize(Math.toIntExact(audioItem.getWidth()),
                    (int) ((float) audioItem.getWidth() * HEIGHT_RATIO / WIDTH_RATIO))
            .placeholder(R.drawable.outline_image_24)
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
    final TextView titleView, descriptionView, durationView;
    final ImageButton button;

    ViewHolder(View view) {
      super(view);
      imageView = view.findViewById(R.id.home_audio_image);
      titleView = view.findViewById(R.id.home_audio_title);
      descriptionView = view.findViewById(R.id.home_audio_description);
      durationView = view.findViewById(R.id.home_audio_duration);
      button = view.findViewById(R.id.home_audio_button);
    }
  }
}
