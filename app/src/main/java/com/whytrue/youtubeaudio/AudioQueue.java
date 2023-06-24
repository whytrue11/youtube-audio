package com.whytrue.youtubeaudio;

import android.util.Log;

import androidx.annotation.NonNull;

import com.whytrue.youtubeaudio.entities.Audio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class AudioQueue {
  private final List<Audio> list = new ArrayList<>();
  private String logTag;
  private int currentIndex = -1;
  private boolean repeat;


  public AudioQueue(boolean repeat) {
    this.repeat = repeat;
  }

  public AudioQueue(boolean repeat, String logTag) {
    this.repeat = repeat;
    this.logTag = logTag;
  }

  public boolean hasNext() {
    return !isEmpty() && size() > 1 && (repeat || size() > currentIndex + 1);
  }

  public int getNextIndex() {
    return hasNext() ? getProperIndex(currentIndex + 1) : getCurrentIndex();
  }

  public boolean goToNext() {
    if (hasNext()) {
      changeCurrentIndex(currentIndex + 1);
      return true;
    }
    return false;
  }

  public boolean hasPrev() {
    return !isEmpty() && size() > 1 && (repeat || currentIndex > 0);
  }

  public int getPrevIndex() {
    return hasPrev() ? getProperIndex(currentIndex - 1) : getCurrentIndex();
  }

  public boolean goToPrev() {
    if (hasPrev()) {
      changeCurrentIndex(currentIndex - 1);
      return true;
    }
    return false;
  }

  private int changeCurrentIndex(int newIndex) {
    return currentIndex = (newIndex + size()) % size();
  }

  private int getProperIndex(int i) {
    return (i + size()) % size();
  }

  public AudioQueue add(@NonNull Audio audio) {
    list.add(audio);
    if (currentIndex == -1) changeCurrentIndex(0);

    if (logTag != null)
      Log.i(logTag, "Audio added to playlist");

    return this;
  }

  public AudioQueue addNext(@NonNull Audio audio) {
    if (currentIndex == -1) {
      list.add(audio);
      changeCurrentIndex(0);
    }
    else {
      list.add(currentIndex + 1, audio);
    }

    if (logTag != null)
      Log.i(logTag, "Audio added to playlist NEXT");

    return this;
  }

  public void goTo(int index) {
    changeCurrentIndex(index);
  }

  public Audio getCurrentAudio() {
    return currentIndex >= 0 ? list.get(currentIndex) : null;
  }

  public boolean isRepeat() {
    return this.repeat;
  }

  public void setRepeat(boolean repeat) {
    this.repeat = repeat;
  }

  public int getCurrentIndex() {
    return currentIndex;
  }


  public int size() {
    return list.size();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public boolean contains(Audio o) {
    return list.contains(o);
  }

  public int indexOf(Audio o) {
    return list.indexOf(o);
  }

  public void addAll(Collection<? extends Audio> c) {
    for (Audio a : c) add(a);
  }

  public void clear() {
    list.clear();
  }

  public Audio get(int index) {
    return list.get(index);
  }

  public List<Audio> getAll() {
    return list;
  }

  public String getStringForComparison() {
    StringBuilder builder = new StringBuilder();
    for (Audio audio : list) {
      builder.append((audio.getTitle()));
    }
    return builder.toString();
  }

  @NonNull
  public Iterator<Audio> iterator() {
    return list.iterator();
  }

  public Audio set(int index, Audio element) {
    return list.set(index, element);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    else if (!(obj instanceof AudioQueue)) return false;
    else {
      AudioQueue a = (AudioQueue) obj;
      return !(a.isEmpty() || this.isEmpty())
              && this.size() == a.size()
              && this.get(0).equals(a.get(0))
              && this.get(this.size() - 1).equals(a.get(a.size() - 1));
    }
  }

  public boolean equalPlaylist(List<Audio> a) {
    return !(a.isEmpty() || this.isEmpty())
            && this.size() == a.size()
            && this.get(0).equals(a.get(0))
            && this.get(this.size() - 1).equals(a.get(a.size() - 1));
  }
}