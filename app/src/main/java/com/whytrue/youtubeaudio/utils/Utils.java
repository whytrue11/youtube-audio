package com.whytrue.youtubeaudio.utils;

import static com.whytrue.youtubeaudio.utils.Constants.HEIGHT_RATIO;
import static com.whytrue.youtubeaudio.utils.Constants.WIDTH_RATIO;

import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;

public class Utils {
  public static enum ImageQuality {
    DEFAULT,
    MEDIUM,
    HIGH,
    STANDARD,
    MAXRES
  }

  public static Thumbnail imageLoad(ThumbnailDetails thumbnailDetails, ImageQuality quality) {
    Thumbnail image = null;
    switch (quality) {
      case MAXRES:
        if (image == null) {
          image = thumbnailDetails.getMaxres();
        }
      case STANDARD:
        if (image == null) {
          image = thumbnailDetails.getStandard();
        }
      case HIGH:
        if (image == null) {
          image = thumbnailDetails.getHigh();
        }
      case MEDIUM:
        if (image == null) {
          image = thumbnailDetails.getMedium();
        }
      case DEFAULT:
        if (image == null) {
          image = thumbnailDetails.getDefault();
        }
        break;
      default:
        image = new Thumbnail();
        image.setUrl(null);
        image.setWidth((long) WIDTH_RATIO);
        image.setHeight((long) HEIGHT_RATIO);
    }
    return image;
  }

  public static String youtubeDurationFormatting(String ytDuration) {
    if (ytDuration.startsWith("PT")) {
      ytDuration = ytDuration.substring(2);
    }
    else {
      ytDuration = ytDuration.substring(1);
    }
    StringBuilder stringBuilder = new StringBuilder();

    int dayIndex = ytDuration.indexOf("DT");
    if (dayIndex != -1) {
      stringBuilder.append(ytDuration.substring(0, dayIndex)).append(":");
      ytDuration = ytDuration.substring(dayIndex + 2);
    }

    int hoursIndex = ytDuration.indexOf("H");
    if (hoursIndex != -1) {
      if (stringBuilder.length() != 0 && hoursIndex == 1) {
        stringBuilder.append("0");
      }
      stringBuilder.append(ytDuration.substring(0, hoursIndex)).append(":");
      ytDuration = ytDuration.substring(hoursIndex + 1);
    }
    else if (dayIndex != -1) {
      stringBuilder.append("00:");
    }

    int minutesIndex = ytDuration.indexOf("M");
    if (minutesIndex != -1) {
      if (stringBuilder.length() != 0 && minutesIndex == 1) {
        stringBuilder.append("0");
      }
      stringBuilder.append(ytDuration.substring(0, minutesIndex)).append(":");
      ytDuration = ytDuration.substring(minutesIndex + 1);
    }
    else {
      if (stringBuilder.length() == 0) {
        stringBuilder.append("0:");
      }
      else {
        stringBuilder.append("00:");
      }
    }

    int secondsIndex = ytDuration.indexOf("S");
    if (secondsIndex != -1) {
      if (stringBuilder.length() != 0 && secondsIndex == 1) {
        stringBuilder.append("0");
      }
      stringBuilder.append(ytDuration.substring(0, secondsIndex));
    }
    else {
      stringBuilder.append("00");
    }
    return stringBuilder.toString();
  }
}
