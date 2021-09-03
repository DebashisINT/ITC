package com.fsmitc.features.weather.api

import com.fsmitc.base.BaseResponse
import com.fsmitc.features.task.api.TaskApi
import com.fsmitc.features.task.model.AddTaskInputModel
import com.fsmitc.features.weather.model.ForeCastAPIResponse
import com.fsmitc.features.weather.model.WeatherAPIResponse
import io.reactivex.Observable

class WeatherRepo(val apiService: WeatherApi) {
    fun getCurrentWeather(zipCode: String): Observable<WeatherAPIResponse> {
        return apiService.getTodayWeather(zipCode)
    }

    fun getWeatherForecast(zipCode: String): Observable<ForeCastAPIResponse> {
        return apiService.getForecast(zipCode)
    }
}