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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author YohPapa
 */
public class PrefUtils {
	
	private static final String PREFS_NAME = PrefUtils.class.getName();

	public static int getInt(Context context, int id, int defaultValue) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(context.getString(id), defaultValue);
	}
	
	public static boolean setInt(Context context, int id, int value) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(context.getString(id), value);
		return editor.commit();
	}
}
