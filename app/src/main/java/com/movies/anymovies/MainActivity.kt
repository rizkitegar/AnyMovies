package com.movies.anymovies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.movies.anymovies.databinding.ViewGenreListHeaderBinding
import com.movies.anymovies.navigation.Route
import com.movies.anymovies.ui.theme.AnyMoviesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnyMoviesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnyMoviesNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
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
            GenreListPlaceholder(
                onGenreClick = {
                    navController.navigate(Route.MovieList(genreId = 28, genreName = "Action"))
                }
            )
        }
        composable<Route.MovieList> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.MovieList>()
            MovieListPlaceholder(
                genreName = route.genreName,
                onMovieClick = { navController.navigate(Route.MovieDetail(movieId = 550)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Route.MovieDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.MovieDetail>()
            MovieDetailPlaceholder(
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
private fun GenreListPlaceholder(onGenreClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        AndroidViewBinding(ViewGenreListHeaderBinding::inflate)
        Text(text = "Route.GenreList", modifier = Modifier.padding(16.dp))
        Button(onClick = onGenreClick) {
            Text("Open genre")
        }
    }
}

@Composable
private fun MovieListPlaceholder(
    genreName: String,
    onMovieClick: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Route.MovieList: $genreName")
        Button(onClick = onMovieClick) {
            Text("Open movie")
        }
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
private fun MovieDetailPlaceholder(
    movieId: Int,
    onSeeAllReviews: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Route.MovieDetail: $movieId")
        Button(onClick = onSeeAllReviews) {
            Text("See all reviews")
        }
        Button(onClick = onBack) {
            Text("Back")
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
