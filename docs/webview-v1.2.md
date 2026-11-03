# WebView 版 v1.2（历史）

v1.2 是「花在哪」的早期 Android 版本：单文件 HTML/CSS/JS + 原生 WebView 壳，数据存在 WebView localStorage。  
v2.0.2 原生版是当前推荐版本；这份文档只保留 v1.2 的结构与迁移说明。

## 结构

```text
src/
├── app.html      页面骨架
├── app.css       样式
├── app.js        界面与交互
└── core.js       区间、聚合、CSV、JSON、示例数据
build.mjs         → dist/花在哪.html + android/assets/index.html
android/
├── AndroidManifest.xml
├── java/.../MainActivity.java   WebView 壳、MediaStore、SAF、返回键
├── res/
├── assets/index.html
└── build-apk.sh
```

## 数据

- 记录在 WebView localStorage，key `huazaina-v1`。
- 导出走 Android MediaStore，写到 `下载/花在哪/`。
- 恢复走 SAF 文件选择器。
- 卸载 / 清除应用数据会删除 localStorage；迁移前先导出 JSON。

## 迁移到原生版

1. 旧版设置 → 备份 JSON。
2. 原生版设置 → 从备份恢复。
3. 核对后再卸载旧版。

## 限制

- 是 WebView，不是 Compose / View 原生界面。
- 首次启动略慢于原生版。
- v1.2 之后不再加新功能；新功能只进原生版。
