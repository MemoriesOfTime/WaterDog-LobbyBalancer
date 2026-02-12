[![release workflow badge](https://github.com/funniray/waterdog-lobbybalancer/actions/workflows/release.yml/badge.svg)](https://github.com/funniray/waterdog-lobbybalancer/releases/latest)
## Waterdog-LobbyBalancer
WaterdogPE 大厅负载均衡插件

[English](README.md)

大厅服务器通过 WaterdogPE 配置中的大厅前缀来识别，默认前缀为 `lobby`。

使用默认配置时，lobby1、lobby2、lobby3 会被识别为大厅服务器，而 game1、game2 则不会。

你可以通过发送带有大厅前缀的转发包将玩家送到一个负载最低的大厅服务器。默认情况下，`/server lobby` 会将玩家送到一个自动选择的大厅服务器。

### 下载
从 [Jenkins CI](https://motci.cn/job/Waterdog-LobbyBalancer/) 下载

### 构建
使用 Maven 构建：`mvn package`

### 配置
```yaml
#大厅服务器名称必须以此字符串开头
#向此前缀发送转发请求会将玩家送到一个大厅服务器
lobbyprefix: lobby

#大厅最少玩家数
#大厅必须达到此人数后，新玩家才会被分配到其他大厅
#这有助于避免所有大厅都只有极少玩家的情况
#默认情况下，lobby1 会先填充到 10 人，然后 lobby2 才开始接收玩家
#当所有大厅都达到最低人数后，新玩家会加入人数最少的大厅
#大厅的优先顺序由 WaterdogPE 配置中的服务器顺序决定
minplayers: 10

#是否注册 /lobby 命令
#设为 false 可禁用该命令
lobbycommand: true

#是否使用 MOTD 查询获取子服真实在线人数
#适用于多代理部署场景，每个代理只知道经自己转发的玩家数
#启用后将通过 UDP MOTD 协议直接查询子服人数
use-motd-query: false

#MOTD 查询间隔（秒）
motd-query-interval: 30
```

### MOTD 查询
在多代理部署场景下，每个 WaterdogPE 代理只知道经自己转发的玩家数量，导致负载均衡不准确。

启用 `use-motd-query` 后，插件会通过 UDP MOTD 协议直接查询每个子服的真实总在线人数。查询失败的服务器会被视为离线，不参与负载均衡。

### 命令/权限
| 命令             | 权限                | 描述                   |
|------------------|---------------------|------------------------|
| /\<lobbyprefix\> | lobbybalancer.lobby | 将玩家传送到大厅服务器 |
