package club.codermax.self;

import club.codermax.test.HttpClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;

public class Client {

    //线程组
    private static final EventLoopGroup group = new NioEventLoopGroup();

    private static ArrayBlockingQueue<Channel> list = new ArrayBlockingQueue<>(10);

    //启动类
    private static final Bootstrap bootstrap = new Bootstrap();

    private static final int PORT = 8080;

    private static final String HOST = "127.0.0.1";


    public static void start() throws InterruptedException {

        try {
            bootstrap.group(group)
                    .remoteAddress(new InetSocketAddress(HOST, PORT))
                    //长连接
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        protected void initChannel(Channel channel) throws Exception {

                            //包含编码器和解码器
                            channel.pipeline().addLast(new HttpClientCodec());
                            //聚合
                            channel.pipeline().addLast(new HttpObjectAggregator(1024 * 10 * 1024));

                            //解压
                            channel.pipeline().addLast(new HttpContentDecompressor());

                            channel.pipeline().addLast(new ClientHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect().sync();

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        HttpClient.start();
    }
}