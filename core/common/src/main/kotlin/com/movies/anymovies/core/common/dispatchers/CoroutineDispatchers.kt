package com.movies.anymovies.core.common.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Injected dispatcher set (Koin `single`), so `data`/`domain` never reference
 * `Dispatchers.*` directly and tests can substitute a `TestDispatcher`.
 */
public interface CoroutineDispatchers {
    public val io: CoroutineDispatcher
    public val main: CoroutineDispatcher
    public val default: CoroutineDispatcher
}

public class DefaultCoroutineDispatchers : CoroutineDispatchers {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val default: CoroutineDispatcher = Dispatchers.Default
}
