## https://blog.csdn.net/fesdgasdgasdg/article/details/106559510

Wifi P2P (peer to peer)：也为 Wifi 点对点，也叫 Wifi 直连(Wifi Direct)，它是 Wifi Display(投屏) 应用的技术基础。

## WLAN 直连 (P2P) 技术
使用 WLAN 直连 (P2P) 技术，可以让具备相应硬件的 Android 4.0（API 级别 14）或更高版本设备在没有中间接入点的情况下，
通过 WLAN 进行直接互联。使用这些 API，您可以实现支持 WLAN P2P 的设备间相互发现和连接，从而获得比蓝牙连接更远距离
的高速连接通信效果。对于多人游戏或照片共享等需要在用户之间共享数据的应用而言，这一技术非常有用。

总结以下优点：
1、有比蓝牙更远的传输距离。
2、有比蓝牙更快速的数据传输速度，更大的带宽。
3、只需要打开 Wifi 即可，不需要加入任何网络或 AP，即可实现对等点连接通讯。
可实现通过 Wifi 连接，同时使用数据网络的场景，比喻：手机遥控无人机的同时，无人机需要访问远程服务器上传数据。

## Wifi P2P 架构：
虽然上面提到两台或多台 Android 设备通过 Wifi P2P 通讯时不需要加入任何网络，但是 Wifi P2P 协议还是需要组件网络
才能发现对方并建立 TCP 连接通讯的。在组网和通讯阶段一共有 3 个角色：

1、P2P Group Owner，或称为群主，充当服务端，并需要创建 ServerSocket 等待客户端的连接，获得 IO 流与客户端通讯
或转发消息给其他客户端。

2、P2P Client，或称为组员，充当客户端，需要创建 Socket 与服务器通讯。

3、P2P Device，在上面的过程中，服务器端和客户端都是一个独立的设备，拥有唯一的设备特征信息。

4、广播接收器：
WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION：检查 Wi-Fi P2P 是否已启用。Android 4.0 以上系统才有此功能。

WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION：对等设备发生变化，一般是在调用 discoverPeers 方法后发送此广播。
在此广播中，你可以调用 requestPeers 方法，获得扫描到的对等设备列表。

WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION：连接状态发生变化，一般在调用 connect 或 cancelConnect 方法
时会发送此广播。状态共有 5 种：WifiP2pDevice.AVAILABLE、WifiP2pDevice.INVITED、WifiP2pDevice.CONNECTED、
WifiP2pDevice.FAILED 和 WifiP2pDevice.UNAVAILABLE 。

当判断连接信息为连接状态时，即 networkInfo.isConnected() ，你应当继续请求连接的具体信息 
mManager.requestConnectionInfo(...)，然后获得群主的详细设备信息，建立 Socket 通讯。

WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION：此设备的WiFi状态更改回调，应用可使用 requestDeviceInfo() 来
检索当前连接信息。

在未组网之前，是不存在群主、组员之称的。只有在设备尝试发现并连接对方时，系统才会通过 P2P 协议尝试使多端设备
组件为一个群组，并自动确定某一个设备为群主。但是本人在实测过程中发现，是需要先有群主，才会加入组员组网通讯的。

连接流程：
绘制了一张流程图，描述我 Demo 的连接过程。
服务端流程：
![img.png](img.png)

客户端流程：
![img_1.png](img_1.png)
