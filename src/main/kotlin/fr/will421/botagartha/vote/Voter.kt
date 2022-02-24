package fr.will421.botagartha.vote

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import fr.will421.botagartha.vote.booth.VoteBoothButtonEnum

data class Voter(val userId: Snowflake, val isProxy: Boolean) {
    var voteButton: VoteBoothButtonEnum? = null
    var voteProxyButton: VoteBoothButtonEnum? = null
    var hasVoted: Boolean = false
    var hasProxyVoted: Boolean = false
    var voteBooth: EphemeralFollowupMessage? = null

}