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

package com.yohpapa.overlaymusicplayer.adapter;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.adapter.artwork.ArtworkCache;
import com.yohpapa.overlaymusicplayer.adapter.artwork.ArtworkTask;
import com.yohpapa.overlaymusicplayer.service.task.SongInfoList;
import com.yohpapa.tools.PrefUtils;

/**
 * @author YohPapa
 *
 */
public class OverlaySongInfoListAdapter extends BaseAdapter implements StickyListHeadersAdapter {
	
	public interface OnClickListener {
		void onClick(int position);
	}
	
	class HeaderViewHolder {
		public ImageView artwork;
		public TextView artist;
	}
	
	class SongViewHolder {
		public TextView trackIndex;
		public TextView title;
	}
	
	private Context _context = null;
	private SongInfoList _songInfoList = null;
	private OnClickListener _listener = null;
	private ArtworkCache _artworkCache = null;
	
	private final int TEXT_COLOR_DARK;
	private final int TEXT_COLOR_LIGHT;
	private int _textColor = 0;
	
	public OverlaySongInfoListAdapter(Context context, SongInfoList list, OnClickListener listener) {
		_context = context;
		_songInfoList = list;
		_listener = listener;
		
		Resources res = context.getResources();
		TEXT_COLOR_DARK = res.getColor(R.color.overlay_title_dark);
		TEXT_COLOR_LIGHT = res.getColor(R.color.overlay_title_light);
		_textColor = getTextColor();
		
		if(list.isHeaderList) {
			_artworkCache = new ArtworkCache(context);
		}
	}

	@Override
	public int getCount() {
		return _songInfoList.getCount();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View view, ViewGroup group) {
		
		if(view == null) {
			LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.overlay_list_song_item, null);
			SongViewHolder holder = new SongViewHolder();
			holder.trackIndex = (TextView)view.findViewById(R.id.text_track_index);
			holder.title = (TextView)view.findViewById(R.id.text_title);
			view.setTag(holder);
		}
		
		if(position >= _songInfoList.getCount()) {
			return view;
		}
		
		SongViewHolder holder = (SongViewHolder)view.getTag();
		
		holder.trackIndex.setText(String.valueOf(_songInfoList.songIndice[position]));
		holder.trackIndex.setTextColor(_textColor);
		
		if(_songInfoList.getCount() <= position) {
			holder.title.setText(null);
			return view;
		}
		
		holder.title.setText(_songInfoList.titles[position]);
		holder.title.setTextColor(_textColor);
		
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(_listener != null) {
					_listener.onClick(position);
				}
			}
		});
		
		return view;
	}
	
	private int getTextColor() {
		int colorMode = PrefUtils.getInt(_context, R.string.pref_foreground_color, 0);
		if(colorMode == 0) {
			return TEXT_COLOR_DARK;
		} else {
			return TEXT_COLOR_LIGHT;
		}
	}
	
	public boolean isChanged(SongInfoList list) {
		if(list == null) {
			return false;
		}
		
		if(list.getCount() != _songInfoList.getCount()) {
			return true;
		}
		
		long[] newSongIds = list.songIds;
		String[] newTitles = list.titles;
		if(newSongIds == null || newTitles == null) {
			return false;
		}
		
		long[] prevSongIds = _songInfoList.songIds;
		String[] prevTitles = list.titles;
		
		for(int i = 0; i < list.getCount(); i ++) {
			if(prevSongIds[i] != newSongIds[i]) {
				return false;
			}
			
			if(prevTitles[i] == null) {
				if(newTitles[i] != null) {
					return true;
				} else {
					continue;
				}
			}
			
			if(!prevTitles[i].equals(newTitles[i])) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public View getHeaderView(int position, View view, ViewGroup parent) {
		
		if(!_songInfoList.isHeaderList) {
			if(view == null) {
				view = new View(_context);
			}
			return view;
		}
		
		if(view == null) {
			LayoutInflater inflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.overlay_list_header_item, null);
			HeaderViewHolder holder = new HeaderViewHolder();
			holder.artwork = (ImageView)view.findViewById(R.id.image_artwork);
			holder.artist = (TextView)view.findViewById(R.id.text_artist);
			view.setTag(holder);
		}
		
		if(position >= _songInfoList.getCount()) {
			return view;
		}
		
		long albumId = _songInfoList.albumIds[position];
		HeaderViewHolder holder = (HeaderViewHolder)view.getTag();
		
		Bitmap artwork = _artworkCache.get(albumId);
		if(artwork != null) {
			holder.artwork.setImageBitmap(artwork);
		} else {
			new ArtworkTask(_context, albumId, holder.artwork, _artworkCache).execute();
		}
		holder.artwork.setTag(R.id.tag_album_id, albumId);
		
		holder.artist.setTextColor(_textColor);
		holder.artist.setText(_songInfoList.artistNames[position]);
		
		return view;
	}

	@Override
	public long getHeaderId(int position) {
		if(!_songInfoList.isHeaderList) {
			return 0;
		}
		
		if(position >= _songInfoList.getCount()) {
			return 0;
		}
		
		return _songInfoList.albumIds[position];
	}
	
	public void setTextColor(int color) {
		_textColor = color;
	}
	
	public void clearArtworkCache() {
		if(_artworkCache != null) {
			_artworkCache.clear();
		}
	}
}
