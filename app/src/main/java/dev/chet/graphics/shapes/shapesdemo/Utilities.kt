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

package dev.chet.graphics.shapes.shapesdemo

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.plus
import androidx.core.graphics.times
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun Float.toRadians() = this * PI.toFloat() / 180f

internal fun Offset.rotate90() = Offset(-y, x)

internal fun directionVector(angleRadians: Float) = Offset(cos(angleRadians), sin(angleRadians))

internal fun Offset.rotate(angleRadians: Float): Offset {
    val vec = directionVector(angleRadians)
    return vec * x + vec.rotate90() * y
}

internal val PointZero = PointF(0f, 0f)

internal fun radialToCartesian(
    radius: Float,
    angleRadians: Float,
    center: PointF = PointZero
) = directionVectorPointF(angleRadians) * radius + center


// Create a new list every time, because mutability is complicated.
internal fun SquarePoints() = listOf(
    PointF(1f, 1f),
    PointF(-1f, 1f),
    PointF(-1f, -1f),
    PointF(1f, -1f)
)

internal fun directionVectorPointF(angleRadians: Float) =
    PointF(cos(angleRadians), sin(angleRadians))
