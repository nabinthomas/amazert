package com.example.fireamaze


class UserData {
    var UserName: String? = null
    var UserEmail: String? = null
    var WifiStatus: String? = null
    var PowerStatus: String? = null

    constructor() {}

    constructor(username: String?, email: String?, phone: String?, wifi: String?, power: String?) {
        this.UserName = username
        this.UserEmail = email
        this.WifiStatus = wifi
        this.PowerStatus = power
    }
}