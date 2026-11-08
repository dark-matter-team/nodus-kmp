package com.darkmatter.nodus.core.di

import com.darkmatter.nodus.core.network.createHttpClient
import com.darkmatter.nodus.core.session.SessionManager
import com.darkmatter.nodus.data.api.AuthApi
import com.darkmatter.nodus.data.api.RoadmapApi
import com.darkmatter.nodus.data.repository.AuthRepository
import com.darkmatter.nodus.data.repository.RoadmapRepository
import com.darkmatter.nodus.data.storage.TokenStorage
import com.russhwolf.settings.Settings

object AppContainer {

    private val settings: Settings by lazy { Settings() }

    private val tokenStorage by lazy { TokenStorage(settings) }

    val sessionManager by lazy { SessionManager(tokenStorage) }

    private val httpClient by lazy { createHttpClient() }

    private val roadmapApi by lazy { RoadmapApi(httpClient) }

    private val authApi by lazy { AuthApi(httpClient) }

    val roadmapRepository by lazy { RoadmapRepository(roadmapApi) }

    val authRepository by lazy { AuthRepository(authApi, sessionManager) }
}
