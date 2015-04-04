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

package com.yohpapa.overlaymusicplayer.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yohpapa.overlaymusicplayer.AppVersion;
import com.yohpapa.overlaymusicplayer.OverlayMusicPlayerApp;
import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.service.OverlayMusicPlayerService;
import com.yohpapa.tools.AssetsUtils;
import com.yohpapa.tools.PrefUtils;

public class SettingsFragment extends PreferenceFragment {

	public static SettingsFragment getInstance() {
		return new SettingsFragment();
	}
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		addPreferencesFromResource(R.xml.preferences);
		
		final Context parent = getActivity();
		
		Preference pref = findPreference(parent.getString(R.string.pref_key_version));
		pref.setSummary(AppVersion.getVersion());
		
		pref = findPreference(parent.getString(R.string.pref_key_background_color));
		pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(!(newValue instanceof Integer))
					return false;
				
				Context context = getActivity();
				PrefUtils.setInt(context, R.string.pref_background_color, ((Integer)newValue).intValue());
				
				Intent intent = new Intent(context, OverlayMusicPlayerService.class);
				intent.setAction(OverlayMusicPlayerService.ACTION_CHANGE_SETTINGS);
				intent.putExtra(OverlayMusicPlayerService.PRM_SETTING_TYPE, OverlayMusicPlayerService.PRM_BACKGROUND_COLOR);
				context.startService(intent);
				return true;
			}
		});
		
		updateTimeToHideSummary();
		pref = findPreference(parent.getString(R.string.pref_key_time_to_hide));
		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			private int _tmpTimeToHide;
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				LayoutInflater inflater = (LayoutInflater)parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.pref_time_to_hide, null);
				final SeekBar seekBar = (SeekBar)layout.findViewById(R.id.pref_time_to_hide_seekbar);
				final TextView valueText = (TextView)layout.findViewById(R.id.pref_time_to_hide_text);
				
				_tmpTimeToHide = PrefUtils.getInt(parent, R.string.pref_key_time_to_hide, OverlayMusicPlayerApp.DEFAULT_TIME_TO_HIDE);
				seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {}
					
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {}
					
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						_tmpTimeToHide = progress;
						String text = getTimeToHideText(parent, _tmpTimeToHide);
						valueText.setText(text);
					}
				});
				seekBar.setProgress(_tmpTimeToHide);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(parent);
				builder.setTitle(R.string.pref_title_time_to_hide);
				builder.setView(layout);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PrefUtils.setInt(parent, R.string.pref_key_time_to_hide, _tmpTimeToHide);
						updateTimeToHideSummary();
						
						Intent intent = new Intent(parent, OverlayMusicPlayerService.class);
						intent.setAction(OverlayMusicPlayerService.ACTION_CHANGE_SETTINGS);
						intent.putExtra(OverlayMusicPlayerService.PRM_SETTING_TYPE, OverlayMusicPlayerService.PRM_TIME_TO_HIDE);
						parent.startService(intent);
					}
				});
				builder.show();

				return true;
			}
		});
		
		pref = findPreference(parent.getString(R.string.pref_key_licenses));
		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				LayoutInflater inflater = (LayoutInflater)parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.dialog_licenses, null);
				
				String license = AssetsUtils.readTextFile(parent, "LICENSES.txt");
				if(TextUtils.isEmpty(license))
					return true;
				
				TextView view = (TextView)layout.findViewById(R.id.text_licenses);
				view.setText(license);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(parent);
				builder.setTitle(R.string.pref_title_licenses);
				builder.setView(layout);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				});
				builder.show();
				return true;
			}
		});
	}
	
	private void updateTimeToHideSummary() {
		Context parent = getActivity();
		Preference pref = findPreference(parent.getString(R.string.pref_key_time_to_hide));
		pref.setSummary(getTimeToHideText(
							parent,
							PrefUtils.getInt(
									parent,
									R.string.pref_key_time_to_hide,
									OverlayMusicPlayerApp.DEFAULT_TIME_TO_HIDE)));

	}
	
	private String getTimeToHideText(Context context, int time) {
		String summary = null;
		if(time == 0) {
			summary = context.getString(R.string.pref_time_to_hide_keep);
		} else if(time == 1) {
			summary = "1 " + context.getString(R.string.pref_time_to_hide_minite);
		} else {
			summary = time + " " + context.getString(R.string.pref_time_to_hide_minites);
		}
		return summary;
	}
}
