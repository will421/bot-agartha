package fr.will421.botagartha.vote.booth

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import fr.will421.botagartha.vote.booth.VoteBoothButtonEnum.Companion.PROXY_VOTE_BUTTONS
import fr.will421.botagartha.vote.booth.VoteBoothButtonEnum.Companion.VOTE_BUTTONS

fun MessageCreateBuilder.voteBoothEmbed(block: VoteBoothEmbedBuilder.() -> Unit) {
    VoteBoothEmbedBuilder().apply(block).build(this)
}

fun MessageModifyBuilder.voteBoothEmbed(block: VoteBoothEmbedBuilder.() -> Unit) {
    VoteBoothEmbedBuilder().apply(block).build(this)
}

class VoteBoothEmbedBuilder {
    var proxyVoteButtonChoosen: VoteBoothButtonEnum? = null
    var voteButtonChoosen: VoteBoothButtonEnum? = null
    lateinit var question: String
    var proxyMode: Boolean = false


    fun build(messageCreateBuilder: MessageCreateBuilder) {
        messageCreateBuilder.apply {
            embed {
                createEmbed(this)
            }
            actionRow {
                createActionRow(this)
            }
            if (proxyMode) {
                actionRow {
                    createActionRowForProxy(this)
                }
            }
        }
    }

    fun build(messageModifyBuilder: MessageModifyBuilder) {
        messageModifyBuilder.apply {
            embed {
                createEmbed(this)
            }
            actionRow {
                createActionRow(this)
            }
            if (proxyMode) {
                actionRow {
                    createActionRowForProxy(this)
                }
            }
        }
    }

    private fun createEmbed(builder: EmbedBuilder) {
        builder.apply {
            title = "Vote : $question"
            footer {
                this.text = "Attention, les votes sont finaux"
            }
        }
    }

    private fun createActionRow(builder: ActionRowBuilder) {
        builder.apply {
            VOTE_BUTTONS.filter { voteButtonChoosen == null || voteButtonChoosen == it }.forEach {
                interactionButton(it.style, it.name) {
                    label = it.label
                    emoji = DiscordPartialEmoji(name = it.emoji)
                    disabled = voteButtonChoosen != null
                }
            }
        }
    }

    private fun createActionRowForProxy(builder: ActionRowBuilder) {
        builder.apply {
            PROXY_VOTE_BUTTONS.filter { proxyVoteButtonChoosen == null || proxyVoteButtonChoosen == it }.forEach {
                interactionButton(it.style, it.name) {
                    label = it.label
                    emoji = DiscordPartialEmoji(name = it.emoji)
                    disabled = proxyVoteButtonChoosen != null
                }
            }
        }
    }
}