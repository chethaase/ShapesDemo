package dev.chet.graphics.shapes.shapesdemo

import android.graphics.PointF
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.div
import androidx.core.graphics.plus
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import dev.chet.graphics.shapes.shapesdemo.compose.MorphComposable
import dev.chet.graphics.shapes.shapesdemo.compose.SizedMorph

val rectangle = listOf(
    PointF(6f, 5f),
    PointF(10f, 5f),
    PointF(10f, 19f),
    PointF(6f, 19f),
)
val leftPause =
    RoundedPolygon(
        vertices = rectangle.transformFrom(24f),
    )

val rightPause =
    RoundedPolygon(
        vertices = rectangle.offset(offset = PointF(8f, 0f)).transformFrom(24f)
    )

val bottomPlay =
    RoundedPolygon(
        vertices = listOf(
            PointF(8f, 19f),
            PointF(8f, 12f),
            PointF(19f, 12f),
        ).transformFrom(24f),
    )

val topPlay =
    RoundedPolygon(
        vertices = listOf(
            PointF(8f, 12f),
            PointF(8f, 5f),
            PointF(19f, 12f),
        ).transformFrom(24f),
    )

private fun List<PointF>.offset(offset: PointF): List<PointF> = this.map { it.plus(offset) }

private fun List<PointF>.transformFrom(fl: Float): List<PointF> = this.map { it.div(fl) }

@Composable
@Preview
fun PlayPausePreview() {
    Box(
        modifier = Modifier
            .size(200.dp)
    ) {
        val state = animateFloatAsState(targetValue = 1f, label = "progress")
        MorphComposable(
            SizedMorph(
                Morph(
                    start = leftPause,
                    end = bottomPlay
                )
            ),
            progress = { state.value },
        )
        MorphComposable(
            SizedMorph(
                Morph(
                    start = rightPause,
                    end = topPlay
                )
            ),
            progress = { state.value },
        )
    }
}