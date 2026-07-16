package com.kraptor;

import android.content.Context;
import android.content.SharedPreferences;
import com.byayzen.TmdbHelper;
import com.lagradost.api.Log;
import com.lagradost.cloudstream3.AnimeSearchResponse;
import com.lagradost.cloudstream3.Episode;
import com.lagradost.cloudstream3.HomePageList;
import com.lagradost.cloudstream3.MainAPI;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.SearchResponse;
import com.lagradost.cloudstream3.TvType;
import com.lagradost.cloudstream3.network.CloudflareKiller;
import com.lagradost.cloudstream3.utils.AppUtils;
import com.lagradost.cloudstream3.utils.ExtractorLink;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.MapsKt;
import kotlin.collections.SetsKt;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.text.StringsKt;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* compiled from: NetflixMirrorProvider.kt */
@Metadata(d1 = {"\u0000¨\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010$\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 Y2\u00020\u0001:\u0004YZ[\\B\u0013\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\u0018\u0010-\u001a\u00020#2\b\b\u0002\u0010.\u001a\u00020#H\u0082@¢\u0006\u0002\u0010/J\u0014\u00100\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00170+H\u0002J \u00101\u001a\u0004\u0018\u0001022\u0006\u00103\u001a\u0002042\u0006\u00105\u001a\u000206H\u0096@¢\u0006\u0002\u00107J\u000e\u00108\u001a\u0004\u0018\u000109*\u00020:H\u0002J\u000e\u0010;\u001a\u0004\u0018\u00010<*\u00020:H\u0002J\u001c\u0010=\u001a\b\u0012\u0004\u0012\u00020<0>2\u0006\u0010?\u001a\u00020\u0017H\u0096@¢\u0006\u0002\u0010@J\u0018\u0010A\u001a\u0004\u0018\u00010B2\u0006\u0010C\u001a\u00020\u0017H\u0096@¢\u0006\u0002\u0010@J@\u0010D\u001a\b\u0012\u0004\u0012\u00020E0>2\u0006\u0010F\u001a\u00020\u00172\u0006\u0010G\u001a\u00020\u00172\u0006\u0010H\u001a\u00020\u00172\u0006\u00103\u001a\u0002042\n\b\u0002\u0010I\u001a\u0004\u0018\u00010JH\u0082@¢\u0006\u0002\u0010KJF\u0010L\u001a\u00020#2\u0006\u0010M\u001a\u00020\u00172\u0006\u0010N\u001a\u00020#2\u0012\u0010O\u001a\u000e\u0012\u0004\u0012\u00020Q\u0012\u0004\u0012\u00020R0P2\u0012\u0010S\u001a\u000e\u0012\u0004\u0012\u00020T\u0012\u0004\u0012\u00020R0PH\u0096@¢\u0006\u0002\u0010UJ\u0012\u0010V\u001a\u0004\u0018\u00010W2\u0006\u0010X\u001a\u00020TH\u0016R\u001b\u0010\u0006\u001a\u00020\u00078BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\n\u0010\u000b\u001a\u0004\b\b\u0010\tR\u001b\u0010\f\u001a\u00020\r8BX\u0082\u0084\u0002¢\u0006\f\n\u0004\b\u0010\u0010\u000b\u001a\u0004\b\u000e\u0010\u000fR\u001a\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012X\u0096\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u001a\u0010\u0016\u001a\u00020\u0017X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0019\"\u0004\b\u001a\u0010\u001bR\u001a\u0010\u001c\u001a\u00020\u0017X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u0019\"\u0004\b\u001e\u0010\u001bR\u001a\u0010\u001f\u001a\u00020\u0017X\u0096\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b \u0010\u0019\"\u0004\b!\u0010\u001bR\u0014\u0010\"\u001a\u00020#X\u0096D¢\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u000e\u0010&\u001a\u00020\u0017X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010'\u001a\u00020\u0017X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010(\u001a\u00020\u0017X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010)\u001a\u00020\u0017X\u0082\u000e¢\u0006\u0002\n\u0000R\u001a\u0010*\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00170+X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010,\u001a\u00020\u0017X\u0082D¢\u0006\u0002\n\u0000¨\u0006]"}, d2 = {"Lcom/kraptor/NetflixMirrorProvider;", "Lcom/lagradost/cloudstream3/MainAPI;", "sharedPref", "Landroid/content/SharedPreferences;", "<init>", "(Landroid/content/SharedPreferences;)V", "cloudflareKiller", "Lcom/lagradost/cloudstream3/network/CloudflareKiller;", "getCloudflareKiller", "()Lcom/lagradost/cloudstream3/network/CloudflareKiller;", "cloudflareKiller$delegate", "Lkotlin/Lazy;", "interceptor", "Lcom/kraptor/NetflixMirrorProvider$CloudflareInterceptor;", "getInterceptor", "()Lcom/kraptor/NetflixMirrorProvider$CloudflareInterceptor;", "interceptor$delegate", "supportedTypes", "", "Lcom/lagradost/cloudstream3/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "lang", "", "getLang", "()Ljava/lang/String;", "setLang", "(Ljava/lang/String;)V", "mainUrl", "getMainUrl", "setMainUrl", "name", "getName", "setName", "hasMainPage", "", "getHasMainPage", "()Z", "cookie_value", "user_token", "add_hash", "data_time", "headers", "", "ott", "ensureBypass", "force", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "bypassCookies", "getMainPage", "Lcom/lagradost/cloudstream3/HomePageResponse;", "page", "", "request", "Lcom/lagradost/cloudstream3/MainPageRequest;", "(ILcom/lagradost/cloudstream3/MainPageRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "toHomePageList", "Lcom/lagradost/cloudstream3/HomePageList;", "Lorg/jsoup/nodes/Element;", "toSearchResult", "Lcom/lagradost/cloudstream3/SearchResponse;", "search", "", "query", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "load", "Lcom/lagradost/cloudstream3/LoadResponse;", "url", "getEpisodes", "Lcom/lagradost/cloudstream3/Episode;", "title", "eid", "sid", "tmdb", "Lcom/byayzen/TmdbHelper$TmdbDetailsExtended;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILcom/byayzen/TmdbHelper$TmdbDetailsExtended;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadLinks", "data", "isCasting", "subtitleCallback", "Lkotlin/Function1;", "Lcom/lagradost/cloudstream3/SubtitleFile;", "", "callback", "Lcom/lagradost/cloudstream3/utils/ExtractorLink;", "(Ljava/lang/String;ZLkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVideoInterceptor", "Lokhttp3/Interceptor;", "extractorLink", "Companion", "CloudflareInterceptor", "Id", "LoadData", "MirrorVerse"}, k = 1, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nNetflixMirrorProvider.kt\nKotlin\n*S Kotlin\n*F\n+ 1 NetflixMirrorProvider.kt\ncom/kraptor/NetflixMirrorProvider\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 3 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 4 NiceResponse.kt\ncom/lagradost/nicehttp/NiceResponse\n+ 5 Extensions.kt\ncom/fasterxml/jackson/module/kotlin/ExtensionsKt\n+ 6 Utils.kt\ncom/kraptor/UtilsKt\n+ 7 _Sequences.kt\nkotlin/sequences/SequencesKt___SequencesKt\n*L\n1#1,522:1\n1642#2,10:523\n1915#2:533\n1916#2:535\n1652#2:536\n1642#2,10:537\n1915#2:547\n1916#2:549\n1652#2:550\n1586#2:552\n1661#2,3:553\n1661#2,2:559\n1663#2:562\n1661#2,3:564\n1915#2:568\n1915#2,2:569\n1915#2,2:571\n1916#2:573\n1915#2,2:574\n1915#2,2:578\n1915#2,2:580\n1#3:534\n1#3:548\n1#3:561\n68#4:551\n68#4:558\n68#4:563\n50#5:556\n43#5:557\n40#6:567\n1342#7,2:576\n*S KotlinDebug\n*F\n+ 1 NetflixMirrorProvider.kt\ncom/kraptor/NetflixMirrorProvider\n*L\n145#1:523,10\n145#1:533\n145#1:535\n145#1:536\n168#1:537,10\n168#1:547\n168#1:549\n168#1:550\n198#1:552\n198#1:553,3\n241#1:559,2\n241#1:562\n312#1:564,3\n387#1:568\n390#1:569,2\n412#1:571,2\n387#1:573\n442#1:574,2\n466#1:578,2\n477#1:580,2\n145#1:534\n168#1:548\n196#1:551\n222#1:558\n311#1:563\n210#1:556\n210#1:557\n340#1:567\n459#1:576,2\n*E\n"})
/* loaded from: C:\temp\mv2\classes.dex */
public final class NetflixMirrorProvider extends MainAPI {
    private static final long BROWSER_DEBOUNCE_MS = 10000;

    /* renamed from: Companion, reason: from kotlin metadata */
    @NotNull
    public static final Companion INSTANCE = new Companion(null);

    @NotNull
    private static final String OMG10 = "aHR0cHM6Ly9vbWcxMC5jb20vNC8xMTEwNDQ4OQ==";

    @Nullable
    private static Context context;
    private static volatile long lastBrowserOpenMs;

    @NotNull
    private String add_hash;

    /* renamed from: cloudflareKiller$delegate, reason: from kotlin metadata */
    @NotNull
    private final Lazy cloudflareKiller;

    @NotNull
    private String cookie_value;

    @NotNull
    private String data_time;
    private final boolean hasMainPage;

    @NotNull
    private final Map<String, String> headers;

    /* renamed from: interceptor$delegate, reason: from kotlin metadata */
    @NotNull
    private final Lazy interceptor;

    @NotNull
    private String lang;

    @NotNull
    private String mainUrl;

    @NotNull
    private String name;

    @NotNull
    private final String ott;

    @NotNull
    private final Set<TvType> supportedTypes;

    @NotNull
    private String user_token;

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public NetflixMirrorProvider() {
        /*
            r2 = this;
            r0 = 0
            r1 = 1
            r2.<init>(r0, r1, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.NetflixMirrorProvider.<init>():void");
    }

    /* compiled from: NetflixMirrorProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u000bX\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\rX\u0082T¢\u0006\u0002\n\u0000¨\u0006\u000f"}, d2 = {"Lcom/kraptor/NetflixMirrorProvider$Companion;", "", "<init>", "()V", "context", "Landroid/content/Context;", "getContext", "()Landroid/content/Context;", "setContext", "(Landroid/content/Context;)V", "OMG10", "", "lastBrowserOpenMs", "", "BROWSER_DEBOUNCE_MS", "MirrorVerse"}, k = 1, mv = {2, 3, 0}, xi = 48)
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        @Nullable
        public final Context getContext() {
            return NetflixMirrorProvider.context;
        }

        public final void setContext(@Nullable Context context) {
            NetflixMirrorProvider.context = context;
        }
    }

    public NetflixMirrorProvider(@Nullable SharedPreferences sharedPref) {
        this.cloudflareKiller = LazyKt.lazy(new Function0() { // from class: com.kraptor.NetflixMirrorProvider$$ExternalSyntheticLambda5
            public final Object invoke() {
                return NetflixMirrorProvider.cloudflareKiller_delegate$lambda$0();
            }
        });
        this.interceptor = LazyKt.lazy(new Function0() { // from class: com.kraptor.NetflixMirrorProvider$$ExternalSyntheticLambda6
            public final Object invoke() {
                return NetflixMirrorProvider.interceptor_delegate$lambda$0(NetflixMirrorProvider.this);
            }
        });
        this.supportedTypes = SetsKt.setOf(new TvType[]{TvType.Movie, TvType.TvSeries});
        this.lang = "tr";
        this.mainUrl = "https://net52.cc";
        this.name = "Netflix";
        this.hasMainPage = true;
        this.cookie_value = "";
        this.user_token = "";
        this.add_hash = "";
        this.data_time = "";
        this.headers = MapsKt.mapOf(new Pair[]{TuplesKt.to("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"), TuplesKt.to("Accept-Language", "en-IN,en-US;q=0.9,en;q=0.8"), TuplesKt.to("Cache-Control", "max-age=0"), TuplesKt.to("Connection", "keep-alive"), TuplesKt.to("sec-ch-ua", "\"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"144\", \"Android WebView\";v=\"144\""), TuplesKt.to("sec-ch-ua-mobile", "?0"), TuplesKt.to("sec-ch-ua-platform", "\"Android\""), TuplesKt.to("Sec-Fetch-Dest", "document"), TuplesKt.to("Sec-Fetch-Mode", "navigate"), TuplesKt.to("Sec-Fetch-Site", "same-origin"), TuplesKt.to("Sec-Fetch-User", "?1"), TuplesKt.to("Upgrade-Insecure-Requests", "1"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 5 Build/TQ3A.230901.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/144.0.7559.132 Safari/537.36 /OS.Gatu v3.0"), TuplesKt.to("X-Requested-With", "XMLHttpRequest")});
        this.ott = "nf";
    }

    public /* synthetic */ NetflixMirrorProvider(SharedPreferences sharedPreferences, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? null : sharedPreferences);
    }

    static final CloudflareKiller cloudflareKiller_delegate$lambda$0() {
        return new CloudflareKiller();
    }

    private final CloudflareKiller getCloudflareKiller() {
        return (CloudflareKiller) this.cloudflareKiller.getValue();
    }

    private final CloudflareInterceptor getInterceptor() {
        return (CloudflareInterceptor) this.interceptor.getValue();
    }

    static final CloudflareInterceptor interceptor_delegate$lambda$0(NetflixMirrorProvider this$0) {
        return new CloudflareInterceptor(this$0.getCloudflareKiller());
    }

    /* compiled from: NetflixMirrorProvider.kt */
    @Metadata(d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\n"}, d2 = {"Lcom/kraptor/NetflixMirrorProvider$CloudflareInterceptor;", "Lokhttp3/Interceptor;", "cloudflareKiller", "Lcom/lagradost/cloudstream3/network/CloudflareKiller;", "<init>", "(Lcom/lagradost/cloudstream3/network/CloudflareKiller;)V", "intercept", "Lokhttp3/Response;", "chain", "Lokhttp3/Interceptor$Chain;", "MirrorVerse"}, k = 1, mv = {2, 3, 0}, xi = 48)
    public static final class CloudflareInterceptor implements Interceptor {

        @NotNull
        private final CloudflareKiller cloudflareKiller;

        public CloudflareInterceptor(@NotNull CloudflareKiller cloudflareKiller) {
            this.cloudflareKiller = cloudflareKiller;
        }

        @NotNull
        public Response intercept(@NotNull Interceptor.Chain chain) {
            Request request = chain.request();
            Response response = chain.proceed(request);
            Document doc = Jsoup.parse(response.peekBody(1048576L).string());
            if (StringsKt.contains$default(doc.html(), "Just a moment", false, 2, (Object) null)) {
                return this.cloudflareKiller.intercept(chain);
            }
            return response;
        }
    }

    @NotNull
    public Set<TvType> getSupportedTypes() {
        return this.supportedTypes;
    }

    @NotNull
    public String getLang() {
        return this.lang;
    }

    public void setLang(@NotNull String str) {
        this.lang = str;
    }

    @NotNull
    public String getMainUrl() {
        return this.mainUrl;
    }

    public void setMainUrl(@NotNull String str) {
        this.mainUrl = str;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public void setName(@NotNull String str) {
        this.name = str;
    }

    public boolean getHasMainPage() {
        return this.hasMainPage;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x00c8  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x00da  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0046  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object ensureBypass(boolean r17, kotlin.coroutines.Continuation<? super java.lang.Boolean> r18) {
        /*
            Method dump skipped, instructions count: 268
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.NetflixMirrorProvider.ensureBypass(boolean, kotlin.coroutines.Continuation):java.lang.Object");
    }

    static /* synthetic */ Object ensureBypass$default(NetflixMirrorProvider netflixMirrorProvider, boolean z, Continuation continuation, int i, Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        return netflixMirrorProvider.ensureBypass(z, continuation);
    }

    private final Map<String, String> bypassCookies() {
        return MapsKt.mapOf(new Pair[]{TuplesKt.to("t_hash_t", this.cookie_value), TuplesKt.to("addhash", this.add_hash), TuplesKt.to("hd", "on"), TuplesKt.to("ott", "nf")});
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0035  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x014f  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x004a  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0080 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0081  */
    /* JADX WARN: Removed duplicated region for block: B:32:0x0057  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.Object getMainPage(int r26, @org.jetbrains.annotations.NotNull com.lagradost.cloudstream3.MainPageRequest r27, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super com.lagradost.cloudstream3.HomePageResponse> r28) {
        /*
            Method dump skipped, instructions count: 388
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.NetflixMirrorProvider.getMainPage(int, com.lagradost.cloudstream3.MainPageRequest, kotlin.coroutines.Continuation):java.lang.Object");
    }

    private final HomePageList toHomePageList(Element $this$toHomePageList) {
        String nameRaw = StringsKt.trim($this$toHomePageList.select("h2, span").text()).toString();
        HomePageList homePageList = null;
        if (StringsKt.isBlank(nameRaw)) {
            return null;
        }
        Log.INSTANCE.d("Netflix_Category", "Kategori: " + nameRaw);
        if (!StringsKt.contains(nameRaw, "Indian", true) && !StringsKt.contains(nameRaw, "Hindi", true) && !StringsKt.contains(nameRaw, "Tamil", true) && !StringsKt.contains(nameRaw, "Telugu", true) && !StringsKt.contains(nameRaw, "India", true) && !StringsKt.contains(nameRaw, "Once Upon a Time in India", true)) {
            String name = UtilsKt.translateCategory(nameRaw);
            Iterable $this$mapNotNull$iv = $this$toHomePageList.select("article, .top10-post");
            Collection destination$iv$iv = new ArrayList();
            for (Object element$iv$iv$iv : $this$mapNotNull$iv) {
                Element it = (Element) element$iv$iv$iv;
                HomePageList homePageList2 = homePageList;
                SearchResponse searchResult = toSearchResult(it);
                if (searchResult != null) {
                    destination$iv$iv.add(searchResult);
                }
                homePageList = homePageList2;
            }
            List items = (List) destination$iv$iv;
            return items.isEmpty() ? homePageList : new HomePageList(name, items, false);
        }
        return null;
    }

    private final SearchResponse toSearchResult(Element $this$toSearchResult) {
        final String id;
        Element selectFirst = $this$toSearchResult.selectFirst("a");
        if (selectFirst == null || (id = selectFirst.attr("data-post")) == null) {
            id = $this$toSearchResult.attr("data-post");
        }
        String str = id;
        if (str == null || StringsKt.isBlank(str)) {
            return null;
        }
        return MainAPIKt.newAnimeSearchResponse$default(this, "", AppUtils.INSTANCE.toJson(new Id(id)), (TvType) null, false, new Function1() { // from class: com.kraptor.NetflixMirrorProvider$$ExternalSyntheticLambda1
            public final Object invoke(Object obj) {
                return NetflixMirrorProvider.toSearchResult$lambda$0(id, this, (AnimeSearchResponse) obj);
            }
        }, 12, (Object) null);
    }

    static final Unit toSearchResult$lambda$0(String $id, NetflixMirrorProvider this$0, AnimeSearchResponse $this$newAnimeSearchResponse) {
        $this$newAnimeSearchResponse.setPosterUrl("https://imgcdn.kim/poster/v/" + $id + ".jpg");
        $this$newAnimeSearchResponse.setPosterHeaders(MapsKt.mapOf(TuplesKt.to("Referer", this$0.getMainUrl() + "/home")));
        return Unit.INSTANCE;
    }

    /* JADX WARN: Removed duplicated region for block: B:11:0x0034  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0174 A[LOOP:0: B:13:0x016e->B:15:0x0174, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0049  */
    /* JADX WARN: Removed duplicated region for block: B:22:0x0078  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x007d  */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0052  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002a  */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.Object search(@org.jetbrains.annotations.NotNull java.lang.String r32, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.util.List<? extends com.lagradost.cloudstream3.SearchResponse>> r33) {
        /*
            Method dump skipped, instructions count: 450
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.NetflixMirrorProvider.search(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit search$lambda$0$0(com.kraptor.entities.SearchResult $it, NetflixMirrorProvider this$0, AnimeSearchResponse $this$newAnimeSearchResponse) {
        $this$newAnimeSearchResponse.setPosterUrl("https://imgcdn.kim/poster/v/" + $it.getId() + ".jpg");
        $this$newAnimeSearchResponse.setPosterHeaders(MapsKt.mapOf(TuplesKt.to("Referer", this$0.getMainUrl() + "/home")));
        return Unit.INSTANCE;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:100:0x02e0 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:101:0x02e1  */
    /* JADX WARN: Removed duplicated region for block: B:104:0x0166  */
    /* JADX WARN: Removed duplicated region for block: B:107:0x019e A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:108:0x019f A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:118:0x0176  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0033  */
    /* JADX WARN: Removed duplicated region for block: B:13:0x005f  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x05cd A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:18:0x05ce  */
    /* JADX WARN: Removed duplicated region for block: B:20:0x009b  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x050c  */
    /* JADX WARN: Removed duplicated region for block: B:31:0x00e0  */
    /* JADX WARN: Removed duplicated region for block: B:36:0x03db  */
    /* JADX WARN: Removed duplicated region for block: B:57:0x0483  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x048f  */
    /* JADX WARN: Removed duplicated region for block: B:66:0x0114  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x02f3  */
    /* JADX WARN: Removed duplicated region for block: B:70:0x0317  */
    /* JADX WARN: Removed duplicated region for block: B:89:0x014b  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    /* JADX WARN: Type inference failed for: r24v4 */
    /* JADX WARN: Type inference failed for: r24v5 */
    /* JADX WARN: Type inference failed for: r24v6, types: [java.lang.Object] */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.Object load(@org.jetbrains.annotations.NotNull java.lang.String r36, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super com.lagradost.cloudstream3.LoadResponse> r37) {
        /*
            Method dump skipped, instructions count: 1524
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.NetflixMirrorProvider.load(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
    }

    static final Unit load$lambda$0(String $title, Episode $this$newEpisode) {
        $this$newEpisode.setName($title);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit load$lambda$1$1(TmdbHelper.TmdbEpisode $tmdbEp, com.kraptor.entities.Episode $epItem, Integer $epNum, Integer $sNum, Episode $this$newEpisode) {
        String t;
        String it;
        Integer intOrNull;
        String it2;
        if ($tmdbEp == null || (t = $tmdbEp.getName()) == null) {
            t = $epItem.getT();
        }
        $this$newEpisode.setName(t);
        $this$newEpisode.setEpisode($epNum);
        $this$newEpisode.setSeason($sNum);
        if ($tmdbEp == null || (it2 = $tmdbEp.getStill_path()) == null || (it = "https://image.tmdb.org/t/p/w500" + it2) == null) {
            it = "https://imgcdn.kim/poster/v/150/" + $epItem.getId() + ".jpg";
        }
        $this$newEpisode.setPosterUrl(it);
        if ($tmdbEp == null || (intOrNull = $tmdbEp.getRuntime()) == null) {
            intOrNull = StringsKt.toIntOrNull(StringsKt.replace$default($epItem.getTime(), "m", "", false, 4, (Object) null));
        }
        $this$newEpisode.setRunTime(intOrNull);
        return Unit.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0034  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x020a  */
    /* JADX WARN: Removed duplicated region for block: B:39:0x02df  */
    /* JADX WARN: Removed duplicated region for block: B:42:0x01d8 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:43:0x01d9  */
    /* JADX WARN: Removed duplicated region for block: B:44:0x02f2 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:45:0x02cc  */
    /* JADX WARN: Removed duplicated region for block: B:46:0x0065  */
    /* JADX WARN: Removed duplicated region for block: B:49:0x008a  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002a  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:38:0x01d9 -> B:12:0x01e3). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object getEpisodes(java.lang.String r35, java.lang.String r36, java.lang.String r37, int r38, com.byayzen.TmdbHelper.TmdbDetailsExtended r39, kotlin.coroutines.Continuation<? super java.util.List<com.lagradost.cloudstream3.Episode>> r40) {
        /*
            Method dump skipped, instructions count: 766
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.NetflixMirrorProvider.getEpisodes(java.lang.String, java.lang.String, java.lang.String, int, com.byayzen.TmdbHelper$TmdbDetailsExtended, kotlin.coroutines.Continuation):java.lang.Object");
    }

    static /* synthetic */ Object getEpisodes$default(NetflixMirrorProvider netflixMirrorProvider, String str, String str2, String str3, int i, TmdbHelper.TmdbDetailsExtended tmdbDetailsExtended, Continuation continuation, int i2, Object obj) {
        TmdbHelper.TmdbDetailsExtended tmdbDetailsExtended2;
        if ((i2 & 16) == 0) {
            tmdbDetailsExtended2 = tmdbDetailsExtended;
        } else {
            tmdbDetailsExtended2 = null;
        }
        return netflixMirrorProvider.getEpisodes(str, str2, str3, i, tmdbDetailsExtended2, continuation);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit getEpisodes$lambda$0$1(TmdbHelper.TmdbEpisode $tmdbEp, com.kraptor.entities.Episode $epItem, Integer $epNum, Integer $seaNum, Episode $this$newEpisode) {
        String t;
        String it;
        Integer intOrNull;
        String it2;
        if ($tmdbEp == null || (t = $tmdbEp.getName()) == null) {
            t = $epItem.getT();
        }
        $this$newEpisode.setName(t);
        $this$newEpisode.setEpisode($epNum);
        $this$newEpisode.setSeason($seaNum);
        if ($tmdbEp == null || (it2 = $tmdbEp.getStill_path()) == null || (it = "https://image.tmdb.org/t/p/w500" + it2) == null) {
            it = "https://imgcdn.kim/epimg/150/" + $epItem.getId() + ".jpg";
        }
        $this$newEpisode.setPosterUrl(it);
        if ($tmdbEp == null || (intOrNull = $tmdbEp.getRuntime()) == null) {
            intOrNull = StringsKt.toIntOrNull(StringsKt.replace$default($epItem.getTime(), "m", "", false, 4, (Object) null));
        }
        $this$newEpisode.setRunTime(intOrNull);
        return Unit.INSTANCE;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(22:363|316|317|318|319|320|(2:335|336)(1:322)|323|(3:326|327|(5:329|330|149|150|(10:152|(3:163|164|(6:166|(24:169|170|171|172|(3:174|175|176)(1:226)|222|178|179|180|(2:212|213)|182|183|184|(1:186)(1:211)|187|188|(1:190)(1:210)|191|192|193|194|(5:196|(1:198)|(1:200)(1:204)|201|202)(2:205|206)|203|167)|230|231|155|(5:161|148|149|150|(12:236|237|238|(1:297)(2:243|(1:296)(4:247|(18:250|251|252|253|(2:283|284)|255|256|257|(1:282)|261|262|263|264|265|266|(4:268|(1:270)|271|272)(2:274|275)|273|248)|292|293))|294|93|(3:95|(2:98|96)|99)|100|(2:103|101)|104|105|(2:107|108)(3:109|13|(2:15|(1:17)(4:18|12|13|(2:19|20)(0)))(0)))(0))(5:159|160|29|30|(15:32|33|34|35|(2:138|139)|37|38|39|(1:137)|43|44|(1:136)|48|49|(5:134|28|29|30|(5:147|148|149|150|(0)(0))(0))(32:53|(2:55|56)|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|83|84|85|(1:87)(8:88|25|26|27|28|29|30|(0)(0))))(0))))|154|155|(1:157)|161|148|149|150|(0)(0))(0)))|325|237|238|(0)|297|294|93|(0)|100|(1:101)|104|105|(0)(0)) */
    /* JADX WARN: Code restructure failed: missing block: B:135:0x099a, code lost:
    
        r32 = r64;
        r12 = r67;
        r54 = r54;
        r59 = r59;
        r61 = r61;
        r62 = r62;
        r60 = r60;
        r53 = r53;
        r56 = r56;
        r2 = r65;
        r33 = r66;
        r13 = r68;
        r58 = r58;
        r57 = r57;
        r55 = r55;
     */
    /* JADX WARN: Code restructure failed: missing block: B:140:0x0679, code lost:
    
        if (r8 == null) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:162:0x0a3c, code lost:
    
        r34 = r7;
        r17 = r67;
        r6 = r68;
        r7 = r24;
        r8 = r28;
        r9 = r29;
        r11 = r30;
        r30 = r64;
        r14 = r66;
        r3 = r65;
     */
    /* JADX WARN: Code restructure failed: missing block: B:177:0x04d4, code lost:
    
        if (r3 == null) goto L74;
     */
    /* JADX WARN: Code restructure failed: missing block: B:214:0x04f9, code lost:
    
        if (r6 == null) goto L83;
     */
    /* JADX WARN: Code restructure failed: missing block: B:285:0x0aff, code lost:
    
        if (r2 == null) goto L240;
     */
    /* JADX WARN: Code restructure failed: missing block: B:298:0x0bbc, code lost:
    
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:299:0x0bbd, code lost:
    
        r5 = r7;
        r40 = r10;
        r4 = r36;
     */
    /* JADX WARN: Removed duplicated region for block: B:103:0x0c71 A[LOOP:1: B:101:0x0c6b->B:103:0x0c71, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:107:0x0cb5  */
    /* JADX WARN: Removed duplicated region for block: B:109:0x0cc3  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0043  */
    /* JADX WARN: Removed duplicated region for block: B:147:0x09c9  */
    /* JADX WARN: Removed duplicated region for block: B:152:0x048e A[Catch: Exception -> 0x0a84, TRY_LEAVE, TryCatch #30 {Exception -> 0x0a84, blocks: (B:150:0x0488, B:152:0x048e), top: B:149:0x0488 }] */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0d05  */
    /* JADX WARN: Removed duplicated region for block: B:19:0x0e03  */
    /* JADX WARN: Removed duplicated region for block: B:21:0x00cb  */
    /* JADX WARN: Removed duplicated region for block: B:236:0x0a5e  */
    /* JADX WARN: Removed duplicated region for block: B:240:0x0ac3 A[ADDED_TO_REGION] */
    /* JADX WARN: Removed duplicated region for block: B:312:0x01b6  */
    /* JADX WARN: Removed duplicated region for block: B:322:0x045e  */
    /* JADX WARN: Removed duplicated region for block: B:326:0x046c A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:32:0x065e A[Catch: Exception -> 0x0a00, TRY_LEAVE, TryCatch #21 {Exception -> 0x0a00, blocks: (B:30:0x0658, B:32:0x065e), top: B:29:0x0658 }] */
    /* JADX WARN: Removed duplicated region for block: B:335:0x044b A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:350:0x0216  */
    /* JADX WARN: Removed duplicated region for block: B:353:0x02d0  */
    /* JADX WARN: Removed duplicated region for block: B:355:0x02d5  */
    /* JADX WARN: Removed duplicated region for block: B:370:0x0249  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x003b  */
    /* JADX WARN: Removed duplicated region for block: B:95:0x0bfd  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:157:0x0639 -> B:29:0x0658). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:18:0x0ddc -> B:12:0x0df2). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:88:0x07f2 -> B:25:0x081f). Please report as a decompilation issue!!! */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.lang.Object loadLinks(@org.jetbrains.annotations.NotNull java.lang.String r64, boolean r65, @org.jetbrains.annotations.NotNull kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.SubtitleFile, kotlin.Unit> r66, @org.jetbrains.annotations.NotNull kotlin.jvm.functions.Function1<? super com.lagradost.cloudstream3.utils.ExtractorLink, kotlin.Unit> r67, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.lang.Boolean> r68) {
        /*
            Method dump skipped, instructions count: 3708
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.NetflixMirrorProvider.loadLinks(java.lang.String, boolean, kotlin.jvm.functions.Function1, kotlin.jvm.functions.Function1, kotlin.coroutines.Continuation):java.lang.Object");
    }

    @Nullable
    public Interceptor getVideoInterceptor(@NotNull ExtractorLink extractorLink) {
        return new Interceptor() { // from class: com.kraptor.NetflixMirrorProvider$getVideoInterceptor$1
            public Response intercept(Interceptor.Chain chain) {
                String str;
                String str2;
                Request request = chain.request();
                String urlStr = request.url().toString();
                if (StringsKt.contains$default(urlStr, ".m3u8", false, 2, (Object) null) || StringsKt.contains$default(urlStr, ".ts", false, 2, (Object) null) || StringsKt.contains$default(urlStr, ".jpg", false, 2, (Object) null)) {
                    Request.Builder header = request.newBuilder().header("Referer", NetflixMirrorProvider.this.getMainUrl() + "/mobile/home?app=1");
                    StringBuilder append = new StringBuilder().append("t_hash_t=");
                    str = NetflixMirrorProvider.this.cookie_value;
                    StringBuilder append2 = append.append(str).append("; addhash=");
                    str2 = NetflixMirrorProvider.this.add_hash;
                    Request newRequest = header.header("Cookie", append2.append(str2).append("; hd=on; ott=nf").toString()).header("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 5 Build/TQ3A.230901.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/144.0.7559.132 Safari/537.36 /OS.Gatu v3.0").header("Origin", NetflixMirrorProvider.this.getMainUrl()).build();
                    return chain.proceed(newRequest);
                }
                return chain.proceed(request);
            }
        };
    }

    /* compiled from: NetflixMirrorProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0004\b\u0004\u0010\u0005J\t\u0010\b\u001a\u00020\u0003HÆ\u0003J\u0013\u0010\t\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\r\u001a\u00020\u000eHÖ\u0081\u0004J\n\u0010\u000f\u001a\u00020\u0003HÖ\u0081\u0004R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007¨\u0006\u0010"}, d2 = {"Lcom/kraptor/NetflixMirrorProvider$Id;", "", "id", "", "<init>", "(Ljava/lang/String;)V", "getId", "()Ljava/lang/String;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "MirrorVerse"}, k = 1, mv = {2, 3, 0}, xi = 48)
    public static final /* data */ class Id {

        @NotNull
        private final String id;

        public static /* synthetic */ Id copy$default(Id id, String str, int i, Object obj) {
            if ((i & 1) != 0) {
                str = id.id;
            }
            return id.copy(str);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getId() {
            return this.id;
        }

        @NotNull
        public final Id copy(@NotNull String id) {
            return new Id(id);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            return (other instanceof Id) && Intrinsics.areEqual(this.id, ((Id) other).id);
        }

        public int hashCode() {
            return this.id.hashCode();
        }

        @NotNull
        public String toString() {
            return "Id(id=" + this.id + ')';
        }

        public Id(@NotNull String id) {
            this.id = id;
        }

        @NotNull
        public final String getId() {
            return this.id;
        }
    }

    /* compiled from: NetflixMirrorProvider.kt */
    @Metadata(d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003¢\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\n\u001a\u00020\u0003HÆ\u0003J\t\u0010\u000b\u001a\u00020\u0003HÆ\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003HÆ\u0001J\u0014\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001HÖ\u0083\u0004J\n\u0010\u0010\u001a\u00020\u0011HÖ\u0081\u0004J\n\u0010\u0012\u001a\u00020\u0003HÖ\u0081\u0004R\u0011\u0010\u0002\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003¢\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b¨\u0006\u0013"}, d2 = {"Lcom/kraptor/NetflixMirrorProvider$LoadData;", "", "title", "", "id", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", "getTitle", "()Ljava/lang/String;", "getId", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "MirrorVerse"}, k = 1, mv = {2, 3, 0}, xi = 48)
    public static final /* data */ class LoadData {

        @NotNull
        private final String id;

        @NotNull
        private final String title;

        public static /* synthetic */ LoadData copy$default(LoadData loadData, String str, String str2, int i, Object obj) {
            if ((i & 1) != 0) {
                str = loadData.title;
            }
            if ((i & 2) != 0) {
                str2 = loadData.id;
            }
            return loadData.copy(str, str2);
        }

        @NotNull
        /* renamed from: component1, reason: from getter */
        public final String getTitle() {
            return this.title;
        }

        @NotNull
        /* renamed from: component2, reason: from getter */
        public final String getId() {
            return this.id;
        }

        @NotNull
        public final LoadData copy(@NotNull String title, @NotNull String id) {
            return new LoadData(title, id);
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LoadData)) {
                return false;
            }
            LoadData loadData = (LoadData) other;
            return Intrinsics.areEqual(this.title, loadData.title) && Intrinsics.areEqual(this.id, loadData.id);
        }

        public int hashCode() {
            return (this.title.hashCode() * 31) + this.id.hashCode();
        }

        @NotNull
        public String toString() {
            return "LoadData(title=" + this.title + ", id=" + this.id + ')';
        }

        public LoadData(@NotNull String title, @NotNull String id) {
            this.title = title;
            this.id = id;
        }

        @NotNull
        public final String getId() {
            return this.id;
        }

        @NotNull
        public final String getTitle() {
            return this.title;
        }
    }
}
