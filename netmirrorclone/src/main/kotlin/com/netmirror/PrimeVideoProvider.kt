package com.netmirror

class PrimeVideoProvider : BaseNetMirrorProvider() {
    override var name = "Prime Video"
    override val ott = "pv"
    override val imgPrefix = "pv"
    override val searchPath = "pv/search.php"
    override val postPath = "pv/post.php"
    override val episodesPath = "pv/episodes.php"
}
