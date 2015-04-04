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
 */
public class ArtistSongInfoRetriever extends AsyncTask<Void, Void, SongInfoList> {

	private Context _context = null;
	private long _artistId = -1L;
	private OnFinishRetrievingInfo _listener = null;
	
	public ArtistSongInfoRetriever(Context context, long artistId, String artistName, OnFinishRetrievingInfo listener) {
		_context = context;
		_artistId = artistId;
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
							MediaStore.Audio.Media.ALBUM_ID,
							MediaStore.Audio.Media.ARTIST,
							MediaStore.Audio.Media.TRACK,
						},
						MediaStore.Audio.Media.ARTIST_ID + "=?", new String[] {String.valueOf(_artistId)},
						MediaStore.Audio.Media.ARTIST + " ASC, "	+
						MediaStore.Audio.Media.ALBUM + " ASC, "	+
						MediaStore.Audio.Media.TRACK + " ASC");
			
			if(cursor == null || !cursor.moveToFirst()) {
				return null;
			}
			
			SongInfoList list = new SongInfoList(cursor.getCount(), true);
			int indexOffset = -1;
			long lastAlbumId = -1L;
			do {
	        	long albumId = CursorHelper.getLong(cursor, MediaStore.Audio.Media.ALBUM_ID);
	        	if(lastAlbumId != albumId) {
	        		indexOffset = -1;
	        		lastAlbumId = albumId;
	        	}
	        	
				int songIndex = CursorHelper.getInt(cursor, MediaStore.Audio.Media.TRACK);
				if(indexOffset == -1) {
					indexOffset = songIndex;
				}
				
				long songId = CursorHelper.getLong(cursor, MediaStore.Audio.Media._ID);
	        	String title = CursorHelper.getString(cursor, MediaStore.Audio.Media.TITLE);
	        	String artistName = CursorHelper.getString(cursor, MediaStore.Audio.Media.ARTIST);
	        	
				list.addSongInfo(songId, songIndex - indexOffset + 1, title, albumId, artistName);

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
