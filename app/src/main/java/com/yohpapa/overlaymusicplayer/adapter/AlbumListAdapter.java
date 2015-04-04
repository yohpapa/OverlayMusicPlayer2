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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.adapter.artwork.ArtworkCache;
import com.yohpapa.overlaymusicplayer.adapter.artwork.ArtworkTask;
import com.yohpapa.tools.CursorHelper;

public class AlbumListAdapter extends CursorAdapter {
	
	private LayoutInflater _inflater = null;
	private View.OnClickListener _listener = null;
	private ArtworkCache _artworkCache = null;
	
	public class ViewHolder {
		public ImageView artwork;
		public TextView title;
		public TextView artist;
	}

	public AlbumListAdapter(Context context, Cursor cursor, View.OnClickListener listener, ArtworkCache cache) {
		super(context, cursor, true);
		
		_inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_listener = listener;
		_artworkCache = cache;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		long albumId = CursorHelper.getLong(cursor, MediaStore.Audio.Albums._ID);
		String albumName = CursorHelper.getString(cursor, MediaStore.Audio.Albums.ALBUM);
		String artistName = CursorHelper.getString(cursor, MediaStore.Audio.Albums.ARTIST);
		
		ViewHolder holder = (ViewHolder)view.getTag();
		holder.title.setText(albumName);
		holder.artist.setText(artistName);

		Bitmap artwork = _artworkCache.get(albumId);
		if(artwork != null) {
			holder.artwork.setImageBitmap(artwork);
		} else {
			new ArtworkTask(context, albumId, holder.artwork, _artworkCache).execute();
		}
		
		view.setTag(R.id.tag_album_id, albumId);
		view.setTag(R.id.tag_album_name, albumName);
		holder.artwork.setTag(R.id.tag_album_id, albumId);
		
		view.setOnClickListener(_listener);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View layout = _inflater.inflate(R.layout.list_album_item, null);
		
		ViewHolder holder = new ViewHolder();
		holder.artwork = (ImageView)layout.findViewById(R.id.image_artwork);
		holder.title = (TextView)layout.findViewById(R.id.text_title);
		holder.artist = (TextView)layout.findViewById(R.id.text_artist);
		layout.setTag(holder);
		
		return layout;
	}
}
