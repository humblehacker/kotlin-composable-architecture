package composablearchitecture.android

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// TODO: Add TextState styling options
@Parcelize
data class TextState(
    val text: String
) : Parcelable
