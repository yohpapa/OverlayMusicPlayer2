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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

public class MusicPlaybackService extends Service
        implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener {

    private static final String TAG = MusicPlaybackService.class.getSimpleName();

    protected static final int PLAY_STATE_STOPPED = 0;
    protected static final int PLAY_STATE_PLAYING = 1;
    protected static final int PLAY_STATE_PAUSED = 2;

    private long[] trackIds = null;
    private int currentIndex = -1;

    private int positionToRestore = -1;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_SEEKING = 3;
    private static final int STATE_FINISHED = 4;
    private int state = STATE_IDLE;

    private MediaPlayer player = null;
    private List<Runnable> postProcesses = null;
    private MetaDataRetriever.MetaData currentTrackInfo = null;

    private float duckingVolumeLevel = 0.3f;
    
    private static final long TIMEOUT_TIMECODE_CHECK = 500;	// msec
    private Handler periodicHandler = null;

    // ---------- Service lifecycle event handlers ----------

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        trackIds = null;
        currentIndex = -1;
        positionToRestore = -1;
        state = STATE_IDLE;

        player = initializePlayer();
        postProcesses = new ArrayList<Runnable>();
        currentTrackInfo = null;
        
        periodicHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        finalizePlayer();
        abandonAudioFocus();

        postProcesses.clear();
        state = STATE_FINISHED;
        
        stopTimecodeTimer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    // ---------- MediaPlayer implementations ----------

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared");

        if(positionToRestore != -1) {
            player.seekTo(positionToRestore);
            state = STATE_SEEKING;
            return;
        }

        state = STATE_PREPARED;

        for(Runnable postProcess : postProcesses) {
            postProcess.run();
        }
        postProcesses.clear();

        onPlayStateChanged(PLAY_STATE_PLAYING);
        
        startTimecodeTimer();

        new MetaDataRetriever(this, trackIds[currentIndex], new MetaDataRetriever.OnRetrieveMetaDataFinished() {
            @Override
            public void onRetrievedMetaData(MetaDataRetriever.MetaData data) {
                if(state == STATE_FINISHED) {
                    Log.d(TAG, "The service has already been finished.");
                    return;
                }

                currentTrackInfo = data;
                onTrackChanged();
            }
        }, true).execute();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");
        nextTrack();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.d(TAG, "onSeekComplete");

        for(Runnable postProcess : postProcesses) {
            postProcess.run();
        }
        postProcesses.clear();

        state = STATE_PREPARED;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError what: " + what + ", extra" + extra);
        onError(what, extra);
        return false;
    }

    private MediaPlayer initializePlayer() {
        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnSeekCompleteListener(this);
        player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        return player;
    }

    private void finalizePlayer() {
        if(player != null) {
            player.pause();
            positionToRestore = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    // ---------- Playback control methods ----------

    private void prepareToPlay(long trackId) {
        Log.d(TAG, "prepareToPlay trackId: " + trackId);

        if(player == null) {
            Log.d(TAG, "Can not start playing. Maybe now audio focus has been taken by an other app");
            postProcesses.add(new Runnable() {
                @Override
                public void run() {
                    playTrack();
                }
            });
            return;
        }

        try {
            player.reset();
            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);
            player.setDataSource(this, uri);
            player.prepareAsync();

            state = STATE_PREPARING;

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    protected void playTrack() {
        Log.d(TAG, "playTrack");

        if(player == null) {
            Log.d(TAG, "Can not start playing. Maybe now audio focus has been taken by an other app");
            return;
        }

        if(state == STATE_PREPARING) {
        	Log.d(TAG, "Can not start playing. Maybe now audio focus has been taken by an other app");
            postProcesses.add(new Runnable() {
                @Override
                public void run() {
                	playTrack();
                }
            });
            return;
        }
        
        if(!player.isPlaying()) {
            boolean result = requestAudioFocus();
            if(!result) {
                Log.d(TAG, "Requesting the audio focus has failed.");
                return;
            }

            player.start();
            onPlayStateChanged(PLAY_STATE_PLAYING);
            
            startTimecodeTimer();
        }
    }

    protected void pauseTrack() {
        Log.d(TAG, "pauseTrack");

        if(player == null) {
            Log.d(TAG, "Can not start playing. Maybe now audio focus has been taken by an other app");
            return;
        }

        if(player.isPlaying()) {
            abandonAudioFocus();
            player.pause();
            onPlayStateChanged(PLAY_STATE_PAUSED);
            
            stopTimecodeTimer();
        }
    }

    protected void stopTrack() {
        Log.d(TAG, "stopTrack");

        if(player.isPlaying()) {
            player.pause();
        }

        stopSelf();
        onPlayStateChanged(PLAY_STATE_STOPPED);
        
        stopTimecodeTimer();
    }

    protected void selectTrack(int newIndex, long[] newTrackIds) {
        Log.d(TAG, "selectTrack newIndex: " + newIndex);

        if(newIndex == -1 || newTrackIds == null || newTrackIds.length <= 0) {
            Log.d(TAG, "Invalid parameters");
            return;
        }

        if(newIndex >= newTrackIds.length) {
            Log.d(TAG, "The new index is over.");
            return;
        }

        if(!needToRefreshList(newTrackIds, newIndex)) {
        	return;
        }

        trackIds = newTrackIds;
        currentIndex = newIndex;
        positionToRestore = -1;

        prepareToPlay(newTrackIds[currentIndex]);
    }
    
    private boolean needToRefreshList(long[] newTrackIds, int newIndex) {
    	if(trackIds == null) {
    		Log.d(TAG, "It is the first selection.");
    		return true;
    	}
    	
    	if(trackIds.length != newTrackIds.length) {
    		Log.d(TAG, "The lengths of the lists are different.");
    		return true;
    	}
    	
    	for(int i = 0; i < trackIds.length; i ++) {
            if(trackIds[i] != newTrackIds[i]) {
            	Log.d(TAG, "The song IDs are different.");
            	return true;
            }
    	}
    	
    	long newTrackId = newTrackIds[newIndex];
        long nowTrackId = trackIds[currentIndex];

        if(nowTrackId != newTrackId) {
            Log.d(TAG, "The track ID has been changed.");
            return true;
        }
    	
    	return false;
    }

    protected void nextTrack() {
        Log.d(TAG, "nextTrack");

        currentIndex = (currentIndex + 1) % trackIds.length;
        positionToRestore = -1;
        prepareToPlay(trackIds[currentIndex]);
        
        postProcesses.add(new Runnable() {
            @Override
            public void run() {
                playTrack();
            }
        });
    }

    protected void prevTrack() {
        Log.d(TAG, "prevTrack");

        try {
            if(player != null) {
                int time = player.getCurrentPosition();
                if(time >= 3000) {
                    player.seekTo(0);
                    postProcesses.add(new Runnable() {
                        @Override
                        public void run() {
                            playTrack();
                        }
                    });
                    return;
                }
            }

            currentIndex --;
            if(currentIndex < 0) {
                currentIndex = trackIds.length - 1;
            }

            prepareToPlay(trackIds[currentIndex]);

        } finally {
            postProcesses.add(new Runnable() {
                @Override
                public void run() {
                    playTrack();
                }
            });
        }
    }

    protected void seekTrack(final int time) {
        Log.d(TAG, "seekTrack");

        if(time < 0) {
            Log.d(TAG, "invalid parameter");
            return;
        }

        if(player == null) {
            Log.d(TAG, "Can not start playing. Maybe now audio focus has been taken by an other app");
            postProcesses.add(new Runnable() {
                @Override
                public void run() {
                    seekTrack(time);
                }
            });
            return;
        }

        player.seekTo(time);
    }

    // ---------- Meta data retrieve methods ----------

    protected MetaDataRetriever.MetaData getCurrentTrackInfo() {
        Log.d(TAG, "getCurrentTrackInfo");

        if(currentTrackInfo == null) {
            Log.d(TAG, "The current track information is unavailable yet.");
            return null;
        }

        return currentTrackInfo.deepCopy();
    }

    // ---------- Audio focus control methods ----------

    protected void setDuckingVolumeLevel(float duckingVolumeLevel) {
        Log.d(TAG, "setDuckingVolumeLevel duckingVolumeLevel: " + duckingVolumeLevel);

        if(duckingVolumeLevel < 0.0f) {
            Log.d(TAG, "invalid parameter");
            return;
        }

        this.duckingVolumeLevel = duckingVolumeLevel;
    }

    private boolean requestAudioFocus() {
        Log.d(TAG, "requestAudioFocus");

        AudioManager manager = (AudioManager)getSystemService(AUDIO_SERVICE);
        int result = manager.requestAudioFocus(
                                onAudioFocusChangeListener,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        Log.d(TAG, "abandonAudioFocus");

        AudioManager manager = (AudioManager)getSystemService(AUDIO_SERVICE);
        manager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChange focusChange: " + focusChange);

            switch(focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if(player == null) {
                        player = initializePlayer();
                        player.setVolume(1.0f, 1.0f);
                        postProcesses.add(new Runnable() {
                            @Override
                            public void run() {
                                playTrack();
                            }
                        });
                        prepareToPlay(trackIds[currentIndex]);
                    } else {
                        player.setVolume(1.0f, 1.0f);
                        if(!player.isPlaying()) {
                            player.start();
                        }
                    }
                    break;

                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    if(player != null) {
                        player.setVolume(1.0f, 1.0f);
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    finalizePlayer();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if(player != null) {
                        player.pause();
                    }
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if(player != null) {
                        player.setVolume(duckingVolumeLevel, duckingVolumeLevel);
                    }
                    break;

                default:
                    Log.d(TAG, "Unknown focus change type: " + focusChange);
                    break;
            }
        }
    };
    
    private void startTimecodeTimer() {
    	periodicHandler.postDelayed(onTimecodeTimerFired, TIMEOUT_TIMECODE_CHECK);
    }
    
    private void stopTimecodeTimer() {
    	periodicHandler.removeCallbacks(onTimecodeTimerFired);
    }
    
    private final Runnable onTimecodeTimerFired = new Runnable() {
		@Override
		public void run() {
			if(player != null) {
				int position = player.getCurrentPosition();
				onTimecodeChanged(position);
				
				periodicHandler.postDelayed(this, TIMEOUT_TIMECODE_CHECK);
			}
		}
	};

    // ---------- Player event handlers for a subclass ----------

    protected void onPlayStateChanged(int playState) {
        Log.d(TAG, "Please override me, onPlayStateChanged playState: " + playState + "!");
    }

    protected void onTrackChanged() {
        Log.d(TAG, "Please override me, onTrackChanged!");
    }

    protected void onError(int what, int extra) {
        Log.d(TAG, "Please override me, onError what: " + what + ", extra: " + extra + "!");
    }
    
    protected void onTimecodeChanged(int position) {
    	Log.d(TAG, "Please override me, onTimecodeChanged position: " + position + "!");
    }
}
