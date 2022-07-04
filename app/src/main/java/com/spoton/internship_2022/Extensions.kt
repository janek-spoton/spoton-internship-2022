package com.spoton.internship_2022

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


fun <T : View> RecyclerView.ViewHolder.viewId(id: () -> Int) = object : Lazy<T> {
    override val value: T
        get() = itemView.findViewById(id())

    override fun isInitialized(): Boolean = itemView.findViewById<T>(id()) != null
}

fun BottomNavigationView.selectBottomMenuItem(id: Int) {
    menu.forEach { it.isChecked = false }
    menu.findItem(id).isChecked = true
}

fun View.shouldRequestFocus(shouldFocus: Boolean) {
    if (shouldFocus) {
        this.requestFocus()
    }
}

fun ViewGroup.inflateFrom(layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)

fun createSlideInOutTransition(
    baseViewGroup: ViewGroup,
    viewOut: View,
    viewIn: View,
    slideIn: Boolean,
) {
    val set = TransitionSet()
    set.addTransition(viewOut.createSlideOutTransition())
    set.addTransition(viewIn.createSlideInTransition())
    TransitionManager.beginDelayedTransition(baseViewGroup, set)
    viewIn.isVisible = slideIn
    viewOut.isVisible = !slideIn
}

private fun View.createSlideInTransition(duration: Long = 200): Transition {
    val transition: Transition = Slide(Gravity.END)
    transition.duration = duration
    transition.addTarget(this)
    return transition
}

private fun View.createSlideOutTransition(duration: Long = 200): Transition {
    val transition: Transition = Slide(Gravity.START)
    transition.duration = duration
    transition.addTarget(this)
    return transition
}

fun View.fade(shouldShow: Boolean) {
    if (animation?.isInitialized == true) {
        animation?.cancel()
    }
    val targetAlpha = if (shouldShow) 1f else 0f
    animate()
        .alpha(targetAlpha)
        .setDuration(200L)
        .setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    isVisible = shouldShow
                }

                override fun onAnimationCancel(animation: Animator?) {
                    isVisible = shouldShow
                }
            }
        )
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.hideKeyboard() {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun EditText.forceShowKeyboard(context: Context) {
    requestFocus()
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

fun View.setAntiSpamOnClickListener(
    spamThreshHold: Long = 500L,
    onClick: (View) -> Unit,
) {
    setOnClickListener(AntiSpamOnClickListener(spamThreshHold, onClick))
}

class AntiSpamOnClickListener(
    private val spamThreshHold: Long,
    private val onClicked: (view: View) -> Unit,
) : View.OnClickListener {
    private var lastClickTimestamp: Long? = null

    override fun onClick(v: View) {
        val now = System.currentTimeMillis()
        val diff = lastClickTimestamp?.let { now - it } ?: 0
        if (lastClickTimestamp == null || diff > spamThreshHold) {
            onClicked(v)
        }
        lastClickTimestamp = now
    }
}

fun EditText.setOnKeyEventListener(
    keyCode: Int,
    eventAction: Int = KeyEvent.ACTION_DOWN,
    listener: () -> Unit,
) {
    setOnEditorActionListener { v, actionId, event ->
        if (event.keyCode == keyCode && event.action == eventAction) {
            listener.invoke()
            true
        } else {
            false
        }
    }
}

fun TextView.setCompatTextAppearance(@StyleRes appearnce: Int) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        setTextAppearance(appearnce)
    } else {
        setTextAppearance(this.context, appearnce)
    }
}

fun TextView.setCompatTooltipText(text: CharSequence?) {
    TooltipCompat.setTooltipText(this, text)
}

fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun TextView.bold() {
    setTypeface(typeface, Typeface.BOLD)
}

val TextView.isTextEllipsized: Boolean
    get() = layout?.run {
        (0 until lineCount).any { getEllipsisCount(it) > 0 }
    } ?: false

fun EditText.setCursorAtEnd() {
    setSelection(this.text.length)
}

fun NestedScrollView.scrollToBottom() {
    post { fullScroll(View.FOCUS_UP) }
}

fun NestedScrollView.scrollVerticallyToView(view: View) {
    post { smoothScrollTo(0, view.bottom - this.height) }
}

fun ChipGroup.clearChips() {
    removeAllViews()
}

fun ChipGroup.addChip(
    context: Context,
    chipText: String,
    onChipClicked: (String) -> Unit,
) {
    Chip(context).apply {
        id = View.generateViewId()
        text = chipText
        ellipsize = TextUtils.TruncateAt.END
        maxLines = 1
        setOnClickListener { onChipClicked(chipText) }
        addView(this)
    }
}

fun janek(): Int = 5