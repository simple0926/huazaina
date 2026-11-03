# 花在哪 · UI 方向 03「仪表盘」

> 这是第二版设计稿，由新安装的 `ui-ux-pro-max` 技能生成设计系统后落地；同样是设计稿，不改业务代码，也不替代现有 APK。

## 一句话

把账本做成一块只给一个人看的数据仪表：**白纸、墨字、一个绿点**，所有金额对齐到同一条网格线。没有衬线、没有纸纹、没有装饰线，留下的只有层级、数字和操作。

## 技能给了什么

查询：`personal finance expense tracker mobile app calm precise data` → 第一次 `--design-system` 返回了「Product Demo + Features / Glassmorphism / 深蓝 / Caveat+Quicksand」，明显是落地页模板，不适合一个离线记账 App。按技能的 Query Contract 重试：

```bash
search.py "expense tracking dashboard" --design-system --variance 2 --density 8 -p "花在哪"
```

重试结果（采用）：

| 项 | 输出 |
| --- | --- |
| 风格 | Minimalism & Swiss Style（企业工具 / 仪表盘 / 专业工具） |
| 色彩 | 背景 `#F8FAFC`、前景 `#0F172A`、主色 `#059669`、危险 `#DC2626`、弱化 `#475569` |
| 字体 | Fira Code / Fira Sans（仪表盘、数据、精确） |
| 效果 | 200–250ms 的过渡、清晰层级、快速加载；避免复杂阴影和 3D |
| 反面 | 复杂阴影、3D、低饱和、低能量 |

聚合结果里的「Pattern: Product Demo + Features」是落地页模式，不适用于 App 结构，**没有采用**；只取风格、色彩、字体和 UX 规则，再按记账 App 的信息结构落地。字体从 Google Fonts 下载 woff2 并本地内联，截图与离线打开都不依赖网络。

补充查询：
- `--domain product "personal finance budget tracker"` → Personal Finance Tracker，次选 Minimalism & Swiss Style，配色「calm blue + success green + alert red」
- `--domain chart "expense breakdown bar chart"` → 分类比较用 Bar Chart，按值降序，直接标注，不靠颜色单独编码

## 四屏

| 屏 | 主角 | 主动作 |
| --- | --- | --- |
| 01 记账 / ENTRY | 金额 32.50 | 数字 → 分类 → 记下这笔 |
| 02 明细 / LEDGER | 本月合计 | 翻月 / 筛选 / 看单笔 |
| 03 报告 / REPORT | 总支出 | 看趋势、排行、支付方式 |
| 04 数据 / DATA | 数据清单 | 导出 / 备份 / 恢复 |

## 视觉系统

- **网格**：390×844，16px 页边，4px 间距，6px 圆角，1px 边框，无阴影。
- **色彩**：背景 `#F8FAFC`，卡片 `#FFFFFF`，主文字 `#0F172A`，次文字 `#475569`，弱化 `#94A3B8`，边框 `#E2E8F0`；主色 `#059669`，浅底 `#ECFDF5`，危险 `#DC2626`。
- **字体**：Fira Sans 走界面与中文回退（中文由 PingFang 承接），Fira Code 只给金额、金额列、百分比、日期等需要对齐的数字；金额统一 `tabular-nums` + 右对齐。
- **组件**：白卡片 + 1px 边框；选中态整块绿；主按钮绿；分段控件用白底滑块；筛选 chips 用方形描边；不混用衬线或纹理。
- **数据**：分类比较用横向条形（绿），时间趋势用 31 点折线（绿 + 单点高亮），支付方式用整条分段色带；不把类别只交给颜色，旁边永远有名称和数值。

## 与方向 02 的差异

| | 02 月结单（oil-ui） | 03 仪表盘（ui-ux-pro-max） |
| --- | --- | --- |
| 隐喻 | 一份私人月结单 | 一块个人数据仪表 |
| 底色 | 暖纸 `#F6F4ED` | 冷白 `#F8FAFC` |
| 字体 | 宋体金额 + 无衬线正文 + 等宽索引 | Fira Code 数字 + Fira Sans 界面 |
| 强调 | 朱红印章、发丝线、编号、点线 | 单一翠绿、方卡片、细边框 |
| 记录 | 像账页，慢读 | 像工具，快扫 |
| 气质 | 编辑、克制、可打印 | 现代、精确、产品化 |
| 适合 | 想要「这是我的账本」的独特感 | 想要「打开就能用」的效率感 |

## 评审状态

方向 02 已做独立评审（7.5/10，问题已修）；方向 03 本轮没有另派独立评审，只做了逐屏截图自检：四屏均无横向溢出，报告页在压缩后 nav 完整可见，ledger 列表不再被汇总覆盖，设置页保留底部主按钮与 18px 页脚。

未做：空态（0 笔 / 0 元 / 无上月）、深色模式、状态栏与手势区、真实字体在 Android WebView 的回退验证。

## 证据

- 总览：`shots/board.png`
- 单屏：`shots/entry.png`、`ledger.png`、`report.png`、`settings.png`
- 源文件：`entry.html`、`ledger.html`、`report.html`、`settings.html`、`ui.css`、`icons.js`
- 字体：`fonts/*.woff2`（Fira Sans 400/500/600，Fira Code 400/500，本地内联）
