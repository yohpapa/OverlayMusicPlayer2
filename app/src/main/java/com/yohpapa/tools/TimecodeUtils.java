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

import android.annotation.SuppressLint;
import java.util.Locale;

/**
 * @author YohPapa
 */
public class TimecodeUtils {
	
	public static final String START_POSITION = "00:00";

	@SuppressLint("DefaultLocale")
	public static String format(long msec) {
		
		long minutes = msec / 60000;
		long seconds = (msec % 60000) / 1000;
		
		return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
	}
}
