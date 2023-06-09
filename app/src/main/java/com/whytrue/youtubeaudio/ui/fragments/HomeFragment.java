package com.whytrue.youtubeaudio.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.whytrue.youtubeaudio.ui.activities.MainActivity;
import com.whytrue.youtubeaudio.services.MusicService;
import com.whytrue.youtubeaudio.R;
import com.whytrue.youtubeaudio.tasks.SearcherYT;
import com.whytrue.youtubeaudio.ui.adapters.AudioHomeAdapter;
import com.whytrue.youtubeaudio.utils.Constants;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
  private static final String[] DEFAULT_SEARCH_QUERY = {"classic music"};
  private static final String LOG_TAG = "HomeFragment";
  private TextView text;

  //Recycler view
  private RecyclerView audioRecyclerView;
  private AudioHomeAdapter homeAdapter;

  GoogleAccountCredential credential;
  private ProgressDialog progressDialog;
  private SearcherYT searchTaskYT;

  private MusicService musicService;
  private BottomSheetBehavior slideUpPanelBottomSheetBehavior;

  public HomeFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    progressDialog = new ProgressDialog(getContext());
    progressDialog.setCancelable(false);
    progressDialog.setMessage(getContext().getResources().getString(R.string.loading));

    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.i(LOG_TAG, "CreateView");
    View view = inflater.inflate(R.layout.fragment_home, container, false);

    text = view.findViewById(R.id.home_error_text_id);
    initPlayerBar();
    initRecyclerView(view);

    return view;
  }

  @Override
  public void onStop() {
    super.onStop();
    progressDialog.cancel();
    searchTaskYT.cancel(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.options_menu_home, menu);
    MenuItem menuItem = menu.findItem(R.id.app_bar_search_id);
    SearchView searchView = (SearchView) menuItem.getActionView();
    searchView.setQueryHint("search...");
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String s) {
        searchTaskYT = new SearcherYT(getContext(), credential,
                s,
                progressDialog, homeAdapter,
                text, false, Constants.HOME_AUDIO_IMAGE_QUALITY);
        searchTaskYT.execute();
        searchView.clearFocus();
        return true;
      }

      @Override
      public boolean onQueryTextChange(String s) {
        return false;
      }
    });
    super.onCreateOptionsMenu(menu, inflater);
  }

  private void initRecyclerView(View view) {
    audioRecyclerView = view.findViewById(R.id.main_audio_list);

    audioRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        if (newState != RecyclerView.SCROLL_STATE_IDLE) slideUpPanelBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (newState == RecyclerView.SCROLL_STATE_IDLE && ((LinearLayoutManager) recyclerView.getLayoutManager())
                .findLastVisibleItemPosition() == homeAdapter.getItemCount() - 1) {
          searchTaskYT = new SearcherYT(getContext(), credential,
                  null,
                  progressDialog, homeAdapter,
                  text, true, Constants.HOME_AUDIO_IMAGE_QUALITY);
          searchTaskYT.execute();
          Log.i(LOG_TAG, "Recycler view updated");
        }
      }
    });

    if (homeAdapter == null || homeAdapter.getItemCount() == 0) {
      homeAdapter = new AudioHomeAdapter(new ArrayList<>(), this::getMusicService,
              (audios, pos) -> {
                getMusicService().replacePlaylist(audios);
                getMusicService().goToAudio(audios.get(pos));
              }
      );
      searchTaskYT = new SearcherYT(getContext(), credential,
              DEFAULT_SEARCH_QUERY[(int) (Math.random() * DEFAULT_SEARCH_QUERY.length)],
              progressDialog, homeAdapter,
              view.findViewById(R.id.home_error_text_id), false, Constants.HOME_AUDIO_IMAGE_QUALITY);
      searchTaskYT.execute();
    }
    audioRecyclerView.setAdapter(homeAdapter);
  }

  private void initPlayerBar() {
    slideUpPanelBottomSheetBehavior = BottomSheetBehavior.from(getActivity().findViewById(R.id.slide_up_panel));
    slideUpPanelBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (homeAdapter != null) homeAdapter.setClickable(newState != BottomSheetBehavior.STATE_EXPANDED);
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {
      }
    });
  }

  private MusicService getMusicService() {
    return musicService = musicService == null ? ((MainActivity) getActivity()).getMusicService() : musicService;
  }
}
