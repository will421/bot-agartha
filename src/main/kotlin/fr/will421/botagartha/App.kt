package fr.will421.botagartha

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import fr.will421.botagartha.config.ConfigRepository
import fr.will421.botagartha.config.GuildConfigLoader
import fr.will421.botagartha.extensions.ping.PingExtension
import fr.will421.botagartha.extensions.transcript.TranscriptExtension
import fr.will421.botagartha.extensions.vote.VoteConfigExtension
import fr.will421.botagartha.extensions.vote.VoteExtension
import fr.will421.botagartha.transcript.TranscriptService
import fr.will421.botagartha.transcript.VoskTranscript
import fr.will421.botagartha.vote.VoteService
import org.koin.dsl.module


private val TOKEN = env("TOKEN")
private val ENV = env("ENV")

suspend fun main() {

    val guildConfig = GuildConfigLoader.load(ENV)

    val bot = ExtensibleBot(TOKEN) {
        applicationCommands {
            // Register all global commands on this guild for testing
            defaultGuild(guildConfig.serverId)
        }
        extensions {
            add(::PingExtension)
            add(::VoteExtension)
            add(::VoteConfigExtension)
            add(::TranscriptExtension)
        }
    }

    val modules = module {
        single { ConfigRepository() }
        single { VoteService(get()) }
        single { TranscriptService() }
        single { VoskTranscript() }
        single { guildConfig }
    }

    bot.getKoin().loadModules(listOf(modules))

    bot.start()
}