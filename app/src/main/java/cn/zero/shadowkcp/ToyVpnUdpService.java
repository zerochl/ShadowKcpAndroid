package cn.zero.shadowkcp;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import cn.zero.shadowkcp.core.DnsProxy;
import cn.zero.shadowkcp.core.HttpHostHeaderParser;
import cn.zero.shadowkcp.core.NatSession;
import cn.zero.shadowkcp.core.NatSessionManager;
import cn.zero.shadowkcp.dns.DnsPacket;
import cn.zero.shadowkcp.tcpip.CommonMethods;
import cn.zero.shadowkcp.tcpip.IPHeader;
import cn.zero.shadowkcp.tcpip.TCPHeader;
import cn.zero.shadowkcp.tcpip.UDPHeader;
import kcp.Kcp;

public class ToyVpnUdpService extends VpnService implements Handler.Callback, Runnable {
    private static final String TAG = "ToyVpnService";

    private String mServerAddress;
    private String mServerPort;
    private byte[] mSharedSecret;
    private PendingIntent mConfigureIntent;

    private Handler mHandler;
    private Thread mThread;
    private byte[] m_Packet;
    private IPHeader m_IPHeader;
    private TCPHeader m_TCPHeader;
    private UDPHeader m_UDPHeader;
    private ByteBuffer m_DNSBuffer;
    private DnsProxy m_DnsProxy;
    private ParcelFileDescriptor mInterface;
    private String mParameters;

    public static ToyVpnUdpService toyVpnUdpService = new ToyVpnUdpService();

    public static ToyVpnUdpService getInstance() {
        return toyVpnUdpService;
    }

    public ToyVpnUdpService() {

        m_Packet = new byte[20000];
        m_IPHeader = new IPHeader(m_Packet, 0);
        m_TCPHeader = new TCPHeader(m_Packet, 20);
        m_UDPHeader = new UDPHeader(m_Packet, 20);
        m_DNSBuffer = ((ByteBuffer) ByteBuffer.wrap(m_Packet).position(28)).slice();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = new Handler(this);
        }

        // Stop the previous session by interrupting the thread.
        if (mThread != null) {
            mThread.interrupt();
        }

        // Extract information from the intent.
        String prefix = getPackageName();
        mServerAddress = intent.getStringExtra(prefix + ".ADDRESS");
        mServerPort = intent.getStringExtra(prefix + ".PORT");
        mSharedSecret = intent.getStringExtra(prefix + ".SECRET").getBytes();

