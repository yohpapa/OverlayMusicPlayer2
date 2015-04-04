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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.fragment.loader.GenreCursorLoader;
import com.yohpapa.tools.CursorHelper;

public class GenreListAdapter extends CursorAdapter {

	private LayoutInflater _inflater = null;
	private View.OnClickListener _listener = null;
	
	public class ViewHolder {
		public TextView genre;
	}
	
	public GenreListAdapter(Context context, Cursor cursor, View.OnClickListener listener) {
		super(context, cursor, true);
		
		_inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_listener = listener;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		long genreId = CursorHelper.getLong(cursor, GenreCursorLoader._ID);
		String genreName = CursorHelper.getString(cursor, GenreCursorLoader.NAME);
		
		ViewHolder holder = (ViewHolder)view.getTag();
		holder.genre.setText(genreName);
		
		view.setTag(R.id.tag_genre_id, genreId);
		view.setTag(R.id.tag_genre_name, genreName);
		
		view.setOnClickListener(_listener);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View layout = _inflater.inflate(R.layout.list_genre_item, null);
		
		ViewHolder holder = new ViewHolder();
		holder.genre = (TextView)layout.findViewById(R.id.text_genre);
		layout.setTag(holder);
		
		return layout;
	}
}
