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

package com.yohpapa.overlaymusicplayer.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.yohpapa.overlaymusicplayer.R;
import com.yohpapa.overlaymusicplayer.activity.MainActivity;
import com.yohpapa.tools.MetaDataRetriever;

/**
 * @author YohPapa
 */
public class NotificationViewManager {

	private Service _service = null;
	private NotificationCompat.Builder _builder = null;
	
	public NotificationViewManager(Service service) {
		_service = service;
		_builder = new NotificationCompat.Builder(_service);
		
		Intent intent = new Intent(_service, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pending = PendingIntent.getActivity(_service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		_builder.setContentIntent(pending);
	}
	
	public void updateMetaData(MetaDataRetriever.MetaData meta) {
		_builder.setContentTitle(meta.title);
		_builder.setContentText(meta.artistName);
		
		if(meta.smallArtwork != null) {
			_builder.setLargeIcon(meta.smallArtwork);
		} else {
			_builder.setLargeIcon(null);
		}
		
		update();
	}
	
	public void updateTimecode(int msec) {

		int minutes = msec / 1000 / 60;
		int seconds = (msec - (minutes * 1000 * 60)) / 1000;
		String timecode = minutes + ":" + seconds;
		_builder.setContentInfo(timecode);
		
		update();
	}
	
	public void updatePlayState(boolean isPlaying) {
		int icon = android.R.drawable.ic_media_pause;
		if(isPlaying) {
			icon = android.R.drawable.ic_media_play;
		}
		_builder.setSmallIcon(icon);
		
		update();
	}
	
	private void update() {
		_service.startForeground(R.id.notification_id, _builder.build());
	}
	
	public void hide() {
		_service.stopForeground(true);
	}
}
