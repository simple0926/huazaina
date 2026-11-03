# 发布到 GitHub

## 1. 创建仓库

把本目录作为仓库根目录：

```bash
git init
git add .
git commit -m "花在哪: native v2.0.2 + webview v1.2"
```

在 GitHub 新建公开空仓库 `simple0926/huazaina`（不要勾选 README / .gitignore / license），然后：

```bash
git branch -M main
git remote add origin git@github.com:simple0926/huazaina.git
git push -u origin main
```

## 2. 发布 Release

建议 tag `v2.0.2`，附上：

- `releases/huazaina-native-v2.0.2.apk`
- `releases/huazaina-webview-v1.2.0.apk`
- `releases/SHA256SUMS.txt`

Release 标题建议使用 `花在哪 v2.0.2`，说明文件使用根目录的 `RELEASE_NOTES-v2.0.2.md`。

如果已安装 GitHub CLI，可以在推送 tag 后运行：

```bash
gh release create v2.0.2 \
  releases/huazaina-native-v2.0.2.apk \
  releases/huazaina-webview-v1.2.0.apk \
  releases/SHA256SUMS.txt \
  --repo simple0926/huazaina \
  --title "花在哪 v2.0.2" \
  --notes-file RELEASE_NOTES-v2.0.2.md
```

## 3. 发布前检查

- [ ] `git ls-files` 里没有 `keystore` / `keystore.properties` / `.env`
- [ ] `releases/` 里的 APK 可以 `apksigner verify`
- [ ] README 图片路径存在
- [ ] `LICENSE` 是你想要的那一种（默认 All rights reserved；要 MIT 就替换）
- [ ] 在手机上装一次，确认桌面名和包名

## 4. 不要上传

**不要上传** `花在哪-对话与项目-*.zip` 这类旧打包：它可能包含签名密钥、密码和内部记录。  
只上传本目录的 GitHub 包。

## 5. 可选：GitHub Pages

`dist/花在哪.html` 是单文件网页版，可以复制到 `docs/index.html` 用 Pages 展示；但正式使用请安装原生 APK。
