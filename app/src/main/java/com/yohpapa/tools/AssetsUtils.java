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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author YohPapa
 *
 */
public class AssetsUtils {
	
	private static final String TAG = AssetsUtils.class.getSimpleName();
	private static final String CR = System.getProperty("line.separator");

	public static String readTextFile(Context context, String fileName) {
		if(context == null || TextUtils.isEmpty(fileName))
			return null;
		
		AssetManager manager = context.getAssets();
		if(manager == null)
			return null;
		
		InputStream stream = null;
		try {
			stream = manager.open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

			StringBuffer result = new StringBuffer();
			String buffer = null;
			while((buffer = reader.readLine()) != null) {
				result.append(buffer + CR);
			}
			
			return result.toString();
			
		} catch(IOException e) {
			Log.e(TAG, e.toString());
			return null;
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
	}
}
