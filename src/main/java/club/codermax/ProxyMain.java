package club.codermax;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.SocketUtils;

import java.net.URI;
import java.util.Map;

public class ProxyMain {
    static final int PORT = 80;
    static final String HOST = "202.108.22.5";

    public static void clientRun (URI uri, HttpHeaders headers) {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new HttpClientInitializer(HOST, PORT));
            ChannelFuture future = b.bind(SocketUtils.socketAddress(HOST, PORT));

            Channel channel = future.sync().channel();

            // HTTP request
            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString(), Unpooled.EMPTY_BUFFER);
            // body encoder
            // HttpRequestEncoder bodyRequestEncoder = new HttpRequestEncoder(request);

            // cookies
            for (Map.Entry<String, String> entry : headers) {
                request.headers().set(entry.getKey(), entry.getValue());
            }

            request.headers().set(HttpHeaderNames.HOST, uri.getHost());
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);


            channel.writeAndFlush(request);

            channel.closeFuture().sync();

            System.out.println("remote host: "
                    + HOST  + " remote port: "  + PORT);
        } catch (InterruptedException e) {
            group.shutdownGracefully();
        }
    }
}
