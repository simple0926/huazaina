# 签名与发布

## 重要

发布用的 keystore 和密码**不在这个仓库里**，也不应该提交。  
仓库里的 `releases/*.apk` 已经用发布者的私钥签好；只想安装的话不需要构建。

## 构建自己的 APK

1. 生成密钥（只做一次，妥善备份）：

```bash
keytool -genkeypair -v \
  -keystore ~/.huazaina-release.keystore \
  -alias huazaina -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Huazaina, OU=Personal, O=Huazaina, L=Shanghai, ST=Shanghai, C=CN"
```

2. 用环境变量构建：

```bash
export KEYSTORE_FILE="$HOME/.huazaina-release.keystore"
export KEYSTORE_PASS='...'
export KEYSTORE_ALIAS='huazaina'
export KEYSTORE_KEY_PASS='...'

cd native
./build-apk.sh
```

3. WebView 版同理：

```bash
cd android
./build-apk.sh
```

## 不要提交

```text
android/huazaina-release.keystore
android/keystore.properties
*.jks
*.p12
.env
```

`.gitignore` 已覆盖这些路径。提交前跑一次：

```bash
git status --ignored
git ls-files | grep -Ei 'keystore|\.jks|\.p12|keystore\.properties'
```

## 升级

Android 只允许同包名、同签名密钥的 APK 覆盖安装。  
换密钥会导致老用户无法升级，只能卸载重装；卸载会丢记录，所以升级前务必让用户导出 JSON。
