package com.kraptor;

import android.content.SharedPreferences;
import android.util.Base64;
import com.kraptor.NetflixMirrorStorage;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.io.CloseableKt;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.reflect.KClass;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import kotlinx.coroutines.BuildersKt;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Utils.kt */
@Metadata(d1 = {"\u0000H\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010$\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u000f\u001a\"\u0010\b\u001a\u0002H\t\"\n\b\u0000\u0010\t\u0018\u0001*\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086\b¢\u0006\u0002\u0010\r\u001a$\u0010\u000e\u001a\u0004\u0018\u0001H\t\"\n\b\u0000\u0010\t\u0018\u0001*\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086\b¢\u0006\u0002\u0010\r\u001a\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\f\u001a\u0018\u0010\u0015\u001a\u00020\f2\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\u0006\u0010\u0018\u001a\u00020\f\u001a\u0016\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\fH\u0086@¢\u0006\u0002\u0010\u001b\u001a\u000e\u0010$\u001a\u00020\f2\u0006\u0010%\u001a\u00020\f\u001a\u000e\u0010'\u001a\u00020\fH\u0086@¢\u0006\u0002\u0010(\u001a0\u0010)\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u001d2\u0006\u0010*\u001a\u00020\f2\u0014\b\u0002\u0010+\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u001d\u001a\u000e\u0010.\u001a\u00020\f2\u0006\u0010/\u001a\u00020\f\"\u0011\u0010\u0000\u001a\u00020\u0001¢\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0003\"\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u000e\u0010\u0012\u001a\u00020\u0013X\u0082T¢\u0006\u0002\n\u0000\"\u000e\u0010\u0014\u001a\u00020\fX\u0082T¢\u0006\u0002\n\u0000\"\u001d\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u001d¢\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001f\"\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\f0!¢\u0006\b\n\u0000\u001a\u0004\b\"\u0010#\"\u000e\u0010&\u001a\u00020\fX\u0082\u000e¢\u0006\u0002\n\u0000\"\u001d\u0010,\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u001d¢\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001f¨\u00060"}, d2 = {"JSONParser", "Lcom/lagradost/nicehttp/ResponseParser;", "getJSONParser", "()Lcom/lagradost/nicehttp/ResponseParser;", "app", "Lcom/lagradost/nicehttp/Requests;", "getApp", "()Lcom/lagradost/nicehttp/Requests;", "parseJson", "T", "", "text", "", "(Ljava/lang/String;)Ljava/lang/Object;", "tryParseJson", "convertRuntimeToMinutes", "", "runtime", "DOMAIN_CACHE_DURATION", "", "LAST_UPDATE_KEY", "getProviderDomain", "sharedPref", "Landroid/content/SharedPreferences;", "providerName", "bypass", "mainUrl", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "newTvBaseHeaders", "", "getNewTvBaseHeaders", "()Ljava/util/Map;", "newTvDomains", "", "getNewTvDomains", "()Ljava/util/List;", "decodeBase64", "value", "resolvedApiUrl", "resolveApiUrl", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "buildNewTvHeaders", "ott", "extra", "homePageCategories", "getHomePageCategories", "translateCategory", "name", "MirrorVerse"}, k = 2, mv = {2, 3, 0}, xi = 48)
@SourceDebugExtension({"SMAP\nUtils.kt\nKotlin\n*S Kotlin\n*F\n+ 1 Utils.kt\ncom/kraptor/UtilsKt\n+ 2 _Maps.kt\nkotlin/collections/MapsKt___MapsKt\n+ 3 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 4 NiceResponse.kt\ncom/lagradost/nicehttp/NiceResponse\n*L\n1#1,337:1\n221#2,2:338\n221#2,2:343\n296#3,2:340\n68#4:342\n*S KotlinDebug\n*F\n+ 1 Utils.kt\ncom/kraptor/UtilsKt\n*L\n149#1:338,2\n238#1:343,2\n156#1:340,2\n222#1:342\n*E\n"})
/* loaded from: C:\temp\mv2\classes.dex */
public final class UtilsKt {
    private static final long DOMAIN_CACHE_DURATION = 300000;

