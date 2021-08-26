package com.demo.features.weather.api

import com.demo.features.task.api.TaskApi
import com.demo.features.task.api.TaskRepo

object WeatherRepoProvider {
    fun weatherRepoProvider(): WeatherRepo {
        return WeatherRepo(WeatherApi.create())
    }
}