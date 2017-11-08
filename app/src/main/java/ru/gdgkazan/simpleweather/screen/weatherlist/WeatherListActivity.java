package ru.gdgkazan.simpleweather.screen.weatherlist;



import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.gdgkazan.simpleweather.R;
import ru.gdgkazan.simpleweather.model.City;
import ru.gdgkazan.simpleweather.screen.general.LoadingDialog;
import ru.gdgkazan.simpleweather.screen.general.LoadingView;
import ru.gdgkazan.simpleweather.screen.general.SimpleDividerItemDecoration;
import ru.gdgkazan.simpleweather.screen.weather.WeatherActivity;
import ru.gdgkazan.simpleweather.screen.weather.WeatherLoader;


public class WeatherListActivity extends AppCompatActivity implements CitiesAdapter.OnItemClick, SwipeRefreshLayout.OnRefreshListener {

    private List<City> loadedCities = new ArrayList<>();
    private List<City> cities;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty)
    View mEmptyView;

    private CitiesAdapter mAdapter;

    private LoadingView mLoadingView;
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_list);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, false));
        mAdapter = new CitiesAdapter(getInitialCities(), this);
        mRecyclerView.setAdapter(mAdapter);
        mLoadingView = LoadingDialog.view(getSupportFragmentManager());
        cities = getInitialCities();
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        load(cities,false);



        /**
         * TODO : task
         *
         * 1) Load all cities forecast using one or multiple loaders
         * 2) Try to run these requests as most parallel as possible
         * or better do as less requests as possible
         * 3) Show loading indicator during loading process
         * 4) Allow to update forecasts with SwipeRefreshLayout
         * 5) Handle configuration changes
         *
         * Note that for the start point you only have cities names, not ids,
         * so you can't load multiple cities in one request.
         *
         * But you should think how to manage this case. I suggest you to start from reading docs mindfully.
         */
    }
    public void loadWeather(boolean restart ,City city ,String cityName, Integer id) {
        mLoadingView.showLoadingIndicator();
        LoaderManager.LoaderCallbacks<City> callbacks = new WeatherCallbacks(city,cityName);

        if(restart) {
          getSupportLoaderManager().restartLoader(id,Bundle.EMPTY,callbacks);
        }
        else getSupportLoaderManager().initLoader(id,Bundle.EMPTY,callbacks);
    }
    public void load(List<City> cities,boolean restart) {
        for (int i = 0; i < cities.size(); i++) {
            String cityName = cities.get(i).getName();
            loadWeather(restart,cities.get(i),cityName,i + 1);
        }
    }
    private void showWeather(@Nullable City city) {
        if(city == null || city.getMain() == null || city.getWeather() == null || city.getWind() == null) {
            showError();
            return;
        }
        loadedCities.add(city);
        if(loadedCities.size() >= cities.size()) {
            mLoadingView.hideLoadingIndicator();
            sortAllCities(loadedCities);
            mAdapter.changeDataSet(loadedCities);
            loadedCities.clear();
        }

    }

    @Override
    public void onRefresh() {
        load(cities,true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        },4000);
    }

    private class WeatherCallbacks implements LoaderManager.LoaderCallbacks<City> {

    private City city;
    private String cityName;


    public WeatherCallbacks(City city, String cityName) {
        this.city = city;
        this.cityName = cityName;
    }

    @Override
    public android.support.v4.content.Loader<City> onCreateLoader(int id, Bundle args) {
        if(id <= cities.size()) {
            return new WeatherLoader(WeatherListActivity.this,cityName);
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<City> loader, City data) {
        showWeather(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<City> loader) {

    }
}

    @Override
    public void onItemClick(@NonNull City city) {
        startActivity(WeatherActivity.makeIntent(this, city.getName()));
    }

    @NonNull
    private List<City> getInitialCities() {
        List<City> cities = new ArrayList<>();
        String[] initialCities = getResources().getStringArray(R.array.initial_cities);
        for (String city : initialCities) {
            cities.add(new City(city));
        }
        return cities;
    }

    private void sortAllCities(List<City> list) {
        Collections.sort(list,new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
    private void showError() {
        mLoadingView.hideLoadingIndicator();
        Snackbar snackbar = Snackbar.make(mRecyclerView,R.string.weather_error,Snackbar.LENGTH_LONG)
                .setAction(R.string.weather_error,v->load(cities,true));
        snackbar.setDuration(4000);
        snackbar.show();
    }

}
