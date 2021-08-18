package octii.app.taxiapp.scripts

import android.animation.ValueAnimator
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator


const val animationDuration: Long = 1300

fun View.fadeTo(visible: Boolean, duration: Long = 500, startDelay: Long = 0, toAlpha: Float = 1f) {
    // Make this idempotent.
    val tagKey = "fadeTo".hashCode()
    if (visible == isVisible && animation == null && getTag(tagKey) == null) return
    if (getTag(tagKey) == visible) return

    setTag(tagKey, visible)
    setTag("fadeToAlpha".hashCode(), toAlpha)

    if (visible && alpha == 1f) alpha = 0f
    animate()
        .alpha(if (visible) toAlpha else 0f)
        .withStartAction {
            if (visible) isVisible = true
        }
        .withEndAction {
            setTag(tagKey, null)
            if (isAttachedToWindow && !visible) isVisible = false
        }
        .setInterpolator(FastOutSlowInInterpolator())
        .setDuration(duration)
        .setStartDelay(startDelay)
        .start()
}

fun View.down(activity: Activity, fullDown: Boolean = true, parent: View? = null) {
    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display
    } else {
        activity.windowManager.defaultDisplay
    }
    if (display != null) {
        val animator =
            when {
                fullDown -> ValueAnimator.ofFloat(0f, display.height.toFloat())
                parent != null -> ValueAnimator.ofFloat(0f, parent.height.toFloat())
                else -> ValueAnimator.ofFloat(0f, display.height.toFloat())
            }

        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            translationY = value
        }

        animator.interpolator = AccelerateInterpolator(1.5f)
        animator.duration = animationDuration
        animator.start()
    }
}

fun View.up(activity: Activity, parent: View? = null) {
    visibility = View.VISIBLE
    isVisible = true
    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display
    } else {
        activity.windowManager.defaultDisplay
    }
    if (display != null) {
        val animator =
            if (parent == null) ValueAnimator.ofFloat(display.height - (height.toFloat()), 0f)
            else ValueAnimator.ofFloat(parent.height.toFloat(), 0f)
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            translationY = value
        }

        animator.interpolator = AccelerateInterpolator(1.5f)
        animator.duration = animationDuration
        animator.start()
    }
}
