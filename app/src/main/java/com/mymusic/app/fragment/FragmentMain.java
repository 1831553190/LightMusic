package com.mymusic.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mymusic.app.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentMain extends Fragment {

	//    Fragment fragmentIndex, fragmentOther, fragmentSinger;
	String tabTitle[] = {"歌曲", "专辑", "艺术家"};
	ViewPager viewPager;
	@BindView(R.id.tabs)
	TabLayout tabs;
	View view;

	static FragmentMain fragmentMain;

	public static Fragment getInstance() {
		if (fragmentMain == null) {
			synchronized (FragmentMain.class) {
				if (fragmentMain == null) {
					fragmentMain = new FragmentMain();
				}
			}
		}
		return fragmentMain;
	}


	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		setRetainInstance(true);
		view = inflater.inflate(R.layout.layout_main, container, false);
		ButterKnife.bind(this, view);
		final List<Fragment> fragmentList = new ArrayList<>();
		viewPager = view.findViewById(R.id.view_pager);
		fragmentList.add(FragmentIndex.getInstance());
		fragmentList.add(FragmentOther.getInstance());
		fragmentList.add(FragmentSinger.getInstance());
		viewPager.setAdapter(new FragmentPagerAdapter(getParentFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
			@Override
			public Fragment getItem(int position) {
				return fragmentList.get(position);
			}

			@Override
			public int getCount() {
				return fragmentList.size();
			}

			@Nullable
			@Override
			public CharSequence getPageTitle(int position) {
				return tabTitle[position];
			}
		});
		tabs.setupWithViewPager(viewPager);
		return view;
	}
}
