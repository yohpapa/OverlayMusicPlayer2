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
import com.yohpapa.overlaymusicplayer.adapter.ArtistListAdapter;
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
public class ArtistListFragment extends CommonListFragment {
	
	public static ArtistListFragment getInstance() {
		return new ArtistListFragment();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(
					getActivity(),
					MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
					new String[] {
						MediaStore.Audio.Artists._ID,
						MediaStore.Audio.Artists.ARTIST,
						MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
					},
					null, null, MediaStore.Audio.Artists.ARTIST + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		super.onLoadFinished(loader, cursor);
		
		ListAdapter adapter = new ArtistListAdapter(getActivity(), cursor, new View.OnClickListener() {
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
		Long artistId = (Long)view.getTag(R.id.tag_artist_id);
		String artistName = (String)view.getTag(R.id.tag_artist_name);
		if(artistId == null) {
			return;
		}
		
		Intent intent = new Intent(getActivity(), OverlayMusicPlayerService.class);
		intent.setAction(OverlayMusicPlayerService.ACTION_SELECT_ARTIST);
		intent.putExtra(OverlayMusicPlayerService.PRM_ARTIST_ID, artistId);
		intent.putExtra(OverlayMusicPlayerService.PRM_ARTIST_NAME, artistName);
		intent.putExtra(OverlayMusicPlayerService.PRM_NEED_TO_PLAY_AFTER_SELECT, true);
		getActivity().startService(intent);
	}
}
