package com.davanok.dvnkquizz.ui.components

import coil3.Extras
import coil3.ImageLoader
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.network.httpHeaders
import coil3.request.Options
import coil3.toUri
import com.davanok.dvnkquizz.core.domain.entities.ExternalFile

class ExternalImageFetcher(
    private val image: ExternalFile,
    private val options: Options,
    private val imageLoader: ImageLoader
): Fetcher {
    override suspend fun fetch(): FetchResult? {
        val extras = options.extras.newBuilder().apply {
            this[Extras.Key.httpHeaders] = options.httpHeaders.newBuilder().apply {
                set("Authorization", "Bearer ${image.accessToken}")
            }.build()
        }
        val (fetcher, _) = imageLoader.components.newFetcher(
            image.url.toUri(),
            options.copy(extras = extras.build()),
            imageLoader
        ) ?: error("No fetcher found for ${image.url}")
        return fetcher.fetch()
    }
}