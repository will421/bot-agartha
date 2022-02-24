package fr.will421.botagartha.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

data class GuildConfig(val serverId: String, val elevatedRole: String, val botAdmin: String)


object GuildConfigLoader {
    private val om: ObjectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    fun load(serverTag: String): GuildConfig {
        return om.readValue(
            File("src/fr.will421.botagartha.main/resources/guildConfig-${serverTag}.yml"),
            GuildConfig::class.java
        )
    }
}