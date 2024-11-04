package com.progdsmfsm.features.weather.api

import com.progdsmfsm.features.task.api.TaskApi
import com.progdsmfsm.features.task.api.TaskRepo

object WeatherRepoProvider {
    fun weatherRepoProvider(): WeatherRepo {
        return WeatherRepo(WeatherApi.create())
    }
}