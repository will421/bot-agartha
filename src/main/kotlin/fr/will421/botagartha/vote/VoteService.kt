package fr.will421.botagartha.vote

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import fr.will421.botagartha.config.ConfigRepository
import org.slf4j.LoggerFactory

class VoteService(private val configRepository: ConfigRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun startVote(
        voteQuestion: String,
        interaction: PublicInteractionResponseBehavior,
        user: UserBehavior
    ) {
        logger.info("Nouveau vote : $voteQuestion")

        val vote = Vote(voteQuestion, interaction, user.fetchUser(), configRepository)
        vote.startVote()
    }

    suspend fun setVoterRole(role: Role) {
        logger.info("Set voter role : ${role.id}")
        configRepository.updateVoterRoleId(role.id.toString())
    }

    fun buildVoterRoleString(): String {
        val voterRoleId = configRepository.getVoterRoleId()
        return if (voterRoleId == null) {
            "Le vote n'est pas limité à un rôle"
        } else {
            "Vote limité au rôle : <@&$voterRoleId>"
        }
    }

    suspend fun clearVoterRole() {
        logger.info("Clear voter role")
        configRepository.updateVoterRoleId(null)
    }

    fun buildProxyString(): String {
        val proxysRef = configRepository.getProxys().map { "<@!$it>" }
        return "Procurations : ${proxysRef.joinToString("")}"
    }

    suspend fun addProxys(userRef: List<User>) {
        val userIds = userRef.map { it.id.toString() }
        logger.info("Add proxys $userIds")
        configRepository.addProxys(userIds)
    }

    suspend fun clearProxys() {
        logger.info("Clear proxys")
        configRepository.clearProxys()
    }
}
