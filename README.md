# Day Day Plan

一个基于 `Kotlin + Jetpack Compose + Room + DataStore` 的安卓日程与待办管理应用。

## 当前功能

- 首页总览：展示今日汇总信息
- 日程管理：支持新增、编辑、删除、完成/恢复
- 每日固定项：日程支持“每日自动添加”
- 日程提醒：支持开始前通知提醒
- 待办管理：支持新增、编辑、删除、优先级、上下移动排序、完成/恢复
- 截止时间：待办支持设置截止时间
- 待办提醒：支持截止前通知提醒
- 已完成页：集中查看今日已完成的日程和待办
- 回顾页：支持按日历查看往日整体回顾
- 本地离线：使用 `Room` 持久化业务数据，使用 `DataStore` 保存界面偏好
- 应用内更新：已接入应用内更新检查流程

## 标签页顺序

- 总览
- 日程
- 待办
- 已完成
- 回顾

## 运行环境

- Android Studio Koala 或更高版本
- JDK 17
- Android SDK 35

## 本地运行

构建 Debug 包：

```bash
./gradlew assembleDebug
```

Windows：

```powershell
.\gradlew.bat assembleDebug
```

## 生成 Release APK

```powershell
.\gradlew.bat assembleRelease
```

生成后的 APK 默认位于：

`app/build/outputs/apk/release/app-release.apk`

## 在 Android Studio 中运行项目

- 使用 Android Studio 打开项目根目录
- 等待 Gradle 同步完成
- 打开模拟器或连接安卓手机
- 运行 `app` 模块即可

## 说明

- 当前项目已配置本地签名，可直接生成可分发的 `release APK`
- 如需覆盖安装新版本，建议同步维护 `versionCode` 与 `versionName`
