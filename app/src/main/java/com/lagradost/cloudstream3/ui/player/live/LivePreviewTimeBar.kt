package com.lagradost.cloudstream3.ui.player.live

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R
import com.github.rubensousa.previewseekbar.media3.PreviewTimeBar
import java.lang.ref.WeakReference


@OptIn(UnstableApi::class)
class LivePreviewTimeBar(val ctx: Context, attrs: AttributeSet) : PreviewTimeBar(ctx, attrs) {

    private var _currentPlayerView: WeakReference<PlayerView>? = null
    val currentPlayer: Player? get() = _currentPlayerView?.get()?.player

    /**
     * When non-null, pins the scrubber thumb to this position instead of the live playback
     * position. Used to preview a deferred dpad seek while the video keeps playing.
     */
    var previewOverridePosition: Long? = null
        set(value) {
            field = value
            if (value != null) setPosition(value)
        }

    fun registerPlayerView(player: PlayerView?) {
        _currentPlayerView = WeakReference(player)
        val controller =
            _currentPlayerView?.get()?.findViewById<PlayerControlView>(R.id.exo_controller)

        // Runs after the control view sets the live position each tick, so overriding here wins
        // and keeps the thumb pinned to the preview target between key presses.
        controller?.setProgressUpdateListener { position, bufferedPosition ->
            val override = previewOverridePosition
            when {
                override != null -> setPosition(override)
                isAtLiveEdge() -> currentPlayer?.let { setPosition(it.duration) }
            }
        }
    }

    fun isAtLiveEdge(): Boolean {
        return LiveHelper.getLiveManager(currentPlayer)?.isAtLiveEdge() == true
    }
}