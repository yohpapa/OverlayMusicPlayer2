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

package com.yohpapa.overlaymusicplayer;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**
 * @author YohPapa
 */
public class AppVersion {
	private static final String TAG = AppVersion.class.getSimpleName();
	private static String _version = null;
	
	public static void initialize(Context context) {
		try {
			_version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	public static String getVersion() {
		return _version;
	}
}
