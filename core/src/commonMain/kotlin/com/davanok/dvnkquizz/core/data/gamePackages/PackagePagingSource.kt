package com.davanok.dvnkquizz.core.data.gamePackages

import androidx.paging.PagingSource
import androidx.paging.PagingState
import co.touchlab.kermit.Logger
import com.davanok.dvnkquizz.core.domain.gamePackage.entities.GamePackage

class PackagePagingSource(
    private val getPage: suspend (query: String, from: Long, count: Int) -> List<GamePackage>,
    private val query: String,
    private val logger: Logger
) : PagingSource<Int, GamePackage>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GamePackage> {
        val page = params.key ?: 0
        val pageSize = params.loadSize
        val from = page * pageSize

        return runCatching {
            getPage(query, from.toLong(), pageSize)
        }.onFailure {
            logger.e(it) { "failed to load packages" }
        }.fold(
            onSuccess = { response ->
                LoadResult.Page(
                    data = response,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (response.isEmpty()) null else page + 1
                )
            },
            onFailure = { e ->
                LoadResult.Error(e)
            }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, GamePackage>): Int? =
        state.anchorPosition?.let { state.closestPageToPosition(it)?.prevKey?.plus(1) }
}