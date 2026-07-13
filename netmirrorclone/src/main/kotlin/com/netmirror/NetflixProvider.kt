package com.netmirror

class NetflixProvider : BaseNetMirrorProvider() {
    override var name = "Netflix"
    override val ott = "nf"
    override val imgPrefix = "poster"
    override val searchPath = "search.php"
    override val postPath = "post.php"
    override val episodesPath = "episodes.php"
}
