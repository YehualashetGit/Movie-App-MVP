package com.rappi.rappitest.ui.list;

import com.androidnetworking.error.ANError;
import com.rappi.rappitest.data.DataManager;
import com.rappi.rappitest.data.db.model.Movie;
import com.rappi.rappitest.data.network.model.MovieListResponse;
import com.rappi.rappitest.ui.base.BasePresenter;
import com.rappi.rappitest.utils.AppLogger;
import com.rappi.rappitest.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class ListPresenter<V extends ListMvpView> extends BasePresenter<V> implements ListMvpPresenter<V> {

    @Inject
    public ListPresenter(DataManager dataManager,
                         SchedulerProvider schedulerProvider,
                         CompositeDisposable compositeDisposable) {
        super(dataManager, schedulerProvider, compositeDisposable);
    }


    @Override
    public void onLoadMoreItems(int page, int position) {

        if (page == 1)
            deleteMovies(position);
        loadMoviesFromApi(page, position);

    }

    @Override
    public void filterList(int position, String filter) {
        getMvpView().resetAdapter();
        loadMoviesFromDataBase(filter, position);
    }

    @Override
    public void onModeChange(int position, boolean offLine) {

        if (offLine) {
            getMvpView().showSearchBar();
        } else {
            getMvpView().hideSearchBar();
            getMvpView().resetAdapter();
            onLoadMoreItems(1, position);
        }

    }

    @Override
    public void onTabChange(int position, boolean offline) {
        getMvpView().resetAdapter();

        if (offline) {
            loadMoviesFromDataBase("", position);
        } else {
            onLoadMoreItems(1, position);
        }


    }

    private List<Movie> filterListItems(String filter, List<Movie> items) {
        List<Movie> filteredList = new ArrayList<>();

        for (Movie movie : items) {
            if (movie.getName().toLowerCase().contains(filter))
                filteredList.add(movie);
            else if (movie.getDate().toLowerCase().contains(filter))
                filteredList.add(movie);
            else if (String.valueOf(movie.getVoteAvg()).toLowerCase().contains(filter))
                filteredList.add(movie);
        }


        return filteredList;
    }

    private void loadMoviesFromDataBase(final String filter, final int position) {
        getCompositeDisposable().add(getDataManager()
                .getMoviesByCategory(position)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<List<Movie>>() {
                    @Override
                    public void accept(List<Movie> movies) throws Exception {

                        if (!isViewAttached()) {
                            return;
                        }

                        movies = filterListItems(filter, movies);
                        if (movies.isEmpty())
                            getMvpView().showLabelNoItem();
                        else
                            getMvpView().hideLabelNoItem();
                        getMvpView().refreshList(movies);
                    }
                }));
    }

    private void loadMoviesFromApi(int page, final int category) {
        getMvpView().showLoading();
        getCompositeDisposable().add(getDataManager()
                .doMovieListApiCall(page, category)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<MovieListResponse>() {
                    @Override
                    public void accept(MovieListResponse response) throws Exception {

                        if (!isViewAttached()) {
                            return;
                        }

                        getMvpView().hideLoading();
                        List<Movie> movies = convertApiToList(response, category);
                        getMvpView().refreshList(movies);
                        saveInDb(movies);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        if (!isViewAttached()) {
                            return;
                        }

                        getMvpView().hideLoading();

                        // handle the login error here
                        if (throwable instanceof ANError) {
                            ANError anError = (ANError) throwable;
                            handleApiError(anError);
                        }
                    }
                }));
    }

    private void deleteMovies(int category) {
        getCompositeDisposable().add(getDataManager()
                .deleteMovies(category)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean response) throws Exception {
                        AppLogger.d("Table Cleared");
                    }
                }));
    }

    private List<Movie> convertApiToList(MovieListResponse response, int category) {

        List<Movie> list = new ArrayList<>();
        for (MovieListResponse.MovieResponse movieResponse : response.getResults()) {
            final Movie movie = new Movie();
            movie.setLanguage(movieResponse.getOriginalLanguage());
            movie.setName(movieResponse.getTitle());
            movie.setVoteAvg(movieResponse.getVoteAverage());
            movie.setImageUrl(movieResponse.getPosterPath());
            movie.setDate(movieResponse.getReleaseDate());
            movie.setCategory(category);
            list.add(movie);
        }

        return list;
    }

    private void saveInDb(List<Movie> movies) {
        for (Movie movie : movies) {
            getCompositeDisposable().add(getDataManager().insertMovie(movie)
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long id) throws Exception {
                            if (!isViewAttached()) {
                                return;
                            }
                        }
                    }));
        }
    }

}

