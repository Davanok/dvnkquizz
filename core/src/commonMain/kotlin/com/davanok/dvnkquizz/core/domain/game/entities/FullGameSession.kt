package com.davanok.dvnkquizz.core.domain.game.entities

import com.davanok.dvnkquizz.core.domain.gamePackage.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.game.enums.ParticipantRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


@Serializable
internal data class FullGameSessionDto(
    val session: GameSession,
    @SerialName("game_package")
    val gamePackage: GamePackage,
    val participants: List<ParticipantDto>,
    val answers: List<SessionAnswer>,
    @SerialName("game_board")
    val gameBoard: List<GameBoardItem>,
    @SerialName("active_question")
    val activeQuestion: QuestionDto?
) {
    private fun getMyRole(currentUserId: Uuid?): ParticipantRole {
        if (currentUserId == null) return ParticipantRole.SPECTATOR
        if (session.hostId == currentUserId) return ParticipantRole.HOST
        return participants
            .firstOrNull { it.id == currentUserId }
            ?.role
            ?: ParticipantRole.SPECTATOR
    }

    inline fun toDomain(
        currentUserId: Uuid?,
        transformActiveQuestion: (QuestionDto?) -> Question?
    ): FullGameSession = FullGameSession(
        session = session,
        myRole = getMyRole(currentUserId),
        gamePackage = gamePackage,
        participants = participants.map { it.toDomain(currentUserId) },
        answers = answers,
        gameBoard = gameBoard,
        activeQuestion = activeQuestion.let(transformActiveQuestion)
    )

}

data class FullGameSession(
    val session: GameSession,
    val myRole: ParticipantRole,
    val gamePackage: GamePackage,
    val participants: List<Participant>,
    val answers: List<SessionAnswer>,
    val gameBoard: List<GameBoardItem>,
    val activeQuestion: Question?
)