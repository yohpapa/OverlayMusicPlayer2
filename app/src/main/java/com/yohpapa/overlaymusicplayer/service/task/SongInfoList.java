/*
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

package com.yohpapa.overlaymusicplayer.service.task;

/**
 * @author YohPapa
 */
public class SongInfoList {

	public boolean isHeaderList = false;
	
	public long[] songIds = null;
	public int[] songIndice = null;
	public String[] titles = null;
	public long[] albumIds = null;
	public String[] artistNames = null;
	
	private int _position = 0;
	
	public SongInfoList(int length, boolean hasHeader) {
		isHeaderList = hasHeader;
		songIds = new long[length];
		songIndice = new int[length];
		titles = new String[length];
		_position = 0;
		
		if(hasHeader) {
			albumIds = new long[length];
			artistNames = new String[length];
		}
	}
	
	public void addSongInfo(long songId, int songIndex, String title, long albumId, String artistName) {
		
		if(isHeaderList) {
			albumIds[_position] = albumId;
			artistNames[_position] = artistName;
		}
		
		addSongInfo(songId, songIndex, title);
	}
	
	public void addSongInfo(long songId, int songIndex, String title) {
		songIds[_position] = songId;
		songIndice[_position] = songIndex;
		titles[_position] = title;
		
		_position ++;
	}
	
	public int getCount() {
		return _position;
	}
}
