package com.movies.anymovies

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.movies.anymovies.navigation.Route
import com.movies.anymovies.feature.detail.presentation.MovieDetailScreen
import com.movies.anymovies.feature.detail.presentation.MovieReviewsScreen
import com.movies.anymovies.feature.genre.presentation.GenreListScreen
import com.movies.anymovies.feature.movies.presentation.MovieListScreen
import com.movies.anymovies.ui.theme.AnyMoviesTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        var firstFrameDrawn by mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !firstFrameDrawn }

        setContent {
            AnyMoviesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnyMoviesNavHost(
                        modifier = Modifier.padding(innerPadding),
                        onGenreListContentReady = { reportFullyDrawn() },
                    )
                }
                SideEffect { firstFrameDrawn = true }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun AnyMoviesNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onGenreListContentReady: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = Route.GenreList,
        modifier = modifier
    ) {
        composable<Route.GenreList> {
            GenreListScreen(
                onGenreClick = { genreId, genreName ->
                    navController.navigate(Route.MovieList(genreId = genreId, genreName = genreName))
                },
                onContentReady = onGenreListContentReady,
            )
        }
        composable<Route.MovieList> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.MovieList>()
            MovieListScreen(
                genreId = route.genreId,
                genreName = route.genreName,
                onMovieClick = { movieId -> navController.navigate(Route.MovieDetail(movieId = movieId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.MovieDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.MovieDetail>()
            MovieDetailScreen(
                movieId = route.movieId,
                onSeeAllReviews = { navController.navigate(Route.MovieReviews(movieId = route.movieId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.MovieReviews> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.MovieReviews>()
            MovieReviewsScreen(
                movieId = route.movieId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
