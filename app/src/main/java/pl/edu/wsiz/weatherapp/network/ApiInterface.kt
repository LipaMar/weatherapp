package pl.edu.wsiz.weatherapp.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
const val WEATHER_UNIT = "metric"

interface ApiInterface {

    @GET("weather")
    fun getCurrentWeather(
        @Query("q") q: String,
        @Query("appid") appid: String,
        @Query("units") units: String = WEATHER_UNIT,
        @Query("lang") lang: String = "pl"
    ): Call<ForecastDTO>

    companion object {
        operator fun invoke(): ApiInterface {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(ApiInterface::class.java)
        }
    }

}