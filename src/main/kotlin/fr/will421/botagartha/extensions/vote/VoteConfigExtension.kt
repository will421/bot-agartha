package fr.will421.botagartha.extensions.vote

import com.kotlindiscord.kord.extensions.checks.topRoleHigherOrEqual
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.role
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.extensions.slashCommandCheck
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.allowedMentions
import fr.will421.botagartha.config.GuildConfig
import fr.will421.botagartha.vote.VoteService
import org.koin.core.component.inject

class VoteConfigExtension : Extension() {
    override val name = "voteconfig"
    val voteService: VoteService by inject()
    val guildConfig: GuildConfig by inject()

    override suspend fun setup() {
        publicSlashCommand {
            name = "voteconfig"
            description = "Change les parametres des votes"

            group("role") {
                description = "Manipule le rôle des votants"
                publicSubCommand {
                    name = "get"
                    description = "Affiche le rôle des votants"

                    action {
                        respond {
                            content = voteService.buildVoterRoleString()
                            allowedMentions { }
                        }
                    }
                }

                publicSubCommand(::RoleSetArgs) {
                    name = "set"
                    description = "Définit le rôle des votants"

                    slashCommandCheck {
                        topRoleHigherOrEqual(Snowflake(guildConfig.elevatedRole))
                    }

                    action {
                        voteService.setVoterRole(arguments.role)
                        respond {
                            content = voteService.buildVoterRoleString()
                            allowedMentions { }
                        }
                    }
                }

                publicSubCommand {
                    name = "clear"
                    description = "Retire la limitation par rôle"

                    slashCommandCheck {
                        topRoleHigherOrEqual(Snowflake(guildConfig.elevatedRole))
                    }

                    action {
                        voteService.clearVoterRole()
                        respond {
                            content = voteService.buildVoterRoleString()
                            allowedMentions { }
                        }
                    }
                }
            }

            group("proxy") {
                description = "Manipule les procurations"

                publicSubCommand {
                    name = "get"
                    description = "Affiche qui détient une procuration"

                    action {
                        respond {
                            content = voteService.buildProxyString()
                            allowedMentions {}
                        }
                    }
                }

                publicSubCommand(::ProxyAddArgs) {
                    name = "add"
                    description = "Définit un utilisateur comme ayant une procuration"

                    slashCommandCheck {
                        topRoleHigherOrEqual(Snowflake(guildConfig.elevatedRole))
                    }

                    action {
                        voteService.addProxys(listOf(arguments.userToAdd))
                        respond {
                            content = voteService.buildProxyString()
                            allowedMentions {}
                        }
                    }
                }

                publicSubCommand {
                    name = "clear"
                    description = "Retire les procurations"

                    slashCommandCheck {
                        topRoleHigherOrEqual(Snowflake(guildConfig.elevatedRole))
                    }

                    action {
                        voteService.clearProxys()
                        respond {
                            content = voteService.buildProxyString()
                            allowedMentions {}
                        }
                    }
                }
            }
        }
    }

    inner class RoleSetArgs : Arguments() {
        val role by role {
            name = "role"
            description = "Reference du rôle des votants"
        }
    }

    inner class ProxyAddArgs : Arguments() {
        val userToAdd by user {
            name = "user"
            description = "Utilisateur pour lequel ajouter une procuration"
        }
    }
}