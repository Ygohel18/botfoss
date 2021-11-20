package `in`.planckstudio.foss.bot.model

class ServiceAccount {
    private var AccountId = 0
    private var AccountType = ""
    private var AccountKey = HashMap<String, String>()

    constructor()

    fun setAccountID(id: Int) {
        this.AccountId = id
    }

    fun setAccountType(type: String) {
        this.AccountType = type
    }

    fun setAccountKey(key: String, value: String) {
        this.AccountKey[key] = value
    }

    fun getAccountId(): Int {
        return this.AccountId
    }

    fun getAccountType(): String {
        return this.AccountType
    }

    fun getAccountKey(key: String): String? {
        return this.AccountKey[key]
    }
}