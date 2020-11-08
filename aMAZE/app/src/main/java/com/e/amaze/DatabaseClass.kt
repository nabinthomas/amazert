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
    var clients: List<Any>
)

//name=wifi.clients,
//value={'freq': 5560, 'clients': {'d4:c9:4b:4f:4c:72': {'auth': True, 'assoc': True, 'authorized': True, 'preauth': False, 'wds': False, 'wmm': False, 'ht': False, 'vht': False, 'wps': False, 'mfp': False, 'rrm': [115, 16, 145, 0, 4], 'aid': 0}}}}
//{'freq': 5560, 'clients': {'d4:c9:4b:4f:4c:72': {'auth': True, 'assoc': True, 'authorized': True, 'preauth': False, 'wds': False, 'wmm': False, 'ht': False, 'vht': False, 'wps': False, 'mfp': False, 'rrm': [115, 16, 145, 0, 4], 'aid': 0}}}
