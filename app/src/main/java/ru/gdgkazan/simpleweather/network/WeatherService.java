package ru.gdgkazan.simpleweather.network;

import android.support.annotation.NonNull;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.gdgkazan.simpleweather.model.City;


public interface WeatherService {

    @GET("data/2.5/weather?units=metric")
    Call<City> getWeather(@NonNull @Query("q") String query);

}
