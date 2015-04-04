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

import android.database.Cursor;
import android.text.TextUtils;

public class CursorHelper {

    public static String getString(Cursor cursor, String key) {
        if(cursor == null || cursor.isClosed() || TextUtils.isEmpty(key))
            return null;

        return cursor.getString(cursor.getColumnIndex(key));
    }

    public static long getLong(Cursor cursor, String key) {
        if(cursor == null || cursor.isClosed() || TextUtils.isEmpty(key))
            return -1L;

        return cursor.getLong(cursor.getColumnIndex(key));
    }

    public static int getInt(Cursor cursor, String key) {
        if(cursor == null || cursor.isClosed() || TextUtils.isEmpty(key))
            return -1;

        return cursor.getInt(cursor.getColumnIndex(key));
    }
}
