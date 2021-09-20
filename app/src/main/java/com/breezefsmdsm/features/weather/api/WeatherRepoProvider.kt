package com.breezefsmdsm.features.weather.api

import com.breezefsmdsm.features.task.api.TaskApi
import com.breezefsmdsm.features.task.api.TaskRepo

object WeatherRepoProvider {
    fun weatherRepoProvider(): WeatherRepo {
        return WeatherRepo(WeatherApi.create())
    }
}