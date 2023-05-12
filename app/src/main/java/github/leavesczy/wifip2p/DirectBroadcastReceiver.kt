package github.leavesczy.wifip2p

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager

/**
 * @Author: CZY
 * @Date: 2022/9/28 14:24
 * @Desc:
 * 可以看出 Wifi P2P 的接口高度异步化，到现在已经用到了三个系统的回调函数，一个用于 WifiP2pManager 的
 * 初始化，两个用于在广播中异步请求数据，为了简化操作，此处统一使用一个自定义的回调函数，方法含义与系
 * 统的回调函数一致
 *
 */
interface DirectActionListener : WifiP2pManager.ChannelListener {

    fun wifiP2pEnabled(enabled: Boolean)

    fun onConnectionInfoAvailable(wifiP2pInfo: WifiP2pInfo)

    fun onDisconnection()

    fun onSelfDeviceAvailable(wifiP2pDevice: WifiP2pDevice)

    fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>)

}

/**
 * 与 Wifi P2P 相关的广播
 */
class DirectBroadcastReceiver(
    private val wifiP2pManager: WifiP2pManager,//对等网络管理器
    private val wifiP2pChannel: WifiP2pManager.Channel,
    private val directActionListener: DirectActionListener
) : BroadcastReceiver() {

    companion object {

        fun getIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            /**
             * WIFI_P2P_STATE_CHANGED_ACTION（ 用于指示 Wifi P2P 是否可用 ）
             * WIFI_P2P_PEERS_CHANGED_ACTION（ 对等节点列表发生了变化 ）
             * WIFI_P2P_CONNECTION_CHANGED_ACTION（ Wifi P2P 的连接状态发生了改变 ）
             * WIFI_P2P_THIS_DEVICE_CHANGED_ACTION（ 本设备的设备信息发生了变化 ）
             **/
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            return intentFilter
        }

    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ////可以判断当前 Wifi P2P是否可用
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val enabled = intent.getIntExtra(
                    WifiP2pManager.EXTRA_WIFI_STATE,
                    -1
                ) == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                directActionListener.wifiP2pEnabled(enabled)
                if (!enabled) {
                    directActionListener.onPeersAvailable(emptyList())
                }
                Logger.log("WIFI_P2P_STATE_CHANGED_ACTION： $enabled")
            }

            //意味设备周围的可用设备列表发生了变化，可以通过 requestPeers 方法得到可用的设备列表，
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Logger.log("WIFI_P2P_PEERS_CHANGED_ACTION")
                wifiP2pManager.requestPeers(wifiP2pChannel) { peers ->
                    directActionListener.onPeersAvailable(
                        peers.deviceList
                    )
                }
            }

            //意味着 Wifi P2P 的连接状态发生了变化，可能是连接到了某设备，或者是与某设备断开了连接
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo =
                    intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)

                Logger.log("WIFI_P2P_CONNECTION_CHANGED_ACTION ： " + networkInfo?.isConnected)

                if (networkInfo != null && networkInfo.isConnected) {
                    wifiP2pManager.requestConnectionInfo(wifiP2pChannel) { info ->
                        if (info != null) {
                            directActionListener.onConnectionInfoAvailable(info)
                        }
                    }
                    Logger.log("已连接 P2P 设备")
                } else {
                    directActionListener.onDisconnection()
                    Logger.log("与 P2P 设备已断开连接")
                }
            }

            //可以获取到本设备变化后的设备信息
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val wifiP2pDevice =
                    intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                if (wifiP2pDevice != null) {
                    directActionListener.onSelfDeviceAvailable(wifiP2pDevice)
                }
                Logger.log("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION ： ${wifiP2pDevice.toString()}")
            }
        }
    }

}