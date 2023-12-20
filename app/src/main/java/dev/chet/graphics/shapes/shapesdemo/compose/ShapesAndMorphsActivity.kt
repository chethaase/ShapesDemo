/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.chet.graphics.shapes.shapesdemo.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.graphics.shapes.Cubic
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.TransformResult
import dev.chet.graphics.shapes.shapesdemo.ShapeParameters
import dev.chet.graphics.shapes.shapesdemo.debugLog
import dev.chet.graphics.shapes.shapesdemo.scaled
import dev.chet.graphics.shapes.shapesdemo.toPath
import dev.chet.graphics.shapes.shapesdemo.toComposePath
import kotlin.math.min
import kotlinx.coroutines.launch

@Composable
fun PolygonComposable(polygon: RoundedPolygon, modifier: Modifier = Modifier) =
    PolygonComposableImpl(polygon, modifier)

@Composable
private fun MorphComposable(
    sizedMorph: Morph,
    progress: Float,
    modifier: Modifier = Modifier,
    isDebug: Boolean = false
) = MorphComposableImpl(sizedMorph, modifier, isDebug, progress)


@Composable
private fun MorphComposableImpl(
    sizedMorph: Morph,
    modifier: Modifier = Modifier,
    isDebug: Boolean = false,
    progress: Float
) {
    Box(
        modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                val scale = min(size.width, size.height)
                val path = sizedMorph.toComposePath(progress, scale = scale)
                if (isDebug) {
                    drawPath(path, Color.Green, style = Stroke(2f))
                    sizedMorph.forEachCubic(progress) { cubic ->
                        cubic.transform { x, y -> TransformResult(x * scale, y * scale) }
                        debugDraw(cubic)
                    }
                } else {
                    drawPath(path, Color.White)
                }
            })
}

@Composable
internal fun PolygonComposableImpl(
    polygon: RoundedPolygon,
    modifier: Modifier = Modifier,
    debug: Boolean = false
) {
    val sizedShapes = remember(polygon) { mutableMapOf<Size, List<Cubic>>() }
    Box(
        modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                val scale = min(size.width, size.height)
                val shape = sizedShapes.getOrPut(size) { polygon.cubics.scaled(scale) }
                if (debug) {
                    shape.forEach { cubic -> debugDraw(cubic) }
                } else {
                    drawPath(shape.toPath(), Color.White)
                }
            })
}

@Composable
fun MainScreen(activity: ShapesAndMorphsActivity) {
    var editing by remember { mutableStateOf<ShapeParameters?>(null) }
    val selectedShape = remember { mutableStateOf(0) }
    val shapes = remember {
        listOf(
            // LINE 1
            // Circle
            ShapeParameters(
                sides = 4,
                roundness = 1f,
                shapeId = ShapeParameters.ShapeId.Polygon
            ),
            // Scallop
            ShapeParameters(
                sides = 12,
                innerRadius = .928f,
                roundness = .1f,
                shapeId = ShapeParameters.ShapeId.Star
            ),
            // Clover
            ShapeParameters(
                sides = 4,
                innerRadius = .352f,
                roundness = .32f,
                rotation = 45f,
                shapeId = ShapeParameters.ShapeId.Star
            ),
            ShapeParameters(
                innerRadius = 0.1f,
                roundness = 0.22f,
                shapeId = ShapeParameters.ShapeId.Triangle
            ),
            ShapeParameters(
                sides = 8,
                innerRadius = .784f,
                roundness = .16f,
                shapeId = ShapeParameters.ShapeId.Star
            ),

            // LINE 2
            ShapeParameters(
                sides = 15,
                innerRadius = .892f,
                roundness = 1f,
                shapeId = ShapeParameters.ShapeId.Star
            ),
            ShapeParameters(
                innerRadius = .19f,
                roundness = 0.86f,
                rotation = -45f,
                shapeId = ShapeParameters.ShapeId.Blob
            ),
            ShapeParameters(
                innerRadius = .19f,
                roundness = 0.86f,
                rotation = 45f,
                shapeId = ShapeParameters.ShapeId.Blob
            ),
            ShapeParameters(
                sides = 12,
                innerRadius = .928f,
                roundness = .928f,
                shapeId = ShapeParameters.ShapeId.Star
            ),
            ShapeParameters(
                sides = 3,
                roundness = .2f,
                rotation = 30f,
                shapeId = ShapeParameters.ShapeId.Polygon
            ),

            // LINE 3
            // CornerSE
            ShapeParameters(roundness = .4f, shapeId = ShapeParameters.ShapeId.CornerSE),

            // Square
            ShapeParameters(
                sides = 4,
                rotation = 45f,
                shapeId = ShapeParameters.ShapeId.Polygon
            ),

            // Pentagon
            ShapeParameters(
                sides = 5,
                rotation = -360f / 20f,
                shapeId = ShapeParameters.ShapeId.Polygon
            ),

            // 5-Sided Star
            ShapeParameters(
                sides = 5,
                rotation = -360f / 20,
                innerRadius = .3f,
                shapeId = ShapeParameters.ShapeId.Star
            ),

            // 8-Sided Star
            ShapeParameters(
                sides = 8,
                innerRadius = .6f,
                shapeId = ShapeParameters.ShapeId.Star
            )
        )
    }
    editing?.let {
        ShapeEditor(it, output = { outputString -> outputShapeInfo(activity, outputString) }) {
            editing = null
        }
    } ?: MorphScreen(shapes, selectedShape) { editing = shapes[selectedShape.value] }
}

