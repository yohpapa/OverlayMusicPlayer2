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

package com.yohpapa.tools;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

public class MetaDataRetriever extends AsyncTask<Void, Void, MetaDataRetriever.MetaData> {

    public class MetaData {
        public final long trackId;
        public final String title;
        public final String artistName;
        public final String albumName;
        public final Bitmap artwork;
        public final Bitmap smallArtwork;
        public final long duration;

        public MetaData(
        		long trackId, String title, String artistName, String albumName, Bitmap artwork, Bitmap smallArtwork, long duration) {
            this.trackId = trackId;
            this.title = title;
            this.artistName = artistName;
            this.albumName = albumName;
            this.artwork = artwork;
            this.smallArtwork = smallArtwork;
            this.duration = duration;
        }

        public MetaData deepCopy() {
        	return new MetaData(this.trackId, this.title, this.artistName, this.albumName, this.artwork, this.smallArtwork, this.duration);
        }
    }

    public interface OnRetrieveMetaDataFinished {
        void onRetrievedMetaData(MetaData data);
    }

    private final Context context;
    private final long trackId;
    private final OnRetrieveMetaDataFinished onFinished;
    private final boolean needSmallIcon;

    public MetaDataRetriever(Context context, long trackId, OnRetrieveMetaDataFinished onFinished, boolean needSmallIcon) {
        this.context = context;
        this.trackId = trackId;
        this.onFinished = onFinished;
        this.needSmallIcon = needSmallIcon;
    }

    @Override
    protected MetaDataRetriever.MetaData doInBackground(Void... params) {

        ContentResolver resolver = context.getContentResolver();
        Cursor trackCursor = null;
        Cursor albumCursor = null;
        try {
            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);
            if(uri == null) {
                return null;
            }

            trackCursor = resolver.query(
                    uri,
                    new String[] {
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.DURATION,
                    }, null, null, null);

            if(trackCursor == null || !trackCursor.moveToFirst() || trackCursor.getCount() != 1) {
                return null;
            }

            String title = CursorHelper.getString(trackCursor, MediaStore.Audio.Media.TITLE);
            String artist = CursorHelper.getString(trackCursor, MediaStore.Audio.Media.ARTIST);
            String album = CursorHelper.getString(trackCursor, MediaStore.Audio.Media.ALBUM);
            long albumId = CursorHelper.getLong(trackCursor, MediaStore.Audio.Media.ALBUM_ID);
            long duration = CursorHelper.getLong(trackCursor, MediaStore.Audio.Media.DURATION);
            Bitmap artwork = null;

            if(albumId != -1L) {
                uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId);
                if(uri == null) {
                    return null;
                }
                albumCursor = resolver.query(
                        uri,
                        new String[] {
                                MediaStore.Audio.Albums.ALBUM_ART,
                        }, null, null, null);

                if(albumCursor == null || !albumCursor.moveToFirst() || albumCursor.getCount() != 1) {
                    return new MetaData(trackId, title, artist, album, null, null, duration);
                }

                String artworkPath = CursorHelper.getString(albumCursor, MediaStore.Audio.Albums.ALBUM_ART);
                artwork = BitmapFactory.decodeFile(artworkPath);
            }

            Bitmap smallIcon = null;
            if(artwork != null && needSmallIcon) {
            	
            	Resources res = context.getResources();
            	
            	float ratio = 0.0f;
            	int width = artwork.getWidth();
            	int height = artwork.getHeight();
            	if(width >= height) {
            		ratio = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_width) / (float)width;
            	} else {
            		ratio = res.getDimensionPixelSize(android.R.dimen.notification_large_icon_height) / (float)height;
            	}
            	
            	smallIcon = Bitmap.createScaledBitmap(artwork, (int)(width * ratio), (int)(height * ratio), false);
            }
            
            return new MetaData(trackId, title, artist, album, artwork, smallIcon, duration);

        } finally {
            if(trackCursor != null) {
                trackCursor.close();
            }
            if(albumCursor != null) {
                albumCursor.close();
            }
        }
    }

    @Override
    protected void onPostExecute(MetaData metaData) {
        if(onFinished != null) {
            onFinished.onRetrievedMetaData(metaData);
        }
    }
}
