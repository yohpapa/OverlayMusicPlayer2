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

import java.util.HashSet;

import com.yohpapa.tools.CursorHelper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

public class GenreSongInfoRetriever extends AsyncTask<Void, Void, SongInfoList> {

	private final long _genreId;
	private final Context _context;
	private final OnFinishRetrievingInfo _onFinish;
	
	public GenreSongInfoRetriever(Context context, long genreId, String genreName, OnFinishRetrievingInfo onFinish) {
		_context = context;
		_genreId = genreId;
		_onFinish = onFinish;
	}
	
	@Override
	protected SongInfoList doInBackground(Void... args) {
		
		ContentResolver resolver = _context.getContentResolver();
		if(resolver == null) {
			return null;
		}
		
		Cursor genreCursor = null;
		Cursor mediaCursor = null;
		try {
			genreCursor = resolver.query(
					MediaStore.Audio.Genres.Members.getContentUri("external", _genreId),
					new String[] {
						MediaStore.Audio.Genres.Members.DATA,
					},
					null, null, null);
			
			if(genreCursor == null || !genreCursor.moveToFirst()) {
				return null;
			}
			
			HashSet<String> songPaths = new HashSet<String>();
			do {
				songPaths.add(CursorHelper.getString(genreCursor, MediaStore.Audio.Genres.Members.DATA));
			} while(genreCursor.moveToNext());
			
			mediaCursor = resolver.query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] {
						MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.DATA,
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.ALBUM_ID,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.TRACK,
					},
					null, null,
					MediaStore.Audio.Media.ARTIST + " ASC, " +
					MediaStore.Audio.Media.ALBUM + " ASC, " +
					MediaStore.Audio.Media.TRACK + " ASC");
			
			if(mediaCursor == null || !mediaCursor.moveToFirst()) {
				return null;
			}
			
			SongInfoList list = new SongInfoList(songPaths.size(), true);
			long[] songIds = new long[songPaths.size()];
			int indexOffset = -1;
			long lastAlbumId = -1L;
			do {
				String path = CursorHelper.getString(mediaCursor, MediaStore.Audio.Media.DATA);
				if(songPaths.contains(path)) {
					
		        	long albumId = CursorHelper.getLong(mediaCursor, MediaStore.Audio.Media.ALBUM_ID);
		        	if(lastAlbumId != albumId) {
		        		indexOffset = -1;
		        		lastAlbumId = albumId;
		        	}
		        	
					int songIndex = CursorHelper.getInt(mediaCursor, MediaStore.Audio.Media.TRACK);
					if(indexOffset == -1) {
						indexOffset = songIndex;
					}
					
					long songId = CursorHelper.getLong(mediaCursor, MediaStore.Audio.Media._ID);
		        	String title = CursorHelper.getString(mediaCursor, MediaStore.Audio.Media.TITLE);
		        	String artistName = CursorHelper.getString(mediaCursor, MediaStore.Audio.Media.ARTIST);
		        	
					list.addSongInfo(songId, songIndex - indexOffset + 1, title, albumId, artistName);
					if(list.getCount() >= songIds.length) {
						break;
					}
				}
				
			} while(mediaCursor.moveToNext());
			
			return list;
			
		} finally {
			if(genreCursor != null) {
				genreCursor.close();
			}
		}
	}

	@Override
	protected void onPostExecute(SongInfoList result) {
		if(_onFinish != null) {
			_onFinish.onFinishRetrieving(result);
		}
	}
}
