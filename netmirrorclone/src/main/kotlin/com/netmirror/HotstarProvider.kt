package com.netmirror

class HotstarProvider : BaseNetMirrorProvider() {
    override var name = "Hotstar"
    override val ott = "hs"
    override val imgPrefix = "hs"
    override val searchPath = "hs/search.php"
    override val postPath = "hs/post.php"
    override val episodesPath = "hs/episodes.php"
    override val playlistPath = "hs/playlist.php"
}
