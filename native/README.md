# 花在哪 · 原生 Android 版

真正的原生 Android 应用：**Java + Android View/Canvas + SQLite**，没有 WebView、没有 HTML 资源、没有 AndroidX/Gradle 依赖。  
包名 `com.simple0926.huazaina.nativeapp`，版本 **2.0.2**，启动器名称「花在哪 原生」。

## 为什么是新的包名

原生版和旧 WebView 版可以**同时安装**，互不覆盖：

- 旧版：`com.simple0926.huazaina` · v1.2.0 · 安装包 `../android/release/花在哪-v1.2.0.apk`
- 原生版：`com.simple0926.huazaina.nativeapp` · v2.0.0 · 安装包 `release/花在哪-原生-v2.0.2.apk`

两版数据格式兼容。迁移步骤：

1. 打开旧版 → 设置 → **备份 JSON**，保存到手机。
2. 安装并打开原生版 → 设置 → **从备份恢复** → 选择刚才的 `.json`。
3. 确认记录和自定义分类都在后，再卸载旧版。

> 直接安装原生版不会自动读取旧 WebView 的 localStorage；必须走 JSON 备份/恢复。

## 数据保护规则（v2.0.2 起）

- **同包名的版本更新不清空记录**：SQLite 数据库随 App 更新保留；`Db.onUpgrade` 只允许加字段/建表/回填，**不允许 DROP 表或 DELETE 用户记录**。
- 启动时如果本地已有记录，即使 `welcomed` 标记丢失，也只补标记，不弹欢迎、不清空。
- 只有用户在设置里明确点「清空所有数据」才会删除；没有自动清理路径。
- 从旧 WebView 版换到原生版是**一次数据迁移**，不是同包更新，需要 JSON 备份/恢复；原生版欢迎页、空列表和设置里都有「从旧版备份恢复」入口。
- - **本地安全备份**：每次保存、编辑、删除、导入、离开 App 都会自动写一份 `files/huazaina-autosave.json`；如果升级后数据库为空，启动时会提示「发现本地安全备份」并一键恢复。
- v2.0.0 → v2.0.1 → v2.0.2 已在模拟器上覆盖安装验证：原有 2 笔记录保留。
- 另做了「删库」演练：手动删除 SQLite 数据库、保留安全备份，重新启动 → 提示恢复 → 2 笔记录完整回来。

## 已实现

- **记账**：支出 / 收入切换；支出 10 类 + 收入 10 类；金额键盘、日期、备注、支付/收款方式；保存、4 秒内撤销。
- **明细**：按月、按日分组；全部/支出/收入筛选；分类筛选；搜索；当日小计（收支分开）；编辑、删除。
- **报告**：日 / 周 / 月 / 年；支出/收入两套；总收支、笔数、日均、环比、三项 KPI、折线、分类占比、支付方式、结论；导出本期 CSV。
- **设置**：数据概况、导出 CSV、备份 JSON、从备份恢复、管理支出分类、关于、清空数据。
- **数据**：SQLite 本地库；CSV 带 BOM、金额不带千分位；JSON 与旧版同 schema；MediaStore 写入 `下载/花在哪/`；SAF 选择恢复文件。
- **设计**：松墨纸色、宋体金额、发丝线、编号 + 点线、01/02/03 底部 tab；图标沿用 A 账页。
- **权限**：仅 `VIBRATE`；没有 `INTERNET`。

## 恢复旧版记录

1. 如果「花在哪」（旧 WebView 版）还在桌面/应用列表里，先打开它 → 设置 → **备份 JSON**；不要先卸载。
2. 原生版 → 设置 → **从备份恢复**，选这个 `.json`。
3. 旧版已经卸载时，先在文件管理器/下载里找 `下载/花在哪/花在哪-备份-*.json`；有就导入。
4. 两者都没有时，Android 卸载会删除旧 App 的 localStorage，原生版无法读取别的 App 的私有数据；只能看 Google/厂商云备份里有没有旧 App 的备份。

## 构建

```bash
cd huazaina/native
./build-apk.sh
# → release/花在哪-原生-v2.0.0.apk
```

脚本使用 `/tmp/android-build` 的 JDK 17 + Android SDK 34（aapt2 / javac / d8 / apksigner），不依赖 Gradle。若 `/tmp` 被清理，需要重新安装 JDK/SDK，或修改 `JAVA_HOME` / `ANDROID_SDK_ROOT`。

## 源码

```
native/
├── AndroidManifest.xml
├── res/                      colors/styles/strings、33 个矢量图标、自适应启动图标
├── java/.../nativeapp/
│   ├── MainActivity.java     四个屏幕、弹层、导入导出
│   ├── Core.java             区间/聚合/CSV/JSON/示例数据
│   ├── Db.java               SQLite（records/cats/meta）
│   ├── Ui.java               色板、字体、间距、drawable 辅助
│   ├── KeypadView.java       数字键盘
│   ├── LineChartView.java    报告折线
│   └── Widgets.java         点线、分类条
├── build-apk.sh
└── release/
```

## 验证

- v2.0.2 覆盖安装后记录保留；删库后从安全备份恢复 2 笔成功。

- v2.0.0 → v2.0.1 覆盖安装，模拟器上原有记录保留，未弹欢迎、未清空。

在 **API 29 arm64 模拟器**（Android 10）上实际安装并走通：

- 首次欢迎页、开始记账；
- 记一笔支出、保存、撤销；
- 收支切换、收入「工资 8000」录入；
- 明细列表、全部/收入筛选、收入绿色 `+`；
- 支出/收入报告、KPI、折线、分类与支付方式；
- 设置、编辑记录、CSV/JSON 导出（文件实际写入 `Download/花在哪/`）。

模拟器用 `wm size/density` 覆盖分辨率时，`adb shell input tap` 的坐标会出现偏移，因此触摸键盘的部分测试用硬件按键完成；**真机触摸键盘尚未实际验证**。如果你在真机上发现键盘点击位置不对，把机型和系统版本告诉我。

## 还没做 / 已知边界

- 收入分类固定 10 类；自定义收入分类未做。
- 没有深色模式；始终纸色浅色。
- 没有小组件、通知、云同步。
- 原生版图标暂用 A「账页」；B/C 图标未应用。
- 未在真机跑过；已用模拟器验证主要流程。
