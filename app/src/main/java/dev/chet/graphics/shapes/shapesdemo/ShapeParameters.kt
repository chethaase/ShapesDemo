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

import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.plus
import androidx.core.graphics.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.Star
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

private val LOG_TAG = "ShapesParameters"
private val DEBUG = false

internal fun debugLog(message: String) {
    if (DEBUG) Log.d(LOG_TAG, message)
}

data class ShapeItem(
    val name: String,
    val shapegen: () -> RoundedPolygon,
    val debugDump: () -> Unit,
    val usesSides: Boolean = true,
    val usesInnerRatio: Boolean = true,
    val usesRoundness: Boolean = true,
    val usesInnerParameters: Boolean = true
)

class ShapeParameters(
    sides: Int = 5,
    innerRadiusRatio: Float = 0.5f,
    roundness: Float = 0f,
    smooth: Float = 0f,
    innerRoundness: Float = roundness,
    innerSmooth: Float = smooth,
    rotation: Float = 0f,
    shapeId: ShapeId = ShapeId.Polygon
) {
    internal val sides = mutableStateOf(sides.toFloat())
    internal val innerRadiusRatio = mutableStateOf(innerRadiusRatio)
    internal val roundness = mutableStateOf(roundness)
    internal val smooth = mutableStateOf(smooth)
    internal val innerRoundness = mutableStateOf(innerRoundness)
    internal val innerSmooth = mutableStateOf(innerSmooth)
    internal val rotation = mutableStateOf(rotation)

    internal var shapeIx by mutableStateOf(shapeId.ordinal)

    fun copy() = ShapeParameters(
        this.sides.value.roundToInt(),
        this.innerRadiusRatio.value,
        this.roundness.value,
        this.smooth.value,
        this.innerRoundness.value,
        this.innerSmooth.value,
        this.rotation.value,
        ShapeId.values()[this.shapeIx]
    )

    enum class ShapeId {
        Star, Polygon, Triangle, Blob, CornerSE
    }

    private fun rotationAsString() =
        if (this.rotation.value != 0f)
            "rotation = ${this.rotation.value}f, "
        else
            ""

    // Primitive shapes we can draw (so far)
    internal val shapes = listOf(
        ShapeItem("Star", shapegen = {
            Star(
                numOuterVertices = this.sides.value.roundToInt(),
                innerRadiusRatio = this.innerRadiusRatio.value,
                rounding = CornerRounding(this.roundness.value, this.smooth.value),
                innerRounding = CornerRounding(
                    this.innerRoundness.value,
                    this.innerSmooth.value
                )
            )
        },
            debugDump = {
                debugLog(
                    "ShapeParameters(sides = ${this.sides.value.roundToInt()}, " +
                            "innerRadiusRatio = ${this.innerRadiusRatio.value}f, " +
                            "roundness = ${this.roundness.value}f, " +
                            "smooth = ${this.smooth.value}f, " +
                            "innerRoundness = ${this.innerRoundness.value}f, " +
                            "innerSmooth = ${this.innerSmooth.value}f, " +
                            rotationAsString() +
                            "shapeId = ShapeParameters.ShapeId.Star)"
                )
            }
        ),
        ShapeItem("Polygon", shapegen = {
            RoundedPolygon(
                numVertices = this.sides.value.roundToInt(),
                rounding = CornerRounding(this.roundness.value, this.smooth.value),
            )
        },
            debugDump = {
                debugLog(
                    "ShapeParameters(sides = ${this.sides.value.roundToInt()}, " +
                            "roundness = ${this.roundness.value}f, " +
                            "smooth = ${this.smooth.value}f, " +
                            rotationAsString() +
                            ")"
                )
            }, usesInnerRatio = false, usesInnerParameters = false
        ),
        ShapeItem(
            "Triangle", shapegen = {
                val points = listOf(
                    radialToCartesian(1f, 270f.toRadians()),
                    radialToCartesian(1f, 30f.toRadians()),
                    radialToCartesian(this.innerRadiusRatio.value, 90f.toRadians()),
                    radialToCartesian(1f, 150f.toRadians()),
                )
                RoundedPolygon(
                    points,
                    CornerRounding(this.roundness.value, this.smooth.value),
                    center = PointZero
                )
            },
            debugDump = {
                debugLog(
                    "ShapeParameters(innerRadiusRatio = ${this.innerRadiusRatio.value}f, " +
                            "smooth = ${this.smooth.value}f, " +
                            rotationAsString() +
                            "shapeId = ShapeParameters.ShapeId.Triangle)"
                )
            },
            usesSides = false, usesInnerParameters = false
        ),
        ShapeItem(
            "Blob", shapegen = {
                val sx = this.innerRadiusRatio.value.coerceAtLeast(0.1f)
                val sy = this.roundness.value.coerceAtLeast(0.1f)
                RoundedPolygon(
                    listOf(
                        PointF(-sx, -sy),
                        PointF(sx, -sy),
                        PointF(sx, sy),
                        PointF(-sx, sy),
                    ),
                    rounding = CornerRounding(this.roundness.value, this.smooth.value),
                    center = PointZero
                )
            },
            debugDump = {
                debugLog(
                    "ShapeParameters(roundness = ${this.roundness.value}f, " +
                            "smooth = ${this.smooth.value}f, " +
                            rotationAsString() +
                            "shapeId = ShapeParameters.ShapeId.Blob)"
                )
            },
            usesSides = false, usesInnerParameters = false
        ),
        ShapeItem(
            "CornerSE", shapegen = {
                RoundedPolygon(
                    SquarePoints(),
                    perVertexRounding = listOf(
                        CornerRounding(this.roundness.value, this.smooth.value),
                        CornerRounding(1f),
                        CornerRounding(1f),
                        CornerRounding(1f)
                    ),
                    center = PointZero
                )
            },
            debugDump = {
                debugLog(
                    "ShapeParameters(roundness = ${this.roundness.value}f, " +
                            "smooth = ${this.smooth.value}f, " +
                            rotationAsString() +
                            "shapeId = ShapeParameters.ShapeId.CornerSE)"
                )
            },
            usesSides = false,
            usesInnerRatio = false,
            usesInnerParameters = false
        )

        /*
        TODO: Add quarty. Needs to be able to specify a rounding radius of up to 2f
        ShapeItem("Quarty", { DefaultShapes.quarty(roundness.value, smooth.value) },
        usesSides = false, usesInnerRatio = false),
        */
    )

    fun selectedShape() = derivedStateOf { shapes[shapeIx] }

    fun genShape(autoSize: Boolean = true) = selectedShape().value.shapegen().apply {
        transform(Matrix().apply {
            if (autoSize) {
                // Move the center to the origin.
                center
                postTranslate(-(bounds.left + bounds.right) / 2, -(bounds.top + bounds.bottom) / 2)

                // Scale to the [-1, 1] range
                val scale = 2f / max(bounds.width(), bounds.height())
                postScale(scale, scale)
            }
            // Apply the needed rotation
            postRotate(rotation.value)
        })
    }
}
