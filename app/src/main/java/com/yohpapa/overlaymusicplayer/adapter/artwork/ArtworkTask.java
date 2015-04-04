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

package com.yohpapa.overlaymusicplayer.adapter.artwork;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.tools.CursorHelper;

public class ArtworkTask extends AsyncTask<Void, Void, Bitmap> {

	private Context _context = null;
	private long _albumId = -1L;
	private ImageView _artwork = null;
	private ArtworkCache _cache = null;
	
	public ArtworkTask(Context context, long albumId, ImageView artwork, ArtworkCache cache) {
		_context = context;
		_albumId = albumId;
		_artwork = artwork;
		_cache = cache;
		
		_artwork.setImageBitmap(null);
	}
	
	@Override
	protected Bitmap doInBackground(Void... params) {
		
		synchronized (ArtworkTask.class) {
			
			Bitmap cache = _cache.get(_albumId);
			if(cache != null) {
				return cache;
			}
		
			Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, _albumId);
            if(uri == null) {
                return null;
            }
            Cursor cursor = null;
            try {
	            cursor = _context.getContentResolver().query(
	                    uri,
	                    new String[] {
	                        MediaStore.Audio.Albums.ALBUM_ART,
	                    }, null, null, null);
	
	            if(cursor == null || !cursor.moveToFirst()) {
	                return null;
	            }
	
	            String artworkPath = CursorHelper.getString(cursor, MediaStore.Audio.Albums.ALBUM_ART);
	            Bitmap result = BitmapFactory.decodeFile(artworkPath);
	            if(artworkPath != null && artworkPath.length() > 0) {
		            result = BitmapFactory.decodeFile(artworkPath);
	            }
	            _cache.put(_albumId, result);
	            
	            return result;

            } finally {
            	if(cursor != null) {
            		cursor.close();
            	}
            }
		}
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		Long currentId = (Long)_artwork.getTag(R.id.tag_album_id);
		if(currentId == null) {
			return;
		}
		
		if(_albumId != currentId) {
			return;
		}
		
		if(result != null) {
			_artwork.setImageBitmap(result);
		} else {
			_artwork.setImageResource(R.drawable.ic_launcher);
		}
	}
}