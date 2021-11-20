package `in`.planckstudio.foss.bot.model.instagram

class SearchHistory {
    private var SearchId: Int = 0
    private var SearchType: String = ""
    private var SearchKey: String = ""
    private var SearchValue: String = ""

    constructor()

    constructor(id: Int, type: String, key: String, value: String) {
        this.SearchId = id
        this.SearchType = type
        this.SearchKey = key
        this.SearchValue = value
    }

    fun setSearchID(id: Int) {
        this.SearchId = id
    }

    fun setSearchType(type: String) {
        this.SearchType = type
    }

    fun setSearchKey(key: String) {
        this.SearchKey = key
    }

    fun setSearchValue(value: String) {
        this.SearchValue = value
    }

    fun getSearchId(): Int {
        return this.SearchId
    }

    fun getSearchType(): String {
        return this.SearchType
    }

    fun getSearchKey(): String {
        return this.SearchKey
    }

    fun getSearchValue(): String {
        return this.SearchValue
    }
}