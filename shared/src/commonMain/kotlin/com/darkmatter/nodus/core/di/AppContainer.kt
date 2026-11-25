package com.darkmatter.nodus.core.di

import com.darkmatter.nodus.core.network.createHttpClient
import com.darkmatter.nodus.core.session.SessionManager
import com.darkmatter.nodus.data.api.AuthApi
import com.darkmatter.nodus.data.api.NodeApi
import com.darkmatter.nodus.data.api.RoadmapApi
import com.darkmatter.nodus.data.repository.AuthRepository
import com.darkmatter.nodus.data.repository.NodeRepository
import com.darkmatter.nodus.data.repository.RoadmapRepository
import com.darkmatter.nodus.data.storage.OnboardingStorage
import com.darkmatter.nodus.data.storage.TokenStorage
import com.russhwolf.settings.Settings

object AppContainer {

    private val settings: Settings by lazy { Settings() }

    private val tokenStorage by lazy { TokenStorage(settings) }

    val sessionManager by lazy { SessionManager(tokenStorage) }

    private val httpClient by lazy { createHttpClient(sessionManager) }

    private val roadmapApi by lazy { RoadmapApi(httpClient) }

    private val authApi by lazy { AuthApi(httpClient) }

    private val nodeApi by lazy { NodeApi(httpClient) }

    val roadmapRepository by lazy { RoadmapRepository(roadmapApi) }

    val nodeRepository by lazy { NodeRepository(nodeApi) }

    val authRepository by lazy { AuthRepository(authApi, sessionManager) }

    val onboardingStorage by lazy { OnboardingStorage(settings) }
}
