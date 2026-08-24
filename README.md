# 简账（Accounting App）

一款面向个人自用的 Android 本地手动记账应用，当前目标设备为红米 K70。

## 当前功能

- 手动记录收入和支出
- 编辑、删除已有账单
- 按月查看收入、支出、结余与账单明细
- 按分类统计月度支出
- 金额以整数“分”保存，避免浮点误差
- 纯本地 Room 数据库，无登录、云端、广告和数据上传

应用不会读取微信、支付宝、银行通知，也不需要通知使用权、后台自启动或持续驻留后台。

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

## 数据说明

账单仅保存在手机本机。当前版本未实现导出或云备份，并主动关闭 Android 系统备份；卸载应用会清除全部账单。升级安装新版 APK 时，Room 数据库会继续保留。

数据库中暂时保留了 0.1.0 自动记账原型使用过的兼容字段，目的是让已经安装过原型 APK 的设备能够直接升级并保留账单。当前应用没有通知监听服务，也不会写入自动账单。

## Git

默认分支为 `main`，远端为：

```text
https://github.com/YYYKblue/accounting_app.git
```
