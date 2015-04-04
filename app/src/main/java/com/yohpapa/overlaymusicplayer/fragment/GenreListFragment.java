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

import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.adapter.GenreListAdapter;
import com.yohpapa.overlaymusicplayer.fragment.loader.GenreCursorLoader;
import com.yohpapa.overlaymusicplayer.service.OverlayMusicPlayerService;

/**
 * @author YohPapa
 */
public class GenreListFragment extends CommonListFragment {

	public static GenreListFragment getInstance() {
		return new GenreListFragment();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new GenreCursorLoader(getActivity());
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		super.onLoadFinished(loader, cursor);
		
		ListAdapter adapter = new GenreListAdapter(getActivity(), cursor, new View.OnClickListener() {
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
		long genreId = (Long)view.getTag(R.id.tag_genre_id);
		String genreName = (String)view.getTag(R.id.tag_genre_name);
		
		Intent intent = new Intent(getActivity(), OverlayMusicPlayerService.class);
		intent.setAction(OverlayMusicPlayerService.ACTION_SELECT_GENRE);
		intent.putExtra(OverlayMusicPlayerService.PRM_GENRE_ID, genreId);
		intent.putExtra(OverlayMusicPlayerService.PRM_GENRE_NAME, genreName);
		intent.putExtra(OverlayMusicPlayerService.PRM_NEED_TO_PLAY_AFTER_SELECT, true);
		getActivity().startService(intent);
	}
}
