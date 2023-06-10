package com.whytrue.youtubeaudio.entities;

public class PlaylistMeta {
  private int count;
  private String name;
  private String firstAudioId;
  private String imageURI;
  private Long width;
  private Long height;

  public PlaylistMeta(int count, String name, String firstAudioId, String imageURI, Long width, Long height) {
    this.count = count;
    this.name = name;
    this.firstAudioId = firstAudioId;
    this.imageURI = imageURI;
    this.width = width;
    this.height = height;
  }

  public int getCount() {
    return count;
  }

  public String getName() {
    return name;
  }

  public String getFirstAudioId() {
    return firstAudioId;
  }

  public String getImageURI() {
    return imageURI;
  }

  public Long getWidth() {
    return width;
  }

  public Long getHeight() {
    return height;
  }

  public void setWidth(Long width) {
    this.width = width;
  }

  public void setHeight(Long height) {
    this.height = height;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setFirstAudioId(String firstAudioId) {
    this.firstAudioId = firstAudioId;
  }

  public void setImageURI(String imageURI) {
    this.imageURI = imageURI;
  }
}
