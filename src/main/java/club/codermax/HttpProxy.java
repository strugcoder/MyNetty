package club.codermax;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HttpProxy {
    // 监听那个端口
    static final int LOCAL_PORT = 8083;
    // 要转发给哪个端口
    static final int PORT = 80;
    static final String HOST = "www.baidu.com";
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpProxyInitializer(HOST, PORT));
            Channel ch = b.bind(LOCAL_PORT).sync().channel();

            System.out.println("proxy server start in: " + LOCAL_PORT + "\n" + "remote host: "
                            + HOST  + " remote port: "  + PORT);
            } catch (InterruptedException e) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
    }
}
