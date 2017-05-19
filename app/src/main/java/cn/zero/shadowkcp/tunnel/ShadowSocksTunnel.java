package cn.zero.shadowkcp.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by hongli on 2017/5/17.
 */

public class ShadowSocksTunnel extends Tunnel{
    public ShadowSocksTunnel(SocketChannel innerChannel, Selector selector) {
        super(innerChannel, selector);
    }

    public ShadowSocksTunnel(InetSocketAddress serverAddress, Selector selector) throws IOException {
        super(serverAddress, selector);
    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {

    }

    @Override
    protected boolean isTunnelEstablished() {
        return false;
    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {

    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {

    }

    @Override
    protected void onDispose() {

    }
}
