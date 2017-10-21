package se.hellsoft.retrofitdemo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class UiResponse(val weatherData: WeatherData?, val error:Throwable?)
data class WeatherData(val city: String, val temperature: Float,
                       val clouds: Int, val windSpeed: Float)

class WeatherViewModel : ViewModel() {
  private val weatherRepository = WeatherRepository()
  private var queuedCalls: Set<Call<*>> = mutableSetOf()
  private val cityIdMap = mapOf(
      "Stockholm" to 2673722,
      "Malmö" to 2692969
  )

  fun loadWeather(cityName: String): LiveData<UiResponse> {
    val mediatorLiveData = MediatorLiveData<UiResponse>()
    val weatherCall: Call<WeatherResponse> = weatherRepository.fetchWeather(cityIdMap[cityName]!!)
    queuedCalls += weatherCall

    weatherCall.enqueue(object: Callback<WeatherResponse?> {
      override fun onFailure(call: Call<WeatherResponse?>?, t: Throwable?) {
        mediatorLiveData.postValue(UiResponse(null, t))
        if (call != null) {
          queuedCalls -= call
        }
      }

      override fun onResponse(call: Call<WeatherResponse?>?, response: Response<WeatherResponse?>?) {
        val weatherResponse: WeatherResponse? = response?.body()
        val weatherData = WeatherData(weatherResponse?.name!!,
            weatherResponse.main.temperature,
            weatherResponse.clouds.all,
            weatherResponse.main.wind.speed)
        mediatorLiveData.postValue(UiResponse(weatherData, null))
        if (call != null) {
          queuedCalls -= call
        }
      }
    })

    return mediatorLiveData
  }

  override fun onCleared() {
    super.onCleared()

    // When the ViewModel is cleared from memory, also abort any ongoing network call
    for (queuedCall in queuedCalls) {
      queuedCall.cancel()
    }
    queuedCalls = mutableSetOf()
  }
}