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

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Matrix
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private val LOG_TAG = "ShapesParameters"

data class ShapeItem(
    val name: String,
    val shapegen: () -> RoundedPolygon,
    val shapeDetails: String = "",
    val usesSides: Boolean = true,
    val usesInnerRatio: Boolean = true,
    val usesRoundness: Boolean = true,
    val usesInnerParameters: Boolean = true
)

class ShapeParameters(
    sides: Int = 5,
    innerRadius: Float = 0.5f,
    roundness: Float = 0f,
    smooth: Float = 0f,
    innerRoundness: Float = roundness,
    innerSmooth: Float = smooth,
    rotation: Float = 0f,
    shapeId: ShapeId = ShapeId.Polygon
) {
    internal val sides = mutableFloatStateOf(sides.toFloat())
    internal val innerRadius = mutableFloatStateOf(innerRadius)
    internal val roundness = mutableFloatStateOf(roundness)
    internal val smooth = mutableFloatStateOf(smooth)
    internal val innerRoundness = mutableFloatStateOf(innerRoundness)
    internal val innerSmooth = mutableFloatStateOf(innerSmooth)
    internal val rotation = mutableFloatStateOf(rotation)

    internal var shapeIx by mutableIntStateOf(shapeId.ordinal)

    fun copy() = ShapeParameters(
        this.sides.value.roundToInt(),
        this.innerRadius.value,
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
            RoundedPolygon.star(
                numVerticesPerRadius = this.sides.floatValue.roundToInt(),
                innerRadius = this.innerRadius.floatValue,
                rounding = CornerRounding(this.roundness.floatValue, this.smooth.floatValue),
                innerRounding = CornerRounding(
                    this.innerRoundness.floatValue,
                    this.innerSmooth.floatValue
                )
            )
        },
            shapeDetails = shapeDescription(id = "Star", sides = this.sides.floatValue.roundToInt(),
                innerRadius = innerRadius, roundness = roundness,
                smooth = smooth, innerRoundness = innerRoundness,
                innerSmooth = innerSmooth, rotation = rotation,
                code = "RoundedPolygon.star(numVerticesPerRadius = $sides, " +
                        "innerRadius = $innerRadius, rounding = CornerRounding($roundness, $smooth), " +
                        "innerRounding = CornerRounding($innerRoundness, $innerSmooth))")
        ),
        ShapeItem("Polygon", shapegen = {
            RoundedPolygon(
                numVertices = this.sides.floatValue.roundToInt(),
                rounding = CornerRounding(this.roundness.floatValue, this.smooth.floatValue),
            )
        },
            shapeDetails = shapeDescription(id = "Polygon",
                sides = this.sides.floatValue.roundToInt(),
                roundness = roundness, smooth = smooth, rotation = rotation,
                code = "RoundedPolygon(numVertices = ${this.sides.floatValue.roundToInt()}," +
                        "rounding = CornerRounding($roundness, $smooth))"
            ),
            usesInnerRatio = false, usesInnerParameters = false
        ),
        ShapeItem(
            "Triangle", shapegen = {
                val points = floatArrayOf(
                    radialToCartesian(1f, 270f.toRadians()).x,
                    radialToCartesian(1f, 270f.toRadians()).y,
                    radialToCartesian(1f, 30f.toRadians()).x,
                    radialToCartesian(1f, 30f.toRadians()).y,
                    radialToCartesian(this.innerRadius.floatValue, 90f.toRadians()).x,
                    radialToCartesian(this.innerRadius.floatValue, 90f.toRadians()).y,
                    radialToCartesian(1f, 150f.toRadians()).x,
                    radialToCartesian(1f, 150f.toRadians()).y
                )
                RoundedPolygon(
                    points,
                    CornerRounding(this.roundness.floatValue, this.smooth.floatValue),
                    centerX = 0f,
                    centerY = 0f
                )
            },
            shapeDetails = shapeDescription(id = "Triangle", innerRadius = innerRadius,
                smooth = smooth, rotation = rotation,
                code = "val points = floatArrayOf(" +
                        "    radialToCartesian(1f, 270f.toRadians()).x,\n" +
                        "    radialToCartesian(1f, 270f.toRadians()).y,\n" +
                        "    radialToCartesian(1f, 30f.toRadians()).x,\n" +
                        "    radialToCartesian(1f, 30f.toRadians()).y,\n" +
                        "    radialToCartesian($innerRadius, 90f.toRadians()).x,\n" +
                        "    radialToCartesian($innerRadius, 90f.toRadians()).y,\n" +
                        "    radialToCartesian(1f, 150f.toRadians()).x,\n" +
                        "    radialToCartesian(1f, 150f.toRadians()).y)\n" +
                        "RoundedPolygon(points, CornerRounding($roundness, $smooth), " +
                        "centerX = 0f, centerY = 0f)"
            ),
            usesSides = false, usesInnerParameters = false
        ),
        ShapeItem(
            "Blob", shapegen = {
                val sx = this.innerRadius.floatValue.coerceAtLeast(0.1f)
                val sy = this.roundness.floatValue.coerceAtLeast(0.1f)
                RoundedPolygon(
                    vertices = floatArrayOf(-sx, -sy,
                        sx, -sy,
                        sx, sy,
                        -sx, sy,
                    ),
                    rounding = CornerRounding(min(sx, sy), this.smooth.floatValue),
                    centerX = 0f, centerY = 0f
                )
            },
            shapeDetails = shapeDescription(id = "Blob", roundness = roundness,
                smooth = smooth, rotation = rotation,
                code = "val sx = $innerRadius.coerceAtLeast(0.1f)\n" +
                        "val sy = $roundness.coerceAtLeast(.1f)\n" +
                        "val verts = floatArrayOf(-sx, -sy, sx, -sy, sx, sy, -sx, sy)\n" +
                        "RoundedPolygon(verts, rounding = CornerRounding(min(sx, sy), $smooth)," +
                        "centerX = 0f, centerY = 0f)"),
            usesSides = false, usesInnerParameters = false),
        ShapeItem(
            "CornerSE", shapegen = {
                RoundedPolygon(
                    squarePoints(),
                    perVertexRounding = listOf(
                        CornerRounding(this.roundness.floatValue, this.smooth.floatValue),
                        CornerRounding(1f),
                        CornerRounding(1f),
                        CornerRounding(1f)
                    ),
                    centerX = 0f,
                    centerY = 0f
                )
            },
            shapeDetails = shapeDescription(id = "cornerSE", roundness = roundness,
                smooth = smooth, rotation = rotation,
                code = "RoundedPolygon(floatArrayOf(1f, 1f, -1f, 1f, -1f, -1f, 1f, -1f), " +
                        "perVertexRounding = listOf(CornerRounding($roundness, $smooth), " +
                        "CornerRounding(1f), CornerRounding(1f),  CornerRounding(1f))," +
                        "centerX = 0f, centerY = 0f)"),
            usesSides = false,
            usesInnerRatio = false,
            usesInnerParameters = false
        ),
        ShapeItem(
            "Circle", shapegen = {
                RoundedPolygon.circle(this.sides.floatValue.roundToInt())
            },
            shapeDetails = shapeDescription(id = "Circle", roundness = roundness,
                smooth = smooth, rotation = rotation,
                code = "RoundedPolygon.circle($sides)"),
            usesSides = true,
            usesInnerRatio = false,
            usesInnerParameters = false
        ),
        ShapeItem(
            "Rectangle", shapegen = {
                RoundedPolygon.rectangle(width = 4f, height = 2f,
                    rounding = CornerRounding(this.roundness.floatValue, this.smooth.floatValue),
                )
            },
            shapeDetails = shapeDescription(id = "Rectangle", numVerts = 4, roundness = roundness,
                smooth = smooth, rotation = rotation,
                code = "RoundedPolygon.rectangle(width = 4f, height = 2f, " +
                        "rounding = CornerRounding($roundness, $smooth))"),
            usesSides = false,
            usesInnerRatio = false,
            usesInnerParameters = false
        )
    )

    fun shapeDescription(
        id: String? = null,
        numVerts: Int? = null,
        sides: Int? = null,
        innerRadius: Float? = null,
        roundness: Float? = null,
        innerRoundness: Float? = null,
        smooth: Float? = null,
        innerSmooth: Float? = null,
        rotation: Float? = null,
        code: String? = null
    ): String {
        var description = "ShapeParameters:\n"
        if (id != null) description += "shapeId = $id, "
        if (numVerts != null) description += "numVertices = $numVerts, "
        if (sides != null) description += "sides = $sides, "
        if (innerRadius != null) description += "innerRadius = $innerRadius, "
        if (roundness != null) description += "roundness = $roundness, "
        if (innerRoundness != null) description += "innerRoundness = $innerRoundness, "
        if (smooth != null) description += "smoothness = $smooth, "
        if (innerSmooth != null) description += "innerSmooth = $innerSmooth, "
        if (rotation != null) description += "rotation = $rotation, "
        if (numVerts != null) description += "numVerts = $numVerts, "
        if (code != null) {
            description += "\nCode:\n$code"
        }
        return description
    }

    fun selectedShape() = derivedStateOf { shapes[shapeIx] }

    fun genShape(autoSize: Boolean = true) = selectedShape().value.shapegen().let { poly ->
        poly.transformed(Matrix().apply {
            if (autoSize) {
                val bounds = poly.getBounds()
                // Move the center to the origin.
                translate(
                    x = -(bounds.left + bounds.right) / 2,
                    y = -(bounds.top + bounds.bottom) / 2
                )

                // Scale to the [-1, 1] range
                val scale = 2f / max(bounds.width, bounds.height)
                scale(x = scale, y = scale)
            }
            // Apply the needed rotation
            rotateZ(rotation.floatValue)
        })
    }
}

private fun squarePoints() = floatArrayOf(1f, 1f, -1f, 1f, -1f, -1f, 1f, -1f)
