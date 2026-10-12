# Mine

一个简单、易用的记账应用。当前版本 0.8.0。

安装包发布在 [Releases](https://github.com/brassface/Mine/releases)。

应用图标来自 [iconfont](http://iconfont.cn/) 和 [material-design-icons](https://github.com/google/material-design-icons)。

## 项目结构

```
Mine
├── app                 主应用（界面、记账、账户、图表、备份）
├── libbase             基础工具与通用组件
├── libupdate           原应用内更新库，当前版本已不再发起检查更新
├── buildSrc            构建用 Gradle 插件
└── source              更新说明与备份格式示例
    └── changelog
```

`app` 里和功能对应的主要代码：

| 路径 | 内容 |
| --- | --- |
| `app/src/main/java/com/coderpage/mine/app/tally/module/home` | 首页、预算与日均剩余 |
| `app/src/main/java/com/coderpage/mine/app/tally/module/edit` | 记一笔、分类 |
| `app/src/main/java/com/coderpage/mine/app/tally/module/account` | 资产账户 |
| `app/src/main/java/com/coderpage/mine/app/tally/module/chart` | 日账单、年账单图表 |
| `app/src/main/java/com/coderpage/mine/app/tally/module/backup` | 数据导出与导入 |
| `app/src/main/java/com/coderpage/mine/app/tally/module/about` | 关于页 |
| `app/src/main/java/com/coderpage/mine/app/tally/persistence` | Room 数据库 |

版本号在根目录 `build.gradle` 的 `appVersionName`、`appVersionCode`。渠道包名为 `official` 时，正式包输出为 `app/build/outputs/apk/official/release/mine-official-<版本号>.apk`。

## 签名

Debug 与 Release 使用同一份签名，配置在 `app/build.gradle` 的 `signingConfigs.release`，密钥信息从项目根目录的 `local.properties` 读取（该文件不提交）。

| 项 | 值 |
| --- | --- |
| 打包实际使用的文件 | `app/debug.keystore` |
| `local.properties` 中的 `keyStore` | `debug.keystore`（相对 `app` 模块） |
| 别名 `keyAlias` | `mine_debug` |

仓库根目录下另有一份 `debug.keystore`，当前打包不会用到。`*.keystore`、`*.jks` 已在 `.gitignore` 中忽略。

## 原作者

原项目由原作者开发。原作者一段时间没有继续更新后，本仓库在其基础上继续维护。

- 仓库：[coderpage/Mine](https://github.com/coderpage/Mine)
- 邮箱：codenobug@163.com
- 微信公众号：miniapp

## 维护

- 仓库：[brassface/Mine](https://github.com/brassface/Mine)
- 邮箱：3543226907@qq.com
- 发布页面：[Releases](https://github.com/brassface/Mine/releases)
