package dev.chet.graphics.shapes.shapesdemo.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

class MorphPolygonShape(
    private val shapeA: RoundedPolygon,
    private val shapeB: RoundedPolygon,
    private val percentage: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val matrixA = calculateMatrix(shapeA.bounds, size.width, size.height)
        shapeA.transform(matrixA)
        val matrixB = calculateMatrix(shapeB.bounds, size.width, size.height)
        shapeB.transform(matrixB)
        val morph = Morph(shapeA, shapeB)
        morph.progress = percentage
        return Outline.Generic(morph.asPath().asComposePath())
    }
}
@Preview
@Composable
fun ShapeAsClip() {
    val shapeA = RoundedPolygon(5, rounding = CornerRounding(0.2f))
    val shapeB = RoundedPolygon(3, rounding = CornerRounding(0.3f))
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedProgress = animateFloatAsState(targetValue = if (isPressed) 1f else 0f,
        label = "progress", animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
    )
    Box(modifier = Modifier
        .padding(8.dp)
        .clip(MorphPolygonShape(shapeA, shapeB, animatedProgress.value))
        .background(Color(0xFF80DEEA))
        .size(200.dp)
        .clickable(interactionSource = interactionSource, indication = null) {
        }
    ) {
    }
}