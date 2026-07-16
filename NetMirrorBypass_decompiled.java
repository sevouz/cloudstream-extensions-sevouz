package com.kraptor;

import com.lagradost.nicehttp.NiceResponse;
import java.util.List;
import java.util.Map;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.MapsKt;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.RegexOption;
import org.jetbrains.annotations.NotNull;

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
    @org.jetbrains.annotations.Nullable
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object bypass(@org.jetbrains.annotations.NotNull java.lang.String r60, @org.jetbrains.annotations.NotNull kotlin.coroutines.Continuation<? super com.kraptor.BypassResult> r61) {
        /*
            Method dump skipped, instructions count: 2520
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kraptor.NetMirrorBypass.bypass(java.lang.String, kotlin.coroutines.Continuation):java.lang.Object");
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
