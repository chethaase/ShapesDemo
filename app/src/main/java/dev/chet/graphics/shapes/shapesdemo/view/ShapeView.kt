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
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import androidx.core.graphics.scaleMatrix
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import dev.chet.graphics.shapes.shapesdemo.toAndroidPath
import kotlin.math.min

/**
 * This custom view takes either a Shape or a Morph. Most of the code is shared between them,
 * with slight adjustments for transforming or rendering the object.
 */
class ShapeView(context: Context, val shape: RoundedPolygon? = null, morph: Morph? = null) : View(context) {

    val paint = Paint()
    val path = Path()
    var progress: Float = 0f

    var morph: Morph? = morph

    init {
        paint.setColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        val scale = min(width, height).toFloat()
        shape?.toPath(path)
        morph?.toAndroidPath(progress = progress, path = path)
        path.transform(scaleMatrix(scale, scale))
        canvas.drawPath(path, paint)
    }
}