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
package com.kkkkan.youtube.tubtub.youtube;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.kkkkan.youtube.tubtub.model.YouTubeVideo;
import com.kkkkan.youtube.tubtub.utils.Config;
import com.kkkkan.youtube.tubtub.utils.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.kkkkan.youtube.tubtub.youtube.YouTubeSingleton.getYouTube;

/**
 * Created by smedic on 13.2.17..
 */

public class YouTubeVideosLoader extends AsyncTaskLoader<List<YouTubeVideo>> {
    private final String TAG = "YouTubeVideosLoader";
    private YouTube youtube = getYouTube();
    private String keywords;

    public YouTubeVideosLoader(Context context, String keywords) {
        super(context);
        this.keywords = keywords;
    }

    @Override
    public List<YouTubeVideo> loadInBackground() {

        ArrayList<YouTubeVideo> items = new ArrayList<>();
        try {
            YouTube.Search.List searchList = youtube.search().list("id,snippet");
            YouTube.Search.List searchPlayList = youtube.search().list("id,snippet");
            YouTube.Videos.List videosList = youtube.videos().list("id,contentDetails,statistics");

            searchList.setKey(Config.YOUTUBE_API_KEY);
            searchList.setType("video"); //TODO ADD PLAYLISTS SEARCH
            searchList.setMaxResults(Config.NUMBER_OF_VIDEOS_RETURNED);
            searchList.setFields("items(id/videoId,snippet/title,snippet/thumbnails/default/url)");

           /* searchPlayList.setKey(Config.YOUTUBE_API_KEY);
            searchPlayList.setType("playlist"); //TODO ADD PLAYLISTS SEARCH
            searchPlayList.setMaxResults(Config.NUMBER_OF_VIDEOS_RETURNED);
            //searchPlayList.setFields("items(id/playlistId,snippet/title,snippet/thumbnails/default/url,id/kind)");
            searchPlayList.setQ(keywords);
            SearchListResponse searchPlayListResponse = searchList.execute();
            List<SearchResult> searchPlayListResults = searchPlayListResponse.getItems();
            for (SearchResult result:searchPlayListResults){
                Log.d(TAG,"searchPlayListResults tytle is : "+result.getSnippet().getTitle());
            }*/

            videosList.setKey(Config.YOUTUBE_API_KEY);
            videosList.setFields("items(id,contentDetails/duration,statistics/viewCount)");

            //search
            searchList.setQ(keywords);
            SearchListResponse searchListResponse = searchList.execute();
            List<SearchResult> searchResults = searchListResponse.getItems();

            //find video list
            videosList.setId(Utils.concatenateIDs(searchResults));  //save all ids from searchList list in order to find video list
            VideoListResponse resp = videosList.execute();
            List<Video> videoResults = resp.getItems();
            //make items for displaying in listView

            Log.d(TAG, "YouTube.Search.List execute result size is : " + searchResults.size());
            Log.d(TAG, "YouTube.Videos.List execute result size is : " + videoResults.size());

            //どうやらYouTube.Search.Listは削除されたビデオもとってきて、
            // YouTube.Videos.Listはとってきた50個のうち削除されたビデオはスルーして、userにデータは返してくれないようだ
            //（つまり50個中1個削除されたビデオがあったら、searchResults.size()は50,videoResults.size()は49）

            int count = 0;
            for (int i = 0, j = 0; i < searchResults.size(); i++) {
                YouTubeVideo youTubeVideo = new YouTubeVideo();

                SearchResult searchResult = searchResults.get(i);
                Video videoResult = videoResults.get(j);

                youTubeVideo.setTitle(searchResult.getSnippet().getTitle());
                youTubeVideo.setId(searchResult.getId().getVideoId());
                if (searchResult.getId().getVideoId().equals(videoResult.getId())) {
                    //Log.d(TAG, String.valueOf(++count) + " : " + searchResult.getSnippet().getTitle() + " : not Deleted");
                    //削除されたビデオではないとき
                    //jをインクリメント
                    j++;
                    //thumbnailDetailsのurlをセット
                    youTubeVideo.setThumbnailURL(searchResult.getSnippet().getThumbnails().getDefault().getUrl());
                    //video由来の情報もyouTubeVideoに追加
                    if (videoResult.getStatistics() != null) {
                        BigInteger viewsNumber = videoResult.getStatistics().getViewCount();
                        String viewsFormatted = NumberFormat.getIntegerInstance().format(viewsNumber) + " views";
                        youTubeVideo.setViewCount(viewsFormatted);
                    }
                    if (videoResult.getContentDetails() != null) {
                        String isoTime = videoResult.getContentDetails().getDuration();
                        String time = Utils.convertISO8601DurationToNormalTime(isoTime);
                        youTubeVideo.setDuration(time);
                    }
                } else {
                    //削除されたビデオの時
                    //Log.d(TAG, String.valueOf(++count) + " : " + searchResult.getSnippet().getTitle() + " : Deleted");
                    //削除されたビデオの場合はthumnailURLにnull、durationに"00:00"を入れる
                    youTubeVideo.setThumbnailURL(null);
                    youTubeVideo.setDuration("00:00");
                }
                items.add(youTubeVideo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "loadInBackground: return " + items.size());
        return items;
    }

    @Override
    public void deliverResult(List<YouTubeVideo> data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            return;
        }
        super.deliverResult(data);
    }
}
