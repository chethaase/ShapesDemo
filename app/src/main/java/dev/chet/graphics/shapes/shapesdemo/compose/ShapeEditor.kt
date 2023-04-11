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

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chet.graphics.shapes.shapesdemo.ShapeParameters
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ShapeEditor(params: ShapeParameters, onClose: () -> Unit) {
    val shapeParams = params.selectedShape().value
    var debug by remember { mutableStateOf(false) }
    var autoSize by remember { mutableStateOf(true) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Base Shape:", color = Color.White)
            Spacer(Modifier.width(10.dp))
            Button(onClick = { params.shapeIx = (params.shapeIx + 1) % params.shapes.size }) {
                Text(params.selectedShape().value.name)
            }
        }
        MySlider("Sides", 3f, 20f, 1f, params.sides, shapeParams.usesSides)
        MySlider(
            "InnerRadiusRatio",
            0.1f,
            0.999f,
            0f,
            params.innerRadiusRatio,
            shapeParams.usesInnerRatio
        )
        MySlider("RoundRadius", 0f, 1f, 0f, params.roundness, shapeParams.usesRoundness)
        MySlider("Smoothing", 0f, 1f, 0f, params.smooth)
        MySlider(
            "InnerRoundRadius",
            0f,
            1f,
            0f,
            params.innerRoundness,
            shapeParams.usesInnerParameters
        )
        MySlider("InnerSmoothing", 0f, 1f, 0f, params.innerSmooth, shapeParams.usesInnerParameters)
        MySlider("Rotation", 0f, 360f, 45f, params.rotation)

        PanZoomRotateBox(
            Modifier
                .clipToBounds()
                .weight(1f)
                .border(1.dp, Color.White)
                .padding(2.dp)
        ) {
            PolygonComposableImpl(params.genShape(autoSize = autoSize).also { poly ->
                if (autoSize) {
                    val matrix = calculateMatrix(poly.bounds, 1f, 1f)
                    poly.transform(matrix)
                }
            }, debug = debug)
        }
        Row {
            MyTextButton(
                onClick = onClose,
                text = "Accept"
            )
            // TODO: add cancel!?
            Spacer(Modifier.weight(1f))
            MyTextButton(
                onClick = { debug = !debug },
                text = if (debug) "Beziers" else "Shape"
            )
            Spacer(Modifier.weight(1f))
            MyTextButton(
                onClick = { autoSize = !autoSize },
                text = if (autoSize) "AutoSize" else "NoSizing"
            )
            Spacer(Modifier.weight(1f))
            MyTextButton(
                onClick = { params.selectedShape().value.debugDump() },
                text = "Dump to Logcat"
            )
        }
    }
}

@Composable
fun MyTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    // Material defaults are 16 & 8
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
) = Button(onClick = onClick, modifier = modifier, contentPadding = contentPadding) {
    Text(text)
}

@Composable
fun MySlider(
    name: String,
    minValue: Float,
    maxValue: Float,
    step: Float,
    valueHolder: MutableState<Float>,
    enabled: Boolean = true
) {
    Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(name, color = Color.White)
        Spacer(Modifier.width(10.dp))
        Slider(
            value = valueHolder.value,
            onValueChange = { valueHolder.value = it },
            valueRange = minValue..maxValue,
            steps = if (step > maxValue - minValue)
                ((maxValue - minValue) / step).roundToInt() - 1
            else
                0,
            enabled = enabled
        )
    }
}

