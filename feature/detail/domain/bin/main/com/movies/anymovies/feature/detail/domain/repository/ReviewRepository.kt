package com.movies.anymovies.feature.detail.domain.repository

import androidx.paging.PagingData
import com.movies.anymovies.feature.detail.domain.model.Review
import kotlinx.coroutines.flow.Flow

public interface ReviewRepository {
    public fun getReviewsPagingData(movieId: Int): Flow<PagingData<Review>>
}
