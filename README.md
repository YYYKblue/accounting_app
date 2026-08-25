# 简账（Accounting App）

一款面向个人自用的 Android 本地手动记账应用，当前目标设备为红米 K70。

## 当前功能

- 手动记录收入、支出和借贷还款
- 自由填写商户/用途，保存后可快速复用
- 自由填写分类，并保留常用自定义分类
- 支出可选择“余额”或“借贷”付款
- 统计当前待还借贷；登记还款后自动冲减，还款不计入总支出
- 编辑、删除已有账单
- 按月查看收入、支出、收支差额与账单明细
- 在统计页切换查看最近 7 天、最近 5 周、2026 年 8 月起的月消费或 2026 年起的年消费柱状图
- 按分类统计月度支出
- 金额以整数“分”保存，避免浮点误差
- 纯本地 Room 数据库，无登录、云端、广告和数据上传

应用不会读取微信、支付宝、银行通知，也不需要通知使用权、后台自启动或持续驻留后台。

## 记账规则

- “余额”和“借贷”付款都属于消费，都会计入支出与柱状图。
- 选择“借贷”付款时，同额增加当前待还借贷。
- 每月还款时选择“还借贷”并填写金额；它只减少待还借贷，不计入支出、分类统计和消费柱状图。
- 从 0.2.0 升级的旧支出默认按“余额”处理，原有账单会保留。

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
