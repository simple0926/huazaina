# 花在哪 · final logo / identity 01

用 `ui-ux-pro-max` 的设计系统重做，不再开第四套三个方向：  
上一轮三版里 C（问号）最成立，这次把它收敛成可落地的一套。

## UI/UX Pro Max 给到的约束

- `--design-system "personal finance ledger private paper minimal"` 第一轮返回的是蓝底 + Caveat 手写体，与“纸账 / 私密 / 克制”不符，**未采用**。
- 改用显式域检索后，真正匹配的是：
  - **E-Ink / Paper**：暖纸、墨色、高对比、无渐变、print-friendly、慢而安静。
  - **Notes & Writing App** 的暖纸/墨色底子。
  - **Chinese Traditional** 的宋体 + 黑体搭配（落地为 Songti SC / Noto Serif SC + Noto Sans SC）。
  - **Minimalism & Swiss** 的网格、单一强调色、无多余装饰。
- 唯一没有照搬的是强调色：数据库建议琥珀色，会把账本变成咖啡馆；保留 App 已经建立的**松墨绿 #2F5A43**，只给方点。
- 对比度：墨/纸约 14:1；松墨/纸约 6:1；不使用渐变、阴影、发光。

## 最终标记

- **问号**：名字本身就是问题。
- **账线**：90° / 45° 折线，来自账页横线与折角；没有一根多余曲线。
- **方点**：一枚只属于你的章；松墨绿只给它。
- **纸**：16 px、单色、反白都成立；不靠纹理和渐变。

## 资产

| 文件 | 用途 |
| --- | --- |
| `symbol.svg` / `symbol.png` | 符号；实心路径，无描边 |
| `symbol-mono.svg` | 单色符号 |
| `lockup-horizontal.svg` / `.png` | 横版锁合（主用） |
| `lockup-stacked.svg` / `.png` | 竖版锁合 |
| `lockup-mono.svg` / `.png` | 单色锁合 |
| `lockup-reversed.svg` / `.png` | 反白锁合 |
| `app-icon.svg` / `.png` | 纸色 App icon（主用） |
| `app-icon-ink.svg` / `.png` | 墨色 App icon（深色桌面） |
| `wordmark.svg` | 字标；Songti SC Regular 已转曲 |
| `brand-sheet.png` | 一页 brand sheet |

## 技术状态

- 字标：**Songti SC Regular**，已用 fontTools 转成路径；不依赖字体。
- 符号：实心多边形，无描边；miter 接头已手工修正。
- App icon：1024 px 圆角方形 mock；Android 自适应图标（foreground/background、mask、单色图标）还没导出，等定稿。
- 16 px / 32 px / 64 px 已检查；单色、反白已检查。
- 还没接进 App；等确认后替换原生版 / WebView 版图标、启动页、设置页 About。

## 下一步

1. 你确认这版（或指出要改的比例 / 方点 / 问号角度）。
2. 我出 Android adaptive icon + legacy mipmap、favicon、启动页、字标资源。
3. 替换两个 App 的当前图标，并保留旧图标备份。
