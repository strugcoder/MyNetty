package club.codermax.test;

import club.codermax.HttpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;

        ByteBuf content = request.content();
        HttpHeaders headers = request.headers();

        System.out.println("content: " + content.toString(CharsetUtil.UTF_8));
        System.out.println("headers: " + headers.toString());


        String path = request.uri();  // 获取路径
        String body = request.content().toString(CharsetUtil.UTF_8); //获取 body

        HttpMethod method = request.method(); //   请求方法
        System.out.println("接收到:" + method + " 请求, 请求路径为：" + path);
        String result;
        if (HttpMethod.GET.equals(method)) {
            //接受到的消息，做业务逻辑处理...
            System.out.println("body:" + body);
            result = "GET请求";
            send(ctx, result, HttpResponseStatus.OK, headers);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        ctx.writeAndFlush("客户端" + InetAddress.getLocalHost().getHostName() + "成功与服务端建立连接！ ");
        super.channelActive(ctx);

    }

    /**
     * 发送的返回值
     *
     * @param ctx     返回
     * @param context 消息
     * @param status  状态
     */
    private void send(ChannelHandlerContext ctx, String context, HttpResponseStatus status, HttpHeaders headers) {
        System.out.println("发送的消息：" + context);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(context, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        Set<Cookie> cookies;
        String value = headers.get(HttpHeaderNames.COOKIE); // 请求头中的 cookie
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Set cookie 到新到请求  response 中是否也应该 加上
            for (Cookie cookie : cookies) {
                System.out.println("cookie" + cookie.toString() + "=====");
                response.headers().add(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
