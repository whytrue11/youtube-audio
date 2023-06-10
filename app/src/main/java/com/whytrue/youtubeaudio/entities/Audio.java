package com.whytrue.youtubeaudio.entities;

import java.math.BigInteger;
import java.util.Objects;

public class Audio {
  private final String id;
  private String title;
  private String channel;
  private BigInteger views;
  private long publicationDate;
  private String imageURI;
  private Long width;
  private Long height;
  private String duration;

  public Audio(String id, String title, String channel, BigInteger views, long publicationDate, String imageURI, Long width, Long height, String duration) {
    this.id = id;
    this.title = title;
    this.channel = channel;
    this.views = views;
    this.publicationDate = publicationDate;
    this.imageURI = imageURI;
    this.width = width;
    this.height = height;
    this.duration = duration;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public BigInteger getViews() {
    return views;
  }

  public void setViews(BigInteger views) {
    this.views = views;
  }

  public long getPublicationDate() {
    return publicationDate;
  }

  public String getImageURI() {
    return imageURI;
  }

  public void setImageURI(String imageURI) {
    this.imageURI = imageURI;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public Long getWidth() {
    return width;
  }

  public void setWidth(Long width) {
    this.width = width;
  }

  public Long getHeight() {
    return height;
  }

  public void setHeight(Long height) {
    this.height = height;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Audio audio = (Audio) o;
    return Objects.equals(id, audio.id);
  }
}
