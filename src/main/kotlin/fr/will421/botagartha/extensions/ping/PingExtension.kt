package fr.will421.botagartha.extensions.ping

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

class PingExtension : Extension() {
    override val name = "ping"

    override suspend fun setup() {
        publicSlashCommand {
            name = "ping"
            description = "ping-pong"

            action {
                respond {
                    this.content = "Pong"
                }
            }
        }

    }
}