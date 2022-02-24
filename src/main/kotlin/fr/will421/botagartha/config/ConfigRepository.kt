package fr.will421.botagartha.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException


class ConfigRepository {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val om: ObjectMapper = ObjectMapper(YAMLFactory())
    private val configPath = "botConfig.yaml"
    private var botConfig: BotConfig
    private val mutex = Mutex()


    init {
        try {
            botConfig = om.readValue(File(configPath), BotConfig::class.java)
        } catch (e: FileNotFoundException) {
            logger.info("Config file not found, initializing a new one")
            botConfig = BotConfig()
            storeConfig()
        }
    }

    suspend fun updateVoterRoleId(id: String?) = mutex.withLock {
        botConfig.allowedRoleId = id
        storeConfig()
    }

    suspend fun addProxys(ids: List<String>) = mutex.withLock {
        botConfig.proxys.addAll(ids)
        storeConfig()
    }

    suspend fun clearProxys() = mutex.withLock {
        botConfig.proxys = mutableSetOf()
        storeConfig()
    }

    fun getProxys(): Set<String> {
        return botConfig.proxys
    }

    private fun storeConfig() {
        om.writeValue(File(configPath), botConfig)
    }

    fun getVoterRoleId(): String? {
        return botConfig.allowedRoleId
    }


}