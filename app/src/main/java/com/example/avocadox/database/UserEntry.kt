package com.example.avocadox.database

class UserEntry {
    var uid: String? = null
    var name: String? = null
    var email: String? = null
    var gender: String? = null

    constructor() {}

    constructor(uid: String?, name: String?, email: String?, gender: String?) {
        this.uid = uid
        this.name = name
        this.email = email
        this.gender = gender
    }
}