        // Start a new session by creating a new thread.
        mThread = new Thread(this, "ToyVpnThread");
        mThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message != null) {
            Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public synchronized void run() {
        try {
            Log.i(TAG, "Starting");

            // If anything needs to be obtained using the network, get it now.
            // This greatly reduces the complexity of seamless handover, which
            // tries to recreate the tunnel without shutting down everything.
            // In this demo, all we need to know is the server address.
//            InetSocketAddress server = new InetSocketAddress(
//                    mServerAddress, Integer.parseInt(mServerPort));
            InetSocketAddress server = new InetSocketAddress("127.0.0.1", 1082);

            // We try to create the tunnel for several times. The better way
            // is to work with ConnectivityManager, such as trying only when
            // the network is avaiable. Here we just use a counter to keep
            // things simple.
//            for (int attempt = 0; attempt < 10; ++attempt) {
//                mHandler.sendEmptyMessage(R.string.connecting);
//
//                // Reset the counter if we were connected.
//                if (run(server)) {
//                    attempt = 0;
//                }
//
//                // Sleep for a while. This also checks if we got interrupted.
//                Thread.sleep(3000);
//            }
            mHandler.sendEmptyMessage(R.string.connecting);
            run(server);
            Thread.sleep(3000);
            Log.i(TAG, "Giving up");
        } catch (Exception e) {
            Log.e(TAG, "Got " + e.toString());
        } finally {
            try {
                mInterface.close();
            } catch (Exception e) {
                // ignore
            }
            mInterface = null;
            mParameters = null;

            mHandler.sendEmptyMessage(R.string.disconnected);
            Log.i(TAG, "Exiting");
        }
    }

    static FileInputStream in;
    static FileOutputStream out;

    private boolean run(InetSocketAddress server) throws Exception {
        DatagramChannel tunnel = null;
        boolean connected = false;
//        try {
        // Create a DatagramChannel as the VPN tunnel.
        tunnel = DatagramChannel.open();

        // Protect the tunnel before connecting to avoid loopback.
//            if (!protect((int)Kcp.getSocketId())) {
//                throw new IllegalStateException("Cannot protect the tunnel");
//            }
//            if (!protect(tunnel.socket())) {
//                throw new IllegalStateException("Cannot protect the tunnel");
//            }
        Log.e("HongLi", "Kcp.getSocketId():" + Kcp.getSocketId() + ";kcp id:" + Kcp.getKcpFd() + ";kcp2:" + Kcp.getKcpFD2() + ";localAddress:" + Kcp.getLocalAddr() +
                ";localport:" + Kcp.getLocalPort() + ";channel port:" + tunnel.socket().getLocalPort() + ";Kcp.getShadowFd():" + Kcp.getShadowFd());
        if (!protect((int) Kcp.getKcpFd())) {
            throw new IllegalStateException("Cannot protect the tunnel");
        }

//        if(!protect((int)Kcp.getShadowFd())){
//            throw new IllegalStateException("Cannot protect the tunnel");
//        }

//        if (!protect((int) Kcp.getKcpFD2())) {
//            throw new IllegalStateException("Cannot protect the tunnel");
//        }

        // Connect to the server.
//            tunnel.connect(server);
//            tunnel.socket().bind(server);

        // For simplicity, we use the same thread for both reading and
        // writing. Here we put the tunnel into non-blocking mode.
//            tunnel.configureBlocking(false);

        // Authenticate and configure the virtual network interface.
//            handshake(tunnel);
        configure("m,1500 a,10.0.2.2:32 r,0 d,8.8.8.8 s,VPNServiceDemo");
        Kcp.startTun2Socks(mInterface.getFd());
        // Now we are connected. Set the flag and show the message.
//            connected = true;
//            mHandler.sendEmptyMessage(R.string.connected);
//
//            // Packets to be sent are queued in this input stream.
//            in = new FileInputStream(mInterface.getFileDescriptor());
////
////            // Packets received need to be written to this output stream.
//            out = new FileOutputStream(mInterface.getFileDescriptor());
////
//////            final byte[] bytes = new byte[32767];
////            // Allocate the buffer for a single packet.
//////            final ByteBuffer packet = ByteBuffer.allocate(32767);
//            final ByteBuffer packet = ByteBuffer.wrap(m_Packet);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//                    Kcp.testRequest2();
//                }
//            }
//        }).start();
//
//        while (true){
//            int length;
//            length = in.read(packet.array());
//            packet.limit(length);
//            out.write(packet.array(), 0, length);
//            packet.clear();
//            Thread.sleep(16);
//            if(1!= 1){
//                break;
//            }
//        }
//
//
//            // We use a timer to determine the status of the tunnel. It
//            // works on both sides. A positive value means sending, and
//            // any other means receiving. We start with receiving.
//            int timer = 0;
//            Log.e("HongLi","before while");
//            // We keep forwarding packets till something goes wrong.
//            //我们一直转发数据包直到出错。
////            while (true) {
//                // Assume that we did not make any progress in this iteration.
//                //假设我们在这个迭代中没有取得任何进展。
//                boolean idle = true;
////            new Thread() {
////
////                @Override
////                public void run() {
////                    try {
////                        DatagramSocket socket = new DatagramSocket(1082);
////                        DatagramPacket packet = new DatagramPacket(new byte[255],255);
////                        Thread.sleep(1000);
////                        while (true) {
////                            try {
////                                Log.e(TAG, "in receive and send");
////                                socket.receive(packet);// 阻塞
////                                socket.send(packet);
////                                packet.setLength(255);
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                        }
////                    } catch (SocketException e) {
////                        e.printStackTrace();
////                    } catch (Exception e){
////                        e.printStackTrace();
////                    }
////                }
////
////            }.start();
//        m_DnsProxy = new DnsProxy();
//        m_DnsProxy.start();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            while (true){
//                                // Read the outgoing packet from the input stream.
//                                //从输入流中读取传出的数据包。
//                                int length = in.read(packet.array());
//                                if(0 != length){
//                                    Log.e("HongLi","in read length:" + length);
//                                }
//                                if (length > 0) {
//                                    // Write the outgoing packet to the tunnel.
//                                    //将传出的包写入隧道。
//                                    packet.limit(length);
////                    tunnel.write(packet);
//                                    Log.e("HongLi","读取的包大小:" + length);
////                                    debugPacket(packet);
//                                    onIPPacketReceived(m_IPHeader,length);
////                                    Kcp.writeVpn(packet.array(),length);
//                                    packet.clear();
//                                    Thread.sleep(100);
//
////                                // There might be more outgoing packets.
////                                //可能会有更多的传出数据包。
////                                idle = false;
////
////                                // If we were receiving, switch to sending.
////                                if (timer < 1) {
////                                    timer = 1;
////                                }
//                                }
//                            }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//
//                // Read the incoming packet from the tunnel.
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
////                        int length = tunnel.read(packet);
//                        try{
//                            while (true){
//                                int length = (int)Kcp.readVpn(packet.array());
//                                if(0 != length){
//                                    Log.e("HongLi","kcp read length:" + length);
//                                }
////                                if (length > 0) {
////                                    // Ignore control messages, which start with zero.
////                                    if (packet.get(0) != 0) {
////                                        // Write the incoming packet to the output stream.
////                                        out.write(packet.array(), 0, length);
////                                    }
////                                    packet.clear();
////                                    // There might be more incoming packets.
//////                                idle = false;
////                                }
//                            }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//
//
////                // If we are idle or waiting for the network, sleep for a
////                // fraction of time to avoid busy looping.
////                if (idle) {
////                    Thread.sleep(100);
////
////                    // Increase the timer. This is inaccurate but good enough,
////                    // since everything is operated in non-blocking mode.
////                    timer += (timer > 0) ? 100 : -100;
////
////                    // We are receiving for a long time but not sending.
////                    if (timer < -15000) {
////                        // Send empty control messages.
////                        packet.put((byte) 0).limit(1);
////                        for (int i = 0; i < 3; ++i) {
////                            packet.position(0);
//////                            tunnel.write(packet);
////                            Kcp.writeVpn(packet.array());
////                        }
////                        packet.clear();
////
////                        // Switch to sending.
////                        timer = 1;
////                    }
////
////                    // We are sending for a long time but not receiving.
////                    if (timer > 20000) {
////                        throw new IllegalStateException("Timed out");
////                    }
////                }
////            }
//
//            while (true){
//                if(false){
//                    break;
//                }
//            }
//        } catch (InterruptedException e) {
//            Log.e(TAG, "Got InterruptedException");
//            e.printStackTrace();
//        } catch (Exception e) {
//            Log.e(TAG, "Got Exception");
//            e.printStackTrace();
//        } finally {
//            try {
//                Kcp.close();
//            } catch (Exception e) {
//                // ignore
//            }
//        }
        return connected;
    }

    public void sendUDPPacket(IPHeader ipHeader, UDPHeader udpHeader) {
        try {
            CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
            this.out.write(ipHeader.m_Data, ipHeader.m_Offset, ipHeader.getTotalLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFakeIP(int ip) {
        return (ip & CommonMethods.ipStringToInt("255.255.0.0")) == CommonMethods.ipStringToInt("10.231.0.0");
    }

    void onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                TCPHeader tcpHeader = m_TCPHeader;
                tcpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == CommonMethods.ipStringToInt("10.0.2.2")) {
                    Log.e("HongLi", "is the same source ip:" + ipHeader.getSourceIP());
                    if ((tcpHeader.getSourcePort() & 0xFFFF) == Integer.parseInt(Kcp.getLocalPort())) {// 收到本地TCP服务器数据
                        Log.e("HongLi", "收到本地TCP服务器数据:Kcp.getLocalPort():" + Kcp.getLocalPort() + ";(tcpHeader.getSourcePort() & 0xFFFF)" + (tcpHeader.getSourcePort() & 0xFFFF));
                        NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
                        if (session != null) {
                            ipHeader.setSourceIP(ipHeader.getDestinationIP());
                            tcpHeader.setSourcePort(session.RemotePort);
                            ipHeader.setDestinationIP(CommonMethods.ipStringToInt("10.0.2.2"));

                            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                            out.write(ipHeader.m_Data, ipHeader.m_Offset, size);
//                            m_ReceivedBytes += size;
                        } else {
//                            System.out.printf("NoSession: %s %s\n", ipHeader.toString(), tcpHeader.toString());
                            Log.e("HongLi", "NoSession: " + ipHeader.toString() + " " + tcpHeader.toString() + "\n");
                        }
                    } else {
                        Log.e("HongLi", "转发tcp:Kcp.getLocalPort():" + Kcp.getLocalPort() + ";(tcpHeader.getSourcePort() & 0xFFFF)" + (tcpHeader.getSourcePort() & 0xFFFF));
                        // 添加端口映射
                        int portKey = tcpHeader.getSourcePort();
                        NatSession session = NatSessionManager.getSession(portKey);
                        if (session == null || session.RemoteIP != ipHeader.getDestinationIP() || session.RemotePort != tcpHeader.getDestinationPort()) {
                            session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader.getDestinationPort());
                        }

                        session.LastNanoTime = System.nanoTime();
                        session.PacketSent++;//注意顺序

                        int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
                        if (session.PacketSent == 2 && tcpDataSize == 0) {
                            return;//丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
                        }

                        //分析数据，找到host
                        if (session.BytesSent == 0 && tcpDataSize > 10) {
                            int dataOffset = tcpHeader.m_Offset + tcpHeader.getHeaderLength();
                            String host = HttpHostHeaderParser.parseHost(tcpHeader.m_Data, dataOffset, tcpDataSize);
                            if (host != null) {
                                session.RemoteHost = host;
                            } else {
                                System.out.printf("No host name found: %s", session.RemoteHost);
                            }
                        }

                        // 转发给本地TCP服务器
                        ipHeader.setSourceIP(ipHeader.getDestinationIP());
                        ipHeader.setDestinationIP(CommonMethods.ipStringToInt("10.0.2.2"));
                        tcpHeader.setDestinationPort((short) Integer.parseInt(Kcp.getLocalPort()));

                        CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                        out.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                        session.BytesSent += tcpDataSize;//注意顺序
//                        m_SentBytes += size;
                    }
                }
                Log.e("HongLi", "is tcp bao");
                Log.e("HongLi", "tcp source ip:" + CommonMethods.ipIntToString(ipHeader.getSourceIP()) + ";des ip:" + CommonMethods.ipIntToString(ipHeader.getDestinationIP())
                        + ";SourcePort():" + (tcpHeader.getSourcePort() & 0xFFFF) + ";desport:" + tcpHeader.getDestinationPort());
                break;
            case IPHeader.UDP:
                Log.e("HongLi", "is udp bao");
                // 转发DNS数据包：
                UDPHeader udpHeader = m_UDPHeader;
                udpHeader.m_Offset = ipHeader.getHeaderLength();//"10.0.2.2"
                Log.e("HongLi", "udp source ip:" + CommonMethods.ipIntToString(ipHeader.getSourceIP()) + ";des ip:" + CommonMethods.ipIntToString(ipHeader.getDestinationIP())
                        + ";SourcePort():" + (udpHeader.getSourcePort() & 0xFFFF) + ";DestinationPort():" + udpHeader.getDestinationPort());
                if (ipHeader.getSourceIP() == CommonMethods.ipStringToInt("10.0.2.2") && udpHeader.getDestinationPort() == 53) {
                    m_DNSBuffer.clear();
                    m_DNSBuffer.limit(ipHeader.getDataLength() - 8);
                    DnsPacket dnsPacket = DnsPacket.FromBytes(m_DNSBuffer);
                    Log.e("HongLi","dnsPacket.Header.QuestionCount:" + dnsPacket.Header.QuestionCount);
                    if (dnsPacket != null && dnsPacket.Header.QuestionCount > 0) {
                        m_DnsProxy.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket);
                    }
                }
                break;
        }
    }

    private void debugPacket(ByteBuffer packet) {
        /*
		 * for(int i = 0; i < length; ++i) { byte buffer = packet.get();
		 *
		 * Log.d(TAG, "byte:"+buffer); }
		 */

        int buffer = packet.get();
        int version;
        int headerlength;
        version = buffer >> 4;
        headerlength = buffer & 0x0F;
        headerlength *= 4;
        Log.d(TAG, "IP Version:" + version);
        Log.d(TAG, "Header Length:" + headerlength);

        String status = "";
        status += "Header Length:" + headerlength;

        buffer = packet.get(); // DSCP + EN
        buffer = packet.getChar(); // Total Length

        Log.d(TAG, "Total Length:" + buffer);

        buffer = packet.getChar(); // Identification
        buffer = packet.getChar(); // Flags + Fragment Offset
        buffer = packet.get(); // Time to Live
        buffer = packet.get(); // Protocol

        Log.d(TAG, "Protocol:" + buffer);

        status += "  Protocol:" + buffer;

        buffer = packet.getChar(); // Header checksum

        String sourceIP = "";
        buffer = packet.get(); // Source IP 1st Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get(); // Source IP 2nd Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get(); // Source IP 3rd Octet
        sourceIP += buffer;
        sourceIP += ".";

        buffer = packet.get(); // Source IP 4th Octet
        sourceIP += buffer;

        Log.d(TAG, "Source IP:" + sourceIP);

        status += "   Source IP:" + sourceIP;

        String destIP = "";
        buffer = packet.get(); // Destination IP 1st Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get(); // Destination IP 2nd Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get(); // Destination IP 3rd Octet
        destIP += buffer;
        destIP += ".";

        buffer = packet.get(); // Destination IP 4th Octet
        destIP += buffer;

        Log.d(TAG, "Destination IP:" + destIP);

        status += "   Destination IP:" + destIP;

        Log.e("HongLi", "status:" + status);

    }

    private void handshake(DatagramChannel tunnel) throws Exception {
        // To build a secured tunnel, we should perform mutual authentication
        // and exchange session keys for encryption. To keep things simple in
        // this demo, we just send the shared secret in plaintext and wait
        // for the server to send the parameters.

        // Allocate the buffer for handshaking.
        ByteBuffer packet = ByteBuffer.allocate(1024);
        mSharedSecret = "m,1500 a,10.0.2.2:32 r,0.0.0.0:0 d,8.8.8.8 s,VPNServiceDemo".getBytes();
        // Control messages always start with zero.
        packet.put((byte) 0).put(mSharedSecret).flip();

        // Send the secret several times in case of packet loss.
        for (int i = 0; i < 3; ++i) {
            packet.position(0);
            tunnel.write(packet);
        }
        packet.clear();
        if (tunnel.isConnected()) {
            Log.e("HongLi", "tunnel.isConnected()");
        }
        // Wait for the parameters within a limited time.
        for (int i = 0; i < 50; ++i) {
            Thread.sleep(100);

            // Normally we should not receive random packets.

            int length = tunnel.read(packet);
//            Log.e("HongLi","lenghth:" + length);
            if (length > 0 && packet.get(0) == 0) {
                configure(new String(packet.array(), 1, length - 1).trim());
                return;
            }
        }
        configure("m,1500 a,10.0.2.2:32 r,0 d,8.8.8.8 s,VPNServiceDemo");
        return;
//        throw new IllegalStateException("Timed out");
    }

    private void configure(String parameters) throws Exception {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null && parameters.equals(mParameters)) {
            Log.i(TAG, "Using the previous interface");
            return;
        }
        Log.e("HongLi", "parameters");
        // Configure a builder while parsing the parameters.
        Builder builder = new Builder();
