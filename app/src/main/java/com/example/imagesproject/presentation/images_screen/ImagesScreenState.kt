package com.example.imagesproject.presentation.images_screen

import androidx.compose.foundation.lazy.grid.LazyGridState
import com.example.imagesproject.core.util.UiText
import com.example.imagesproject.domain.model.ImageItem

data class ImagesScreenState(
    val isLoading: Boolean = false,
    val imagesList: List<ImageItem> = emptyList(),
    val error: UiText? = null,
    val isExpanded: Boolean = false,
    val currentImageIndex: Int = 0,
    val currentImageUrl: String? = null,
    val topBarVisible: Boolean = false,
    val systemNavigationBarVisible: Boolean = true,
    val isExpandAnimated: Boolean = false,
    val openedImageLayer: Boolean = false,
    val lazyGridState: LazyGridState = LazyGridState(),
    val gridLayoutParams: GridLayoutParams? = null,
    val indexToScroll: Int? = null,
    val imageScreenState: ImageScreenState = ImageScreenState(),
)