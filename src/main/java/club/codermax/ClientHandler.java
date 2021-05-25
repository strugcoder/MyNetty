package club.codermax;

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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

/**
 * 客户端处理器
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private FullHttpRequest request;
    private ChannelHandlerContext serverCtx;

    public ClientHandler(FullHttpRequest request, ChannelHandlerContext serverCtx) {
        this.request = request;
        this.serverCtx = serverCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpResponse response = (FullHttpResponse) msg;


        serverCtx.writeAndFlush(response);

        /*ByteBuf content = response.content();
        HttpHeaders headers = response.headers();



        System.out.println("content:" + content.toString(CharsetUtil.UTF_8));
        System.out.println("headers:" + headers.toString());


//        send(serverCtx, content.toString(CharsetUtil.UTF_8), HttpResponseStatus.OK, response);
        send(serverCtx, content.toString(), HttpResponseStatus.OK, response);*/
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        String path = request.uri();  // 请求路径
        HttpHeaders headers = request.headers();
        String body = request.content().toString(CharsetUtil.UTF_8); //获取 body

        HttpMethod method = request.method(); //   请求方法
        System.out.println("接收到:" + method + " 请求, 请求路径为：" + path);


        URI url = new URI(path);

        //配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest req = new DefaultFullHttpRequest(
                request.protocolVersion(), HttpMethod.GET, url.toASCIIString(), Unpooled.wrappedBuffer(body.getBytes(StandardCharsets.UTF_8)));

        req.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8")
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                // cookie
                // .set(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode("key1", "value1"))

                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, req.content().readableBytes());

        //发送数据
        ctx.writeAndFlush(req);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private void send(ChannelHandlerContext ctx, String context, HttpResponseStatus status, FullHttpResponse resp) {
        System.out.println("发送的消息：" + context);
        HttpHeaders headers = resp.headers();
        FullHttpResponse response = new DefaultFullHttpResponse(resp.protocolVersion(), status, Unpooled.wrappedBuffer(context.getBytes(StandardCharsets.UTF_8)));

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
                System.out.println("cookie: " + cookie.toString() + "=====");
                response.headers().add(HttpHeaderNames.COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}