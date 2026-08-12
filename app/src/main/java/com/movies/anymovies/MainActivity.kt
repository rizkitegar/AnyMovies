package com.movies.anymovies

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.movies.anymovies.navigation.Route
import com.movies.anymovies.feature.detail.presentation.MovieDetailScreen
import com.movies.anymovies.feature.genre.presentation.GenreListScreen
import com.movies.anymovies.feature.movies.presentation.MovieListScreen
import com.movies.anymovies.ui.theme.AnyMoviesTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            AnyMoviesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnyMoviesNavHost(modifier = Modifier.padding(innerPadding))
                }
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
    navController: NavHostController = rememberNavController()
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
                }
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
            MovieReviewsPlaceholder(
                movieId = route.movieId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MovieReviewsPlaceholder(movieId: Int, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Route.MovieReviews: $movieId")
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
