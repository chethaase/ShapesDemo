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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.drawMorph
import androidx.graphics.shapes.drawPolygon
import kotlin.math.min

/**
 * This custom view takes either a Shape or a Morph. Most of the code is shared between them,
 * with slight adjustments for transforming or rendering the object.
 */
class ShapeView(context: Context, val shape: RoundedPolygon? = null,
                morph: Morph? = null) : View(context) {

    val paint = Paint()
    var morph: Morph? = morph
    set(value) {
        field = value
        updateTransform()
    }

    init {
        paint.setColor(Color.WHITE)
    }

    private fun calculateScale(bounds: RectF): Float {
        val scaleX = width / (bounds.right - bounds.left)
        val scaleY = height / (bounds.bottom - bounds.top)
        val scaleFactor = min(scaleX, scaleY)
        return scaleFactor
    }
    private fun calculateMatrix(bounds: RectF): Matrix {
        val scale = calculateScale(bounds)
        val scaledLeft = scale * bounds.left
        val scaledTop = scale * bounds.top
        val scaledWidth = scale * bounds.right - scaledLeft
        val scaledHeight = scale * bounds.bottom - scaledTop
        val newLeft = scaledLeft - (width - scaledWidth) / 2
        val newTop = scaledTop - (height - scaledHeight) / 2
        val matrix = Matrix()
        matrix.preTranslate(-newLeft, -newTop)
        matrix.preScale(scale, scale)
        return matrix
    }

    private fun updateTransform() {
        if (shape != null) {
            val matrix = calculateMatrix(shape.bounds)
            shape.transform(matrix)
        } else if (morph != null) {
            val matrix = calculateMatrix(morph!!.bounds)
            morph!!.transform(matrix)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        updateTransform()
    }

    override fun onDraw(canvas: Canvas) {
        if (shape != null) canvas.drawPolygon(shape, paint)
        else if (morph != null) canvas.drawMorph(morph!!, paint)
    }
}