    @NotNull
    private static final ResponseParser JSONParser = new ResponseParser() { // from class: com.kraptor.UtilsKt$JSONParser$1
        public <T> T parse(String text, KClass<T> kClass) {
            return (T) MainAPIKt.getMapper().readValue(text, JvmClassMappingKt.getJavaClass(kClass));
        }

        public <T> T parseSafe(String text, KClass<T> kClass) {
            try {
                return (T) MainAPIKt.getMapper().readValue(text, JvmClassMappingKt.getJavaClass(kClass));
            } catch (Exception e) {
                return null;
            }
        }

        public String writeValueAsString(Object obj) {
            return MainAPIKt.getMapper().writeValueAsString(obj);
        }
    };

    @NotNull
    private static final String LAST_UPDATE_KEY = "last_domain_update";

    @NotNull
    private static final Requests app;

    @NotNull
    private static final Map<String, String> homePageCategories;

    @NotNull
    private static final Map<String, String> newTvBaseHeaders;

    @NotNull
    private static final List<String> newTvDomains;

    @NotNull
    private static String resolvedApiUrl;

    static {
        Requests $this$app_u24lambda_u240 = new Requests((OkHttpClient) null, (Map) null, (String) null, (Map) null, (Map) null, 0, (TimeUnit) null, 0L, JSONParser, 255, (DefaultConstructorMarker) null);
        $this$app_u24lambda_u240.setDefaultHeaders(MapsKt.mapOf(TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36")));
        app = $this$app_u24lambda_u240;
        newTvBaseHeaders = MapsKt.mapOf(new Pair[]{TuplesKt.to("Cache-Control", "no-cache, no-store, must-revalidate"), TuplesKt.to("Pragma", "no-cache"), TuplesKt.to("Expires", "0"), TuplesKt.to("X-Requested-With", "NetmirrorNewTV v1.0"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:136.0) Gecko/20100101 Firefox/136.0 /OS.GatuNewTV v1.0"), TuplesKt.to("Accept", "application/json, text/plain, */*")});
        newTvDomains = CollectionsKt.listOf(new String[]{"aHR0cHM6Ly9tb2JpbGVkZXRlY3RzLmNvbQ==", "aHR0cHM6Ly9tb2JpbGVkZXRlY3QuYXBw", "aHR0cHM6Ly9tb2JpZGV0ZWN0LmFydA==", "aHR0cHM6Ly9tb2JpZGV0ZWN0LmNj", "aHR0cHM6Ly9tb2JpZGV0ZWN0LmNsaWNr", "aHR0cHM6Ly9tb2JpZGV0ZWN0Lmluaw==", "aHR0cHM6Ly9tb2JpZGV0ZWN0LmxpdmU=", "aHR0cHM6Ly9tb2JpZGV0ZWN0LnBybw==", "aHR0cHM6Ly9tb2JpZGV0ZWN0LnNob3A=", "aHR0cHM6Ly9tb2JpZGV0ZWN0LnNpdGU=", "aHR0cHM6Ly9tb2JpZGV0ZWN0LnNwYWNl", "aHR0cHM6Ly9tb2JpZGV0ZWN0LnN0b3Jl", "aHR0cHM6Ly9tb2JpZGV0ZWN0LnZpcA==", "aHR0cHM6Ly9tb2JpZGV0ZWN0Lndpa2k=", "aHR0cHM6Ly9tb2JpZGV0ZWN0Lnh5eg==", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5hcnQ=", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5jYw==", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5pbmZv", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5pbms=", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5saXZl", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5wcm8=", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5zdG9yZQ==", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy50b3A=", "aHR0cHM6Ly9tb2JpZGV0ZWN0cy54eXo="});
        resolvedApiUrl = "";
        homePageCategories = MapsKt.mapOf(new Pair[]{TuplesKt.to("Critically Acclaimed Criminal Investigation TV Dramas", "Eleştirmenler Tarafından Övülen Suç Araştırma Dizileri"), TuplesKt.to("Crime Action & Adventure", "Suç Aksiyon ve Macera"), TuplesKt.to("New on Netflix", "Netflix'te Yeni"), TuplesKt.to("Young Adult Movies & Shows", "Genç Yetişkin Filmleri ve Programları"), TuplesKt.to("Top 10 Movies in Netflix Today", "Bugün Netflix'te İlk 10 Film"), TuplesKt.to("Star Power", "Yıldız Gücü"), TuplesKt.to("Top 10 Series in Netflix Today", "Bugün Netflix'te İlk 10 Dizi"), TuplesKt.to("Jokes on Us", "Bizim İçin Şakalar"), TuplesKt.to("Rousing TV Shows", "Heyecan Verici TV Programları"), TuplesKt.to("Only on Netflix", "Sadece Netflix'te"), TuplesKt.to("Geminis Spill the Tea", "İkizler Söylüyor"), TuplesKt.to("Critically-acclaimed High School Series", "Eleştirmenler Tarafından Övülen Lise Dizileri"), TuplesKt.to("International TV Shows", "Uluslararası TV Programları"), TuplesKt.to("Acclaimed Writers", "Övülen Yazarlar"), TuplesKt.to("Animation", "Animasyon"), TuplesKt.to("Documentaries", "Belgeseller"), TuplesKt.to("Exciting Series Based on Books", "Kitaplara Dayalı Heyecan Verici Diziler"), TuplesKt.to("Adult Animation", "Yetişkin Animasyonu"), TuplesKt.to("90-Minute Films", "90 Dakikalık Filmler"), TuplesKt.to("Watch It Again", "Tekrar İzle"), TuplesKt.to("Crime Movies", "Suç Filmleri"), TuplesKt.to("US Movies based on Books", "Kitaplara Dayalı ABD Filmleri"), TuplesKt.to("Critically-acclaimed US Auteur Cinema", "Eleştirmenler Tarafından Övülen ABD Yönetmen Filmleri"), TuplesKt.to("Kids Films", "Çocuk Filmleri"), TuplesKt.to("Independent Movies", "Bağımsız Filmler"), TuplesKt.to("Emotional Movies", "Duygusal Filmler"), TuplesKt.to("Action Thrillers", "Aksiyon Gerilimleri"), TuplesKt.to("Movies Based on Real Life", "Gerçek Hayata Dayalı Filmler"), TuplesKt.to("Epic Worlds", "Epik Dünyalar"), TuplesKt.to("Modern Classic Movies based on Books", "Kitaplara Dayalı Modern Klasik Filmler"), TuplesKt.to("Raunchy Comedy Movies", "Kabaca Komedi Filmleri"), TuplesKt.to("International Movies", "Uluslararası Filmler"), TuplesKt.to("Dark Movies", "Karanlık Filmler"), TuplesKt.to("Retro TV", "Retro TV"), TuplesKt.to("Gems for You", "Senin İçin Değerli Yapımlar"), TuplesKt.to("Drama Anime", "Dram Anime"), TuplesKt.to("Casual Viewing", "Rahat İzleme"), TuplesKt.to("We Think You’ll Love These", "Bunları Seveceğini Düşünüyoruz"), TuplesKt.to("Horror TV Serials", "Korku TV Serileri"), TuplesKt.to("Crime TV Shows", "Suç TV Programları"), TuplesKt.to("Critically Acclaimed TV Shows", "Eleştirmenler Tarafından Övülen TV Programları"), TuplesKt.to("Crime TV Dramas", "Suç TV Dramaları"), TuplesKt.to("Children & Family TV", "Çocuk ve Aile TV"), TuplesKt.to("Sci-Fi TV", "Bilim Kurgu TV"), TuplesKt.to("Fantasy TV Shows", "Fantastik TV Programları"), TuplesKt.to("Supernatural TV Shows", "Doğaüstü TV Programları"), TuplesKt.to("Family Time TV", "Aile Zamanı TV"), TuplesKt.to("Competition Reality TV", "Yarışma Gerçeklik TV"), TuplesKt.to("US TV Dramas", "ABD TV Dramaları"), TuplesKt.to("Amazon Originals", "Amazon Orijinal Yapımları"), TuplesKt.to("Movies We Think You'll Love", "Seveceğinizi Düşündüğümüz Filmler"), TuplesKt.to("Action and adventure movies", "Aksiyon ve Macera Filmleri"), TuplesKt.to("Comedy movies", "Komedi Filmleri"), TuplesKt.to("Drama movies", "Dram Filmleri"), TuplesKt.to("Horror movies", "Korku Filmleri"), TuplesKt.to("Kids and Family", "Çocuk ve Aile"), TuplesKt.to("TV Shows We Think You'll Love", "Seveceğinizi Düşündüğümüz Diziler"), TuplesKt.to("TV Shows", "Diziler"), TuplesKt.to("Movies", "Filmler"), TuplesKt.to("TV Dramas", "TV Dramaları"), TuplesKt.to("Made in India", "Hindistan Üretimi"), TuplesKt.to("Get In on the Action", "Aksiyona Katılın"), TuplesKt.to("Japanese Anime", "Japon Anime"), TuplesKt.to("TV Mysteries", "TV Gizemleri"), TuplesKt.to("30-Minute Laughs", "30 Dakikalık Gülüşler"), TuplesKt.to("Reality TV", "Gerçeklik TV"), TuplesKt.to("Award-Winning TV Dramas", "Ödüllü TV Dramaları"), TuplesKt.to("Award-winning Dark US TV Shows", "Ödüllü Karanlık ABD TV Programları"), TuplesKt.to("Dark Japanese TV Shows", "Karanlık Japon TV Programları"), TuplesKt.to("Critically Acclaimed US TV Dramas", "Eleştirmenler Tarafından Övülen ABD TV Dramaları"), TuplesKt.to("Award-Winning TV Shows", "Ödüllü TV Programları"), TuplesKt.to("Exciting US Crime TV Dramas", "Heyecan Verici ABD Suç TV Dramaları"), TuplesKt.to("Saturn Award Nominees", "Saturn Ödülü Adayları"), TuplesKt.to("20th-Century Period Piece Movies", "20. Yüzyıl Tarihçe Filmleri"), TuplesKt.to("International Dramas", "Uluslararası Dramalar"), TuplesKt.to("Action Comedies", "Aksiyon Komedileri"), TuplesKt.to("Sports Movies", "Spor Filmleri")});
    }

    @NotNull
    public static final ResponseParser getJSONParser() {
        return JSONParser;
    }

    @NotNull
    public static final Requests getApp() {
        return app;
    }

    public static final /* synthetic */ <T> T parseJson(String str) {
        ResponseParser jSONParser = getJSONParser();
        Intrinsics.reifiedOperationMarker(4, "T");
        return (T) jSONParser.parse(str, Reflection.getOrCreateKotlinClass(Object.class));
    }

    public static final /* synthetic */ <T> T tryParseJson(String str) {
        try {
            ResponseParser jSONParser = getJSONParser();
            Intrinsics.reifiedOperationMarker(4, "T");
            return (T) jSONParser.parseSafe(str, Reflection.getOrCreateKotlinClass(Object.class));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final int convertRuntimeToMinutes(@NotNull String runtime) {
        int minutes;
        int totalMinutes = 0;
        List<String> parts = StringsKt.split$default(runtime, new String[]{" "}, false, 0, 6, (Object) null);
        for (String part : parts) {
            if (StringsKt.endsWith$default(part, "h", false, 2, (Object) null)) {
                Integer intOrNull = StringsKt.toIntOrNull(StringsKt.trim(StringsKt.removeSuffix(part, "h")).toString());
                minutes = intOrNull != null ? intOrNull.intValue() : 0;
                totalMinutes += minutes * 60;
            } else if (StringsKt.endsWith$default(part, "m", false, 2, (Object) null)) {
                Integer intOrNull2 = StringsKt.toIntOrNull(StringsKt.trim(StringsKt.removeSuffix(part, "m")).toString());
                minutes = intOrNull2 != null ? intOrNull2.intValue() : 0;
                totalMinutes += minutes;
            }
        }
        return totalMinutes;
    }

    @NotNull
    public static final String getProviderDomain(@Nullable SharedPreferences sharedPref, @NotNull String providerName) {
        return (String) BuildersKt.runBlocking$default((CoroutineContext) null, new UtilsKt$getProviderDomain$1(sharedPref, providerName, null), 1, (Object) null);
    }

    @Nullable
    public static final Object bypass(@NotNull String mainUrl, @NotNull Continuation<? super String> continuation) {
        Object element$iv;
        String substringAfter$default;
        NetflixMirrorStorage.MirrorData saved = NetflixMirrorStorage.INSTANCE.getData();
        if ((saved.getCookie().length() > 0) && System.currentTimeMillis() - saved.getTimestamp() < 54000000) {
            return saved.getCookie();
        }
        try {
            Map headers = MapsKt.mapOf(new Pair[]{TuplesKt.to("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"), TuplesKt.to("Accept-Encoding", "gzip, deflate, br, zstd"), TuplesKt.to("Accept-Language", "en-US,en;q=0.9"), TuplesKt.to("Cache-Control", "max-age=0"), TuplesKt.to("Connection", "keep-alive"), TuplesKt.to("Content-Type", "application/x-www-form-urlencoded"), TuplesKt.to("Origin", "https://net22.cc"), TuplesKt.to("Referer", "https://net22.cc/verify2"), TuplesKt.to("sec-ch-ua", "\"Google Chrome\";v=\"147\", \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"147\""), TuplesKt.to("sec-ch-ua-mobile", "?0"), TuplesKt.to("sec-ch-ua-platform", "\"Windows\""), TuplesKt.to("Sec-Fetch-Dest", "document"), TuplesKt.to("Sec-Fetch-Mode", "navigate"), TuplesKt.to("Sec-Fetch-Site", "same-origin"), TuplesKt.to("Sec-Fetch-User", "?1"), TuplesKt.to("Upgrade-Insecure-Requests", "1"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36")});
            RequestBody build = new FormBody.Builder((Charset) null, 1, (DefaultConstructorMarker) null).add("g-recaptcha-response", UUID.randomUUID().toString()).build();
            OkHttpClient client = app.getBaseClient().newBuilder().followRedirects(false).followSslRedirects(false).build();
            Request.Builder $this$bypass_u24lambda_u240 = new Request.Builder().url("https://net52.cc/verify.php").post(build);
            for (Map.Entry element$iv2 : headers.entrySet()) {
                String key = (String) element$iv2.getKey();
                String value = (String) element$iv2.getValue();
                $this$bypass_u24lambda_u240.addHeader(key, value);
            }
            Request request = $this$bypass_u24lambda_u240.build();
            Response response = (Closeable) client.newCall(request).execute();
            try {
                Response response2 = response;
                Iterable $this$firstOrNull$iv = response2.headers("Set-Cookie");
                Iterator it = $this$firstOrNull$iv.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        element$iv = null;
                        break;
                    }
                    element$iv = it.next();
                    String it2 = (String) element$iv;
                    Response response3 = response2;
                    if (StringsKt.startsWith$default(it2, "t_hash_t=", false, 2, (Object) null)) {
                        break;
                    }
                    response2 = response3;
                }
                String str = (String) element$iv;
                String newCookie = (str == null || (substringAfter$default = StringsKt.substringAfter$default(str, "t_hash_t=", (String) null, 2, (Object) null)) == null) ? null : StringsKt.substringBefore$default(substringAfter$default, ";", (String) null, 2, (Object) null);
                if (newCookie == null) {
                    newCookie = "";
                }
                CloseableKt.closeFinally(response, (Throwable) null);
                if (newCookie.length() > 0) {
                    NetflixMirrorStorage.INSTANCE.saveData(newCookie, "", "", "");
                }
                return newCookie;
            } finally {
            }
        } catch (Exception e) {
            NetflixMirrorStorage.INSTANCE.clearData();
            throw e;
        }
    }

    @NotNull
    public static final Map<String, String> getNewTvBaseHeaders() {
        return newTvBaseHeaders;
    }

    @NotNull
    public static final List<String> getNewTvDomains() {
        return newTvDomains;
    }

    @NotNull
    public static final String decodeBase64(@NotNull String value) {
        return new String(Base64.decode(value, 0), Charsets.UTF_8);
    }

    /* JADX WARN: Can't wrap try/catch for region: R(10:35|(1:36)|37|38|39|40|41|42|43|(1:45)(6:46|15|16|(2:21|(5:23|24|25|26|27)(3:58|33|(2:56|57)(0)))|59|(0)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(10:35|36|37|38|39|40|41|42|43|(1:45)(6:46|15|16|(2:21|(5:23|24|25|26|27)(3:58|33|(2:56|57)(0)))|59|(0)(0))) */
    /* JADX WARN: Code restructure failed: missing block: B:49:0x0135, code lost:
    
        r10 = r2;
        r2 = r22;
     */
    /* JADX WARN: Code restructure failed: missing block: B:52:0x013e, code lost:
    
        r10 = r8;
        r2 = r2;
     */
    /* JADX WARN: Removed duplicated region for block: B:11:0x0033  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0112 A[Catch: Exception -> 0x012e, TRY_LEAVE, TryCatch #0 {Exception -> 0x012e, blocks: (B:16:0x00e1, B:18:0x0106, B:23:0x0112, B:26:0x011a), top: B:15:0x00e1 }] */
    /* JADX WARN: Removed duplicated region for block: B:35:0x006b  */
    /* JADX WARN: Removed duplicated region for block: B:56:0x015b  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x0129  */
    /* JADX WARN: Removed duplicated region for block: B:64:0x004e  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002b  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:46:0x00d9 -> B:15:0x00e1). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:49:0x0135 -> B:32:0x0158). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:52:0x013e -> B:32:0x0158). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:55:0x014c -> B:32:0x0158). Please report as a decompilation issue!!! */
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static final java.lang.Object resolveApiUrl(@org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super java.lang.String> r27) {
        /*
            Method dump skipped, instructions count: 366
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.UtilsKt.resolveApiUrl(kotlin.coroutines.Continuation):java.lang.Object");
    }

    public static /* synthetic */ Map buildNewTvHeaders$default(String str, Map map, int i, Object obj) {
        if ((i & 2) != 0) {
            map = MapsKt.emptyMap();
        }
        return buildNewTvHeaders(str, map);
    }

    @NotNull
    public static final Map<String, String> buildNewTvHeaders(@NotNull String ott, @NotNull Map<String, String> map) {
        Map result = MapsKt.toMutableMap(newTvBaseHeaders);
        result.put("Ott", ott);
        for (Map.Entry element$iv : map.entrySet()) {
            String key = element$iv.getKey();
            String value = element$iv.getValue();
            result.put(key, value);
        }
        return result;
    }

    @NotNull
    public static final Map<String, String> getHomePageCategories() {
        return homePageCategories;
    }

    @NotNull
    public static final String translateCategory(@NotNull String name) {
        String str = homePageCategories.get(name);
        return str == null ? name : str;
    }
}
