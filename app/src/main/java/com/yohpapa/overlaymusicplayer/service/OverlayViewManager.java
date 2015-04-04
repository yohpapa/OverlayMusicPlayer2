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

package com.yohpapa.overlaymusicplayer.service;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yohpapa.overlaymusicplayer.OverlayMusicPlayerApp;
import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.activity.MainActivity;
import com.yohpapa.overlaymusicplayer.adapter.OverlaySongInfoListAdapter;
import com.yohpapa.overlaymusicplayer.service.task.SongInfoList;
import com.yohpapa.tools.MetaDataRetriever;
import com.yohpapa.tools.PrefUtils;
import com.yohpapa.tools.TimecodeUtils;

/**
 * @author YohPapa
 */
public class OverlayViewManager {
	
	private final int TEXT_COLOR_DARK;
	private final int TEXT_COLOR_LIGHT;
	
	private static final int[] panelTextIds = new int[] {
		R.id.text_title,
		R.id.text_artist,
		R.id.text_position,
		R.id.text_duration,
	};
	
	private Context _context = null;
	
	private View _panelView = null;
	private View _openView = null;
	private View _frontView = null;
	
	private WindowManager.LayoutParams _panelParams = null;
	private WindowManager.LayoutParams _openParams = null;
	
	private WindowManager _windowManager = null;
	
	private Handler _timeoutHandler = null;

	public OverlayViewManager(Context context) {
		_context = context;
		_windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		_timeoutHandler = new Handler();
		
		Resources resources = _context.getResources();
		TEXT_COLOR_DARK = resources.getColor(R.color.overlay_title_dark);
		TEXT_COLOR_LIGHT = resources.getColor(R.color.overlay_title_light);
		
		_panelParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		_panelParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		
		_openParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);
		_openParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final int[] panelButtonIds = new int[] {
			R.id.image_artwork,
			R.id.button_hide,
			R.id.button_track_down,
			R.id.button_play_or_pause,
			R.id.button_track_up,
			R.id.button_stop,
			R.id.button_open_close_list,
		};
		final View.OnClickListener[] panelListeners = new View.OnClickListener[] {
			_onArtworkClickListener,
			_onHideClickListener,
			_onTrackDownClickListener,
			_onPlayPauseClickListener,
			_onTrackUpClickListener,
			_onStopClickListener,
			_onOpenCloseListClickListener,
		};
		_panelView = inflater.inflate(R.layout.overlay_play_panel, null);
		setupButtonListener(_panelView, panelButtonIds, panelListeners);
		
		setupPlayPauseButton(_panelView, false);
		
