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

package dev.chet.graphics.shapes.shapesdemo.view

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.Star
import dev.chet.graphics.shapes.shapesdemo.SquarePoints
import dev.chet.graphics.shapes.shapesdemo.radialToCartesian
import dev.chet.graphics.shapes.shapesdemo.toRadians

class ShapesActivity : Activity() {

    val shapes = mutableListOf<RoundedPolygon>()
    var currentShapeIndex = 0
    lateinit var morphView: ShapeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = LinearLayout(this)
        container.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        container.orientation = LinearLayout.VERTICAL
        container.setBackgroundColor(Color.BLACK)
        setContentView(container)

        setupShapes()

        addShapeViews(container)

        addMorphView(container)
    }

    private fun getShapeView(shape: RoundedPolygon, width: Int, height: Int): View {
        val view = ShapeView(this, shape)
        val layoutParams = LinearLayout.LayoutParams(width, height)
        layoutParams.setMargins(7, 30, 7, 5)
        view.layoutParams = layoutParams
        // TODO: add click listener to show expanded version of shape at bottom of container
        return view
    }

    private fun setupShapes() {
        // Triangle vertices
        val triangleInnerRadiusRatio = .1f
        val trianglePoints = listOf(
            radialToCartesian(1f, 270f.toRadians()),
            radialToCartesian(1f, 30f.toRadians()),
            radialToCartesian(triangleInnerRadiusRatio, 90f.toRadians()),
            radialToCartesian(1f, 150f.toRadians()),
        )

        // CornerSE per-vertex rounding values
        val cornerSERounding = listOf(
            CornerRounding(.4f),
            CornerRounding(1f),
            CornerRounding(1f),
            CornerRounding(1f)
        )

        // We could use the same ShapeParameters list used by the Compose version of the app.
        // But that structure is set up specifically for the editor in that app, so there's
        // more structure and abstraction than we really need for creating shapes.
        // The shapes below are similar to those other shapes, but use the APIs directly to
        // show how to simply create one or more rounded shapes.
        shapes.addAll(listOf(
            // Line 1

            // Circle
            RoundedPolygon(4, rounding = CornerRounding(1f)),
            Star(12, innerRadius = .928f, rounding = CornerRounding(.1f)),

            // Clovers
            Star(4, innerRadius = .352f, rounding = CornerRounding(.32f)),
            Star(4, innerRadius = .152f, rounding = CornerRounding(.22f),
                    innerRounding = CornerRounding.Unrounded),

            // Irregular Triangle
            RoundedPolygon(trianglePoints, CornerRounding(.22f)),

            // Line 2

            // Rounded triangle
            RoundedPolygon(3, rounding = CornerRounding(.3f)),
            // Rounded+smoothed triangle
            RoundedPolygon(3, rounding = CornerRounding(.3f, 1f)),

            // CornerSE
            RoundedPolygon(SquarePoints(), perVertexRounding = cornerSERounding),

            // Unrounded Pentagon
            RoundedPolygon(5),

            // Unrounded 8-point star
            Star(8, innerRadius = .6f)
        ))
    }

    private fun addShapeViews(container: ViewGroup) {
        val width = 200
        val height = 200

        var shapeIndex = 0
        var row: LinearLayout? = null
        while (shapeIndex < shapes.size) {
            if (shapeIndex % 5 == 0) {
                row = LinearLayout(this)
                val layoutParams = LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                row.layoutParams = layoutParams
                row.orientation = LinearLayout.HORIZONTAL
                container.addView(row)
            }
            val shapeView = getShapeView(shapes[shapeIndex], width, height) as ShapeView
            row!!.addView(shapeView)
            setupMorphClick(shapeIndex = shapeIndex, shapeView)
            ++shapeIndex
        }
    }

    private fun setupMorphClick(shapeIndex: Int, view: ShapeView) {
        view.setOnClickListener {
            morphView.morph = Morph(shapes[currentShapeIndex], shapes[shapeIndex])
            val animator = ObjectAnimator.ofFloat(morphView.morph, "progress",
                0f, 1f)
            animator.addUpdateListener {
                morphView.invalidate()
            }
            animator.start()
            currentShapeIndex = shapeIndex
        }
    }

    private fun addMorphView(container: ViewGroup) {
        val morph = Morph(shapes[0], shapes[1])
        morphView = ShapeView(this, morph = morph)
        val layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(7, 30, 7, 5)
        morphView.layoutParams = layoutParams
        container.addView(morphView)
    }
}
