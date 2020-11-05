package com.e.amaze

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SettingItem(
    var name: String? = "",
    var value: String? = ""
)

@IgnoreExtraProperties
data class SettingDetails(
    var settingName:String? = "",
    var displayName: String? = "",
    var inputOptions: String? = "",
    var isSwitch: String? = "false"
)