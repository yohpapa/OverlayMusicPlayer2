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

import android.content.Intent;
import android.util.Log;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.service.task.AlbumSongInfoRetriever;
import com.yohpapa.overlaymusicplayer.service.task.AllSongInfoRetriever;
import com.yohpapa.overlaymusicplayer.service.task.ArtistSongInfoRetriever;
import com.yohpapa.overlaymusicplayer.service.task.GenreSongInfoRetriever;
import com.yohpapa.overlaymusicplayer.service.task.OnFinishRetrievingInfo;
import com.yohpapa.overlaymusicplayer.service.task.PlaylistSongInfoRetriever;
import com.yohpapa.overlaymusicplayer.service.task.SongInfoList;
import com.yohpapa.tools.MetaDataRetriever;
import com.yohpapa.tools.MusicPlaybackService;
import com.yohpapa.tools.PrefUtils;
import com.yohpapa.tools.ToastUtils;

public class OverlayMusicPlayerService extends MusicPlaybackService {

	private static final String TAG = OverlayMusicPlayerService.class.getSimpleName();
	private static final String BASE_URI = OverlayMusicPlayerService.class.getName() + ".";
	
	public static final String ACTION_SELECT_ALBUM = BASE_URI + "ACTION_SELECT_ALBUM";
	public static final String PRM_ALBUM_ID = BASE_URI + "PRM_ALBUM_ID";
	public static final String PRM_ALBUM_NAME = BASE_URI + "PRM_ALBUM_NAME";
	public static final String PRM_NEED_TO_PLAY_AFTER_SELECT = BASE_URI + "PRM_NEED_TO_PLAY_AFTER_SELECT";

	public static final String ACTION_SELECT_GENRE = BASE_URI + "ACTION_SELECT_GENRE";
	public static final String PRM_GENRE_ID = BASE_URI + "PRM_GENRE_ID";
	public static final String PRM_GENRE_NAME = BASE_URI + "PRM_GENRE_NAME";
	
	public static final String ACTION_SELECT_ARTIST = BASE_URI + "ACTION_SELECT_ARTIST";
	public static final String PRM_ARTIST_ID = BASE_URI + "PRM_ARTIST_ID";
	public static final String PRM_ARTIST_NAME = BASE_URI + "PRM_ARTIST_NAME";
	
	public static final String ACTION_SELECT_PLAYLIST = BASE_URI + "ACTION_SELECT_PLAYLIST";
	public static final String PRM_PLAYLIST_ID = BASE_URI + "PRM_PLAYLIST_ID";
	public static final String PRM_PLAYLIST_NAME = BASE_URI + "PRM_PLAYLIST_NAME";

	public static final String ACTION_SELECT_SONG = BASE_URI + "ACTION_SELECT_SONG";
	public static final String PRM_SONG_ID = BASE_URI + "PRM_SONG_ID";
	
	public static final String ACTION_PLAY = BASE_URI + "ACTION_PLAY";
	public static final String ACTION_PAUSE = BASE_URI + "ACTION_PAUSE";
	public static final String ACTION_STOP = BASE_URI + "ACTION_STOP";
	public static final String ACTION_TRACKUP = BASE_URI + "ACTION_TRACKUP";
	public static final String ACTION_TRACKDOWN = BASE_URI + "ACTION_TRACKDOWN";
	
	public static final String ACTION_SEEK = BASE_URI + "ACTION_SEEK";
	public static final String PRM_SEEK_TIME = BASE_URI + "PRM_SEEK_TIME";
	
	public static final String ACTION_SELECT_INDEX = BASE_URI + "ACTION_SELECT_INDEX";
	public static final String PRM_SONG_INDEX = BASE_URI + "PRM_SONG_INDEX";
	
	public static final String ACTION_CHANGE_SETTINGS = BASE_URI + "ACTION_CHANGE_SETTINGS";
	public static final String PRM_SETTING_TYPE = BASE_URI + "PRM_SETTING_TYPE";
	public static final int PRM_BACKGROUND_COLOR = 0;
	public static final int PRM_TIME_TO_HIDE = 1;
	
