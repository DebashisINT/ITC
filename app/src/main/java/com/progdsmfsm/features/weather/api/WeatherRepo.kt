package com.progdsmfsm.features.weather.api

import com.progdsmfsm.base.BaseResponse
import com.progdsmfsm.features.task.api.TaskApi
import com.progdsmfsm.features.task.model.AddTaskInputModel
import com.progdsmfsm.features.weather.model.ForeCastAPIResponse
import com.progdsmfsm.features.weather.model.WeatherAPIResponse
import io.reactivex.Observable

class WeatherRepo(val apiService: WeatherApi) {
    fun getCurrentWeather(zipCode: String): Observable<WeatherAPIResponse> {
        return apiService.getTodayWeather(zipCode)
    }

    fun getWeatherForecast(zipCode: String): Observable<ForeCastAPIResponse> {
        return apiService.getForecast(zipCode)
    }
}