//        for (String parameter : parameters.split(" ")) {
//            String[] fields = parameter.split(",");
//            try {
//                switch (fields[0].charAt(0)) {
//                    case 'm':
////                        builder.setMtu(Short.parseShort(fields[1]));
//                        builder.setMtu(Short.parseShort(fields[1]));
//                        break;
//                    case 'a':
////                        builder.addAddress(fields[1], Integer.parseInt(fields[2]));
//                        builder.addAddress(fields[1], Integer.parseInt(fields[2]));
//                        break;
//                    case 'r':
////                        builder.addRoute(fields[1], Integer.parseInt(fields[2]));
//                        builder.addRoute(fields[1], Integer.parseInt(fields[2]));
//                        break;
//                    case 'd':
////                        builder.addDnsServer(fields[1]);
//                        builder.addDnsServer(fields[1]);
//                        break;
//                    case 's':
////                        builder.addSearchDomain(fields[1]);
//                        builder.addSearchDomain(fields[1]);
//                        break;
//                }
//            } catch (Exception e) {
//                throw new IllegalArgumentException("Bad parameter: " + parameter);
//            }
//        }
        builder.setMtu(1500);
        builder.addAddress("10.0.2.2", 32);
        builder.addRoute("0.0.0.0", 0);
        builder.addDnsServer("8.8.8.8");
        builder.addSearchDomain("VPNServiceDemo");
        // Close the old interface since the parameters have been changed.
        try {
            mInterface.close();
        } catch (Exception e) {
            // ignore
        }

        // Create a new interface using the builder and save the parameters.
        mInterface = builder.setSession(mServerAddress)
                .setConfigureIntent(mConfigureIntent)
                .establish();
        mParameters = parameters;
        Log.i(TAG, "New interface: " + parameters);
    }
}