	private OverlayViewManager _overlayManager = null;
	private NotificationViewManager _notificationManager = null;
	private SongInfoList _currentSongList = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		_overlayManager = new OverlayViewManager(this);
		_notificationManager = new NotificationViewManager(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		_overlayManager.hide();
		_notificationManager.hide();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null) {
			return START_REDELIVER_INTENT;
		}
		
		String action = intent.getAction();
		if(ACTION_SELECT_GENRE.equals(action)) {
			onActionSelectGenre(intent);
		} else if(ACTION_SELECT_ARTIST.equals(action)) {
			onActionSelectArtist(intent);
		} else if(ACTION_SELECT_ALBUM.equals(action)) {
			onActionSelectAlbum(intent);
		} else if(ACTION_SELECT_PLAYLIST.equals(action)) {
			onActionSelectPlaylist(intent);
		} else if(ACTION_SELECT_SONG.equals(action)) {
			onActionSelectSong(intent);
		} else if(ACTION_PLAY.equals(action)) {
			onActionPlay(intent);
		} else if(ACTION_PAUSE.equals(action)) {
			onActionPause(intent);
		} else if(ACTION_STOP.equals(action)) {
			onActionStop(intent);
		} else if(ACTION_TRACKUP.equals(action)) {
			onActionTrackUp(intent);
		} else if(ACTION_TRACKDOWN.equals(action)) {
			onActionTrackDown(intent);
		} else if(ACTION_SEEK.equals(action)) {
			onActionSeek(intent);
		} else if(ACTION_SELECT_INDEX.equals(action)) {
			onActionSelectIndex(intent);
		} else if(ACTION_CHANGE_SETTINGS.equals(action)) {
			onActionChangeSettings(intent);
		} else {
			Log.e(TAG, "Unknown request: " + action);
		}
		
