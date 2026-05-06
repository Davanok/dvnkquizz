package com.davanok.dvnkquizz.core.data

import com.davanok.dvnkquizz.core.domain.entities.FullGamePackageDto
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.mappers.toGamePackage
import com.davanok.dvnkquizz.core.domain.repositories.Storage
import com.davanok.dvnkquizz.core.domain.repositories.get
import com.davanok.dvnkquizz.core.domain.repositories.set
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.uuid.Uuid

@Inject
@SingleIn(AppScope::class)
internal class DraftsStorage(
    private val storage: Storage
) {

    fun setDraft(draft: FullGamePackageDto) {
        val key = buildKey(draft.id)
        val gamePackage = draft.toGamePackage()

        storage.set(key, draft)

        val updatedList = storage
            .get<List<GamePackage>>(PACKAGES_LIST_KEY)
            .orEmpty()
            .toMutableList()
            .apply {
                val index = indexOfFirst { it.id == draft.id }
                if (index >= 0) {
                    this[index] = gamePackage
                } else {
                    add(gamePackage)
                }
            }

        storage.set(PACKAGES_LIST_KEY, updatedList)
    }

    fun getDraft(draftId: Uuid): FullGamePackageDto? =
        storage.get(buildKey(draftId))

    fun getSavedDrafts(): List<GamePackage> =
        storage.get<List<GamePackage>>(PACKAGES_LIST_KEY).orEmpty()

    fun deleteDraft(draftId: Uuid) {
        storage.delete(buildKey(draftId))

        val updatedList = storage
            .get<List<GamePackage>>(PACKAGES_LIST_KEY)
            .orEmpty()
            .filterNot { it.id == draftId }

        storage.set(PACKAGES_LIST_KEY, updatedList)
    }

    companion object {
        private const val PACKAGES_LIST_KEY = "game-packages"
        private const val FULL_GAME_PACKAGE_PREFIX = "game-package"

        private fun buildKey(packageId: Uuid) =
            "$FULL_GAME_PACKAGE_PREFIX:$packageId"
    }
}