package com.breezedsm.features.weather.api

import com.breezedsm.base.BaseResponse
import com.breezedsm.features.task.api.TaskApi
import com.breezedsm.features.task.model.AddTaskInputModel
import com.breezedsm.features.weather.model.ForeCastAPIResponse
import com.breezedsm.features.weather.model.WeatherAPIResponse
import io.reactivex.Observable

class WeatherRepo(val apiService: WeatherApi) {
    fun getCurrentWeather(zipCode: String): Observable<WeatherAPIResponse> {
        return apiService.getTodayWeather(zipCode)
    }

    fun getWeatherForecast(zipCode: String): Observable<ForeCastAPIResponse> {
        return apiService.getForecast(zipCode)
    }
}