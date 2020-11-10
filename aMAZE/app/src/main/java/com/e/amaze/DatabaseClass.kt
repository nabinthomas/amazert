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


@IgnoreExtraProperties
data class StatusItem(
    var freq: String? = "",
    var clients: Any
)
@IgnoreExtraProperties
data class StatusClientList(
    var MAC:String? = "",
    var ClientDetails:StatusClientInfo
)

@IgnoreExtraProperties
data class StatusClientInfo(
    var auth:String? = "",
    var assoc: String? = "",
    var authorized: String? = "",
    var preauth: String? = "",
    var wds: String? = "",
    var wmm: String? = "",
    var ht: String? = "",
    var vht: String? = "",
    var wps: String? = "",
    var mfp: String? = "",
    var rrm: ArrayList<Int> ,
    var aid: String? = ""
)
