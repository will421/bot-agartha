package fr.will421.botagartha.extensions.transcript

import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import dev.kord.common.entity.Snowflake
import fr.will421.botagartha.config.GuildConfig
import fr.will421.botagartha.transcript.TranscriptService
import fr.will421.botagartha.transcript.VoskTranscript
import fr.will421.botagartha.utils.checkUserInGuildVoiceChannel
import fr.will421.botagartha.utils.checkUserIs
import org.koin.core.component.inject

class TranscriptExtension : Extension() {
    override val name = "transcript"
    val guildConfig: GuildConfig by inject()
    val transcriptService: TranscriptService by inject()

    companion object {
        init {
            VoskTranscript.initModel()
        }
    }

    override suspend fun setup() {
        publicSlashCommand {
            name = "voice"
            description = "Groupe de commande utilisant la voix"

            slashCommandCheck {
                checkUserInGuildVoiceChannel()
                checkUserIs(Snowflake(guildConfig.botAdmin))
            }

            publicSubCommand {
                name = "echo"
                description = "le bot repete ce qu'il entend"

                action {
                    val member = memberFor(event)
                    val voiceChannel = member!!.getVoiceState().getChannelOrNull()!!
                    interactionResponse.delete()
                    transcriptService.startEcho(voiceChannel)
                }
            }

            publicSubCommand {
                name = "speaker"
                description = "le bot transmet ce qu'il entend sur la machine hôte"

                action {
                    val member = memberFor(event)
                    val voiceChannel = member!!.getVoiceState().getChannelOrNull()!!
                    interactionResponse.delete()
                    transcriptService.startSendToSpeaker(voiceChannel)
                }
            }

            publicSubCommand {
                name = "transcript"
                description = "Ecrit ce qu'il entend dans le canal"

                action {
                    val member = memberFor(event)
                    val voiceChannel = member!!.getVoiceState().getChannelOrNull()!!
                    interactionResponse.delete()
                    transcriptService.startTranscript(voiceChannel)
                }
            }

            publicSubCommand {
                name = "stop"
                description = "Déconnecte le bot du canal"

                action {
                    interactionResponse.delete()
                    transcriptService.stop()
                }
            }
        }

    }
}