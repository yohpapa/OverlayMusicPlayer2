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

package com.yohpapa.overlaymusicplayer.fragment;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListAdapter;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.adapter.AlbumListAdapter;
import com.yohpapa.overlaymusicplayer.service.OverlayMusicPlayerService;

public class AlbumListFragment extends ArtworkCacheListFragment {

	public static AlbumListFragment getInstance() {
		return new AlbumListFragment();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
				getActivity(),
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                    MediaStore.Audio.Albums.ARTIST,
                    MediaStore.Audio.Albums.ALBUM_ART,
                },
                null, null, MediaStore.Audio.Albums.ALBUM + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		super.onLoadFinished(loader, cursor);
		
		ListAdapter adapter = new AlbumListAdapter(getActivity(), cursor, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClickItem(view);
			}
		}, getArtworkCache());
		setListAdapter(adapter);
		
		resumeListPosition();
	}
	
	@Override
	protected void onClickItem(View view) {
		long albumId = (Long)view.getTag(R.id.tag_album_id);
		String albumName = (String)view.getTag(R.id.tag_album_name);
		
		Intent intent = new Intent(getActivity(), OverlayMusicPlayerService.class);
		intent.setAction(OverlayMusicPlayerService.ACTION_SELECT_ALBUM);
		intent.putExtra(OverlayMusicPlayerService.PRM_ALBUM_ID, albumId);
		intent.putExtra(OverlayMusicPlayerService.PRM_ALBUM_NAME, albumName);
		intent.putExtra(OverlayMusicPlayerService.PRM_NEED_TO_PLAY_AFTER_SELECT, true);
		getActivity().startService(intent);
	}
}
