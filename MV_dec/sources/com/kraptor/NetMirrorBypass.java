package com.kraptor;

import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.lagradost.cloudstream3.MainAPIKt;
import com.lagradost.cloudstream3.MainActivityKt;
import com.lagradost.nicehttp.NiceResponse;
import com.lagradost.nicehttp.Requests;
import com.lagradost.nicehttp.ResponseParser;
import java.util.Iterator;
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
/* loaded from: C:\Users\pradh\myprojects\clones\cloudstream-extensions-sevouz\MV_ext\classes.dex */
public final class NetMirrorBypass {

    @NotNull
    private static final String TAG = "kraptor_Netflix";

    @NotNull
    public static final NetMirrorBypass INSTANCE = new NetMirrorBypass();

    @NotNull
    private static final Map<String, String> baseHeaders = MapsKt.mapOf(new Pair[]{TuplesKt.to("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"), TuplesKt.to("Accept-Language", "en-IN,en-US;q=0.9,en;q=0.8"), TuplesKt.to("Cache-Control", "max-age=0"), TuplesKt.to("Connection", "keep-alive"), TuplesKt.to("sec-ch-ua", "\"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"144\", \"Android WebView\";v=\"144\""), TuplesKt.to("sec-ch-ua-mobile", "?0"), TuplesKt.to("sec-ch-ua-platform", "\"Android\""), TuplesKt.to("Sec-Fetch-Dest", "document"), TuplesKt.to("Sec-Fetch-Mode", "navigate"), TuplesKt.to("Sec-Fetch-Site", "same-origin"), TuplesKt.to("Sec-Fetch-User", "?1"), TuplesKt.to("Upgrade-Insecure-Requests", "1"), TuplesKt.to("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 5 Build/TQ3A.230901.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/144.0.7559.132 Safari/537.36 /OS.Gatu v3.0"), TuplesKt.to("X-Requested-With", "XMLHttpRequest")});

    private NetMirrorBypass() {
    }

