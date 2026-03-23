package com.davanok.dvnkquizz.core.data

import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.SessionAnswer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
internal class GameSessionEnricher(
    private val observeActiveQuestion: () -> Flow<Question>,
    private val getSessionBoard: suspend (Uuid) -> List<GameBoardItem>
) {
    private data class GameBoardCache(
        val roundId: Uuid,
        val answersCount: Int,
        val items: List<GameBoardItem>
    )

    private var boardCache: GameBoardCache? = null

    fun observeEnrichedSession(
        statusFlow: Flow<GameSessionStatus>,
        answersFlow: Flow<List<SessionAnswer>>
    ): Flow<FullGameSession> = channelFlow {
        val sharedStatus = statusFlow.shareIn(
            scope = this,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1
        )

        // 1. Create a stable question stream that ONLY restarts if the ID changes
        val questionStream: Flow<Question?> = sharedStatus
            .map { it.session.currentQuestionId }
            .distinctUntilChanged() // <--- CRITICAL: Prevents restart on status updates
            .flatMapLatest { questionId ->
                if (questionId == null) flowOf(null)
                else observeActiveQuestion()
            }

        // 2. Combine the stable question stream with the fast-moving session/answer data
        combine(
            sharedStatus,
            answersFlow,
            questionStream
        ) { status, answers, activeQuestion ->
            val session = status.session

            // The board fetch is suspend/cached, it won't interrupt the question stream
            val boardItems = updateBoardIfNeeded(session.currentRoundId, answers.size)

            FullGameSession(
                session = session,
                gamePackage = status.gamePackage,
                participants = status.participants,
                answers = answers,
                isHost = status.isHost,
                gameBoard = boardItems,
                activeQuestion = activeQuestion
            )
        }.collect {
            send(it)
        }
    }

    private suspend fun updateBoardIfNeeded(roundId: Uuid?, answersCount: Int): List<GameBoardItem> {
        if (roundId == null) {
            boardCache = null
            return emptyList()
        }
        // Only re-fetch if round changes or an answer is submitted
        if (boardCache?.roundId != roundId || boardCache?.answersCount != answersCount) {
            val items = getSessionBoard(roundId)
            boardCache = GameBoardCache(roundId, answersCount, items)
        }
        return boardCache?.items.orEmpty()
    }
}