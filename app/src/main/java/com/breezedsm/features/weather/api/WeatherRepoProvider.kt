package com.breezedsm.features.weather.api

import com.breezedsm.features.task.api.TaskApi
import com.breezedsm.features.task.api.TaskRepo

object WeatherRepoProvider {
    fun weatherRepoProvider(): WeatherRepo {
        return WeatherRepo(WeatherApi.create())
    }
}