package `in`.planckstudio.foss.bot

class SearchModel {
    private var searchId: Int = 0
    private var searchService: String = ""
    private var searchType: String = ""
    private var searchKey: String = ""
    private var searchValue: String = ""
    private var searchImage: String = ""

    constructor()

    constructor(id: Int, servie: String,type: String, key: String, value: String, image: String) {
        this.searchId = id
        this.searchService = servie
        this.searchType = type
        this.searchKey = key
        this.searchValue = value
        this.searchImage = image
    }

    fun setSearchID(id: Int) {
        this.searchId = id
    }

    fun setSearchType(type: String) {
        this.searchType = type
    }

    fun setSearchService(servie: String) {
        this.searchService = servie
    }

    fun setSearchImage(image: String) {
        this.searchImage = image
    }

    fun setSearchKey(key: String) {
        this.searchKey = key
    }

    fun setSearchValue(value: String) {
        this.searchValue = value
    }

    fun getSearchId(): Int {
        return this.searchId
    }

    fun getSearchType(): String {
        return this.searchType
    }

    fun getSearchKey(): String {
        return this.searchKey
    }

    fun getSearchValue(): String {
        return this.searchValue
    }

    fun getSearchService(): String {
        return this.searchService
    }

    fun getSearchImage(): String {
        return this.searchImage
    }
}