    private final String extractCookie(NiceResponse response, String name) {
        Iterator it = response.getHeaders().values("set-cookie").iterator();
        while (it.hasNext()) {
            MatchResult find$default = Regex.find$default(new Regex(name + "=([^;]+)", RegexOption.IGNORE_CASE), (String) it.next(), 0, 2, (Object) null);
            if (find$default != null) {
                return (String) find$default.getGroupValues().get(1);
            }
        }
        return "";
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
    public final Object bypass(@NotNull String str, @NotNull Continuation<? super BypassResult> continuation) {
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$1;
        Object obj;
        Object obj2;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$12;
        String str7;
        Object obj3;
        String str8;
        Ref.ObjectRef objectRef;
        Ref.ObjectRef objectRef2;
        Document document;
        String html;
        String str9;
        String str10;
        String str11;
        NiceResponse niceResponse;
        Ref.ObjectRef objectRef3;
        Ref.ObjectRef objectRef4;
        String str12;
        String str13;
        String str14;
        String str15;
        String str16;
        String str17;
        String str18;
        String str19;
        String str20;
        String str21;
        String str22;
        Document document2;
        String str23;
        Requests app;
        Map<String, String> map;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$13;
        String str24;
        String str25;
        String str26;
        NiceResponse niceResponse2;
        String str27;
        List groupValues;
        List groupValues2;
        String str28;
        String str29;
        String str30;
        Document document3;
        String str31;
        String str32;
        String str33;
        Ref.ObjectRef objectRef5;
        Ref.ObjectRef objectRef6;
        String str34;
        NetMirrorBypass netMirrorBypass;
        Object obj4;
        Ref.BooleanRef booleanRef;
        Document document4;
        String str35;
        String str36;
        String str37;
        String str38;
        int i;
        int i2;
        String str39;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$14;
        Continuation<? super BypassResult> continuation2;
        Ref.ObjectRef objectRef7;
        String str40;
        NiceResponse niceResponse3;
        int i3;
        String str41;
        String str42;
        int i4;
        String str43;
        Continuation<? super BypassResult> continuation3;
        Ref.BooleanRef booleanRef2;
        int i5;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$15;
        int i6;
        Ref.ObjectRef objectRef8;
        Ref.ObjectRef objectRef9;
        String str44;
        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$16;
        String str45;
        String str46;
        Exception exc;
        Continuation<? super BypassResult> continuation4;
        Object obj5;
        String str47;
        Object post$default;
        Continuation<? super BypassResult> continuation5;
        String str48;
        Continuation<? super BypassResult> continuation6;
        Object obj6;
        NetMirrorBypass netMirrorBypass2;
        Ref.ObjectRef objectRef10;
        String str49;
        Ref.ObjectRef objectRef11;
        String str50;
        String extractCookie;
        JsonNode readTree;
        String str51;
        String asText;
        JsonNode jsonNode;
        JsonNode jsonNode2;
        JsonNode jsonNode3;
        JsonNode jsonNode4;
        String extractCookie2;
        Element selectFirst;
        String str52;
        Element selectFirst2;
        String str53;
        if (continuation instanceof NetMirrorBypass$bypass$1) {
            netMirrorBypass$bypass$1 = (NetMirrorBypass$bypass$1) continuation;
            if ((netMirrorBypass$bypass$1.label & Integer.MIN_VALUE) != 0) {
                netMirrorBypass$bypass$1.label -= Integer.MIN_VALUE;
                Object obj7 = netMirrorBypass$bypass$1.result;
                Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                switch (netMirrorBypass$bypass$1.label) {
                    case 0:
                        ResultKt.throwOnFailure(obj7);
                        String str54 = str + "/mobile/home?app=1";
                        Ref.ObjectRef objectRef12 = new Ref.ObjectRef();
                        objectRef12.element = "";
                        Ref.ObjectRef objectRef13 = new Ref.ObjectRef();
                        objectRef13.element = "";
                        Requests app2 = MainActivityKt.getApp();
                        Map<String, String> map2 = baseHeaders;
                        netMirrorBypass$bypass$1.L$0 = str;
                        netMirrorBypass$bypass$1.L$1 = str54;
                        netMirrorBypass$bypass$1.L$2 = objectRef12;
                        netMirrorBypass$bypass$1.L$3 = objectRef13;
                        netMirrorBypass$bypass$1.label = 1;
                        obj = obj7;
                        obj2 = coroutine_suspended;
                        NetMirrorBypass$bypass$1 netMirrorBypass$bypass$17 = netMirrorBypass$bypass$1;
                        str2 = TAG;
                        str3 = "";
                        str4 = "data-time";
                        str5 = "body";
                        str6 = "t_hash_t";
                        Object obj8 = Requests.get$default(app2, str54, map2, str54, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$17, 4088, (Object) null);
                        netMirrorBypass$bypass$12 = netMirrorBypass$bypass$17;
                        if (obj8 == obj2) {
                            return obj2;
                        }
                        str7 = str;
                        obj3 = obj8;
                        str8 = str54;
                        objectRef = objectRef12;
                        objectRef2 = objectRef13;
                        NiceResponse niceResponse4 = (NiceResponse) obj3;
                        objectRef.element = extractCookie(niceResponse4, str6);
                        document = niceResponse4.getDocument();
                        html = document.html();
                        if (StringsKt.contains$default(html, "We Need Support", false, 2, (Object) null) && !StringsKt.contains$default(html, "open-support", false, 2, (Object) null)) {
                            Element selectFirst3 = document.selectFirst(str5);
                            if (selectFirst3 == null || (str29 = selectFirst3.attr(str4)) == null) {
                                str29 = str3;
                            }
                            Log.d(str2, "bypass: ad-wall yok, cookie = " + ((String) objectRef.element) + " dataTime = " + str29);
                            String str55 = str3;
                            return new BypassResult((String) objectRef.element, str55, str55, str29);
                        }
                        String str56 = str3;
                        String str57 = str4;
                        String str58 = str5;
                        String str59 = str2;
                        Element selectFirst4 = document.selectFirst(str58);
                        String attr = selectFirst4 == null ? selectFirst4.attr("data-addhash") : null;
                        str9 = attr != null ? str56 : attr;
                        if (!StringsKt.isBlank(str9)) {
                            Element selectFirst5 = document.selectFirst(str58);
                            if (selectFirst5 == null || (str28 = selectFirst5.attr(str57)) == null) {
                                str28 = str56;
                            }
                            Log.d(str59, "bypass: ad-hash yok, cookie = " + ((String) objectRef.element) + " dataTime = " + str28);
                            return new BypassResult((String) objectRef.element, str56, str56, str28);
                        }
                        Log.d(str59, "bypass: addhash = " + str9);
                        MatchResult find$default = Regex.find$default(new Regex("Qury\\s*=\\s*\"([^\"]+)\""), html, 0, 2, (Object) null);
                        if (find$default != null && (groupValues2 = find$default.getGroupValues()) != null) {
                            str10 = (String) groupValues2.get(1);
                            break;
                        }
                        str10 = "ffr455";
                        MatchResult find$default2 = Regex.find$default(new Regex("Vsite2\\s*=\\s*\"([^\"]+)\""), html, 0, 2, (Object) null);
                        if (find$default2 != null && (groupValues = find$default2.getGroupValues()) != null) {
                            str11 = (String) groupValues.get(1);
                            break;
                        }
                        str11 = "userver";
                        String str60 = "https://" + str11 + ".net52.cc/?" + str10 + '=' + str9 + "&a=y&t=" + Math.random();
                        try {
                            app = MainActivityKt.getApp();
                            map = baseHeaders;
                            netMirrorBypass$bypass$12.L$0 = str7;
                            netMirrorBypass$bypass$12.L$1 = str8;
                            netMirrorBypass$bypass$12.L$2 = objectRef;
                            netMirrorBypass$bypass$12.L$3 = objectRef2;
                            netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse4);
                            netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(document);
                            netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(html);
                            netMirrorBypass$bypass$12.L$7 = str9;
                            netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(str10);
                            netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(str11);
                            netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(str60);
                            netMirrorBypass$bypass$12.label = 2;
                            Ref.ObjectRef objectRef14 = objectRef;
                            str12 = str60;
                            objectRef4 = objectRef2;
                            str14 = str8;
                            netMirrorBypass$bypass$13 = netMirrorBypass$bypass$12;
                            str15 = str58;
                            str24 = str9;
                            str16 = str57;
                            str13 = str11;
                            objectRef3 = objectRef14;
                            niceResponse = niceResponse4;
                            str25 = str10;
                            str17 = str56;
                            str18 = str59;
                            str19 = null;
                            str20 = str7;
                            try {
                                netMirrorBypass$bypass$12 = netMirrorBypass$bypass$13;
                            } catch (Exception e) {
                                e = e;
                                netMirrorBypass$bypass$12 = netMirrorBypass$bypass$13;
                                str21 = str25;
                                str22 = html;
                                document2 = document;
                                str23 = str24;
                                Log.d(str18, "bypass: ad-click hatası - " + e.getMessage());
                                str26 = str12;
                                niceResponse2 = niceResponse;
                                String str61 = str23;
                                str30 = str21;
                                document3 = document2;
                                str31 = str22;
                                str32 = str61;
                                str33 = str20;
                                objectRef5 = objectRef4;
                                objectRef6 = objectRef3;
                                netMirrorBypass$bypass$12.L$0 = str33;
                                netMirrorBypass$bypass$12.L$1 = str14;
                                netMirrorBypass$bypass$12.L$2 = objectRef6;
                                netMirrorBypass$bypass$12.L$3 = objectRef5;
                                netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                                netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(document3);
                                netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(str31);
                                netMirrorBypass$bypass$12.L$7 = str32;
                                netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(str30);
                                netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(str13);
                                netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(str26);
                                netMirrorBypass$bypass$12.label = 3;
                                if (DelayKt.delay(25000L, netMirrorBypass$bypass$12) != obj2) {
                                }
                            }
                        } catch (Exception e2) {
                            e = e2;
                            niceResponse = niceResponse4;
                            String str62 = str10;
                            objectRef3 = objectRef;
                            objectRef4 = objectRef2;
                            str12 = str60;
                            str13 = str11;
                            str14 = str8;
                            str15 = str58;
                            str16 = str57;
                            str17 = str56;
                            str18 = str59;
                            str19 = null;
                            str20 = str7;
                            str21 = str62;
                            str22 = html;
                            document2 = document;
                            str23 = str9;
                        }
                        if (Requests.get$default(app, str12, map, str14, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$13, 4088, (Object) null) == obj2) {
                            return obj2;
                        }
                        str21 = str25;
                        str26 = str12;
                        niceResponse2 = niceResponse;
                        str22 = html;
                        document2 = document;
                        str23 = str24;
                        str27 = str14;
                        str14 = str27;
                        String str63 = str23;
                        str30 = str21;
                        document3 = document2;
                        str31 = str22;
                        str32 = str63;
                        str33 = str20;
                        objectRef5 = objectRef4;
                        objectRef6 = objectRef3;
                        netMirrorBypass$bypass$12.L$0 = str33;
                        netMirrorBypass$bypass$12.L$1 = str14;
                        netMirrorBypass$bypass$12.L$2 = objectRef6;
                        netMirrorBypass$bypass$12.L$3 = objectRef5;
                        netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                        netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(document3);
                        netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(str31);
                        netMirrorBypass$bypass$12.L$7 = str32;
                        netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(str30);
                        netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(str13);
                        netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(str26);
                        netMirrorBypass$bypass$12.label = 3;
                        if (DelayKt.delay(25000L, netMirrorBypass$bypass$12) != obj2) {
                            return obj2;
                        }
                        str34 = str14;
                        netMirrorBypass = this;
                        obj4 = obj2;
                        booleanRef = new Ref.BooleanRef();
                        document4 = document3;
                        str35 = str30;
                        str36 = str32;
                        str37 = str31;
                        str38 = str26;
                        i = 10;
                        i2 = 0;
                        str39 = str33;
                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$12;
                        continuation2 = continuation;
                        objectRef7 = objectRef5;
                        str40 = str34;
                        niceResponse3 = niceResponse2;
                        if (i2 < i) {
                            i6 = i2;
                            i5 = 0;
                            if (booleanRef.element) {
                                int i7 = i;
                                Object obj9 = obj4;
                                str46 = str17;
                                str50 = str38;
                                obj5 = obj9;
                                i = i7;
                                i2++;
                                str17 = str46;
                                obj4 = obj5;
                                str38 = str50;
                                if (i2 < i) {
                                }
                            } else {
                                netMirrorBypass$bypass$14.L$0 = str39;
                                netMirrorBypass$bypass$14.L$1 = str40;
                                netMirrorBypass$bypass$14.L$2 = objectRef6;
                                netMirrorBypass$bypass$14.L$3 = objectRef7;
                                netMirrorBypass$bypass$14.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse3);
                                netMirrorBypass$bypass$14.L$5 = SpillingKt.nullOutSpilledVariable(document4);
                                netMirrorBypass$bypass$14.L$6 = SpillingKt.nullOutSpilledVariable(str37);
                                netMirrorBypass$bypass$14.L$7 = str36;
                                netMirrorBypass$bypass$14.L$8 = SpillingKt.nullOutSpilledVariable(str35);
                                netMirrorBypass$bypass$14.L$9 = SpillingKt.nullOutSpilledVariable(str13);
                                netMirrorBypass$bypass$14.L$10 = SpillingKt.nullOutSpilledVariable(str38);
                                netMirrorBypass$bypass$14.L$11 = booleanRef;
                                netMirrorBypass$bypass$14.I$0 = i;
                                netMirrorBypass$bypass$14.I$1 = i2;
                                netMirrorBypass$bypass$14.I$2 = i6;
                                netMirrorBypass$bypass$14.I$3 = 0;
                                netMirrorBypass$bypass$14.label = 4;
                                i4 = i;
                                int i8 = i2;
                                if (DelayKt.delay(2000L, netMirrorBypass$bypass$14) == obj4) {
                                    return obj4;
                                }
                                Ref.ObjectRef objectRef15 = objectRef6;
                                str44 = str39;
                                booleanRef2 = booleanRef;
                                netMirrorBypass$bypass$15 = netMirrorBypass$bypass$14;
                                objectRef8 = objectRef7;
                                objectRef9 = objectRef15;
                                continuation3 = continuation2;
                                i3 = i8;
                                str43 = str38;
                                str42 = str35;
                                str41 = str13;
                                try {
                                } catch (Exception e3) {
                                    str45 = str40;
                                    netMirrorBypass$bypass$16 = netMirrorBypass$bypass$15;
                                    coroutine_suspended = obj4;
                                    str46 = str17;
                                    booleanRef = booleanRef2;
                                    exc = e3;
                                    continuation4 = continuation3;
                                }
                                Requests app3 = MainActivityKt.getApp();
                                String str64 = str43;
                                String str65 = str44 + "/mobile/verify2.php";
                                Map mapOf = MapsKt.mapOf(TuplesKt.to("verify", str36));
                                Map<String, String> map3 = baseHeaders;
                                netMirrorBypass$bypass$15.L$0 = str44;
                                netMirrorBypass$bypass$15.L$1 = str40;
                                netMirrorBypass$bypass$15.L$2 = objectRef9;
                                netMirrorBypass$bypass$15.L$3 = objectRef8;
                                netMirrorBypass$bypass$15.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse3);
                                netMirrorBypass$bypass$15.L$5 = SpillingKt.nullOutSpilledVariable(document4);
                                netMirrorBypass$bypass$15.L$6 = SpillingKt.nullOutSpilledVariable(str37);
                                netMirrorBypass$bypass$15.L$7 = str36;
                                netMirrorBypass$bypass$15.L$8 = SpillingKt.nullOutSpilledVariable(str42);
                                netMirrorBypass$bypass$15.L$9 = SpillingKt.nullOutSpilledVariable(str41);
                                netMirrorBypass$bypass$15.L$10 = SpillingKt.nullOutSpilledVariable(str64);
                                netMirrorBypass$bypass$15.L$11 = booleanRef2;
                                netMirrorBypass$bypass$15.I$0 = i4;
                                netMirrorBypass$bypass$15.I$1 = i3;
                                netMirrorBypass$bypass$15.I$2 = i6;
                                netMirrorBypass$bypass$15.I$3 = i5;
                                netMirrorBypass$bypass$15.label = 5;
                                str45 = str40;
                                netMirrorBypass$bypass$16 = netMirrorBypass$bypass$15;
                                post$default = Requests.post$default(app3, str65, map3, str45, (Map) null, (Map) null, mapOf, (List) null, (Object) null, (RequestBody) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$16, 65496, (Object) null);
                                if (post$default != obj4) {
                                    return obj4;
                                }
                                str43 = str64;
                                booleanRef = booleanRef2;
                                coroutine_suspended = obj4;
                                continuation5 = continuation3;
                                try {
                                } catch (Exception e4) {
                                    Continuation<? super BypassResult> continuation7 = continuation5;
                                    str46 = str17;
                                    exc = e4;
                                    continuation4 = continuation7;
                                }
                                NiceResponse niceResponse5 = (NiceResponse) post$default;
                                String text = niceResponse5.getText();
                                Continuation<? super BypassResult> continuation8 = continuation5;
                                String str66 = str44;
                                Log.d(str18, "bypass: verify[" + (i6 + 1) + "/10] → " + text);
                                extractCookie = INSTANCE.extractCookie(niceResponse5, str6);
                                if (!StringsKt.isBlank(extractCookie)) {
                                    try {
                                    } catch (Exception e5) {
                                        str44 = str66;
                                        exc = e5;
                                        str46 = str17;
                                        continuation4 = continuation8;
                                        Continuation<? super BypassResult> continuation9 = continuation4;
                                        Log.d(str18, "bypass: verify hatası - " + exc.getMessage());
                                        str39 = str44;
                                        obj5 = coroutine_suspended;
                                        String str67 = str43;
                                        i2 = i3;
                                        str47 = str67;
                                        continuation2 = continuation9;
                                        objectRef6 = objectRef9;
                                        objectRef7 = objectRef8;
                                        str13 = str41;
                                        str35 = str42;
                                        str40 = str45;
                                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                                        str50 = str47;
                                        i = i4;
                                        i2++;
                                        str17 = str46;
                                        obj4 = obj5;
                                        str38 = str50;
                                        if (i2 < i) {
                                        }
                                    }
                                    objectRef9.element = extractCookie;
                                }
                                readTree = MainAPIKt.getMapper().readTree(text);
                                if (readTree == null) {
                                    try {
                                    } catch (Exception e6) {
                                        str46 = str17;
                                        str44 = str66;
                                        exc = e6;
                                        continuation4 = continuation8;
                                    }
                                    JsonNode jsonNode5 = readTree.get("statusup");
                                    if (jsonNode5 != null) {
                                        str46 = str17;
                                        try {
                                        } catch (Exception e7) {
                                            str44 = str66;
                                            exc = e7;
                                            continuation4 = continuation8;
                                            Continuation<? super BypassResult> continuation92 = continuation4;
                                            Log.d(str18, "bypass: verify hatası - " + exc.getMessage());
                                            str39 = str44;
                                            obj5 = coroutine_suspended;
                                            String str672 = str43;
                                            i2 = i3;
                                            str47 = str672;
                                            continuation2 = continuation92;
                                            objectRef6 = objectRef9;
                                            objectRef7 = objectRef8;
                                            str13 = str41;
                                            str35 = str42;
                                            str40 = str45;
                                            netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                                            str50 = str47;
                                            i = i4;
                                            i2++;
                                            str17 = str46;
                                            obj4 = obj5;
                                            str38 = str50;
                                            if (i2 < i) {
                                            }
                                        }
                                        str51 = jsonNode5.asText(str46);
                                        if (str51 == null) {
                                            str51 = str46;
                                        }
                                        obj5 = coroutine_suspended;
                                        if (StringsKt.equals(str51, "All Done", true)) {
                                            booleanRef.element = true;
                                            Log.d(str18, "bypass: All Done ✓");
                                        }
                                        if (readTree != null || (jsonNode4 = readTree.get("usertoken")) == null || (asText = jsonNode4.asText(str46)) == null) {
                                            asText = (readTree != null || (jsonNode3 = readTree.get("token")) == null) ? str19 : jsonNode3.asText(str46);
                                            if (asText == null) {
                                                asText = (readTree == null || (jsonNode2 = readTree.get("utoken")) == null) ? str19 : jsonNode2.asText(str46);
                                                if (asText == null) {
                                                    asText = (readTree == null || (jsonNode = readTree.get("user_token")) == null) ? str19 : jsonNode.asText(str46);
                                                    if (asText == null) {
                                                        asText = str46;
                                                    }
                                                }
                                            }
                                        }
                                        if (!StringsKt.isBlank(asText)) {
                                            objectRef8.element = asText;
                                        }
                                        str39 = str66;
                                        String str68 = str43;
                                        i2 = i3;
                                        str47 = str68;
                                        continuation2 = continuation8;
                                        objectRef6 = objectRef9;
                                        objectRef7 = objectRef8;
                                        str13 = str41;
                                        str35 = str42;
                                        str40 = str45;
                                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                                        str50 = str47;
                                        i = i4;
                                        i2++;
                                        str17 = str46;
                                        obj4 = obj5;
                                        str38 = str50;
                                        if (i2 < i) {
                                            Object obj10 = obj4;
                                            Requests app4 = MainActivityKt.getApp();
                                            Map plus = MapsKt.plus(baseHeaders, MapsKt.mapOf(TuplesKt.to("addhash", str36)));
                                            netMirrorBypass$bypass$14.L$0 = SpillingKt.nullOutSpilledVariable(str39);
                                            netMirrorBypass$bypass$14.L$1 = SpillingKt.nullOutSpilledVariable(str40);
                                            netMirrorBypass$bypass$14.L$2 = objectRef6;
                                            netMirrorBypass$bypass$14.L$3 = objectRef7;
                                            netMirrorBypass$bypass$14.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse3);
                                            netMirrorBypass$bypass$14.L$5 = SpillingKt.nullOutSpilledVariable(document4);
                                            netMirrorBypass$bypass$14.L$6 = SpillingKt.nullOutSpilledVariable(str37);
                                            netMirrorBypass$bypass$14.L$7 = str36;
                                            netMirrorBypass$bypass$14.L$8 = SpillingKt.nullOutSpilledVariable(str35);
                                            netMirrorBypass$bypass$14.L$9 = SpillingKt.nullOutSpilledVariable(str13);
                                            netMirrorBypass$bypass$14.L$10 = SpillingKt.nullOutSpilledVariable(str38);
                                            netMirrorBypass$bypass$14.L$11 = SpillingKt.nullOutSpilledVariable(booleanRef);
                                            netMirrorBypass$bypass$14.label = 6;
                                            str48 = str17;
                                            Ref.ObjectRef objectRef16 = objectRef6;
                                            String str69 = str36;
                                            Ref.ObjectRef objectRef17 = objectRef7;
                                            Continuation<? super BypassResult> continuation10 = continuation2;
                                            Object obj11 = Requests.get$default(app4, str40, plus, str40, (Map) null, (Map) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$14, 4088, (Object) null);
                                            if (obj11 == obj10) {
                                                return obj10;
                                            }
                                            continuation6 = continuation10;
                                            obj6 = obj11;
                                            netMirrorBypass2 = netMirrorBypass;
                                            objectRef10 = objectRef16;
                                            str49 = str69;
                                            objectRef11 = objectRef17;
                                            NiceResponse niceResponse6 = (NiceResponse) obj6;
                                            extractCookie2 = netMirrorBypass2.extractCookie(niceResponse6, str6);
                                            if (!StringsKt.isBlank(extractCookie2)) {
                                                objectRef10.element = extractCookie2;
                                            }
                                            Document document5 = niceResponse6.getDocument();
                                            selectFirst = document5.selectFirst(str15);
                                            if (selectFirst == null) {
                                                str52 = str16;
                                                String attr2 = selectFirst.attr(str52);
                                                if (attr2 != null) {
                                                    str53 = attr2;
                                                    Log.d(str18, "bypass: final cookie=" + ((String) objectRef10.element) + " usertoken=" + ((String) objectRef11.element) + " addhash=" + str49 + " dataTime=" + str53);
                                                    return new BypassResult((String) objectRef10.element, (String) objectRef11.element, str49, str53);
                                                }
                                            } else {
                                                str52 = str16;
                                            }
                                            selectFirst2 = document5.selectFirst(".body");
                                            if (selectFirst2 != null) {
                                                str19 = selectFirst2.attr(str52);
                                            }
                                            str53 = str19 != null ? str48 : str19;
                                            Log.d(str18, "bypass: final cookie=" + ((String) objectRef10.element) + " usertoken=" + ((String) objectRef11.element) + " addhash=" + str49 + " dataTime=" + str53);
                                            return new BypassResult((String) objectRef10.element, (String) objectRef11.element, str49, str53);
                                        }
                                    } else {
                                        str46 = str17;
                                    }
                                } else {
                                    str46 = str17;
                                }
                                str51 = str19;
                                if (str51 == null) {
                                }
                                obj5 = coroutine_suspended;
                                if (StringsKt.equals(str51, "All Done", true)) {
                                }
                                if (readTree != null) {
                                }
                                if (readTree != null) {
                                }
                                if (asText == null) {
                                }
                                if (!StringsKt.isBlank(asText)) {
                                }
                                str39 = str66;
                                String str682 = str43;
                                i2 = i3;
                                str47 = str682;
                                continuation2 = continuation8;
                                objectRef6 = objectRef9;
                                objectRef7 = objectRef8;
                                str13 = str41;
                                str35 = str42;
                                str40 = str45;
                                netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                                str50 = str47;
                                i = i4;
                                i2++;
                                str17 = str46;
                                obj4 = obj5;
                                str38 = str50;
                                if (i2 < i) {
                                }
                            }
                        }
                        break;
                    case 1:
                        Ref.ObjectRef objectRef18 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef objectRef19 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        str8 = (String) netMirrorBypass$bypass$1.L$1;
                        String str70 = (String) netMirrorBypass$bypass$1.L$0;
                        ResultKt.throwOnFailure(obj7);
                        objectRef2 = objectRef18;
                        obj2 = coroutine_suspended;
                        objectRef = objectRef19;
                        netMirrorBypass$bypass$12 = netMirrorBypass$bypass$1;
                        str6 = "t_hash_t";
                        obj = obj7;
                        str4 = "data-time";
                        str5 = "body";
                        str2 = TAG;
                        str3 = "";
                        str7 = str70;
                        obj3 = obj;
                        NiceResponse niceResponse42 = (NiceResponse) obj3;
                        objectRef.element = extractCookie(niceResponse42, str6);
                        document = niceResponse42.getDocument();
                        html = document.html();
                        if (StringsKt.contains$default(html, "We Need Support", false, 2, (Object) null)) {
                            break;
                        }
                        String str562 = str3;
                        String str572 = str4;
                        String str582 = str5;
                        String str592 = str2;
                        Element selectFirst42 = document.selectFirst(str582);
                        if (selectFirst42 == null) {
                        }
                        if (attr != null) {
                        }
                        if (!StringsKt.isBlank(str9)) {
                        }
                        break;
                    case 2:
                        String str71 = (String) netMirrorBypass$bypass$1.L$10;
                        String str72 = (String) netMirrorBypass$bypass$1.L$9;
                        str21 = (String) netMirrorBypass$bypass$1.L$8;
                        str23 = (String) netMirrorBypass$bypass$1.L$7;
                        str22 = (String) netMirrorBypass$bypass$1.L$6;
                        document2 = (Document) netMirrorBypass$bypass$1.L$5;
                        str26 = str71;
                        niceResponse2 = (NiceResponse) netMirrorBypass$bypass$1.L$4;
                        Ref.ObjectRef objectRef20 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef objectRef21 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        str27 = (String) netMirrorBypass$bypass$1.L$1;
                        String str73 = (String) netMirrorBypass$bypass$1.L$0;
                        try {
                            ResultKt.throwOnFailure(obj7);
                            str20 = str73;
                            obj2 = coroutine_suspended;
                            str13 = str72;
                            str16 = "data-time";
                            str15 = "body";
                            str18 = TAG;
                            str17 = "";
                            objectRef4 = objectRef20;
                            objectRef3 = objectRef21;
                            str19 = null;
                            netMirrorBypass$bypass$12 = netMirrorBypass$bypass$1;
                            obj = obj7;
                            str6 = "t_hash_t";
                            str14 = str27;
                            String str632 = str23;
                            str30 = str21;
                            document3 = document2;
                            str31 = str22;
                            str32 = str632;
                            str33 = str20;
                            objectRef5 = objectRef4;
                            objectRef6 = objectRef3;
                        } catch (Exception e8) {
                            e = e8;
                            str20 = str73;
                            obj2 = coroutine_suspended;
                            str13 = str72;
                            str16 = "data-time";
                            str15 = "body";
                            str18 = TAG;
                            str17 = "";
                            str12 = str26;
                            objectRef4 = objectRef20;
                            objectRef3 = objectRef21;
                            str19 = null;
                            netMirrorBypass$bypass$12 = netMirrorBypass$bypass$1;
                            obj = obj7;
                            str6 = "t_hash_t";
                            niceResponse = niceResponse2;
                            str14 = str27;
                            Log.d(str18, "bypass: ad-click hatası - " + e.getMessage());
                            str26 = str12;
                            niceResponse2 = niceResponse;
                            String str612 = str23;
                            str30 = str21;
                            document3 = document2;
                            str31 = str22;
                            str32 = str612;
                            str33 = str20;
                            objectRef5 = objectRef4;
                            objectRef6 = objectRef3;
                            netMirrorBypass$bypass$12.L$0 = str33;
                            netMirrorBypass$bypass$12.L$1 = str14;
                            netMirrorBypass$bypass$12.L$2 = objectRef6;
                            netMirrorBypass$bypass$12.L$3 = objectRef5;
                            netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                            netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(document3);
                            netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(str31);
                            netMirrorBypass$bypass$12.L$7 = str32;
                            netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(str30);
                            netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(str13);
                            netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(str26);
                            netMirrorBypass$bypass$12.label = 3;
                            if (DelayKt.delay(25000L, netMirrorBypass$bypass$12) != obj2) {
                            }
                        }
                        netMirrorBypass$bypass$12.L$0 = str33;
                        netMirrorBypass$bypass$12.L$1 = str14;
                        netMirrorBypass$bypass$12.L$2 = objectRef6;
                        netMirrorBypass$bypass$12.L$3 = objectRef5;
                        netMirrorBypass$bypass$12.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse2);
                        netMirrorBypass$bypass$12.L$5 = SpillingKt.nullOutSpilledVariable(document3);
                        netMirrorBypass$bypass$12.L$6 = SpillingKt.nullOutSpilledVariable(str31);
                        netMirrorBypass$bypass$12.L$7 = str32;
                        netMirrorBypass$bypass$12.L$8 = SpillingKt.nullOutSpilledVariable(str30);
                        netMirrorBypass$bypass$12.L$9 = SpillingKt.nullOutSpilledVariable(str13);
                        netMirrorBypass$bypass$12.L$10 = SpillingKt.nullOutSpilledVariable(str26);
                        netMirrorBypass$bypass$12.label = 3;
                        if (DelayKt.delay(25000L, netMirrorBypass$bypass$12) != obj2) {
                        }
                        break;
                    case 3:
                        String str74 = (String) netMirrorBypass$bypass$1.L$10;
                        String str75 = (String) netMirrorBypass$bypass$1.L$9;
                        str30 = (String) netMirrorBypass$bypass$1.L$8;
                        str32 = (String) netMirrorBypass$bypass$1.L$7;
                        str31 = (String) netMirrorBypass$bypass$1.L$6;
                        document3 = (Document) netMirrorBypass$bypass$1.L$5;
                        str26 = str74;
                        niceResponse2 = (NiceResponse) netMirrorBypass$bypass$1.L$4;
                        Ref.ObjectRef objectRef22 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef objectRef23 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        str34 = (String) netMirrorBypass$bypass$1.L$1;
                        str33 = (String) netMirrorBypass$bypass$1.L$0;
                        ResultKt.throwOnFailure(obj7);
                        obj2 = coroutine_suspended;
                        str13 = str75;
                        str16 = "data-time";
                        str15 = "body";
                        str18 = TAG;
                        str17 = "";
                        objectRef5 = objectRef22;
                        str19 = null;
                        netMirrorBypass$bypass$12 = netMirrorBypass$bypass$1;
                        obj = obj7;
                        str6 = "t_hash_t";
                        objectRef6 = objectRef23;
                        netMirrorBypass = this;
                        obj4 = obj2;
                        booleanRef = new Ref.BooleanRef();
                        document4 = document3;
                        str35 = str30;
                        str36 = str32;
                        str37 = str31;
                        str38 = str26;
                        i = 10;
                        i2 = 0;
                        str39 = str33;
                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$12;
                        continuation2 = continuation;
                        objectRef7 = objectRef5;
                        str40 = str34;
                        niceResponse3 = niceResponse2;
                        if (i2 < i) {
                        }
                        break;
                    case 4:
                        int i9 = netMirrorBypass$bypass$1.I$3;
                        int i10 = netMirrorBypass$bypass$1.I$2;
                        i3 = netMirrorBypass$bypass$1.I$1;
                        int i11 = netMirrorBypass$bypass$1.I$0;
                        Ref.BooleanRef booleanRef3 = (Ref.BooleanRef) netMirrorBypass$bypass$1.L$11;
                        String str76 = (String) netMirrorBypass$bypass$1.L$10;
                        str41 = (String) netMirrorBypass$bypass$1.L$9;
                        str42 = (String) netMirrorBypass$bypass$1.L$8;
                        String str77 = (String) netMirrorBypass$bypass$1.L$7;
                        String str78 = (String) netMirrorBypass$bypass$1.L$6;
                        Document document6 = (Document) netMirrorBypass$bypass$1.L$5;
                        NiceResponse niceResponse7 = (NiceResponse) netMirrorBypass$bypass$1.L$4;
                        Ref.ObjectRef objectRef24 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef objectRef25 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        String str79 = (String) netMirrorBypass$bypass$1.L$1;
                        String str80 = (String) netMirrorBypass$bypass$1.L$0;
                        ResultKt.throwOnFailure(obj7);
                        i4 = i11;
                        str43 = str76;
                        continuation3 = continuation;
                        netMirrorBypass = this;
                        str16 = "data-time";
                        str15 = "body";
                        str18 = TAG;
                        booleanRef2 = booleanRef3;
                        str17 = "";
                        i5 = i9;
                        str36 = str77;
                        niceResponse3 = niceResponse7;
                        netMirrorBypass$bypass$15 = netMirrorBypass$bypass$1;
                        obj4 = coroutine_suspended;
                        i6 = i10;
                        str6 = "t_hash_t";
                        objectRef8 = objectRef24;
                        objectRef9 = objectRef25;
                        str40 = str79;
                        str19 = null;
                        str37 = str78;
                        document4 = document6;
                        str44 = str80;
                        Requests app32 = MainActivityKt.getApp();
                        String str642 = str43;
                        String str652 = str44 + "/mobile/verify2.php";
                        Map mapOf2 = MapsKt.mapOf(TuplesKt.to("verify", str36));
                        Map<String, String> map32 = baseHeaders;
                        netMirrorBypass$bypass$15.L$0 = str44;
                        netMirrorBypass$bypass$15.L$1 = str40;
                        netMirrorBypass$bypass$15.L$2 = objectRef9;
                        netMirrorBypass$bypass$15.L$3 = objectRef8;
                        netMirrorBypass$bypass$15.L$4 = SpillingKt.nullOutSpilledVariable(niceResponse3);
                        netMirrorBypass$bypass$15.L$5 = SpillingKt.nullOutSpilledVariable(document4);
                        netMirrorBypass$bypass$15.L$6 = SpillingKt.nullOutSpilledVariable(str37);
                        netMirrorBypass$bypass$15.L$7 = str36;
                        netMirrorBypass$bypass$15.L$8 = SpillingKt.nullOutSpilledVariable(str42);
                        netMirrorBypass$bypass$15.L$9 = SpillingKt.nullOutSpilledVariable(str41);
                        netMirrorBypass$bypass$15.L$10 = SpillingKt.nullOutSpilledVariable(str642);
                        netMirrorBypass$bypass$15.L$11 = booleanRef2;
                        netMirrorBypass$bypass$15.I$0 = i4;
                        netMirrorBypass$bypass$15.I$1 = i3;
                        netMirrorBypass$bypass$15.I$2 = i6;
                        netMirrorBypass$bypass$15.I$3 = i5;
                        netMirrorBypass$bypass$15.label = 5;
                        str45 = str40;
                        netMirrorBypass$bypass$16 = netMirrorBypass$bypass$15;
                        post$default = Requests.post$default(app32, str652, map32, str45, (Map) null, (Map) null, mapOf2, (List) null, (Object) null, (RequestBody) null, false, 0, (TimeUnit) null, 0L, (Interceptor) null, false, (ResponseParser) null, netMirrorBypass$bypass$16, 65496, (Object) null);
                        if (post$default != obj4) {
                        }
                        break;
                    case 5:
                        int i12 = netMirrorBypass$bypass$1.I$3;
                        int i13 = netMirrorBypass$bypass$1.I$2;
                        i3 = netMirrorBypass$bypass$1.I$1;
                        int i14 = netMirrorBypass$bypass$1.I$0;
                        Ref.BooleanRef booleanRef4 = (Ref.BooleanRef) netMirrorBypass$bypass$1.L$11;
                        str43 = (String) netMirrorBypass$bypass$1.L$10;
                        str41 = (String) netMirrorBypass$bypass$1.L$9;
                        str42 = (String) netMirrorBypass$bypass$1.L$8;
                        String str81 = (String) netMirrorBypass$bypass$1.L$7;
                        String str82 = (String) netMirrorBypass$bypass$1.L$6;
                        Document document7 = (Document) netMirrorBypass$bypass$1.L$5;
                        NiceResponse niceResponse8 = (NiceResponse) netMirrorBypass$bypass$1.L$4;
                        Ref.ObjectRef objectRef26 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef objectRef27 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        String str83 = (String) netMirrorBypass$bypass$1.L$1;
                        String str84 = (String) netMirrorBypass$bypass$1.L$0;
                        try {
                            ResultKt.throwOnFailure(obj7);
                            netMirrorBypass = this;
                            netMirrorBypass$bypass$16 = netMirrorBypass$bypass$1;
                            post$default = obj7;
                            str6 = "t_hash_t";
                            str16 = "data-time";
                            str15 = "body";
                            str18 = TAG;
                            str17 = "";
                            booleanRef = booleanRef4;
                            str36 = str81;
                            niceResponse3 = niceResponse8;
                            objectRef8 = objectRef26;
                            str45 = str83;
                            str44 = str84;
                            i4 = i14;
                            str37 = str82;
                            document4 = document7;
                            continuation5 = continuation;
                            i6 = i13;
                            objectRef9 = objectRef27;
                            str19 = null;
                        } catch (Exception e9) {
                            netMirrorBypass = this;
                            netMirrorBypass$bypass$16 = netMirrorBypass$bypass$1;
                            str6 = "t_hash_t";
                            str16 = "data-time";
                            str15 = "body";
                            str18 = TAG;
                            str46 = "";
                            booleanRef = booleanRef4;
                            str36 = str81;
                            niceResponse3 = niceResponse8;
                            objectRef8 = objectRef26;
                            str45 = str83;
                            i4 = i14;
                            str37 = str82;
                            document4 = document7;
                            objectRef9 = objectRef27;
                            str19 = null;
                            str44 = str84;
                            exc = e9;
                            continuation4 = continuation;
                            Continuation<? super BypassResult> continuation922 = continuation4;
                            Log.d(str18, "bypass: verify hatası - " + exc.getMessage());
                            str39 = str44;
                            obj5 = coroutine_suspended;
                            String str6722 = str43;
                            i2 = i3;
                            str47 = str6722;
                            continuation2 = continuation922;
                            objectRef6 = objectRef9;
                            objectRef7 = objectRef8;
                            str13 = str41;
                            str35 = str42;
                            str40 = str45;
                            netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                            str50 = str47;
                            i = i4;
                            i2++;
                            str17 = str46;
                            obj4 = obj5;
                            str38 = str50;
                            if (i2 < i) {
                            }
                        }
                        NiceResponse niceResponse52 = (NiceResponse) post$default;
                        String text2 = niceResponse52.getText();
                        Continuation<? super BypassResult> continuation82 = continuation5;
                        String str662 = str44;
                        Log.d(str18, "bypass: verify[" + (i6 + 1) + "/10] → " + text2);
                        extractCookie = INSTANCE.extractCookie(niceResponse52, str6);
                        if (!StringsKt.isBlank(extractCookie)) {
                        }
                        readTree = MainAPIKt.getMapper().readTree(text2);
                        if (readTree == null) {
                        }
                        str51 = str19;
                        if (str51 == null) {
                        }
                        obj5 = coroutine_suspended;
                        if (StringsKt.equals(str51, "All Done", true)) {
                        }
                        if (readTree != null) {
                        }
                        if (readTree != null) {
                        }
                        if (asText == null) {
                        }
                        if (!StringsKt.isBlank(asText)) {
                        }
                        str39 = str662;
                        String str6822 = str43;
                        i2 = i3;
                        str47 = str6822;
                        continuation2 = continuation82;
                        objectRef6 = objectRef9;
                        objectRef7 = objectRef8;
                        str13 = str41;
                        str35 = str42;
                        str40 = str45;
                        netMirrorBypass$bypass$14 = netMirrorBypass$bypass$16;
                        str50 = str47;
                        i = i4;
                        i2++;
                        str17 = str46;
                        obj4 = obj5;
                        str38 = str50;
                        if (i2 < i) {
                        }
                        break;
                    case 6:
                        str49 = (String) netMirrorBypass$bypass$1.L$7;
                        Ref.ObjectRef objectRef28 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$3;
                        Ref.ObjectRef objectRef29 = (Ref.ObjectRef) netMirrorBypass$bypass$1.L$2;
                        ResultKt.throwOnFailure(obj7);
                        str16 = "data-time";
                        str15 = "body";
                        str48 = "";
                        objectRef11 = objectRef28;
                        str19 = null;
                        continuation6 = continuation;
                        netMirrorBypass2 = this;
                        str6 = "t_hash_t";
                        str18 = TAG;
                        objectRef10 = objectRef29;
                        obj6 = obj7;
                        NiceResponse niceResponse62 = (NiceResponse) obj6;
                        extractCookie2 = netMirrorBypass2.extractCookie(niceResponse62, str6);
                        if (!StringsKt.isBlank(extractCookie2)) {
                        }
                        Document document52 = niceResponse62.getDocument();
                        selectFirst = document52.selectFirst(str15);
                        if (selectFirst == null) {
                        }
                        selectFirst2 = document52.selectFirst(".body");
                        if (selectFirst2 != null) {
                        }
                        if (str19 != null) {
                        }
                        Log.d(str18, "bypass: final cookie=" + ((String) objectRef10.element) + " usertoken=" + ((String) objectRef11.element) + " addhash=" + str49 + " dataTime=" + str53);
                        return new BypassResult((String) objectRef10.element, (String) objectRef11.element, str49, str53);
                    default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
            }
        }
        netMirrorBypass$bypass$1 = new NetMirrorBypass$bypass$1(this, continuation);
        Object obj72 = netMirrorBypass$bypass$1.result;
        Object coroutine_suspended2 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch (netMirrorBypass$bypass$1.label) {
        }
    }
}