		return START_REDELIVER_INTENT;
	}
	
	private void onActionSelectGenre(Intent intent) {
		long genreId = intent.getLongExtra(PRM_GENRE_ID, -1L);
		String genreName = intent.getStringExtra(PRM_GENRE_NAME);
		if(genreId == -1L) {
			return;
		}
		final boolean needToPlay = intent.getBooleanExtra(PRM_NEED_TO_PLAY_AFTER_SELECT, false);
		
		GenreSongInfoRetriever task = new GenreSongInfoRetriever(this, genreId, genreName, new OnFinishRetrievingInfo() {
			@Override
			public void onFinishRetrieving(SongInfoList list) {
				onRetrievedSongInfoList(list, 0, needToPlay);
			}
		});
		task.execute();
	}
	
	private void onRetrievedSongInfoList(SongInfoList list, int index, boolean needToPlay) {
		if(list == null) {
			ToastUtils.show(OverlayMusicPlayerService.this, R.string.msg_no_song);
			return;
		}
		
		selectTrack(index, list.songIds);
		
		_currentSongList = list;
		
		if(needToPlay) {
			playTrack();
			_overlayManager.show();
		}
	}
	
	private void onActionSelectArtist(Intent intent) {
		long artistId = intent.getLongExtra(PRM_ARTIST_ID, -1L);
		String artistName = intent.getStringExtra(PRM_ARTIST_NAME);
		if(artistId == -1L) {
			return;
		}
		final boolean needToPlay = intent.getBooleanExtra(PRM_NEED_TO_PLAY_AFTER_SELECT, false);
		
		ArtistSongInfoRetriever task = new ArtistSongInfoRetriever(this, artistId, artistName, new OnFinishRetrievingInfo() {
			@Override
			public void onFinishRetrieving(SongInfoList list) {
				onRetrievedSongInfoList(list, 0, needToPlay);
			}
		});
		task.execute();
	}
	
	private void onActionSelectAlbum(Intent intent) {
		long albumId = intent.getLongExtra(PRM_ALBUM_ID, -1L);
		String albumName = intent.getStringExtra(PRM_ALBUM_NAME);
		if(albumId == -1L) {
			return;
		}
		final boolean needToPlay = intent.getBooleanExtra(PRM_NEED_TO_PLAY_AFTER_SELECT, false);
		
		AlbumSongInfoRetriever task = new AlbumSongInfoRetriever(this, albumId, albumName, new OnFinishRetrievingInfo() {
			@Override
			public void onFinishRetrieving(SongInfoList list) {
				onRetrievedSongInfoList(list, 0, needToPlay);
			}
		});
		task.execute();
	}
	
	private void onActionSelectPlaylist(Intent intent) {
		long playlistId = intent.getLongExtra(PRM_PLAYLIST_ID, -1L);
		String playlistName = intent.getStringExtra(PRM_PLAYLIST_NAME);
		if(playlistId == -1L) {
			return;
		}
		final boolean needToPlay = intent.getBooleanExtra(PRM_NEED_TO_PLAY_AFTER_SELECT, false);
		
		PlaylistSongInfoRetriever task = new PlaylistSongInfoRetriever(this, playlistId, playlistName, new OnFinishRetrievingInfo() {
			@Override
			public void onFinishRetrieving(SongInfoList list) {
				onRetrievedSongInfoList(list, 0, needToPlay);
			}
		});
		task.execute();
	}
	
	private void onActionSelectSong(Intent intent) {
		final long songId = intent.getLongExtra(PRM_SONG_ID, -1L);
		if(songId == -1L) {
			return;
		}
		final boolean needToPlay = intent.getBooleanExtra(PRM_NEED_TO_PLAY_AFTER_SELECT, false);
		
		AllSongInfoRetriever task = new AllSongInfoRetriever(this, new OnFinishRetrievingInfo() {
			@Override
			public void onFinishRetrieving(SongInfoList list) {
				if(list == null || list.songIds == null || list.songIds.length <= 0) {
					return;
				}
				
				int index = 0;
				for(int i = 0; i < list.songIds.length; i ++) {
					if(songId == list.songIds[i]) {
						index = i;
						break;
					}
				}
				onRetrievedSongInfoList(list, index, needToPlay);
			}
		});
		task.execute();
	}
	
	private void onActionPlay(Intent intent) {
		playTrack();
	}
	
	private void onActionPause(Intent intent) {
		pauseTrack();
	}
	
	private void onActionStop(Intent intent) {
		stopTrack();
		_overlayManager.hide();
	}
	
	private void onActionTrackUp(Intent intent) {
		nextTrack();
	}
	
	private void onActionTrackDown(Intent intent) {
		prevTrack();
	}
	
	private void onActionSeek(Intent intent) {
		int time = intent.getIntExtra(PRM_SEEK_TIME, -1);
		if(time == -1) {
			return;
		}
		
		seekTrack(time);
	}
	
	private void onActionSelectIndex(Intent intent) {
		
		if(_currentSongList == null) {
			return;
		}

		int index = intent.getIntExtra(PRM_SONG_INDEX, -1);
		if(index == -1) {
			return;
		}
		
		long[] songIds = _currentSongList.songIds;
		if(songIds == null || index >= songIds.length) {
			return;
		}
		
		onRetrievedSongInfoList(_currentSongList, index, true);
	}
	
	private void onActionChangeSettings(Intent intent) {
		int type = intent.getIntExtra(PRM_SETTING_TYPE, PRM_BACKGROUND_COLOR);
		if(type == PRM_BACKGROUND_COLOR) {
			int defaultColor = getResources().getColor(R.color.overlay_panel_background);
			int color = PrefUtils.getInt(this, R.string.pref_background_color, defaultColor);
			_overlayManager.setBackgroundColor(color);
		} else if(type == PRM_TIME_TO_HIDE) {
			_overlayManager.refreshTimeoutTimer();
		}
	}
	
	@Override
	protected void onTrackChanged() {
		MetaDataRetriever.MetaData meta = getCurrentTrackInfo();
		
		_overlayManager.setMetaInformation(meta);
		_overlayManager.setListInformation(_currentSongList);
		_notificationManager.updateMetaData(meta);
	}
	
	@Override
	protected void onPlayStateChanged(int playState) {
		boolean isPlaying = playState == PLAY_STATE_PLAYING;
		
		_overlayManager.setPlayState(isPlaying);
		_notificationManager.updatePlayState(isPlaying);
	}
	
	@Override
	protected void onTimecodeChanged(int position) {
		_overlayManager.setPosition(position);
	}
}
