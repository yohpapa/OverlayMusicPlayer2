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

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * @author YohPapa
 */
public abstract class CommonListFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnTapSelectedTabListener {

	private int _lastPosition = 0;
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		
		if(savedState != null) {
			_lastPosition = savedState.getInt("_lastPosition");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		_lastPosition = getListLastPosition();
	}
	
	private int getListLastPosition() {
		ListView list = getListView();
		if(list == null) {
			return 0;
		}
		
		return list.getFirstVisiblePosition();
	}
	
	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		
		if(savedState != null) {
			_lastPosition = savedState.getInt("_lastPosition");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// It is not possible to get contents, that is, views on the fragment.
		// The reason is not clear for me but I guess that ...
		// It is because ViewPager which is parent for the fragment,
		// destroys contents of the fragment on the process of transition
		// from foreground to background.
		
		if(outState != null) {
			outState.putInt("_lastPosition", _lastPosition);
		}
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		ListView list = getListView();
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onClickItem(view);
			}
		});
	}
	
	protected void resumeListPosition() {
		ListView list = getListView();
		list.setSelectionFromTop(_lastPosition, 0);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {}
	
	protected abstract void onClickItem(View view);
	
	@Override
	public void onTapWhenSelected() {
		if(isResumed()) {
			ListView list = getListView();
			list.setSelection(0);
		}
	}
}
