/*
 * Copyright (C) 2017 kkkkan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kkkkan.youtube.tubtub.utils;

import android.arch.lifecycle.MutableLiveData;

import com.kkkkan.youtube.tubtub.model.YouTubeVideo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/12/07.
 */

public class PlaylistsCash {
    private final String TAG = "PlaylistsCash";
    public static int tag = 0;
    static public PlaylistsCash Instance = new PlaylistsCash();

    private PlaylistsCash() {

    }

    //回転しても検索内容覚えておくためのList
    private List<YouTubeVideo> searchResultsList;

    public void setSearchResultsList(List<YouTubeVideo> searchResultsList) {
        this.searchResultsList = new ArrayList<>(searchResultsList);
    }

    public List<YouTubeVideo> getSearchResultsList() {
        return searchResultsList != null ? new ArrayList<>(searchResultsList) : null;
    }

    //NowPlayingListFragment用の、今再生しているplaylistとpositionを入れとくためのList
    private List<YouTubeVideo> nowPlaylist;
    private int currentVideoIndex;
    //NowPlayingListFragmentで今再生中のビデオのみ色を変えるためのMutableLiveData
    private MutableLiveData<Integer> mutableCurrentVideoIndex = new MutableLiveData<>();

    public void setCurrentVideoIndex(int currentVideoIndex) {
        this.currentVideoIndex = currentVideoIndex;
        mutableCurrentVideoIndex.postValue(currentVideoIndex);
    }

    public void setNowPlaylist(List<YouTubeVideo> nowPlaylist) {
        //nowPlaylistと参照先を変えないと履歴の再読み込みとかでthis.nowPlaylistまで変わっちゃう
        this.nowPlaylist = new ArrayList<>(nowPlaylist);
    }

    public int getCurrentVideoIndex() {
        return currentVideoIndex;
    }

    public List<YouTubeVideo> getNowPlaylist() {
        return nowPlaylist != null ? new ArrayList<>(nowPlaylist) : null;
    }

    public MutableLiveData<Integer> getMutableCurrentVideoIndex() {
        return mutableCurrentVideoIndex;
    }
}
