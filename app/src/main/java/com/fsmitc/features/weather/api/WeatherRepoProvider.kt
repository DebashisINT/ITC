package com.fsmitc.features.weather.api

import com.fsmitc.features.task.api.TaskApi
import com.fsmitc.features.task.api.TaskRepo

object WeatherRepoProvider {
    fun weatherRepoProvider(): WeatherRepo {
        return WeatherRepo(WeatherApi.create())
    }
}