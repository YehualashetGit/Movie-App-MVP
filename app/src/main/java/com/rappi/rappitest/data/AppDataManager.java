/*
 * Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://mindorks.com/license/apache-v2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.rappi.rappitest.data;


import android.content.Context;

import com.rappi.rappitest.data.db.DbHelper;
import com.rappi.rappitest.data.db.model.Movie;
import com.rappi.rappitest.data.network.ApiHelper;
import com.rappi.rappitest.data.network.model.MovieListResponse;
import com.rappi.rappitest.di.ApplicationContext;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;


@Singleton
public class AppDataManager implements DataManager {

    private static final String TAG = "AppDataManager";

    private final Context mContext;
    private final DbHelper mDbHelper;
    private final ApiHelper mApiHelper;

    @Inject
    public AppDataManager(@ApplicationContext Context context,
                          DbHelper dbHelper,
                          ApiHelper apiHelper) {
        mContext = context;
        mDbHelper = dbHelper;
        mApiHelper = apiHelper;
    }

    @Override
    public Observable<Long> insertMovie(Movie movie) {
        return mDbHelper.insertMovie(movie);
    }


    @Override
    public Observable<List<Movie>> getMoviesByCategory(int category) {
        return mDbHelper.getMoviesByCategory(category);
    }

    @Override
    public Observable<Boolean> deleteMovies(int category) {
        return mDbHelper.deleteMovies(category);
    }

    @Override
    public Observable<MovieListResponse> doMovieListApiCall(int page, int category) {
        return mApiHelper.doMovieListApiCall(page, category);
    }
}
