package fr.will421.botagartha.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
fun <T> Flow<T>.bufferTimeout(size: Int, duration: Duration): Flow<List<T>> {
    require(size > 0) { "Window size should be greater than 0" }
    require(duration.toMillis() > 0) { "Duration should be greater than 0" }

    return flow {
        coroutineScope {
            val events = ArrayList<T>(size)
            var tickerChannel: ReceiveChannel<Unit>? = null
            try {
                val upstreamValues = produce { collect { send(it) } }

                while (isActive) {
                    var hasTimedOut = false

                    select<Unit> {
                        upstreamValues.onReceive {
                            events.add(it)
                            tickerChannel?.cancel()
                            tickerChannel = ticker(duration.toMillis())
                        }
                        tickerChannel?.onReceive {
                            hasTimedOut = true
                        }
                    }

                    if (events.size == size || (hasTimedOut && events.isNotEmpty())) {
                        emit(events.toList())
                        events.clear()
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // drain remaining events
                if (events.isNotEmpty()) emit(events.toList())
            } finally {
                tickerChannel?.cancel()
            }
        }
    }
}