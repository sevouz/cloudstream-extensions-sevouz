package com.netmirrorv2

class HotstarProvider : BaseNetMirrorProvider() {
    override var name = "Hotstar V2"
    override val ott = "hs"
    override val imgPrefix = "hs"
    override val epImgPrefix = "hsepimg/150"
    override val searchPath = "hs/search.php"
    override val postPath = "hs/post.php"
    override val episodesPath = "hs/episodes.php"
    override val playlistPath = "hs/playlist.php"
}

