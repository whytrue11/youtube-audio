package com.whytrue.youtubeaudio;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class CustomBehavior extends CoordinatorLayout.Behavior<View> {
  public CustomBehavior() {
    // Конструктор
  }

  public CustomBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    // Определите, от каких дочерних представлений (dependency) зависит ваше представление (child)
    return true;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
    // Измените параметры представления (child) при изменении зависимого представления (dependency)
    // Здесь вы можете изменять параметры представления, такие как размер, положение и т. д.
    int top = dependency.getTop();
    child.setBottom(top);
    child.setMinimumHeight(top);
    child.requestLayout();
    return true;
  }
}
