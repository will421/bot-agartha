package fr.will421.botagartha.vote

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.entity.User
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import fr.will421.botagartha.Emoji


fun MessageCreateBuilder.voteEmbed(block: VoteEmbedBuilder.() -> Unit) {
    VoteEmbedBuilder().apply(block).build(this)
}

fun MessageModifyBuilder.voteEmbed(block: VoteEmbedBuilder.() -> Unit) {
    VoteEmbedBuilder().apply(block).build(this)
}

class VoteEmbedBuilder {
    var question = ""
    var votingCount = 0
    var voteCount = 0
    var proxyCount = 0
    lateinit var creator: User
    private var voteResult: VoteResult? = null

    fun voteResult(block: VoteResult.() -> Unit) {
        voteResult = VoteResult().apply(block)
    }

    fun build(messageCreateBuilder: MessageCreateBuilder) {
        messageCreateBuilder.apply {
            embed { createEmbed(this) }
            actionRow { createActionRow(this) }

        }
    }

    fun build(messageModifyBuilder: MessageModifyBuilder) {
        messageModifyBuilder.apply {
            embed { createEmbed(this) }
            if (voteResult == null) {
                actionRow { createActionRow(this) }
            } else {
                components = mutableListOf()
            }
        }
    }

    private fun createEmbed(builder: EmbedBuilder): EmbedBuilder {
        return builder.apply {
            title = "Vote : $question"

            field {
                this.name = "Votants"
                this.value = votingCount.toString()
                this.inline = true
            }
            field {
                this.name = "Ont voté (procurations)"
                this.value = "$voteCount ($proxyCount)"
                this.inline = true
            }
            if (voteResult == null) {
                footer {
                    this.text = "Seulement ${creator.username} peut clore le vote"
                }
            } else {
                field {
                    this.name = "Pour | Contre | Blanc"
                    this.value = "${voteResult!!.approve} | ${voteResult!!.against} | ${voteResult!!.blank}"
                    this.inline = true
                }
            }
        }
    }

    private fun createActionRow(actionRowBuilder: ActionRowBuilder) {
        actionRowBuilder.apply {
            interactionButton(ButtonStyle.Primary, Vote.VOTE_BUTTON_ID) {
                label = "Voter"
                emoji = DiscordPartialEmoji(name = Emoji.EMOJI_BALLOT_BOX)
            }
            interactionButton(ButtonStyle.Primary, Vote.CLOSE_VOTE_BUTTON_ID) {
                label = "Clore le vote"
                emoji = DiscordPartialEmoji(name = Emoji.EMOJI_STOP_SIGN)
            }
        }
    }
}

class VoteResult(var approve: Int = 0, var against: Int = 0, var blank: Int = 0) {
}