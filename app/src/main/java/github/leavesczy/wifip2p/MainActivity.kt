package github.leavesczy.wifip2p

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import github.leavesczy.wifip2p.receiver.FileReceiverActivity
import github.leavesczy.wifip2p.sender.FileSenderActivity


/**
开发步骤分为以下几点：

1.在 AndroidManifest 中声明相关权限（网络和文件读写权限）

2.获取 WifiP2pManager ，注册相关广播监听Wifi直连的状态变化
WifiP2pManager 对等网络管理器
WifiP2pManager.Channel 通道
DirectBroadcastReceiver(mWifiP2pManager, wifiP2pChannel, directActionListener) 广播

与 Wifi P2P 相关的广播有以下几个：
WIFI_P2P_STATE_CHANGED_ACTION（ 用于指示 Wifi P2P 是否可用 ）
WIFI_P2P_PEERS_CHANGED_ACTION（ 对等节点列表发生了变化 ）
mWifiP2pManager.requestPeers(mChannel,...) 获取可用设备列表
WIFI_P2P_CONNECTION_CHANGED_ACTION（ Wifi P2P 的连接状态发生了改变 ）
mWifiP2pManager.requestConnectionInfo(mChannel,...) 获取连接信息
WIFI_P2P_THIS_DEVICE_CHANGED_ACTION（ 本设备的设备信息发生了变化 ）

3.指定某一台设备为服务器（用来接收文件），创建群组并作为群主存在，在指定端口监听客户端的连接请求，等待
客户端发起连接请求以及文件传输请求
wifiP2pManager.createGroup(channe,..) 创建群组
new ServerSocket() 传输

4.客户端（用来发送文件）主动搜索附近的设备，加入到服务器创建的群组，获取服务器的 IP 地址，向其发起文件传输请求
mWifiP2pManager.discoverPeers(mChannel,..)  搜索周边设备
mWifiP2pManager.connect(mChannel,..)  连接设备
socket = new Socket();
socket.connect((new InetSocketAddress(strings[0], PORT)), 10000);

5.校验文件完整性

WifiP2pManager有例如以下方法能够非常方便的进行P2P操作:
方法			功能
initialize()	在使用WiFi P2P功能时必须先调用这种方法，用来通过WiFi P2P框架注冊我们的应用
connect()	依据配置(WifiP2pConfig对象)与指定设备(WifiP2pDevice对象)进行P2P连接
cancelConnect()	关闭某个P2P连接
requestConnectInfo()	获取设备的连接信息
createGroup()	以当前的设备为组长创建P2P小组
removeGroup()	移除当前的P2P小组
requestGroupInfo()	获取P2P小组的信息
discoverPeers()	初始化peers发现操作
requestPeers()	获取当前的peers列表(通过discoverPeers发现来的)

 **/

/**
 * @Author: CZY
 * @Date: 2022/9/28 14:24
 * @Desc:
 */
class MainActivity : BaseActivity() {

    private val requestedPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private val requestPermissionLaunch = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { it ->
        if (it.all { it.value }) {
            showToast("已获得全部权限")
        } else {
            onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btnCheckPermission).setOnClickListener {
            requestPermissionLaunch.launch(requestedPermissions)
        }
        findViewById<View>(R.id.btnSender).setOnClickListener {
            if (allPermissionGranted()) {
                startActivity(FileSenderActivity::class.java)
            } else {
                onPermissionDenied()
            }
        }
        findViewById<View>(R.id.btnReceiver).setOnClickListener {
            if (allPermissionGranted()) {
                startActivity(FileReceiverActivity::class.java)
            } else {
                onPermissionDenied()
            }
        }
    }

    private fun onPermissionDenied() {
        showToast("缺少权限，请先授予权限")
    }

    private fun allPermissionGranted(): Boolean {
        requestedPermissions.forEach {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

}