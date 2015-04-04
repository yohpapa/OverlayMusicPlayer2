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

package com.yohpapa.overlaymusicplayer.service.task;

import com.yohpapa.tools.CursorHelper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

/**
 * @author YohPapa
 *
 */
public class AllSongInfoRetriever extends AsyncTask<Void, Void, SongInfoList> {

	private Context _context = null;
	private OnFinishRetrievingInfo _listener = null;
	
	public AllSongInfoRetriever(Context context, OnFinishRetrievingInfo listener) {
		_context = context;
		_listener = listener;
	}
	
	@Override
	protected SongInfoList doInBackground(Void... args) {
		ContentResolver resolver = _context.getContentResolver();
		if(resolver == null) {
			return null;
		}
		
		Cursor cursor = null;
		try {
			cursor = resolver.query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						new String[] {
							MediaStore.Audio.Media._ID,
							MediaStore.Audio.Media.TITLE,
						},
						MediaStore.Audio.Media.IS_MUSIC + "!=0", null,
						MediaStore.Audio.Media.TITLE + " ASC");
			
			if(cursor == null || !cursor.moveToFirst()) {
				return null;
			}
			
			SongInfoList list = new SongInfoList(cursor.getCount(), false);
			int index = 0;
			do {
				list.addSongInfo(
						CursorHelper.getLong(cursor, MediaStore.Audio.Media._ID),
						++ index,
						CursorHelper.getString(cursor, MediaStore.Audio.Media.TITLE));
			} while(cursor.moveToNext());
			
			return list;
			
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
	}
	
	@Override
	protected void onPostExecute(SongInfoList result) {
		if(_listener != null) {
			_listener.onFinishRetrieving(result);
		}
	}
}
