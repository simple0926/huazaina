# Releases

- `huazaina-native-v2.0.2.apk` — 原生 Android 版，Android 10+，包名 `com.simple0926.huazaina.nativeapp`
- `huazaina-webview-v1.2.0.apk` — WebView 版，Android 10+，包名 `com.simple0926.huazaina`
- `SHA256SUMS.txt` — 校验和

```bash
shasum -a 256 -c SHA256SUMS.txt
```

两个 APK 均只申请 `VIBRATE`，没有 `INTERNET`。旧版数据迁移见根目录 README。

版本更新内容、升级提醒与已知限制见 [`RELEASE_NOTES-v2.0.2.md`](../RELEASE_NOTES-v2.0.2.md)。
