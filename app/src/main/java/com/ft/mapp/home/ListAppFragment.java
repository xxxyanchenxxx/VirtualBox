package com.ft.mapp.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ft.mapp.R;
import com.ft.mapp.VCommends;
import com.ft.mapp.abs.ui.VFragment;
import com.ft.mapp.home.adapters.CloneAppListAdapter;
import com.ft.mapp.home.models.AppInfo;
import com.ft.mapp.home.models.AppInfoLite;
import com.ft.mapp.widgets.quicksidebar.QuickSideBarTipsView;
import com.ft.mapp.widgets.quicksidebar.QuickSideBarView;
import com.ft.mapp.widgets.quicksidebar.listener.OnQuickSideBarTouchListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Lody
 */
public class ListAppFragment extends VFragment<ListAppContract.ListAppPresenter>
        implements ListAppContract.ListAppView, OnQuickSideBarTouchListener {
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private CloneAppListAdapter mAdapter;
    private QuickSideBarView mQuickSideBarView;
    private QuickSideBarTipsView mQuickSideBarTipsView;
    private RecyclerView.SmoothScroller mSmoothScroller;
    private HashMap<String, Integer> mLetters = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_app, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mQuickSideBarTipsView = view.findViewById(R.id.quickSideBarTipsView);
        mQuickSideBarView = view.findViewById(R.id.quickSideBarView);
        mQuickSideBarView.setOnQuickSideBarTouchListener(this);
        mQuickSideBarView.setVisibility(View.INVISIBLE);

        mRecyclerView = view.findViewById(R.id.select_app_recycler_view);
        mProgressBar = view.findViewById(R.id.select_app_progress_bar);
        LinearLayoutManager
                layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(new ColorDrawable(Color.GRAY));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mAdapter = new CloneAppListAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new CloneAppListAdapter.ItemEventListener() {
            @Override
            public void onItemClick(AppInfo info, int position) {
                ArrayList<AppInfoLite> dataList = new ArrayList<>(1);
                dataList.add(new AppInfoLite(info.packageName, info.path, info.fastOpen));
                Intent data = new Intent();
                data.putParcelableArrayListExtra(VCommends.EXTRA_APP_INFO_LIST, dataList);
                getActivity().setResult(Activity.RESULT_OK, data);
                getActivity().finish();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX,
                                           int oldScrollY) {
                    try {
                        LinearLayoutManager l =
                                (LinearLayoutManager) mRecyclerView.getLayoutManager();
                        int position = l.findFirstVisibleItemPosition();
                        AppInfo info = mAdapter.getItem(position);
                        if (info.firstLetter.equals("#")) {
                            mQuickSideBarView.setChooseLetter(26);
                        } else {
                            long letterPos = info.firstLetter.charAt(0);
                            int choose = (int) (letterPos - 'A');
                            mQuickSideBarView.setChooseLetter(choose);
                        }
                    } catch (Throwable ignore) {
                        //
                    }
                }
            });
        }
        new ListAppPresenterImpl(getActivity(), this).start();

        mSmoothScroller = new LinearSmoothScroller(getContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return super.calculateSpeedPerPixel(displayMetrics) / 5;
            }
        };
    }

    @Override
    public void startLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void loadFinish(List<AppInfo> infoList) {
        mLetters.clear();
        List<String> list = new ArrayList<>();
        int position = 0;
        for (AppInfo appInfo : infoList) {
            String letter = appInfo.firstLetter;
            if (!mLetters.containsKey(letter)) {
                mLetters.put(letter, position);
                list.add(letter);
            }
            ++position;
        }

        mQuickSideBarView.setLetters(list);
        mQuickSideBarView.setVisibility(View.VISIBLE);

        mAdapter.setList(infoList);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(ListAppContract.ListAppPresenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onLetterChanged(String letter, int position, float y) {
        mQuickSideBarTipsView.setText(letter, position, y);
        if (mLetters.containsKey(letter)) {
            int pos = mLetters.get(letter);
            mSmoothScroller.setTargetPosition(pos);
            mRecyclerView.getLayoutManager().startSmoothScroll(mSmoothScroller);
        }
    }

    @Override
    public void onLetterTouching(boolean touching) {
        if (touching) {
            mQuickSideBarTipsView.setVisibility(View.VISIBLE);
        } else {
            mQuickSideBarTipsView.setVisibility(View.INVISIBLE);
        }
    }
}
