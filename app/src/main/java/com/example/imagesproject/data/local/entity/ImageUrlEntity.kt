package com.example.imagesproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.imagesproject.presentation.Constants.IMAGES_URL_TABLE_NAME

@Entity(tableName = IMAGES_URL_TABLE_NAME)
data class ImageUrlEntity(
    @PrimaryKey
    var imageUrl: String,
)
