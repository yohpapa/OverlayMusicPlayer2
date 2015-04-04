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

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.adapter.PlayListAdapter;
import com.yohpapa.overlaymusicplayer.service.OverlayMusicPlayerService;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListAdapter;

/**
 * @author YohPapa
 */
public class PlayListFragment extends CommonListFragment {
	
	public static PlayListFragment getInstance() {
		return new PlayListFragment();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
				getActivity(),
				MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
				new String[] {
					MediaStore.Audio.Playlists._ID,
					MediaStore.Audio.Playlists.NAME,
				},
				null, null,
				MediaStore.Audio.Playlists.NAME + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		super.onLoadFinished(loader, cursor);
		
		ListAdapter adapter = new PlayListAdapter(getActivity(), cursor, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClickItem(view);
			}
		});
		setListAdapter(adapter);
		
		resumeListPosition();
	}
	
	@Override
	protected void onClickItem(View view) {
		Long playlistId = (Long)view.getTag(R.id.tag_playlist_id);
		String playlistName = (String)view.getTag(R.id.tag_playlist_name);
		if(playlistId == null) {
			return;
		}
		
		Intent intent = new Intent(getActivity(), OverlayMusicPlayerService.class);
		intent.setAction(OverlayMusicPlayerService.ACTION_SELECT_PLAYLIST);
		intent.putExtra(OverlayMusicPlayerService.PRM_PLAYLIST_ID, playlistId);
		intent.putExtra(OverlayMusicPlayerService.PRM_PLAYLIST_NAME, playlistName);
		intent.putExtra(OverlayMusicPlayerService.PRM_NEED_TO_PLAY_AFTER_SELECT, true);
		getActivity().startService(intent);
	}
}
