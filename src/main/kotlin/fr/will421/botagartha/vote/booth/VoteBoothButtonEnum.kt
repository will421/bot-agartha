package fr.will421.botagartha.vote.booth

import dev.kord.common.entity.ButtonStyle
import fr.will421.botagartha.Emoji

enum class VoteBoothButtonEnum(val label: String, val emoji: String, val style: ButtonStyle) {

    POSITIVE("Pour", Emoji.EMOJI_THUMBS_UP, ButtonStyle.Primary),
    NEGATIVE("Contre", Emoji.EMOJI_THUMBS_DOWN, ButtonStyle.Danger),
    NEUTRAL("Neutre", Emoji.EMOJI_SHRUGGING, ButtonStyle.Secondary),

    PROXY_POSITIVE("Procuration : Pour", Emoji.EMOJI_THUMBS_UP, ButtonStyle.Primary),
    PROXY_NEGATIVE("Procuration : Contre", Emoji.EMOJI_THUMBS_DOWN, ButtonStyle.Danger),
    PROXY_NEUTRAL("Procuration : Neutre", Emoji.EMOJI_SHRUGGING, ButtonStyle.Secondary);

    fun isAButton(): Boolean = this in VOTE_BUTTONS
    fun isAProxyButton(): Boolean = this in PROXY_VOTE_BUTTONS

    companion object {
        val VOTE_BUTTONS = listOf(POSITIVE, NEGATIVE, NEUTRAL)
        val PROXY_VOTE_BUTTONS = listOf(PROXY_POSITIVE, PROXY_NEGATIVE, PROXY_NEUTRAL)

        fun valueOfOrNull(buttonId: String): VoteBoothButtonEnum? {
            return try {
                valueOf(buttonId)
            } catch (ex: IllegalArgumentException) {
                null
            }
        }
    }
}