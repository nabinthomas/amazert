package com.e.amaze

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SettingItem(
    var name: String? = "",
    var value: String? = ""
)