fun outputShapeInfo(activity: FragmentActivity, info: String) {
    // Output to logcat and also share intent
    println(info)
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, info)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    activity.startActivity(shareIntent)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MorphScreen(
    shapeParams: List<ShapeParameters>,
    selectedShape: MutableState<Int>,
    onEditClicked: () -> Unit
) {
    val shapes = remember {
        shapeParams.map { sp ->
            sp.genShape().let { poly -> poly.normalized() }
        }
    }

    var currShape by remember { mutableStateOf(selectedShape.value) }
    val progress = remember { Animatable(0f) }

    var debug by remember { mutableStateOf(false) }

    val morphed by remember {
        derivedStateOf {
            // NOTE: We need to access this variable to ensure we recalculate the morph !
            debugLog("Re-computing morph / $debug")
            Morph(
                shapes[currShape],
                shapes[selectedShape.value]
            )
        }
    }

    val scope = rememberCoroutineScope()
    val clickFn: (Int) -> Unit = remember {
            { shapeIx ->
            scope.launch {
                currShape = selectedShape.value
                selectedShape.value = shapeIx
                doAnimation(progress)
            }
        }
    }
    Column(
        Modifier
            .background(Color.Black)
            .padding(16.dp)
            .fillMaxSize()

    ) {
        FlowRow(Modifier.fillMaxWidth(), maxItemsInEachRow = 5) {
            shapes.forEachIndexed { index, shape ->
                val borderAlpha = (
                        (if (index == selectedShape.value) progress.value else 0f) +
                                (if (index == currShape) 1 - progress.value else 0f)
                        ).coerceIn(0f, 1f)
                Box(
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(horizontal = 5.dp)
                        .border(
                            3.dp,
                            Color.Red.copy(alpha = borderAlpha)
                        )
                ) {
                    // draw shape
                    PolygonComposable(shape, Modifier.clickable { clickFn(index) })
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Button(onClick = { debug = !debug }) {
                Text(if (debug) "Debug" else "Shape")
            }
            Button(onClick = onEditClicked) {
                Text("Edit")
            }
        }
        Slider(value = progress.value.coerceIn(0f, 1f), onValueChange = {
            scope.launch { progress.snapTo(it) }
        })
        MorphComposable(morphed, progress.value,
            Modifier
                .fillMaxSize()
                .clickable(
                    indication = null, // Eliminate the ripple effect.
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    scope.launch { doAnimation(progress) }
                }, debug)
    }
}

private suspend fun doAnimation(progress: Animatable<Float, AnimationVector1D>) {
    progress.snapTo(0f)
    progress.animateTo(
        1f,
        animationSpec =
             spring(0.6f, 50f)
    )
}

class ShapesAndMorphsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(parent = null) {
            MaterialTheme {
                MainScreen(this)
            }
        }
    }
}