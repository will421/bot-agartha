@file:OptIn(KordPreview::class)

package fr.will421.botagartha.vote

import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.live.live
import dev.kord.core.live.on
import fr.will421.botagartha.config.ConfigRepository
import fr.will421.botagartha.vote.booth.VoteBoothButtonEnum
import fr.will421.botagartha.vote.booth.VoteBoothButtonEnum.*
import fr.will421.botagartha.vote.booth.voteBoothEmbed
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

class Vote(
    private var voteQuestion: String,
    private val interaction: PublicInteractionResponseBehavior,
    private val voteCreator: User,
    private val botConfigRepository: ConfigRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private lateinit var voteMessage: Message
    private val voters = mutableSetOf<Voter>()
    private var voteApprove = 0
    private var voteAgainst = 0
    private var voteBlank = 0
    private var active = true
    private val mutex = Mutex()

    suspend fun startVote() {
        val response = interaction.followUp {
            voteEmbed {
                question = voteQuestion
                creator = voteCreator
                votingCount = 0
                voteCount = 0
                proxyCount = 0
            }
        }.message

        voteMessage = response

        val liveVote = response.live()
        liveVote.on<InteractionCreateEvent>() {
            val interaction = it.interaction
            if (interaction !is ComponentInteraction) {
                return@on
            }
            val componentId = interaction.componentId
            when (componentId) {
                VOTE_BUTTON_ID -> addVoter(interaction.user, interaction.acknowledgeEphemeralDeferredMessageUpdate())
                CLOSE_VOTE_BUTTON_ID -> {
                    interaction.acknowledgeEphemeralDeferredMessageUpdate()
                    endVote(interaction.user)
                }
            }
        }
    }

    private suspend fun addVoter(user: User, interaction: EphemeralInteractionResponseBehavior) = mutex.withLock {
        val existingVoter = voters.find { it.userId == user.id }

        val voter = if (existingVoter == null) {
            logger.info("[$voteQuestion] add voter ${user.id}")
            val isProxy = botConfigRepository.getProxys().any { it == user.id.toString() }
            Voter(user.id, isProxy)
        } else {
            logger.info("[$voteQuestion] Already voter ${user.id}")
            existingVoter
        }
        voters.add(voter)

        val voteBooth = interaction.followUpEphemeral {
            voteBoothEmbed {
                question = voteQuestion
                proxyMode = voter.isProxy
            }
        }

        // Edit old voteBooth to keep only one active
        voter.voteBooth?.edit {
            content = "Un nouveau message a été renvoyé"
            embeds = mutableListOf()
            components = mutableListOf()
        }

        voter.voteBooth = voteBooth
        updateVoteBooth(voter)

        voteBooth.message.live().on<InteractionCreateEvent> {
            val buttonInteraction = it.interaction
            if (buttonInteraction !is ComponentInteraction) {
                return@on
            }
            val voteBoothButton = VoteBoothButtonEnum.valueOfOrNull(buttonInteraction.componentId)
            if (voteBoothButton != null) {
                buttonInteraction.acknowledgeEphemeralDeferredMessageUpdate()
                voteAction(voter, voteBoothButton)
            }
        }

        updateVoteMessage()
    }

    private suspend fun voteAction(voter: Voter, voteBoothButton: VoteBoothButtonEnum) = mutex.withLock {
        if (voteBoothButton.isAButton() && !voter.hasVoted) {
            when (voteBoothButton) {
                POSITIVE -> voteApprove++
                NEGATIVE -> voteAgainst++
                NEUTRAL -> voteBlank++
                else -> {}
            }
            voter.hasVoted = true
            voter.voteButton = voteBoothButton

            updateVoteBooth(voter)

        } else if (voteBoothButton.isAProxyButton() && voter.isProxy && !voter.hasProxyVoted) {
            when (voteBoothButton) {
                PROXY_POSITIVE -> voteApprove++
                PROXY_NEGATIVE -> voteAgainst++
                PROXY_NEUTRAL -> voteBlank++
                else -> {}
            }

            voter.hasProxyVoted = true
            voter.voteProxyButton = voteBoothButton

            updateVoteBooth(voter)
        }

        updateVoteMessage()
    }

    private suspend fun updateVoteBooth(voter: Voter) {
        voter.voteBooth?.edit {
            voteBoothEmbed {
                question = voteQuestion
                proxyMode = voter.isProxy
                voteButtonChoosen = voter.voteButton
                proxyVoteButtonChoosen = voter.voteProxyButton
            }
        }
    }

    private suspend fun updateVoteMessage() {
        if (active) {
            voteMessage.edit {
                voteEmbed {
                    question = voteQuestion
                    creator = voteCreator
                    votingCount = voters.size
                    voteCount = voters.count { it.hasVoted }
                    voteCount = voters.count { it.hasVoted }
                    proxyCount = voters.count { it.isProxy && it.hasProxyVoted }
                }
            }
        } else {
            voteMessage.edit {
                voteEmbed {
                    question = voteQuestion
                    creator = voteCreator
                    votingCount = voters.size
                    voteCount = voters.count { it.hasVoted }
                    voteCount = voters.count { it.hasVoted }
                    proxyCount = voters.count { it.isProxy && it.hasProxyVoted }
                    voteResult {
                        approve = voteApprove
                        against = voteAgainst
                        blank = voteBlank
                    }
                }
            }
        }
    }

    private suspend fun endVote(user: UserBehavior) {
        if (user.id != voteCreator.id) {
            return
        }

        mutex.withLock {
            active = false
            updateVoteMessage()
        }
    }

    companion object {
        const val VOTE_BUTTON_ID = "vote"
        const val CLOSE_VOTE_BUTTON_ID = "close_vote"
    }


}