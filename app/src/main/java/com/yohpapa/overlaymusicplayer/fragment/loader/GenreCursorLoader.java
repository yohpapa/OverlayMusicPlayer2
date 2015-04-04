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

package com.yohpapa.overlaymusicplayer.fragment.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.MediaStore;

import com.yohpapa.tools.CursorHelper;

public class GenreCursorLoader extends SimpleCursorLoader {
	
	public static final String _ID = "_id";
	public static final String NAME = "name";

	public GenreCursorLoader(Context context) {
		super(context);
	}

	@Override
	public Cursor loadInBackground() {
		ContentResolver resolver = getContext().getContentResolver();
		if(resolver == null) {
			return null;
		}
		
		Cursor cursor = null;
		try {
			cursor = resolver.query(
					MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
					new String[] {
						MediaStore.Audio.Genres._ID,
						MediaStore.Audio.Genres.NAME,
					},
					null, null, MediaStore.Audio.Genres.NAME + " ASC");
			
			if(cursor == null || !cursor.moveToFirst()) {
				return null;
			}
			
			List<Long> genreIds = new ArrayList<Long>();
			HashMap<Long, String> genreNameTable = new HashMap<Long, String>();
			do {
				long genreId = CursorHelper.getLong(cursor, MediaStore.Audio.Genres._ID);
				String genreName = CursorHelper.getString(cursor, MediaStore.Audio.Genres.NAME);
				
				genreIds.add(genreId);
				genreNameTable.put(genreId, genreName);
				
			} while(cursor.moveToNext());
			
			List<Long> excludes = new ArrayList<Long>();
			for(int i = 0; i < genreIds.size(); i ++) {
				Cursor genre = null;
				long genreId = genreIds.get(i);
				try {
					genre = resolver.query(
							MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
							null,
							MediaStore.Audio.Genres.Members.IS_MUSIC + "!=0", null, null);
					
					if(genre == null || !genre.moveToFirst()) {
						excludes.add(genreId);
					}
					
				} finally {
					if(genre != null) {
						genre.close();
					}
				}
			}
			
			genreIds.removeAll(excludes);
			
			MatrixCursor result = new MatrixCursor(new String[] {
				_ID, NAME
			});
			for(int i = 0; i < genreIds.size(); i ++) {
				long genreId = genreIds.get(i);
				result.addRow(new Object[] {genreId, genreNameTable.get(genreId)});
			}
			
			return result;
			
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}
	}
}
