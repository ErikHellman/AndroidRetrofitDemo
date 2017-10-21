package se.hellsoft.retrofitdemo

import com.squareup.moshi.Json
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Data classes corresponsidng to Open Weather Map API (see http://openweathermap.org/api)
// TODO Implement all the correct Json mappings using @Json()
data class Coordinates(@Json(name = "lon") val longitude: Float,
                       @Json(name = "lat") val latitude: Float)
data class Weather(val id: Int, val main: String,
                   val description: String, val icon: String)
data class Wind(val speed: Float, val degree: Int)
data class Main(val temperature: Float, val pressure: Int,
                val humidity: Int, val temp_min: Float,
                val temp_max: Float, val wind: Wind)
data class Clouds(val all: Int)
data class WeatherResponse(@Json(name = "coord") val coordinates: Coordinates,
                           val weather: Weather, val main: Main,
                           val clouds: Clouds, val id: Int, val name: String)

// Retrofit interface for fetching Weather data for a specific city
// TODO Add other API calls as needed
interface WeatherApi {
  @GET("data/2.5/weather")
  fun loadWeather(@Query("id") cityId: Int, // City ID, see http://openweathermap.org/appid#work
                  @Query("units") unit:String, // Units; metric, imperial or standard. See http://openweathermap.org/current#data
                  @Query("appid") apiKey: String): Call<WeatherResponse> // The API key
}

class WeatherRepository {
  companion object {
    val API_KEY = "" // Your API key, see http://openweathermap.org/appid#get
  }
  private val weatherApi:WeatherApi

  init {
    // Construct the JSON converter, the Ratrofit instance and create an instance of our WeatherApi
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val retrofit = Retrofit.Builder()
        .baseUrl("http://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    weatherApi = retrofit.create(WeatherApi::class.java)
  }

  // Prepare a network call to fetch weather data for specific city Id
  fun fetchWeather(cityId: Int): Call<WeatherResponse> {
    return weatherApi.loadWeather(cityId, "metric", API_KEY)
  }
}