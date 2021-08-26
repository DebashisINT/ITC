package com.demo.features.weather.api

import com.demo.base.BaseResponse
import com.demo.features.task.api.TaskApi
import com.demo.features.task.model.AddTaskInputModel
import com.demo.features.weather.model.ForeCastAPIResponse
import com.demo.features.weather.model.WeatherAPIResponse
import io.reactivex.Observable

class WeatherRepo(val apiService: WeatherApi) {
    fun getCurrentWeather(zipCode: String): Observable<WeatherAPIResponse> {
        return apiService.getTodayWeather(zipCode)
    }

    fun getWeatherForecast(zipCode: String): Observable<ForeCastAPIResponse> {
        return apiService.getForecast(zipCode)
    }
}