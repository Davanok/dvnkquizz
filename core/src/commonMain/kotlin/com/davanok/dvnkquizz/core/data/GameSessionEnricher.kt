package com.davanok.dvnkquizz.core.data

import com.davanok.dvnkquizz.core.domain.entities.FullGameSession
import com.davanok.dvnkquizz.core.domain.entities.GameBoardItem
import com.davanok.dvnkquizz.core.domain.entities.GameSessionStatus
import com.davanok.dvnkquizz.core.domain.entities.Question
import com.davanok.dvnkquizz.core.domain.entities.SessionAnswer
import kotlin.uuid.Uuid

// Private helper class to manage state and fetching
internal class GameSessionEnricher(
    private val getActiveQuestion: suspend () -> Question,
    private val getSessionBoard: suspend (roundId: Uuid) -> List<GameBoardItem>
) {
    private data class QuestionCache(
        val questionId: Uuid,
        val isVisible: Boolean,
        val question: Question
    )

    private data class GameBoardCache(
        val roundId: Uuid,
        val answersCount: Int,
        val items: List<GameBoardItem>
    )
    private var questionCache: QuestionCache? = null
    private var boardCache: GameBoardCache? = null

    suspend fun enrich(
        status: GameSessionStatus, // Assuming this is the type from observeGameSessionStatus
        answers: List<SessionAnswer>
    ): FullGameSession {
        val session = status.session

        val question = updateQuestionIfNeeded(session.currentQuestionId, session.isAnswerVisible)
        val boardItems = updateBoardIfNeeded(session.currentRoundId, answers.size)

        return FullGameSession(
            session = session,
            gamePackage = status.gamePackage,
            participants = status.participants,
            answers = answers,
            isHost = status.isHost,
            gameBoard = boardItems,
            activeQuestion = question
        )
    }

    private suspend fun updateQuestionIfNeeded(id: Uuid?, visible: Boolean): Question? {
        if (id == null) {
            questionCache = null
            return null
        }
        if (questionCache?.questionId != id || questionCache?.isVisible != visible) {
            questionCache = QuestionCache(id, visible, getActiveQuestion())
        }
        return questionCache?.question
    }

    private suspend fun updateBoardIfNeeded(roundId: Uuid?, answersCount: Int): List<GameBoardItem> {
        if (roundId == null) {
            boardCache = null
            return emptyList()
        }
        if (boardCache?.roundId != roundId || boardCache?.answersCount != answersCount) {
            boardCache = GameBoardCache(roundId, answersCount, getSessionBoard(roundId))
        }
        return boardCache?.items.orEmpty()
    }
}