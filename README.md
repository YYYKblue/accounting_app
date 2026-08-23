# 简账（Accounting App）

一款面向个人自用的 Android 本地记账应用，当前目标设备为红米 K70 / 小米澎湃 OS。

## 当前功能

- 手动记录、编辑、删除收入和支出
- 按月查看收入、支出、结余与账单明细
- 按分类统计月度支出
- 通过 Android `NotificationListenerService` 事件驱动地识别支付通知
- 支付宝、微信和常见银行通知使用独立解析器
- 通知指纹去重，自动账单保留原始匹配文本并提示人工核对
- 纯本地 Room 数据库，无登录、云端、广告和数据上传
- 通知使用权、电池无限制、小米自启动设置引导

## 技术栈

- Kotlin 2.3.20
- Android Gradle Plugin 8.13.2 / Gradle 8.13
- Jetpack Compose（2025.10 stable BOM）+ Material 3
- Room 2.8.4
- Coroutines / Flow
- minSdk 26，targetSdk / compileSdk 36

## 构建

1. 安装 Android Studio，并安装 Android SDK Platform 36 与 SDK Build-Tools。
2. 用 Android Studio 打开仓库，等待 Gradle Sync 完成。
3. 连接已开启 USB 调试的红米 K70，运行 `app` 配置；或在终端执行：

   ```powershell
   .\gradlew.bat testDebugUnitTest assembleDebug
   ```

调试 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

## 红米 K70 首次使用

安装并启动后，在“设置”页依次完成：

1. 开启“通知使用权”。
2. 将电池策略设为“无限制”。
3. 允许后台自启动，建议在最近任务中锁定应用。
4. 确认微信、支付宝或银行 App 本身允许显示交易通知。
5. 先进行小额测试，并核对自动账单的金额、方向和分类。

## 自动识别边界

自动记账依赖第三方 App 实际发布到系统的通知文本。若支付 App 隐藏金额、关闭交易通知、改变文案，或澎湃 OS 停用监听服务，系统无法可靠识别。当前解析策略宁可漏记，也避免将聊天、验证码和营销优惠误记为账单；因此不能把自动识别当作银行流水的完整替代。

## Git

默认分支为 `main`，远端为：

```text
git@github.com:YYYKblue/accounting_app.git
```
