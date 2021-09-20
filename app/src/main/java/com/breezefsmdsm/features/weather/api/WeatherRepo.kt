package com.breezefsmdsm.features.weather.api

import com.breezefsmdsm.base.BaseResponse
import com.breezefsmdsm.features.task.api.TaskApi
import com.breezefsmdsm.features.task.model.AddTaskInputModel
import com.breezefsmdsm.features.weather.model.ForeCastAPIResponse
import com.breezefsmdsm.features.weather.model.WeatherAPIResponse
import io.reactivex.Observable

class WeatherRepo(val apiService: WeatherApi) {
    fun getCurrentWeather(zipCode: String): Observable<WeatherAPIResponse> {
        return apiService.getTodayWeather(zipCode)
    }

    fun getWeatherForecast(zipCode: String): Observable<ForeCastAPIResponse> {
        return apiService.getForecast(zipCode)
    }
}