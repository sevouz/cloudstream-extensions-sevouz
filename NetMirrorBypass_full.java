package com.kraptor;

import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.ResultKt;
import kotlin.TuplesKt;
import kotlin.collections.MapsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.SpillingKt;
import kotlin.jvm.internal.Ref;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.RegexOption;
import kotlin.text.StringsKt;
import kotlinx.coroutines.DelayKt;
import okhttp3.Interceptor;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/* compiled from: NetMirrorBypass.kt */
@Metadata(d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0016\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0005H\u0086@¢\u0006\u0002\u0010\u000bJ\u0018\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0005H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00050\u0007X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0010"}, d2 = {"Lcom/kraptor/NetMirrorBypass;", "", "<init>", "()V", "TAG", "", "baseHeaders", "", "bypass", "Lcom/kraptor/BypassResult;", "baseUrl", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractCookie", "response", "Lcom/lagradost/nicehttp/NiceResponse;", "name", "MirrorVerse"}, k = 1, mv = {2, 3, 0}, xi = 48)
/* loaded from: C:\temp\mv2\classes.dex */
public final class NetMirrorBypass {

    @NotNull
    private static final String TAG = "kraptor_Netflix";

    @NotNull
    public static final NetMirrorBypass INSTANCE = new NetMirrorBypass();

