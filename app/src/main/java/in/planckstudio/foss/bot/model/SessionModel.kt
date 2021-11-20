package `in`.planckstudio.foss.bot.model

class SessionModel {
    private var session = HashMap<String, String>()

    constructor()

    constructor(user: String, type: String, name: String, value: String) {
        this.session["user"] = user
        this.session["type"] = type
        this.session["name"] = name
        this.session["value"] = value
    }

    fun setSessionId(id: Int) {
        this.session["id"] = id.toString()
    }

    fun setSessionUser(user: String) {
        this.session["user"] = user
    }

    fun setSessionType(type: String) {
        this.session["type"] = type
    }

    fun setSessionName(name: String) {
        this.session["name"] = name
    }

    fun setSessionValue(value: String) {
        this.session["user"] = value
    }

    fun getSessionId(): Int {
        return this.session["id"]!!.toInt()
    }

    fun getSessionUser(): String {
        return this.session["user"].toString()
    }

    fun getSessionType(): String {
        return this.session["type"].toString()
    }

    fun getSessionName(): String {
        return this.session["name"].toString()
    }

    fun getSessionValue(): String {
        return this.session["value"].toString()
    }

    fun getSession(): HashMap<String, String> {
        return this.session
    }
}