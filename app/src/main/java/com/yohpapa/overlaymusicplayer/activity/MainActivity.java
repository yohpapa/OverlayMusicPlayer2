/**
 * Copyright 2014 Kensuke Nakai<kemumaki.kemuo@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yohpapa.overlaymusicplayer.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.fragment.AlbumListFragment;
import com.yohpapa.overlaymusicplayer.fragment.ArtistListFragment;
import com.yohpapa.overlaymusicplayer.fragment.CommonListFragment;
import com.yohpapa.overlaymusicplayer.fragment.GenreListFragment;
import com.yohpapa.overlaymusicplayer.fragment.PlayListFragment;
import com.yohpapa.overlaymusicplayer.fragment.SettingsFragment;
import com.yohpapa.overlaymusicplayer.fragment.SongListFragment;
import com.yohpapa.tools.PrefUtils;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Fragment[] fragments = new Fragment[] {
		GenreListFragment.getInstance(),
		ArtistListFragment.getInstance(),
		AlbumListFragment.getInstance(),
		PlayListFragment.getInstance(),
		SongListFragment.getInstance(),
		SettingsFragment.getInstance(),
	};
	private final int[] fragmentNames = {
		R.string.tab_genres,
		R.string.tab_artists,
		R.string.tab_albums,
		R.string.tab_playlists,
		R.string.tab_songs,
		R.string.tab_settings,
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setupViewPager();
		setupActionBar();
	}
	
	private void setupViewPager() {
		ViewPager pager = (ViewPager)findViewById(R.id.fragment_pager);
		pager.setAdapter(new TabPagerAdapter(getFragmentManager()));
		pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				ActionBar bar = getActionBar();
				bar.setSelectedNavigationItem(position);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				Log.d(TAG, String.format("onPageScrolled(%d, %f, %d)", position, positionOffset, positionOffsetPixels));
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				Log.d(TAG, String.format("onPageScrollStateChanged(%d)", state));
			}
		});
	}
	
	private void setupActionBar() {
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayShowHomeEnabled(false);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		
		int lastPosition = PrefUtils.getInt(this, R.string.pref_last_tab_position, 0);
		
		for(int i = 0; i < fragmentNames.length; i ++) {
			
			ActionBar.Tab tab = bar.newTab();
			tab.setText(fragmentNames[i]);
			tab.setTabListener(new ActionBar.TabListener() {
				@Override
				public void onTabUnselected(Tab tab, FragmentTransaction ft) {
					Log.d(TAG, "onTabUnselected");
				}
				
				@Override
				public void onTabSelected(Tab tab, FragmentTransaction ft) {
					ViewPager pager = (ViewPager)findViewById(R.id.fragment_pager);
					pager.setCurrentItem(tab.getPosition());
					PrefUtils.setInt(MainActivity.this, R.string.pref_last_tab_position, tab.getPosition());
				}
				
				@Override
				public void onTabReselected(Tab tab, FragmentTransaction ft) {
					Fragment fragment = fragments[tab.getPosition()];
					if(fragment instanceof CommonListFragment) {
						((CommonListFragment)fragment).onTapWhenSelected();
					}
				}
			});
			boolean isSelected = false;
			if(i == lastPosition) {
				isSelected = true;
			}
			bar.addTab(tab, isSelected);
		}
	}
	
	private class TabPagerAdapter extends FragmentPagerAdapter {

		public TabPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments[position];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}
	}
}