    @NotNull
    private static final Map<String, String> baseHeaders = MapsKt.mapOf(new Pair[]{TuplesKt.to("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"), TuplesKt.to("Accept-Language", "en-IN,en-US;q=0.9,en;q=0.8"), TuplesKt.to("Cache-Control", "max-age=0"), TuplesKt.to("Connection", "keep-alive"), TuplesKt.to("sec-ch-ua", "\"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"144\", \"Android WebView\";v=\"144\""), TuplesKt.to("sec-ch-ua-mobile", "?0"), TuplesKt.to("sec-ch-ua-platform", "\"Android\""), TuplesKt.to("Sec-Fetch-Dest", "document"), TuplesKt.to("Sec-Fetch-Mode", "navigate"), TuplesKt.to("Sec-Fetch-Site", "same-origin"), TuplesKt.to("Sec-Fetch-User", "?1"), TuplesKt.to("Upgrade-Insecure-Requests", "1"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 5 Build/TQ3A.230901.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/144.0.7559.132 Safari/537.36 /OS.Gatu v3.0"), TuplesKt.to("X-Requested-With", "XMLHttpRequest")});

    private NetMirrorBypass() {
    }

    /* JADX WARN: Can't wrap try/catch for region: R(10:77|(1:78)|79|80|81|82|83|84|85|(1:87)(30:88|33|34|35|36|37|38|39|40|(2:135|136)|42|(4:44|45|46|(17:48|49|50|(1:52)|53|54|55|56|57|(1:59)|(2:(1:122)(1:107)|(2:(1:121)(1:112)|(2:(1:120)(1:117)|(1:119))))|65|(1:67)|68|69|70|(2:100|(1:102)(10:103|12|(1:14)|15|(2:17|(3:19|20|21))(1:28)|22|(1:24)|(1:26)(1:27)|20|21))(0))(1:129))(1:134)|130|(0)|53|54|55|56|57|(0)|(1:61)|(1:105)|122|(0)|65|(0)|68|69|70|(0)(0))) */
    /* JADX WARN: Can't wrap try/catch for region: R(30:88|33|34|35|36|37|38|39|40|(2:135|136)|42|(4:44|45|46|(17:48|49|50|(1:52)|53|54|55|56|57|(1:59)|(2:(1:122)(1:107)|(2:(1:121)(1:112)|(2:(1:120)(1:117)|(1:119))))|65|(1:67)|68|69|70|(2:100|(1:102)(10:103|12|(1:14)|15|(2:17|(3:19|20|21))(1:28)|22|(1:24)|(1:26)(1:27)|20|21))(0))(1:129))(1:134)|130|(0)|53|54|55|56|57|(0)|(1:61)|(1:105)|122|(0)|65|(0)|68|69|70|(0)(0)) */
    /* JADX WARN: Code restructure failed: missing block: B:124:0x07e8, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:125:0x07e9, code lost:
    
        r4 = r61;
        r2 = r0;
        r5 = r24;
        r0 = r60;
     */
    /* JADX WARN: Code restructure failed: missing block: B:141:0x07fc, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:142:0x07fd, code lost:
    
        r9 = r37;
        r4 = r61;
        r2 = r0;
        r0 = r60;
     */
    /* JADX WARN: Code restructure failed: missing block: B:144:0x0807, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:145:0x0808, code lost:
    
        r9 = r37;
        r2 = r0;
        r0 = r60;
     */
    /* JADX WARN: Code restructure failed: missing block: B:199:0x0437, code lost:
    
        if (r1 == null) goto L65;
     */
    /* JADX WARN: Code restructure failed: missing block: B:205:0x0460, code lost:
    
        if (r9 == null) goto L74;
     */
    /* JADX WARN: Code restructure failed: missing block: B:90:0x081f, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:91:0x0820, code lost:
    
        r5 = r9;
        r9 = r37;
        r11 = r61;
        r8 = r2;
        r2 = r0;
        r0 = r60;
     */
    /* JADX WARN: Code restructure failed: missing block: B:94:0x082a, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:95:0x082b, code lost:
    
        r41 = r5;
        r56 = r8;
        r5 = r9;
        r9 = r37;
        r11 = r61;
        r8 = r2;
        r2 = r0;
        r0 = r60;
     */
    /* JADX WARN: Removed duplicated region for block: B:100:0x0891  */
    /* JADX WARN: Removed duplicated region for block: B:105:0x0791 A[Catch: Exception -> 0x07e8, TryCatch #11 {Exception -> 0x07e8, blocks: (B:57:0x0772, B:59:0x0778, B:61:0x0781, B:63:0x0789, B:65:0x07c9, B:67:0x07d3, B:105:0x0791, B:107:0x0799, B:110:0x07a4, B:112:0x07ac, B:115:0x07b7, B:117:0x07bf), top: B:56:0x0772 }] */
    /* JADX WARN: Removed duplicated region for block: B:109:0x07a2  */
    /* JADX WARN: Removed duplicated region for block: B:11:0x003c  */
    /* JADX WARN: Removed duplicated region for block: B:134:0x0760  */
    /* JADX WARN: Removed duplicated region for block: B:135:0x071f A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0949  */
    /* JADX WARN: Removed duplicated region for block: B:152:0x0138  */
    /* JADX WARN: Removed duplicated region for block: B:153:0x01b4  */
    /* JADX WARN: Removed duplicated region for block: B:155:0x0206  */
    /* JADX WARN: Removed duplicated region for block: B:162:0x05c9 A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:163:0x05ca  */
    /* JADX WARN: Removed duplicated region for block: B:168:0x027a  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x0957  */
    /* JADX WARN: Removed duplicated region for block: B:182:0x03ad  */
    /* JADX WARN: Removed duplicated region for block: B:184:0x03b7  */
    /* JADX WARN: Removed duplicated region for block: B:187:0x03c3  */
    /* JADX WARN: Removed duplicated region for block: B:194:0x03fc  */
    /* JADX WARN: Removed duplicated region for block: B:226:0x03b9  */
    /* JADX WARN: Removed duplicated region for block: B:227:0x03b4  */
    /* JADX WARN: Removed duplicated region for block: B:228:0x02a5  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x096c  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0974  */
    /* JADX WARN: Removed duplicated region for block: B:27:0x0977  */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0962  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x0097  */
    /* JADX WARN: Removed duplicated region for block: B:44:0x0736  */
    /* JADX WARN: Removed duplicated region for block: B:52:0x076a  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x0778 A[Catch: Exception -> 0x07e8, TryCatch #11 {Exception -> 0x07e8, blocks: (B:57:0x0772, B:59:0x0778, B:61:0x0781, B:63:0x0789, B:65:0x07c9, B:67:0x07d3, B:105:0x0791, B:107:0x0799, B:110:0x07a4, B:112:0x07ac, B:115:0x07b7, B:117:0x07bf), top: B:56:0x0772 }] */
    /* JADX WARN: Removed duplicated region for block: B:61:0x0781 A[Catch: Exception -> 0x07e8, TryCatch #11 {Exception -> 0x07e8, blocks: (B:57:0x0772, B:59:0x0778, B:61:0x0781, B:63:0x0789, B:65:0x07c9, B:67:0x07d3, B:105:0x0791, B:107:0x0799, B:110:0x07a4, B:112:0x07ac, B:115:0x07b7, B:117:0x07bf), top: B:56:0x0772 }] */
    /* JADX WARN: Removed duplicated region for block: B:67:0x07d3 A[Catch: Exception -> 0x07e8, TRY_LEAVE, TryCatch #11 {Exception -> 0x07e8, blocks: (B:57:0x0772, B:59:0x0778, B:61:0x0781, B:63:0x0789, B:65:0x07c9, B:67:0x07d3, B:105:0x0791, B:107:0x0799, B:110:0x07a4, B:112:0x07ac, B:115:0x07b7, B:117:0x07bf), top: B:56:0x0772 }] */
    /* JADX WARN: Removed duplicated region for block: B:72:0x05f0  */
    /* JADX WARN: Removed duplicated region for block: B:87:0x06dd A[RETURN] */
    /* JADX WARN: Removed duplicated region for block: B:88:0x06de  */
    /* JADX WARN: Removed duplicated region for block: B:8:0x0034  */
    /* JADX WARN: Unreachable blocks removed: 2, instructions: 5 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:88:0x06de -> B:33:0x06e4). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:92:0x0847 -> B:69:0x0877). Please report as a decompilation issue!!! */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:99:0x087a -> B:70:0x0885). Please report as a decompilation issue!!! */
    @Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final Object bypass(@NotNull String baseUrl, @NotNull Continuation<? super BypassResult> continuation) {
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$1;
        Object $result;
        Object obj;
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$12;
        String baseUrl2;
        Object obj2;
        String homeUrl;
        Ref.ObjectRef cookie;
        Ref.ObjectRef usertoken;
        Document doc;
        String html;
        String addhash;
        String qury;
        String vsite;
        NiceResponse initial;
        Ref.ObjectRef cookie2;
        Ref.ObjectRef usertoken2;
        String adClickUrl;
        String vsite2;
        String homeUrl2;
        String str6;
        String str7;
        String str8;
        String qury2;
        String str9;
        String baseUrl3;
        String homeUrl3;
        String html2;
        Document doc2;
        String addhash2;
        Requests app;
        Map<String, String> map;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$13;
        String addhash3;
        String qury3;
        String adClickUrl2;
        NiceResponse initial2;
        String homeUrl4;
        List groupValues;
        List groupValues2;
        String dt;
        String dt2;
        String qury4;
        Document doc3;
        String html3;
        String addhash4;
        String baseUrl4;
        Ref.ObjectRef usertoken3;
        Ref.ObjectRef cookie3;
        String homeUrl5;
        NetMirrorBypass netMirrorBypass;
        Object obj3;
        Ref.BooleanRef verified;
        Document doc4;
        String qury5;
        String addhash5;
        String homeUrl6;
        String adClickUrl3;
        int i;
        int i2;
        String t;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$14;
        Continuation $completion;
        Ref.ObjectRef cookie4;
        String homeUrl7;
        NiceResponse initial3;
        int i3;
        String vsite3;
        String qury6;
        int i4;
        String adClickUrl4;
        Continuation $completion2;
        Ref.BooleanRef verified2;
        int i5;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$15;
        int i6;
        Ref.ObjectRef usertoken4;
        Ref.ObjectRef cookie5;
        String baseUrl5;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$16;
        String homeUrl8;
        String body;
        Exception e;
        Continuation $completion3;
        Object obj4;
        String adClickUrl5;
        Object post$default;
        Continuation $completion4;
        String str10;
        Continuation $completion5;
        Object obj5;
        NetMirrorBypass netMirrorBypass2;
        Ref.ObjectRef cookie6;
        String addhash6;
        Ref.ObjectRef usertoken5;
        String adClickUrl6;
        String c;
        JsonNode node;
        String status;
        String t2;
        JsonNode jsonNode;
        JsonNode jsonNode2;
        JsonNode jsonNode3;
        JsonNode jsonNode4;
        String finalCookie;
        Element selectFirst;
        String str11;
        Element selectFirst2;
        String dataTime;
        if (continuation instanceof NetMirrorBypass$bypass$1) {
            netMirrorBypass$bypass$1 = (NetMirrorBypass$bypass$1) continuation;
            if ((netMirrorBypass$bypass$1.label & Integer.MIN_VALUE) != 0) {
                netMirrorBypass$bypass$1.label -= Integer.MIN_VALUE;
                Object $result2 = netMirrorBypass$bypass$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (netMirrorBypass$bypass$1.label) {
                    case 0:
                        ResultKt.throwOnFailure($result2);
                        String homeUrl9 = baseUrl + "/mobile/home?app=1";
                        Ref.ObjectRef cookie7 = new Ref.ObjectRef();
                        cookie7.element = "";
                        Ref.ObjectRef usertoken6 = new Ref.ObjectRef();
                        usertoken6.element = "";
                        Requests app2 = MainActivityKt.getApp();
                        Map<String, String> map2 = baseHeaders;
                        netMirrorBypass$bypass$1.L$0 = baseUrl;
                        netMirrorBypass$bypass$1.L$1 = homeUrl9;
                        netMirrorBypass$bypass$1.L$2 = cookie7;
                        netMirrorBypass$bypass$1.L$3 = usertoken6;
                        netMirrorBypass$bypass$1.label = 1;
                        $result = $result2;
                        obj = coroutine_suspended;
                        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$17 = netMirrorBypass$bypass$1;
                        str = TAG;
                        str2 = "";
                        str3 = "data-time";
                        str4 = "body";
                        str5 = "t_hash_t";
                        Object obj6 = Requests.get$default(app2, homeUrl9, map2, homeUrl9, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$17, 4088, (Object) null);
                        netMirrorBypass$bypass$12 = netMirrorBypass$bypass$17;
                        if (obj6 == obj) {
                            return obj;
                        }
                        baseUrl2 = baseUrl;
                        obj2 = obj6;
                        homeUrl = homeUrl9;
                        cookie = cookie7;
                        usertoken = usertoken6;
                        NiceResponse initial4 = (NiceResponse) obj2;
                        cookie.element = extractCookie(initial4, str5);
                        doc = initial4.getDocument();
                        html = doc.html();
                        if (StringsKt.contains$default(html, "We Need Support", false, 2, (Object) null) && !StringsKt.contains$default(html, "open-support", false, 2, (Object) null)) {
                            Element selectFirst3 = doc.selectFirst(str4);
                            if (selectFirst3 == null || (dt2 = selectFirst3.attr(str3)) == null) {
                                dt2 = str2;
                            }
                            Log.d(str, "bypass: ad-wall yok, cookie = " + ((String) cookie.element) + " dataTime = " + dt2);
                            String str12 = str2;
                            return new BypassResult((String) cookie.element, str12, str12, dt2);
                        }
                        String str13 = str2;
                        String str14 = str3;
                        String str15 = str4;
                        String str16 = str;
                        Element selectFirst4 = doc.selectFirst(str15);
                        String attr = selectFirst4 == null ? selectFirst4.attr("data-addhash") : null;
                        addhash = attr != null ? str13 : attr;
                        if (!StringsKt.isBlank(addhash)) {
                            Element selectFirst5 = doc.selectFirst(str15);
                            if (selectFirst5 == null || (dt = selectFirst5.attr(str14)) == null) {
                                dt = str13;
                            }
                            Log.d(str16, "bypass: ad-hash yok, cookie = " + ((String) cookie.element) + " dataTime = " + dt);
                            return new BypassResult((String) cookie.element, str13, str13, dt);
                        }
                        Log.d(str16, "bypass: addhash = " + addhash);
                        MatchResult find$default = Regex.find$default(new Regex("Qury\\s*=\\s*\"([^\"]+)\""), html, 0, 2, (Object) null);
                        if (find$default != null && (groupValues2 = find$default.getGroupValues()) != null) {
                            qury = (String) groupValues2.get(1);
                            break;
                        }
                        qury = "ffr455";
                        MatchResult find$default2 = Regex.find$default(new Regex("Vsite2\\s*=\\s*\"([^\"]+)\""), html, 0, 2, (Object) null);
                        if (find$default2 != null && (groupValues = find$default2.getGroupValues()) != null) {
                            vsite = (String) groupValues.get(1);
                            break;
                        }
                        vsite = "userver";
                        String adClickUrl7 = "https://" + vsite + ".net52.cc/?" + qury + '=' + addhash + "&a=y&t=" + Math.random();
                        try {
                            app = MainActivityKt.getApp();
                            map = baseHeaders;
                            netMirrorBypass$bypass$12.L$0 = baseUrl2;
                            netMirrorBypass$bypass$12.L$1 = homeUrl;
                            netMirrorBypass$bypass$12.L$2 = cookie;
                            netMirrorBypass$bypass$12.L$3 = usertoken;
                            netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(initial4);
                            netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(doc);
                            netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(html);
                            netMirrorBypass$bypass$12.L$7 = addhash;
                            netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(qury);
                            netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(vsite);
                            netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl7);
                            netMirrorBypass$bypass$12.label = 2;
                            Ref.ObjectRef cookie8 = cookie;
                            adClickUrl = adClickUrl7;
                            usertoken2 = usertoken;
                            homeUrl2 = homeUrl;
                            netMirrorBypass$bypass$13 = netMirrorBypass$bypass$12;
                            str6 = str15;
                            addhash3 = addhash;
                            str7 = str14;
                            vsite2 = vsite;
                            cookie2 = cookie8;
                            initial = initial4;
                            qury3 = qury;
                            str8 = str13;
                            qury2 = str16;
                            str9 = null;
                            baseUrl3 = baseUrl2;
                            try {
                                netMirrorBypass$bypass$12 = netMirrorBypass$bypass$13;
                            } catch (Exception e2) {
                                e = e2;
                                netMirrorBypass$bypass$12 = netMirrorBypass$bypass$13;
                                homeUrl3 = qury3;
                                html2 = html;
                                doc2 = doc;
                                addhash2 = addhash3;
                                Log.d(qury2, "bypass: ad-click hatası - " + e.getMessage());
                                adClickUrl2 = adClickUrl;
                                initial2 = initial;
                                String str17 = addhash2;
                                qury4 = homeUrl3;
                                doc3 = doc2;
                                html3 = html2;
                                addhash4 = str17;
                                baseUrl4 = baseUrl3;
                                usertoken3 = usertoken2;
                                cookie3 = cookie2;
                                netMirrorBypass$bypass$12.L$0 = baseUrl4;
                                netMirrorBypass$bypass$12.L$1 = homeUrl2;
                                netMirrorBypass$bypass$12.L$2 = cookie3;
                                netMirrorBypass$bypass$12.L$3 = usertoken3;
                                netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(initial2);
                                netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(doc3);
                                netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(html3);
                                netMirrorBypass$bypass$12.L$7 = addhash4;
                                netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(qury4);
                                netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(vsite2);
                                netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl2);
                                netMirrorBypass$bypass$12.label = 3;
                                if (DelayKt.delay(25000L, netMirrorBypass$bypass$12) != obj) {
                                }
                            }
                        } catch (Exception e3) {
                            e = e3;
                            initial = initial4;
                            String str18 = qury;
                            cookie2 = cookie;
                            usertoken2 = usertoken;
                            adClickUrl = adClickUrl7;
                            vsite2 = vsite;
                            homeUrl2 = homeUrl;
                            str6 = str15;
                            str7 = str14;
                            str8 = str13;
                            qury2 = str16;
                            str9 = null;
                            baseUrl3 = baseUrl2;
                            homeUrl3 = str18;
                            html2 = html;
                            doc2 = doc;
                            addhash2 = addhash;
                        }
                        if (Requests.get$default(app, adClickUrl, map, homeUrl2, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$13, 4088, (Object) null) == obj) {
                            return obj;
                        }
                        homeUrl3 = qury3;
                        adClickUrl2 = adClickUrl;
                        initial2 = initial;
                        html2 = html;
                        doc2 = doc;
                        addhash2 = addhash3;
                        homeUrl4 = homeUrl2;
                        homeUrl2 = homeUrl4;
                        String str19 = addhash2;
                        qury4 = homeUrl3;
                        doc3 = doc2;
                        html3 = html2;
                        addhash4 = str19;
                        baseUrl4 = baseUrl3;
                        usertoken3 = usertoken2;
                        cookie3 = cookie2;
                        netMirrorBypass$bypass$12.L$0 = baseUrl4;
                        netMirrorBypass$bypass$12.L$1 = homeUrl2;
                        netMirrorBypass$bypass$12.L$2 = cookie3;
                        netMirrorBypass$bypass$12.L$3 = usertoken3;
                        netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(initial2);
                        netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(doc3);
                        netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(html3);
                        netMirrorBypass$bypass$12.L$7 = addhash4;
                        netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(qury4);
                        netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(vsite2);
                        netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl2);
                        netMirrorBypass$bypass$12.label = 3;
                        if (DelayKt.delay(25000L, netMirrorBypass$bypass$12) != obj) {
                            return obj;
                        }
                        homeUrl5 = homeUrl2;
                        Ref.BooleanRef verified3 = new Ref.BooleanRef();
                        netMirrorBypass = this;
                        obj3 = obj;
                        verified = verified3;
                        doc4 = doc3;
                        qury5 = qury4;
                        addhash5 = addhash4;
                        homeUrl6 = html3;
                        adClickUrl3 = adClickUrl2;
                        i = 10;
                        i2 = 0;
                        t = baseUrl4;
                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$12;
                        $completion = continuation;
                        cookie4 = usertoken3;
                        homeUrl7 = homeUrl5;
                        initial3 = initial2;
                        if (i2 < i) {
                            i6 = i2;
                            i5 = 0;
                            if (verified.element) {
                                int i7 = i;
                                Object obj7 = obj3;
                                body = str8;
                                adClickUrl6 = adClickUrl3;
                                obj4 = obj7;
                                i = i7;
                                i2++;
                                str8 = body;
                                obj3 = obj4;
                                adClickUrl3 = adClickUrl6;
                                if (i2 < i) {
                                }
                            } else {
                                netMirrorBypass$bypass$14.L$0 = t;
                                netMirrorBypass$bypass$14.L$1 = homeUrl7;
                                netMirrorBypass$bypass$14.L$2 = cookie3;
                                netMirrorBypass$bypass$14.L$3 = cookie4;
                                netMirrorBypass$bypass$14.L$4 = SpillingKt.nullOutSpilledVariable(initial3);
                                netMirrorBypass$bypass$14.L$5 = SpillingKt.nullOutSpilledVariable(doc4);
                                netMirrorBypass$bypass$14.L$6 = SpillingKt.nullOutSpilledVariable(homeUrl6);
                                netMirrorBypass$bypass$14.L$7 = addhash5;
                                netMirrorBypass$bypass$14.L$8 = SpillingKt.nullOutSpilledVariable(qury5);
                                netMirrorBypass$bypass$14.L$9 = SpillingKt.nullOutSpilledVariable(vsite2);
                                netMirrorBypass$bypass$14.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl3);
                                netMirrorBypass$bypass$14.L$11 = verified;
                                netMirrorBypass$bypass$14.I$0 = i;
                                netMirrorBypass$bypass$14.I$1 = i2;
                                netMirrorBypass$bypass$14.I$2 = i6;
                                netMirrorBypass$bypass$14.I$3 = 0;
                                netMirrorBypass$bypass$14.label = 4;
                                i4 = i;
                                int i8 = i2;
                                if (DelayKt.delay(2000L, netMirrorBypass$bypass$14) == obj3) {
                                    return obj3;
                                }
                                Ref.ObjectRef objectRef = cookie3;
                                baseUrl5 = t;
                                verified2 = verified;
                                netMirrorBypass$bypass$15 = netMirrorBypass$bypass$14;
                                usertoken4 = cookie4;
                                cookie5 = objectRef;
                                $completion2 = $completion;
                                i3 = i8;
                                adClickUrl4 = adClickUrl3;
                                qury6 = qury5;
                                vsite3 = vsite2;
                                try {
                                } catch (Exception e4) {
                                    homeUrl8 = homeUrl7;
                                    netMirrorBypass$bypass$16 = netMirrorBypass$bypass$15;
                                    coroutine_suspended = obj3;
                                    body = str8;
                                    verified = verified2;
                                    e = e4;
                                    $completion3 = $completion2;
                                }
                                Requests app3 = MainActivityKt.getApp();
                                String adClickUrl8 = adClickUrl4;
                                String str20 = baseUrl5 + "/mobile/verify2.php";
                                Map mapOf = MapsKt.mapOf(TuplesKt.to("verify", addhash5));
                                Map<String, String> map3 = baseHeaders;
                                netMirrorBypass$bypass$15.L$0 = baseUrl5;
                                netMirrorBypass$bypass$15.L$1 = homeUrl7;
                                netMirrorBypass$bypass$15.L$2 = cookie5;
                                netMirrorBypass$bypass$15.L$3 = usertoken4;
                                netMirrorBypass$bypass$15.L$4 = SpillingKt.nullOutSpilledVariable(initial3);
                                netMirrorBypass$bypass$15.L$5 = SpillingKt.nullOutSpilledVariable(doc4);
                                netMirrorBypass$bypass$15.L$6 = SpillingKt.nullOutSpilledVariable(homeUrl6);
                                netMirrorBypass$bypass$15.L$7 = addhash5;
                                netMirrorBypass$bypass$15.L$8 = SpillingKt.nullOutSpilledVariable(qury6);
                                netMirrorBypass$bypass$15.L$9 = SpillingKt.nullOutSpilledVariable(vsite3);
                                netMirrorBypass$bypass$15.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl8);
                                netMirrorBypass$bypass$15.L$11 = verified2;
                                netMirrorBypass$bypass$15.I$0 = i4;
                                netMirrorBypass$bypass$15.I$1 = i3;
                                netMirrorBypass$bypass$15.I$2 = i6;
                                netMirrorBypass$bypass$15.I$3 = i5;
                                netMirrorBypass$bypass$15.label = 5;
                                homeUrl8 = homeUrl7;
                                netMirrorBypass$bypass$16 = netMirrorBypass$bypass$15;
                                post$default = Requests.post$default(app3, str20, map3, homeUrl8, (Map) null, (Map) null, mapOf, (List) null, (Object) null, (RequestBody) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$16, 65496, (Object) null);
                                if (post$default != obj3) {
                                    return obj3;
                                }
                                adClickUrl4 = adClickUrl8;
                                verified = verified2;
                                coroutine_suspended = obj3;
                                $completion4 = $completion2;
                                try {
                                } catch (Exception e5) {
                                    Continuation continuation2 = $completion4;
                                    body = str8;
                                    e = e5;
                                    $completion3 = continuation2;
                                }
                                NiceResponse resp = (NiceResponse) post$default;
                                String body2 = resp.getText();
                                Continuation $completion6 = $completion4;
                                String baseUrl6 = baseUrl5;
                                Log.d(qury2, "bypass: verify[" + (i6 + 1) + "/10] → " + body2);
                                c = INSTANCE.extractCookie(resp, str5);
                                if (!StringsKt.isBlank(c)) {
                                    try {
                                    } catch (Exception e6) {
                                        baseUrl5 = baseUrl6;
                                        e = e6;
                                        body = str8;
                                        $completion3 = $completion6;
                                        Continuation $completion7 = $completion3;
                                        Log.d(qury2, "bypass: verify hatası - " + e.getMessage());
                                        t = baseUrl5;
                                        obj4 = coroutine_suspended;
                                        String str21 = adClickUrl4;
                                        i2 = i3;
                                        adClickUrl5 = str21;
                                        $completion = $completion7;
                                        cookie3 = cookie5;
                                        cookie4 = usertoken4;
                                        vsite2 = vsite3;
                                        qury5 = qury6;
                                        homeUrl7 = homeUrl8;
                                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                                        adClickUrl6 = adClickUrl5;
                                        i = i4;
                                        i2++;
                                        str8 = body;
                                        obj3 = obj4;
                                        adClickUrl3 = adClickUrl6;
                                        if (i2 < i) {
                                        }
                                    }
                                    cookie5.element = c;
                                }
                                node = MainAPIKt.getMapper().readTree(body2);
                                if (node == null) {
                                    try {
                                    } catch (Exception e7) {
                                        body = str8;
                                        baseUrl5 = baseUrl6;
                                        e = e7;
                                        $completion3 = $completion6;
                                    }
                                    JsonNode jsonNode5 = node.get("statusup");
                                    if (jsonNode5 != null) {
                                        body = str8;
                                        try {
                                        } catch (Exception e8) {
                                            baseUrl5 = baseUrl6;
                                            e = e8;
                                            $completion3 = $completion6;
                                            Continuation $completion72 = $completion3;
                                            Log.d(qury2, "bypass: verify hatası - " + e.getMessage());
                                            t = baseUrl5;
                                            obj4 = coroutine_suspended;
                                            String str212 = adClickUrl4;
                                            i2 = i3;
                                            adClickUrl5 = str212;
                                            $completion = $completion72;
                                            cookie3 = cookie5;
                                            cookie4 = usertoken4;
                                            vsite2 = vsite3;
                                            qury5 = qury6;
                                            homeUrl7 = homeUrl8;
                                            netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                                            adClickUrl6 = adClickUrl5;
                                            i = i4;
                                            i2++;
                                            str8 = body;
                                            obj3 = obj4;
                                            adClickUrl3 = adClickUrl6;
                                            if (i2 < i) {
                                            }
                                        }
                                        status = jsonNode5.asText(body);
                                        if (status == null) {
                                            status = body;
                                        }
                                        obj4 = coroutine_suspended;
                                        if (StringsKt.equals(status, "All Done", true)) {
                                            verified.element = true;
                                            Log.d(qury2, "bypass: All Done ✓");
                                        }
                                        if (node != null || (jsonNode4 = node.get("usertoken")) == null || (t2 = jsonNode4.asText(body)) == null) {
                                            t2 = (node != null || (jsonNode3 = node.get("token")) == null) ? str9 : jsonNode3.asText(body);
                                            if (t2 == null) {
                                                t2 = (node == null || (jsonNode2 = node.get("utoken")) == null) ? str9 : jsonNode2.asText(body);
                                                if (t2 == null) {
                                                    t2 = (node == null || (jsonNode = node.get("user_token")) == null) ? str9 : jsonNode.asText(body);
                                                    if (t2 == null) {
                                                        t2 = body;
                                                    }
                                                }
                                            }
                                        }
                                        if (!StringsKt.isBlank(t2)) {
                                            usertoken4.element = t2;
                                        }
                                        t = baseUrl6;
                                        String str22 = adClickUrl4;
                                        i2 = i3;
                                        adClickUrl5 = str22;
                                        $completion = $completion6;
                                        cookie3 = cookie5;
                                        cookie4 = usertoken4;
                                        vsite2 = vsite3;
                                        qury5 = qury6;
                                        homeUrl7 = homeUrl8;
                                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                                        adClickUrl6 = adClickUrl5;
                                        i = i4;
                                        i2++;
                                        str8 = body;
                                        obj3 = obj4;
                                        adClickUrl3 = adClickUrl6;
                                        if (i2 < i) {
                                            Object obj8 = obj3;
                                            Requests app4 = MainActivityKt.getApp();
                                            Map plus = MapsKt.plus(baseHeaders, MapsKt.mapOf(TuplesKt.to("addhash", addhash5)));
                                            netMirrorBypass$bypass$14.L$0 = SpillingKt.nullOutSpilledVariable(t);
                                            netMirrorBypass$bypass$14.L$1 = SpillingKt.nullOutSpilledVariable(homeUrl7);
                                            netMirrorBypass$bypass$14.L$2 = cookie3;
                                            netMirrorBypass$bypass$14.L$3 = cookie4;
                                            netMirrorBypass$bypass$14.L$4 = SpillingKt.nullOutSpilledVariable(initial3);
                                            netMirrorBypass$bypass$14.L$5 = SpillingKt.nullOutSpilledVariable(doc4);
                                            netMirrorBypass$bypass$14.L$6 = SpillingKt.nullOutSpilledVariable(homeUrl6);
                                            netMirrorBypass$bypass$14.L$7 = addhash5;
                                            netMirrorBypass$bypass$14.L$8 = SpillingKt.nullOutSpilledVariable(qury5);
                                            netMirrorBypass$bypass$14.L$9 = SpillingKt.nullOutSpilledVariable(vsite2);
                                            netMirrorBypass$bypass$14.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl3);
                                            netMirrorBypass$bypass$14.L$11 = SpillingKt.nullOutSpilledVariable(verified);
                                            netMirrorBypass$bypass$14.label = 6;
                                            str10 = str8;
                                            Ref.ObjectRef cookie9 = cookie3;
                                            String addhash7 = addhash5;
                                            Ref.ObjectRef usertoken7 = cookie4;
                                            Continuation $completion8 = $completion;
                                            Object obj9 = Requests.get$default(app4, homeUrl7, plus, homeUrl7, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$14, 4088, (Object) null);
                                            if (obj9 == obj8) {
                                                return obj8;
                                            }
                                            $completion5 = $completion8;
                                            obj5 = obj9;
                                            netMirrorBypass2 = netMirrorBypass;
                                            cookie6 = cookie9;
                                            addhash6 = addhash7;
                                            usertoken5 = usertoken7;
                                            NiceResponse finalResp = (NiceResponse) obj5;
                                            finalCookie = netMirrorBypass2.extractCookie(finalResp, str5);
                                            if (!StringsKt.isBlank(finalCookie)) {
                                                cookie6.element = finalCookie;
                                            }
                                            Document finalDoc = finalResp.getDocument();
                                            selectFirst = finalDoc.selectFirst(str6);
                                            if (selectFirst == null) {
                                                str11 = str7;
                                                String attr2 = selectFirst.attr(str11);
                                                if (attr2 != null) {
                                                    dataTime = attr2;
                                                    Log.d(qury2, "bypass: final cookie=" + ((String) cookie6.element) + " usertoken=" + ((String) usertoken5.element) + " addhash=" + addhash6 + " dataTime=" + dataTime);
                                                    return new BypassResult((String) cookie6.element, (String) usertoken5.element, addhash6, dataTime);
                                                }
                                            } else {
                                                str11 = str7;
                                            }
                                            selectFirst2 = finalDoc.selectFirst(".body");
                                            if (selectFirst2 != null) {
                                                str9 = selectFirst2.attr(str11);
                                            }
                                            dataTime = str9 != null ? str10 : str9;
                                            Log.d(qury2, "bypass: final cookie=" + ((String) cookie6.element) + " usertoken=" + ((String) usertoken5.element) + " addhash=" + addhash6 + " dataTime=" + dataTime);
                                            return new BypassResult((String) cookie6.element, (String) usertoken5.element, addhash6, dataTime);
                                        }
                                    } else {
                                        body = str8;
                                    }
                                } else {
                                    body = str8;
                                }
                                status = str9;
                                if (status == null) {
                                }
                                obj4 = coroutine_suspended;
                                if (StringsKt.equals(status, "All Done", true)) {
                                }
                                if (node != null) {
                                }
                                if (node != null) {
                                }
                                if (t2 == null) {
                                }
                                if (!StringsKt.isBlank(t2)) {
                                }
                                t = baseUrl6;
                                String str222 = adClickUrl4;
                                i2 = i3;
                                adClickUrl5 = str222;
                                $completion = $completion6;
                                cookie3 = cookie5;
                                cookie4 = usertoken4;
                                vsite2 = vsite3;
                                qury5 = qury6;
                                homeUrl7 = homeUrl8;
                                netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                                adClickUrl6 = adClickUrl5;
                                i = i4;
                                i2++;
                                str8 = body;
                                obj3 = obj4;
                                adClickUrl3 = adClickUrl6;
                                if (i2 < i) {
                                }
                            }
                        }
                        break;
                    case 1:
                        Ref.ObjectRef usertoken8 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef cookie10 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        homeUrl = (String) netMirrorBypass$bypass$1.L$1;
                        String baseUrl7 = (String) netMirrorBypass$bypass$1.L$0;
                        ResultKt.throwOnFailure($result2);
                        usertoken = usertoken8;
                        obj = coroutine_suspended;
                        cookie = cookie10;
                        netMirrorBypass$bypass$12 = netMirrorBypass$bypass$1;
                        str5 = "t_hash_t";
                        $result = $result2;
                        str3 = "data-time";
                        str4 = "body";
                        str = TAG;
                        str2 = "";
                        baseUrl2 = baseUrl7;
                        obj2 = $result;
                        NiceResponse initial42 = (NiceResponse) obj2;
                        cookie.element = extractCookie(initial42, str5);
                        doc = initial42.getDocument();
                        html = doc.html();
                        if (StringsKt.contains$default(html, "We Need Support", false, 2, (Object) null)) {
                            break;
                        }
                        String str132 = str2;
                        String str142 = str3;
                        String str152 = str4;
                        String str162 = str;
                        Element selectFirst42 = doc.selectFirst(str152);
                        if (selectFirst42 == null) {
                        }
                        if (attr != null) {
                        }
                        if (!StringsKt.isBlank(addhash)) {
                        }
                        break;
                    case 2:
                        String adClickUrl9 = (String) netMirrorBypass$bypass$1.L$10;
                        String vsite4 = (String) netMirrorBypass$bypass$1.L$9;
                        homeUrl3 = (String) netMirrorBypass$bypass$1.L$8;
                        addhash2 = (String) netMirrorBypass$bypass$1.L$7;
                        html2 = (String) netMirrorBypass$bypass$1.L$6;
                        doc2 = (Document) netMirrorBypass$bypass$1.L$5;
                        adClickUrl2 = adClickUrl9;
                        initial2 = (NiceResponse) netMirrorBypass$bypass$1.L$4;
                        Ref.ObjectRef usertoken9 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef cookie11 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        homeUrl4 = (String) netMirrorBypass$bypass$1.L$1;
                        String baseUrl8 = (String) netMirrorBypass$bypass$1.L$0;
                        try {
                            ResultKt.throwOnFailure($result2);
                            baseUrl3 = baseUrl8;
                            obj = coroutine_suspended;
                            vsite2 = vsite4;
                            str7 = "data-time";
                            str6 = "body";
                            qury2 = TAG;
                            str8 = "";
                            usertoken2 = usertoken9;
                            cookie2 = cookie11;
                            str9 = null;
                            netMirrorBypass$bypass$12 = netMirrorBypass$bypass$1;
                            $result = $result2;
                            str5 = "t_hash_t";
                            homeUrl2 = homeUrl4;
                            String str192 = addhash2;
                            qury4 = homeUrl3;
                            doc3 = doc2;
                            html3 = html2;
                            addhash4 = str192;
                            baseUrl4 = baseUrl3;
                            usertoken3 = usertoken2;
                            cookie3 = cookie2;
                        } catch (Exception e9) {
                            e = e9;
                            baseUrl3 = baseUrl8;
                            obj = coroutine_suspended;
                            vsite2 = vsite4;
                            str7 = "data-time";
                            str6 = "body";
                            qury2 = TAG;
                            str8 = "";
                            adClickUrl = adClickUrl2;
                            usertoken2 = usertoken9;
                            cookie2 = cookie11;
                            str9 = null;
                            netMirrorBypass$bypass$12 = netMirrorBypass$bypass$1;
                            $result = $result2;
                            str5 = "t_hash_t";
                            initial = initial2;
                            homeUrl2 = homeUrl4;
                            Log.d(qury2, "bypass: ad-click hatası - " + e.getMessage());
                            adClickUrl2 = adClickUrl;
                            initial2 = initial;
                            String str172 = addhash2;
                            qury4 = homeUrl3;
                            doc3 = doc2;
                            html3 = html2;
                            addhash4 = str172;
                            baseUrl4 = baseUrl3;
                            usertoken3 = usertoken2;
                            cookie3 = cookie2;
                            netMirrorBypass$bypass$12.L$0 = baseUrl4;
                            netMirrorBypass$bypass$12.L$1 = homeUrl2;
                            netMirrorBypass$bypass$12.L$2 = cookie3;
                            netMirrorBypass$bypass$12.L$3 = usertoken3;
                            netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(initial2);
                            netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(doc3);
                            netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(html3);
                            netMirrorBypass$bypass$12.L$7 = addhash4;
                            netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(qury4);
                            netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(vsite2);
                            netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl2);
                            netMirrorBypass$bypass$12.label = 3;
                            if (DelayKt.delay(25000L, netMirrorBypass$bypass$12) != obj) {
                            }
                        }
                        netMirrorBypass$bypass$12.L$0 = baseUrl4;
                        netMirrorBypass$bypass$12.L$1 = homeUrl2;
                        netMirrorBypass$bypass$12.L$2 = cookie3;
                        netMirrorBypass$bypass$12.L$3 = usertoken3;
                        netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(initial2);
                        netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(doc3);
                        netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(html3);
                        netMirrorBypass$bypass$12.L$7 = addhash4;
                        netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(qury4);
                        netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(vsite2);
                        netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl2);
                        netMirrorBypass$bypass$12.label = 3;
                        if (DelayKt.delay(25000L, netMirrorBypass$bypass$12) != obj) {
                        }
                        break;
                    case 3:
                        String adClickUrl10 = (String) netMirrorBypass$bypass$1.L$10;
                        String vsite5 = (String) netMirrorBypass$bypass$1.L$9;
                        qury4 = (String) netMirrorBypass$bypass$1.L$8;
                        addhash4 = (String) netMirrorBypass$bypass$1.L$7;
                        html3 = (String) netMirrorBypass$bypass$1.L$6;
                        doc3 = (Document) netMirrorBypass$bypass$1.L$5;
                        adClickUrl2 = adClickUrl10;
                        initial2 = (NiceResponse) netMirrorBypass$bypass$1.L$4;
                        Ref.ObjectRef usertoken10 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef cookie12 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        homeUrl5 = (String) netMirrorBypass$bypass$1.L$1;
                        baseUrl4 = (String) netMirrorBypass$bypass$1.L$0;
                        ResultKt.throwOnFailure($result2);
                        obj = coroutine_suspended;
                        vsite2 = vsite5;
                        str7 = "data-time";
                        str6 = "body";
                        qury2 = TAG;
                        str8 = "";
                        usertoken3 = usertoken10;
                        str9 = null;
                        netMirrorBypass$bypass$12 = netMirrorBypass$bypass$1;
                        $result = $result2;
                        str5 = "t_hash_t";
                        cookie3 = cookie12;
                        Ref.BooleanRef verified32 = new Ref.BooleanRef();
                        netMirrorBypass = this;
                        obj3 = obj;
                        verified = verified32;
                        doc4 = doc3;
                        qury5 = qury4;
                        addhash5 = addhash4;
                        homeUrl6 = html3;
                        adClickUrl3 = adClickUrl2;
                        i = 10;
                        i2 = 0;
                        t = baseUrl4;
                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$12;
                        $completion = continuation;
                        cookie4 = usertoken3;
                        homeUrl7 = homeUrl5;
                        initial3 = initial2;
                        if (i2 < i) {
                        }
                        break;
                    case 4:
                        int i9 = netMirrorBypass$bypass$1.I$3;
                        int i10 = netMirrorBypass$bypass$1.I$2;
                        i3 = netMirrorBypass$bypass$1.I$1;
                        int i11 = netMirrorBypass$bypass$1.I$0;
                        Ref.BooleanRef verified4 = (Ref.BooleanRef) netMirrorBypass$bypass$1.L$11;
                        String adClickUrl11 = (String) netMirrorBypass$bypass$1.L$10;
                        vsite3 = (String) netMirrorBypass$bypass$1.L$9;
                        qury6 = (String) netMirrorBypass$bypass$1.L$8;
                        String addhash8 = (String) netMirrorBypass$bypass$1.L$7;
                        String html4 = (String) netMirrorBypass$bypass$1.L$6;
                        Document doc5 = (Document) netMirrorBypass$bypass$1.L$5;
                        NiceResponse initial5 = (NiceResponse) netMirrorBypass$bypass$1.L$4;
                        Ref.ObjectRef usertoken11 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef cookie13 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        String homeUrl10 = (String) netMirrorBypass$bypass$1.L$1;
                        String baseUrl9 = (String) netMirrorBypass$bypass$1.L$0;
                        ResultKt.throwOnFailure($result2);
                        i4 = i11;
                        adClickUrl4 = adClickUrl11;
                        $completion2 = continuation;
                        netMirrorBypass = this;
                        str7 = "data-time";
                        str6 = "body";
                        qury2 = TAG;
                        verified2 = verified4;
                        str8 = "";
                        i5 = i9;
                        addhash5 = addhash8;
                        initial3 = initial5;
                        netMirrorBypass$bypass$15 = netMirrorBypass$bypass$1;
                        obj3 = coroutine_suspended;
                        i6 = i10;
                        str5 = "t_hash_t";
                        usertoken4 = usertoken11;
                        cookie5 = cookie13;
                        homeUrl7 = homeUrl10;
                        str9 = null;
                        homeUrl6 = html4;
                        doc4 = doc5;
                        baseUrl5 = baseUrl9;
                        Requests app32 = MainActivityKt.getApp();
                        String adClickUrl82 = adClickUrl4;
                        String str202 = baseUrl5 + "/mobile/verify2.php";
                        Map mapOf2 = MapsKt.mapOf(TuplesKt.to("verify", addhash5));
                        Map<String, String> map32 = baseHeaders;
                        netMirrorBypass$bypass$15.L$0 = baseUrl5;
                        netMirrorBypass$bypass$15.L$1 = homeUrl7;
                        netMirrorBypass$bypass$15.L$2 = cookie5;
                        netMirrorBypass$bypass$15.L$3 = usertoken4;
                        netMirrorBypass$bypass$15.L$4 = SpillingKt.nullOutSpilledVariable(initial3);
                        netMirrorBypass$bypass$15.L$5 = SpillingKt.nullOutSpilledVariable(doc4);
                        netMirrorBypass$bypass$15.L$6 = SpillingKt.nullOutSpilledVariable(homeUrl6);
                        netMirrorBypass$bypass$15.L$7 = addhash5;
                        netMirrorBypass$bypass$15.L$8 = SpillingKt.nullOutSpilledVariable(qury6);
                        netMirrorBypass$bypass$15.L$9 = SpillingKt.nullOutSpilledVariable(vsite3);
                        netMirrorBypass$bypass$15.L$10 = SpillingKt.nullOutSpilledVariable(adClickUrl82);
                        netMirrorBypass$bypass$15.L$11 = verified2;
                        netMirrorBypass$bypass$15.I$0 = i4;
                        netMirrorBypass$bypass$15.I$1 = i3;
                        netMirrorBypass$bypass$15.I$2 = i6;
                        netMirrorBypass$bypass$15.I$3 = i5;
                        netMirrorBypass$bypass$15.label = 5;
                        homeUrl8 = homeUrl7;
                        netMirrorBypass$bypass$16 = netMirrorBypass$bypass$15;
                        post$default = Requests.post$default(app32, str202, map32, homeUrl8, (Map) null, (Map) null, mapOf2, (List) null, (Object) null, (RequestBody) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$16, 65496, (Object) null);
                        if (post$default != obj3) {
                        }
                        break;
                    case 5:
                        int i12 = netMirrorBypass$bypass$1.I$3;
                        int i13 = netMirrorBypass$bypass$1.I$2;
                        i3 = netMirrorBypass$bypass$1.I$1;
                        int i14 = netMirrorBypass$bypass$1.I$0;
                        Ref.BooleanRef verified5 = (Ref.BooleanRef) netMirrorBypass$bypass$1.L$11;
                        adClickUrl4 = (String) netMirrorBypass$bypass$1.L$10;
                        vsite3 = (String) netMirrorBypass$bypass$1.L$9;
                        qury6 = (String) netMirrorBypass$bypass$1.L$8;
                        String addhash9 = (String) netMirrorBypass$bypass$1.L$7;
                        String html5 = (String) netMirrorBypass$bypass$1.L$6;
                        Document doc6 = (Document) netMirrorBypass$bypass$1.L$5;
                        NiceResponse initial6 = (NiceResponse) netMirrorBypass$bypass$1.L$4;
                        Ref.ObjectRef usertoken12 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef cookie14 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        String homeUrl11 = (String) netMirrorBypass$bypass$1.L$1;
                        String baseUrl10 = (String) netMirrorBypass$bypass$1.L$0;
                        try {
                            ResultKt.throwOnFailure($result2);
                            netMirrorBypass = this;
                            netMirrorBypass$bypass$16 = netMirrorBypass$bypass$1;
                            post$default = $result2;
                            str5 = "t_hash_t";
                            str7 = "data-time";
                            str6 = "body";
                            qury2 = TAG;
                            str8 = "";
                            verified = verified5;
                            addhash5 = addhash9;
                            initial3 = initial6;
                            usertoken4 = usertoken12;
                            homeUrl8 = homeUrl11;
                            baseUrl5 = baseUrl10;
                            i4 = i14;
                            homeUrl6 = html5;
                            doc4 = doc6;
                            $completion4 = continuation;
                            i6 = i13;
                            cookie5 = cookie14;
                            str9 = null;
                        } catch (Exception e10) {
                            netMirrorBypass = this;
                            netMirrorBypass$bypass$16 = netMirrorBypass$bypass$1;
                            str5 = "t_hash_t";
                            str7 = "data-time";
                            str6 = "body";
                            qury2 = TAG;
                            body = "";
                            verified = verified5;
                            addhash5 = addhash9;
                            initial3 = initial6;
                            usertoken4 = usertoken12;
                            homeUrl8 = homeUrl11;
                            i4 = i14;
                            homeUrl6 = html5;
                            doc4 = doc6;
                            cookie5 = cookie14;
                            str9 = null;
                            baseUrl5 = baseUrl10;
                            e = e10;
                            $completion3 = continuation;
                            Continuation $completion722 = $completion3;
                            Log.d(qury2, "bypass: verify hatası - " + e.getMessage());
                            t = baseUrl5;
                            obj4 = coroutine_suspended;
                            String str2122 = adClickUrl4;
                            i2 = i3;
                            adClickUrl5 = str2122;
                            $completion = $completion722;
                            cookie3 = cookie5;
                            cookie4 = usertoken4;
                            vsite2 = vsite3;
                            qury5 = qury6;
                            homeUrl7 = homeUrl8;
                            netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                            adClickUrl6 = adClickUrl5;
                            i = i4;
                            i2++;
                            str8 = body;
                            obj3 = obj4;
                            adClickUrl3 = adClickUrl6;
                            if (i2 < i) {
                            }
                        }
                        NiceResponse resp2 = (NiceResponse) post$default;
                        String body22 = resp2.getText();
                        Continuation $completion62 = $completion4;
                        String baseUrl62 = baseUrl5;
                        Log.d(qury2, "bypass: verify[" + (i6 + 1) + "/10] → " + body22);
                        c = INSTANCE.extractCookie(resp2, str5);
                        if (!StringsKt.isBlank(c)) {
                        }
                        node = MainAPIKt.getMapper().readTree(body22);
                        if (node == null) {
                        }
                        status = str9;
                        if (status == null) {
                        }
                        obj4 = coroutine_suspended;
                        if (StringsKt.equals(status, "All Done", true)) {
                        }
                        if (node != null) {
                        }
                        if (node != null) {
                        }
                        if (t2 == null) {
                        }
                        if (!StringsKt.isBlank(t2)) {
                        }
                        t = baseUrl62;
                        String str2222 = adClickUrl4;
                        i2 = i3;
                        adClickUrl5 = str2222;
                        $completion = $completion62;
                        cookie3 = cookie5;
                        cookie4 = usertoken4;
                        vsite2 = vsite3;
                        qury5 = qury6;
                        homeUrl7 = homeUrl8;
                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                        adClickUrl6 = adClickUrl5;
                        i = i4;
                        i2++;
                        str8 = body;
                        obj3 = obj4;
                        adClickUrl3 = adClickUrl6;
                        if (i2 < i) {
                        }
                        break;
                    case 6:
                        addhash6 = (String) netMirrorBypass$bypass$1.L$7;
                        Ref.ObjectRef usertoken13 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef cookie15 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        ResultKt.throwOnFailure($result2);
                        str7 = "data-time";
                        str6 = "body";
                        str10 = "";
                        usertoken5 = usertoken13;
                        str9 = null;
                        $completion5 = continuation;
                        netMirrorBypass2 = this;
                        str5 = "t_hash_t";
                        qury2 = TAG;
                        cookie6 = cookie15;
                        obj5 = $result2;
                        NiceResponse finalResp2 = (NiceResponse) obj5;
                        finalCookie = netMirrorBypass2.extractCookie(finalResp2, str5);
                        if (!StringsKt.isBlank(finalCookie)) {
                        }
                        Document finalDoc2 = finalResp2.getDocument();
                        selectFirst = finalDoc2.selectFirst(str6);
                        if (selectFirst == null) {
                        }
                        selectFirst2 = finalDoc2.selectFirst(".body");
                        if (selectFirst2 != null) {
                        }
                        if (str9 != null) {
                        }
                        Log.d(qury2, "bypass: final cookie=" + ((String) cookie6.element) + " usertoken=" + ((String) usertoken5.element) + " addhash=" + addhash6 + " dataTime=" + dataTime);
                        return new BypassResult((String) cookie6.element, (String) usertoken5.element, addhash6, dataTime);
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        netMirrorBypass$bypass$1 = new NetMirrorBypass$bypass$1(this, continuation);
        Object $result22 = netMirrorBypass$bypass$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (netMirrorBypass$bypass$1.label) {
        }
    }

    private final String extractCookie(NiceResponse response, String name) {
        List<String> setCookies = response.getHeaders().values("set-cookie");
        for (String sc : setCookies) {
            MatchResult m = Regex.find$default(new Regex(name + "=([^;]+)", RegexOption.IGNORE_CASE), sc, 0, 2, (Object) null);
            if (m != null) {
                return (String) m.getGroupValues().get(1);
            }
        }
        return "";
    }
}
