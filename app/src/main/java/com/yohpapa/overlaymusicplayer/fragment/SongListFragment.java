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
import com.yohpapa.overlaymusicplayer.adapter.SongListAdapter;
import com.yohpapa.overlaymusicplayer.service.OverlayMusicPlayerService;

/**
 * @author YohPapa
 *
 */
public class SongListFragment extends ArtworkCacheListFragment {
	
	public static SongListFragment getInstance() {
		return new SongListFragment();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
					getActivity(),
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] {
						MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM_ID,
					},
					MediaStore.Audio.Media.IS_MUSIC + "!=0", null,
					MediaStore.Audio.Media.TITLE + " ASC");
	}

	@Override
	public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
		super.onLoadFinished(loader, cursor);
		
		ListAdapter adapter = new SongListAdapter(getActivity(), cursor, new View.OnClickListener() {
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
		Long songId = (Long)view.getTag(R.id.tag_song_id);
		if(songId == null) {
			return;
		}
		
		Intent intent = new Intent(getActivity(), OverlayMusicPlayerService.class);
		intent.setAction(OverlayMusicPlayerService.ACTION_SELECT_SONG);
		intent.putExtra(OverlayMusicPlayerService.PRM_SONG_ID, songId);
		intent.putExtra(OverlayMusicPlayerService.PRM_NEED_TO_PLAY_AFTER_SELECT, true);
		getActivity().startService(intent);
	}
}
