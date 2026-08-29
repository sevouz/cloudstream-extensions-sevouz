package com.netmirrorv2

class PrimeVideoProvider : BaseNetMirrorProvider() {
    override var name = "Prime Video V2"
    override val ott = "pv"
    override val imgPrefix = "pv"
    override val epImgPrefix = "pvepimg"
    override val searchPath = "pv/search.php"
    override val postPath = "pv/post.php"
    override val episodesPath = "pv/episodes.php"
    override val playlistPath = "pv/playlist.php"
}

