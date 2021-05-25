package club.codermax;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class HttpClient {

    //线程组
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public static void start(String host, int port, FullHttpRequest request, ChannelHandlerContext serverCtx) {

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
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

                            channel.pipeline().addLast(new ClientHandler(request, serverCtx));
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(host, port);

           /* channelFuture.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                       // return channelFuture.channel();
                    } else {
                        System.out.println("创建到目标服务器转发的client失败");
                        if (serverCtx.channel().isActive()) {  // server 端要转发还存在
                            System.out.println("Client Reconnect");
                            EventLoop eventLoop = future.channel().eventLoop();
                            eventLoop.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    HttpClient.start(host, port, request, serverCtx);
                                }
                            }, 5, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            });*/

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
