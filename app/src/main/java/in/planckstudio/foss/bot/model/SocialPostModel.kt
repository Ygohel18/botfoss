package `in`.planckstudio.foss.bot.model

class SocialPostModel {
    private var PostId: String = ""
    private var PostThumbUrl: String = ""
    private var PostSrcUrl: String = ""
    private var PostTitle: String = ""
    private var PostMediaType: String = ""

    fun setPostId(id: String) {
        this.PostId = id
    }

    fun setPostThumbUrl(url: String) {
        this.PostThumbUrl = url
    }

    fun setPostSrcUrl(url: String) {
        this.PostSrcUrl = url
    }

    fun setPostTitle(title: String) {
        this.PostTitle = title
    }

    fun setPostMediaType(type: String) {
        this.PostMediaType = type
    }

    fun getPostId(): String {
        return this.PostId
    }

    fun getPostThumbUrl(): String {
        return this.PostThumbUrl
    }

    fun getPostSrcUrl(): String {
        return this.PostSrcUrl
    }

    fun getPostTitle(): String {
        return this.PostTitle
    }

    fun getPostMediaType(): String {
        return this.PostMediaType
    }
}