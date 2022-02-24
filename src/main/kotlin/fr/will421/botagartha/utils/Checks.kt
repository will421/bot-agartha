package fr.will421.botagartha.utils

import com.kotlindiscord.kord.extensions.checks.memberFor
import com.kotlindiscord.kord.extensions.checks.nullMember
import com.kotlindiscord.kord.extensions.checks.passed
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.checks.userFor
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event
import mu.KotlinLogging

suspend fun <T : Event> CheckContext<T>.checkUserInGuildVoiceChannel() {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("fr.will421.botagartha.checks.checkUserInGuildVoiceChannel")
    val member = memberFor(event)

    if (member == null) {
        logger.nullMember(event)
        fail()
        return
    }

    val voiceChannel = member.getVoiceStateOrNull()?.getChannelOrNull()
    if (voiceChannel == null) {
        logger.debug { "User needs to be in a voice channel" }
        fail("User needs to be in a voice channel")
    } else {
        logger.passed()
        pass()
    }
}

suspend fun <T : Event> CheckContext<T>.checkUserIs(id: Snowflake) {
    if (!passed) {
        return
    }

    val logger = KotlinLogging.logger("fr.will421.botagartha.checks.checkUserIs")
    val user = userFor(event)

    if (user == null) {
        logger.debug { "User for event $event is null. This type of event may not be supported." }
        fail()
        return
    }

    if (user.id == id) {
        logger.passed()
        pass()
    } else {
        logger.debug { "Only user with id $id can do this" }
        fail("Only user with id $id can do this")
    }
}