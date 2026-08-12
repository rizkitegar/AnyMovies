package com.movies.anymovies.core.testing.di

import org.junit.Test
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules

class KoinModuleGraphTest : KoinTest {

    private val rootModules: List<Module> = listOf()

    @Test
    fun `aggregated module graph resolves without error`() {
        checkModules {
            modules(*rootModules.toTypedArray())
        }
    }
}
