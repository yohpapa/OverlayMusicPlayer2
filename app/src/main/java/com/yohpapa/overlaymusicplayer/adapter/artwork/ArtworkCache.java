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

import java.util.HashSet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import com.yohpapa.overlaymusicplayer.R;

/**
 * @author YohPapa
 */
public class ArtworkCache {

	// Use 1/8th of the available memory for this memory cache.
	private final int ARTWORK_CACHE_SIZE = (int)(Runtime.getRuntime().maxMemory() / 1024) / 8;
	
	private LruCache<Long, Bitmap> _artworkCache = null;
	private HashSet<Long> _noArtworkList = null;
	private Bitmap _defaultArtwork = null;
	
	public ArtworkCache(Context context) {
		_artworkCache = new LruCache<Long, Bitmap>(ARTWORK_CACHE_SIZE) {
	        @Override
	        protected int sizeOf(Long key, Bitmap bitmap) {
	            return bitmap.getByteCount() / 1024;
	        }
	    };
	    _noArtworkList = new HashSet<Long>();
	    _defaultArtwork = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
	}
	
	public Bitmap get(long albumId) {
		if(_noArtworkList.contains(albumId)) {
			return _defaultArtwork;
		}
		
		return _artworkCache.get(albumId);
	}
	
	public void put(long albumId, Bitmap artwork) {
		if(artwork == null) {
			_noArtworkList.add(albumId);
			return;
		}
		
		_artworkCache.put(albumId, artwork);
	}
	
	public void clear() {
		_noArtworkList.clear();
		_artworkCache.evictAll();
	}
}
