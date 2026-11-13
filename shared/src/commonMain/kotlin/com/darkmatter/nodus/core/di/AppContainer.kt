package com.darkmatter.nodus.core.di

import com.darkmatter.nodus.core.network.createHttpClient
import com.darkmatter.nodus.data.api.RoadmapApi
import com.darkmatter.nodus.data.repository.RoadmapRepository
import com.darkmatter.nodus.data.storage.OnboardingStorage

object AppContainer {

    private val httpClient by lazy { createHttpClient() }

    private val roadmapApi by lazy { RoadmapApi(httpClient) }

    val roadmapRepository by lazy { RoadmapRepository(roadmapApi) }

    val onboardingStorage by lazy { OnboardingStorage() }
}