		View layout = _panelView.findViewById(R.id.layout_panel);
		layout.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				startTimeoutTimer();
				return false;
			}
		});
		int defaultColor = _context.getResources().getColor(R.color.overlay_panel_background);
		int color = PrefUtils.getInt(_context, R.string.pref_background_color, defaultColor);
		layout.setBackgroundColor(color);
		
		StickyListHeadersListView list = (StickyListHeadersListView)_panelView.findViewById(R.id.list_current_songs);
		list.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				startTimeoutTimer();
				return false;
			}
		});
		list.setDrawingListUnderStickyHeader(false);
		
		SeekBar bar = (SeekBar)_panelView.findViewById(R.id.seekbar_position);
		bar.setTag(R.id.tag_is_dragging, false);
		bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				seekBar.setTag(R.id.tag_is_dragging, false);
				startTimeoutTimer();
				
				Intent intent = new Intent(_context, OverlayMusicPlayerService.class);
				intent.setAction(OverlayMusicPlayerService.ACTION_SEEK);
				intent.putExtra(OverlayMusicPlayerService.PRM_SEEK_TIME, seekBar.getProgress());
				_context.startService(intent);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				seekBar.setTag(R.id.tag_is_dragging, true);
				stopTimeoutTimer();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser) {
					TextView time = (TextView)_panelView.findViewById(R.id.text_position);
					time.setText(TimecodeUtils.format(progress));
				}
			}
		});
		
		int colorMode = PrefUtils.getInt(_context, R.string.pref_foreground_color, 0);
		if(colorMode == 0) {
			color = TEXT_COLOR_DARK;
		} else {
			color = TEXT_COLOR_LIGHT;
		}
		setTextColorConfiguration(_panelView, panelTextIds, color);
		
		final int[] openButtonIds = new int[] {
			R.id.button_open,
		};
		final View.OnClickListener[] openlListeners = new View.OnClickListener[] {
			_onOpenClickListener,
		};
		_openView = inflater.inflate(R.layout.overlay_open_button, null);
		setupButtonListener(_openView, openButtonIds, openlListeners);

		layout = _openView.findViewById(R.id.layout_open);
		layout.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				View view = _openView.findViewById(R.id.button_open);
				int visiblity = view.getVisibility();
				if(visiblity != View.VISIBLE) {
					view.setVisibility(View.VISIBLE);
				}
				
				startTimeoutTimer();
				
				return false;
			}
		});
	}
	
	private void setTextColorConfiguration(View view, final int[] ids, int color) {
		for(int id : ids) {
			TextView text = (TextView)_panelView.findViewById(id);
			text.setOnClickListener(_onTextClickListener);
			text.setTextColor(color);
		}
	}
	
	private void setupButtonListener(View parent, int[] ids, View.OnClickListener[] listeners) {
		
		for(int i = 0; i < ids.length; i ++) {
			View button = parent.findViewById(ids[i]);
			button.setOnClickListener(listeners[i]);
		}
	}
	
	private void setupPlayPauseButton(View parent, boolean isPlaying) {
		Button button = (Button)_panelView.findViewById(R.id.button_play_or_pause);
		button.setTag(R.id.tag_play_state, isPlaying);
		if(isPlaying) {
			button.setBackgroundResource(android.R.drawable.ic_media_pause);
		} else {
			button.setBackgroundResource(android.R.drawable.ic_media_play);
		}
	}
	
	private final View.OnClickListener _onTextClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int colorMode = PrefUtils.getInt(_context, R.string.pref_foreground_color, 0);
			colorMode = (colorMode + 1) % 2;
			int color;
			if(colorMode == 0) {
				color = TEXT_COLOR_DARK;
			} else {
				color = TEXT_COLOR_LIGHT;
			}
			
			setTextColorConfiguration(_panelView, panelTextIds, color);
			
			TextView text = (TextView)_panelView.findViewById(R.id.text_title);
			text.setTextColor(color);
			text = (TextView)_panelView.findViewById(R.id.text_artist);
			text.setTextColor(color);
			
			StickyListHeadersListView list = (StickyListHeadersListView)_panelView.findViewById(R.id.list_current_songs);
			OverlaySongInfoListAdapter adapter = (OverlaySongInfoListAdapter)list.getAdapter();
			if(adapter != null) {
				adapter.setTextColor(color);
				adapter.notifyDataSetChanged();
			}
			
			PrefUtils.setInt(_context, R.string.pref_foreground_color, colorMode);
			
			startTimeoutTimer();
		}
	};
	
	private final View.OnClickListener _onPlayPauseClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Button button = (Button)_panelView.findViewById(R.id.button_play_or_pause);
			boolean isPlaying = (Boolean)button.getTag(R.id.tag_play_state);
			
			Intent intent = new Intent(_context, OverlayMusicPlayerService.class);
			if(isPlaying) {
				intent.setAction(OverlayMusicPlayerService.ACTION_PAUSE);
			} else {
				intent.setAction(OverlayMusicPlayerService.ACTION_PLAY);
			}
			_context.startService(intent);
			
			startTimeoutTimer();
		}
	};
	
	private final View.OnClickListener _onTrackDownClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(_context, OverlayMusicPlayerService.class);
			intent.setAction(OverlayMusicPlayerService.ACTION_TRACKDOWN);
			_context.startService(intent);
			
			startTimeoutTimer();
		}
	};
	
	private final View.OnClickListener _onTrackUpClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(_context, OverlayMusicPlayerService.class);
			intent.setAction(OverlayMusicPlayerService.ACTION_TRACKUP);
			_context.startService(intent);
			
			startTimeoutTimer();
		}
	};
	
	private final View.OnClickListener _onStopClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(_context, OverlayMusicPlayerService.class);
			intent.setAction(OverlayMusicPlayerService.ACTION_STOP);
			_context.startService(intent);
			
			startTimeoutTimer();
		}
	};
	
	private final View.OnClickListener _onHideClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showView(_openView, _openParams, true);
		}
	};
	
	private final View.OnClickListener _onArtworkClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(_context, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(intent);
			
			showView(_openView, _openParams, true);
		}
	};
	
	private final View.OnClickListener _onOpenCloseListClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			int visibility = View.VISIBLE;
			View list = _panelView.findViewById(R.id.list_current_songs);
			if(list.getVisibility() == View.VISIBLE) {
				visibility = View.GONE;
			}
			list.setVisibility(visibility);
			
			startTimeoutTimer();
		}
	};
	
	private final View.OnClickListener _onOpenClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showView(_panelView, _panelParams, true);
		}
	};

	public void show() {
		showView(_panelView, _panelParams, true);
	}
	
	private void showView(View view, WindowManager.LayoutParams params, boolean startTimer) {
		if(_frontView == view) {
			return;
		}
		
		hide();
		_windowManager.addView(view, params);
		_frontView = view;
		
		if(startTimer) {
			startTimeoutTimer();
		}
	}

	public void hide() {
		if(_frontView == null) {
			return;
		}
		
		_windowManager.removeView(_frontView);
		_frontView = null;
		
		stopTimeoutTimer();
	}
	
	public void setMetaInformation(MetaDataRetriever.MetaData meta) {
		
		if(meta == null) {
			return;
		}
		
		ImageButton artwork = (ImageButton)_panelView.findViewById(R.id.image_artwork);
		if(meta.artwork != null) {
			artwork.setImageBitmap(meta.artwork);
		} else {
			artwork.setImageResource(R.drawable.ic_launcher);
		}
		
		TextView text = (TextView)_panelView.findViewById(R.id.text_title);
		text.setText(meta.title);
		
		text = (TextView)_panelView.findViewById(R.id.text_artist);
		text.setText(meta.artistName);
		
		text = (TextView)_panelView.findViewById(R.id.text_position);
		text.setText(TimecodeUtils.START_POSITION);
		
		SeekBar bar = (SeekBar)_panelView.findViewById(R.id.seekbar_position);
		bar.setMax((int)meta.duration);
		bar.setProgress(0);
		
		text = (TextView)_panelView.findViewById(R.id.text_duration);
		text.setText(TimecodeUtils.format(meta.duration));
	}
	
	public void setListInformation(SongInfoList info) {
		StickyListHeadersListView list = (StickyListHeadersListView)_panelView.findViewById(R.id.list_current_songs);
		OverlaySongInfoListAdapter adapter = (OverlaySongInfoListAdapter)list.getAdapter();
		if(adapter == null || adapter.isChanged(info)) {
			
			if(adapter != null) {
				adapter.clearArtworkCache();
			}
			
			adapter = new OverlaySongInfoListAdapter(_context, info, _onListItemClickListener);
			list.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}
	
	private final OverlaySongInfoListAdapter.OnClickListener _onListItemClickListener = new OverlaySongInfoListAdapter.OnClickListener() {
		@Override
		public void onClick(int position) {
			Intent intent = new Intent(_context, OverlayMusicPlayerService.class);
			intent.setAction(OverlayMusicPlayerService.ACTION_SELECT_INDEX);
			intent.putExtra(OverlayMusicPlayerService.PRM_SONG_INDEX, position);
			intent.putExtra(OverlayMusicPlayerService.PRM_NEED_TO_PLAY_AFTER_SELECT, true);
			_context.startService(intent);
		}
	};
	
	public void setPlayState(boolean isPlaying) {
		setupPlayPauseButton(_panelView, isPlaying);
	}
	
	private void startTimeoutTimer() {
		stopTimeoutTimer();
		
		long timeout = getTimeoutTime();
		if(timeout > 0) {
			_timeoutHandler.postDelayed(_onTimerExpired, timeout);
		}
	}
	
	private final Runnable _onTimerExpired = new Runnable() {
		@Override
		public void run() {
			showView(_openView, _openParams, false);
			
			View view = _openView.findViewById(R.id.button_open);
			view.setVisibility(View.INVISIBLE);
		}
	};
	
	private void stopTimeoutTimer() {
		_timeoutHandler.removeCallbacks(_onTimerExpired);
	}
	
	public void setBackgroundColor(int color) {
		LinearLayout layout = (LinearLayout)_panelView.findViewById(R.id.layout_panel);
		layout.setBackgroundColor(color);
	}
	
	public void setPosition(int position) {
		SeekBar bar = (SeekBar)_panelView.findViewById(R.id.seekbar_position);
		Boolean isDragging = (Boolean)bar.getTag(R.id.tag_is_dragging);
		if(isDragging) {
			return;
		}
		
		bar.setProgress(position);
		
		TextView text = (TextView)_panelView.findViewById(R.id.text_position);
		text.setText(TimecodeUtils.format(position));
	}
	
	private long getTimeoutTime() {
		return PrefUtils.getInt(
				_context,
				R.string.pref_key_time_to_hide, OverlayMusicPlayerApp.DEFAULT_TIME_TO_HIDE) * 1000L;
	}
	
	public void refreshTimeoutTimer() {
		if(_frontView != null) {
			startTimeoutTimer();
		}
	}
}
