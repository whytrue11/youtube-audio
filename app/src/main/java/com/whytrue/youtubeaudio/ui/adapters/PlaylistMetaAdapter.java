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
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.callbacks.ClickPlaylistListener;
import com.whytrue.youtubeaudio.entities.PlaylistMeta;
import com.whytrue.youtubeaudio.utils.playlist.PlaylistLoader;

import java.io.IOException;
import java.util.List;

public class PlaylistMetaAdapter extends RecyclerView.Adapter<PlaylistMetaAdapter.ViewHolder> {
  private List<PlaylistMeta> playlistItems;
  private Context context;
  private ClickPlaylistListener clickPlaylistListener;
  private TextView text;

  public PlaylistMetaAdapter(List<PlaylistMeta> playlistItems, ClickPlaylistListener clickPlaylistListener, TextView text) {
    this.playlistItems = playlistItems;
    this.clickPlaylistListener = clickPlaylistListener;
    this.text = text;
  }

  @SuppressLint("NotifyDataSetChanged")
  public void setPlaylistItems(List<PlaylistMeta> playlistItems) {
    this.playlistItems = playlistItems;
    notifyDataSetChanged();
  }

  @SuppressLint("NotifyDataSetInserted")
  public void addPlaylistItem(PlaylistMeta playlistItem) {
    this.playlistItems.add(playlistItem);
    notifyItemInserted(playlistItems.size() - 1);
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    context = recyclerView.getContext();
  }

  @Override
  public PlaylistMetaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
    return new PlaylistMetaAdapter.ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(PlaylistMetaAdapter.ViewHolder holder, int position) {
    PlaylistMeta playlistItem = playlistItems.get(position);

    holder.nameView.setText(playlistItem.getName());
    holder.countView.setText(Integer.toString(playlistItem.getCount()));
    holder.itemView.setOnClickListener(v -> clickPlaylistListener.onClick(playlistItems, position));
    holder.button.setOnClickListener(v -> {
      try {
        PlaylistLoader.getInstance(context.getFilesDir().getPath()).deletePlaylist(playlistItem.getName());
        removePlaylistItem(playlistItem);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    });

    Picasso.get()
            .load(playlistItem.getImageURI())
            .centerCrop()
            .resize(Math.toIntExact(playlistItem.getWidth()),
                    (int) ((float) playlistItem.getWidth() * HEIGHT_RATIO / WIDTH_RATIO))
            .placeholder(R.drawable.outline_image_24)
            .noFade()
            .into(holder.imageView);
  }

  @Override
  public int getItemCount() {
    return playlistItems.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView imageView;
    final TextView nameView, countView;
    final ImageButton button;

    ViewHolder(View view) {
      super(view);
      imageView = view.findViewById(R.id.item_playlist_image);
      nameView = view.findViewById(R.id.item_playlist_name);
      countView = view.findViewById(R.id.item_playlist_audio_count);
      button = view.findViewById(R.id.item_playlist_delete_button);
    }
  }

  @SuppressLint("NotifyItemRemoved")
  private void removePlaylistItem(PlaylistMeta playlistItem) {
    int position = playlistItems.indexOf(playlistItem);
    if (position != -1) {
      this.playlistItems.remove(position);
      notifyItemRemoved(position);
      if (text != null) text.setText(context.getResources().getString(R.string.playlist_absence));
    }
  }
}
