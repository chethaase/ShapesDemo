package dev.chet.graphics.shapes.shapesdemo.compose

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star

/**
 * This demo displays several shapes at the top of the window, which acts as buttons.
 * It also displays a single, larger Shape below the top two rows. When one of the shapes at
 * the top is clicked, two morph animations are triggered. First, the shape itself is
 * morphed between its default view (the static shape seen when it is not clicked) and
 * a rounded square object. This animation runs quickly and then reverses back to the start,
 * showing the default shape again.
 * Second, the larger shape at the bottom of the window morphs from whatever it was before to the
 * new shape just clicked on.
 */

// Current shape being morphed from. Changing this value causes the creation of a new
// Morph object from current to next.
val currentShapeIndex = mutableStateOf(0)
// Next shape being morphed to. Changing this value causes the creation of a new
// Morph object from current to next.
val nextShapeIndex = mutableStateOf(0)
// Progress of morph animation. Changing this value causes redrawing of the large morph
// object, which uses this value to set its internal progress property.
val morphProgress = mutableStateOf(0f)

// Shapes displayed at the top of the window. These shapes are used to create Morph
// objects between each shape and the RoundedSquare shape.
val shapes = listOf<RoundedPolygon>(
    // Row 1: Unrounded
    RoundedPolygon(4),
    RoundedPolygon(5),
    RoundedPolygon.star(6),
    RoundedPolygon.star(7),

    // Row 2: Rounded
    RoundedPolygon(4, rounding = CornerRounding(.2f)),
    RoundedPolygon(5, rounding = CornerRounding(.3f, .8f)),
    RoundedPolygon.star(6, rounding = CornerRounding(.2f)),
    RoundedPolygon.star(7, rounding = CornerRounding(.3f, .8f),
        innerRounding = CornerRounding.Unrounded),
)

// Used as the underlying shape that each of the shaped buttons animates to/from when clicked
val RoundedSquare = RoundedPolygon(4, rounding = CornerRounding(.3f, .5f))

class OutlineActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(parent = null) {
            MaterialTheme {
                Content()
            }
        }
    }
}

/**
 * Creates a Compose-friendly Shape object, which can return an Outline for composables
 * to be clipped by. It takes a Morph object, which is used to create the underlying
 * path for this shape.
 */
class MorphShape(val morph: Morph) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val matrix = calculateMatrix(morph.bounds, size.width, size.height)
        morph.transform(matrix)
        return Outline.Generic(morph.asPath().asComposePath())
    }
}

/**
 * This Composable will be clipped by a morph created by the shape at the given
 * index and the RoundedSquare.
 */
@Composable
fun MorphAsClip(shapeIndex: Int) {
    println("*** Creating morph for shape $shapeIndex")
    val buttonMorph = Morph(shapes[shapeIndex], RoundedSquare)
    MorphAsClipImpl(morph = buttonMorph, shapeIndex = shapeIndex)
}

/**
 * This Composable holds the actual logic for the MorphAsClip Composable. Breaking them into
 * two allows this object to be recomposed when there is a change in the progress value
 * being animated for the shape. We do not want to recreate the actual Morph on every frame;
 * that should only happen once. Updating the progress for that Morph on every frame of the
 * animation is cheap, however, so this Composable exists to do that work.
 */
@Composable
fun MorphAsClipImpl(morph: Morph, shapeIndex: Int) {
    println("*** Recomposing morphClipShape for $shapeIndex")
    var thisMorphProgress by remember {
        mutableStateOf(0f)
    }
    morph.progress = thisMorphProgress
    Box(modifier = Modifier
        .padding(8.dp)
        .clip(MorphShape(morph))
        .background(Color(0xFF80DEEA))
        .size(80.dp)
        .clickable {
            // A click causes two animations: first, a morph of the shape itself to
            // the RoundedSquare polygon and back.
            currentShapeIndex.value = nextShapeIndex.value
            nextShapeIndex.value = shapeIndex
            val buttonProgressAnimator = ValueAnimator.ofFloat(0f, 1f)
            buttonProgressAnimator.setDuration(100)
            buttonProgressAnimator.addUpdateListener {
                thisMorphProgress = it.animatedValue as Float
            }

            // Second, the large Morph object
            // at the bottom of the window will animate from the current shape to the one
            // just clicked.
            buttonProgressAnimator.repeatCount = 1
            buttonProgressAnimator.repeatMode = ValueAnimator.REVERSE
            buttonProgressAnimator.start()
            val progressAnimator = ValueAnimator.ofFloat(0f, 1f)
            progressAnimator.addUpdateListener { morphProgress.value = it.animatedValue as Float }
            progressAnimator.start()
        }
    )
}

/**
 * Two rows of shapes, which are actually Morph objects using the polygon at the given
 * index and the RoundedSquare object that will be morphed into and out of when the
 * polygon is clicked.
 */
@Composable
fun Shapes() {
    Column() {
        var shapeIndex = 0
        Row() {
            while (shapeIndex < 4) MorphAsClip(shapeIndex++)
        }
        Row() {
            while (shapeIndex < 8) MorphAsClip(shapeIndex++)
        }
    }
}

/**
 * This Composable displays a large polygon. The Morph object is animated via its progress
 * value, which is updated when one of the shapes at the top is clicked. Note that this
 * function will be called whenever the morphProgress is updated, but the actual Morph
 * object is only created once for each shape change.
 */
@Composable
private fun MorphComposable(
    sizedMorph: SizedMorph,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                sizedMorph.resizeMaybe(size.width, size.height)
                println("*** setting morph progress to ${morphProgress.value} and redrawing")
                sizedMorph.morph.progress = morphProgress.value
                drawPath(
                    sizedMorph.morph
                        .asPath()
                        .asComposePath(), Color.Red
                )
            })
}

/**
 * This function is only recomposed when the current/next shapes change.
 */
@Composable
fun MorphView() {
    println("*** creating new morph from ${currentShapeIndex.value} to ${nextShapeIndex.value}")
    MorphComposable(SizedMorph(Morph(shapes[currentShapeIndex.value], shapes[nextShapeIndex.value])))
}

@Composable
fun Content() {
    Column(
        Modifier
            .background(Color.Black)
            .padding(0.dp)
            .fillMaxSize()
    ) {
        Shapes()
        MorphView()
    }
}
