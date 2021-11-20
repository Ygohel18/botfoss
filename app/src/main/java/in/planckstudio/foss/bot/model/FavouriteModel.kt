package `in`.planckstudio.foss.bot.model

class FavouriteModel {
    private var favouriteId: Int = 0
    private var favouriteType: String = ""
    private var favouriteName: String = ""
    private var favouriteValue: Int = 0

    constructor()

    constructor(type: String, name: String, value: Int) {
        this.favouriteType = type
        this.favouriteName = name
        this.favouriteValue = value
    }

    fun setFavouriteID(id: Int) {
        this.favouriteId = id
    }

    fun setFavouriteType(type: String) {
        this.favouriteType = type
    }

    fun setFavouriteName(name: String) {
        this.favouriteName = name
    }

    fun setFavouriteValue(value: Int) {
        this.favouriteValue = value
    }

    fun getFavouriteId(): Int {
        return this.favouriteId
    }

    fun getFavouriteType(): String {
        return this.favouriteType
    }

    fun getFavouriteName(): String {
        return this.favouriteName
    }

    fun getFavouriteValue(): Int {
        return this.favouriteValue
    }
}