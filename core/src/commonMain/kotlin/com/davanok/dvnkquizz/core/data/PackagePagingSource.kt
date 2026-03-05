package com.davanok.dvnkquizz.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import io.github.jan.supabase.postgrest.Postgrest

class PackagePagingSource(
    private val postgrest: Postgrest,
    private val query: String
) : PagingSource<Int, GamePackage>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GamePackage> {
        val page = params.key ?: 0
        val pageSize = params.loadSize
        val from = page * pageSize
        val to = from + pageSize - 1

        return runCatching {
            postgrest.from("game_packages")
                .select {
                    if (query.isNotEmpty()) filter { ilike("title", "%$query%") }
                    filter { eq("is_public", true) }
                    range(from.toLong(), to.toLong())
                }
                .decodeList<GamePackage>()
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