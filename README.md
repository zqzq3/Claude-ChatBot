# Claude-BotForQQ

## 介绍

an **unofficial** implement of Claude in **Tencent QQ**.

🌹🌹🌹感谢[acheong08/ChatGPT](https://github.com/acheong08/ChatGPT)、[PlexPt/chatgpt-java](https://github.com/PlexPt/chatgpt-java)、[TheoKanning/openai-java](https://github.com/TheoKanning/openai-java)和[mamoe/mirai](https://github.com/mamoe/mirai.git) 🌹🌹🌹

## 原理

使用mirai登录qq并监听消息->调用Claude接口将消息向gpt提问->使用mirai在qq里回复Claude的回答

## 特性
- qq登录失败时会尝试更换登陆方式进行重新登录，能一定程度上减少qq风控的影响
- 回复为引用回复，且默认情况下，在群聊需@才会回复
- 向机器人发送 “重置会话” 可以清除会话历史
- 不定期更新最新api

## 使用

你只需要

1.  clone本项目

2.  然后启动

tips：机器人响应速度与你的网络环境挂钩。