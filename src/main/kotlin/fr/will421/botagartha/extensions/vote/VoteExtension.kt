package fr.will421.botagartha.extensions.vote

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import fr.will421.botagartha.vote.VoteService
import org.koin.core.component.inject

class VoteExtension : Extension() {
    override val name = "vote"
    val voteService: VoteService by inject()

    override suspend fun setup() {
        publicSlashCommand(::VoteArgs) {
            name = "vote"
            description = "Lance un nouveau vote"

            action {
                voteService.startVote(arguments.voteQuestion, interactionResponse, user)
            }
        }
    }

    inner class VoteArgs : Arguments() {
        val voteQuestion by string {
            this.name = "objet"
            this.description = "L'objet du vote"
        }
    }
}
