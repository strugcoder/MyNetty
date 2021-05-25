package club.codermax;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpProxyHandler extends SimpleChannelInboundHandler<HttpObject> {

    private String host;
    private int port;

    private static final byte[] con = {'h', 'e', 'h', 'e'};
    public HttpProxyHandler (String host, int port) {
        this.host = host;
        this.port = port;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            boolean keepAlive = HttpUtil.isKeepAlive(req);


            // 获取请求的路径
            URI uri = new URI(req.uri());

            StringBuilder responseContent = new StringBuilder();
            // 获取所有的请求头
            for (Map.Entry<String, String> entry : req.headers()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
                //responseContent.append();
            }
            Set<Cookie> cookies;
            String value = req.headers().get(HttpHeaderNames.COOKIE);
            if (value == null) {
                cookies = Collections.emptySet();
            } else {
                cookies = ServerCookieDecoder.STRICT.decode(value);
            }
            if (!cookies.isEmpty()) {
                // Set cookie 到新到请求  response 中是否也应该 加上
                for (Cookie cookie : cookies) {

                }
            }

            ProxyMain.clientRun(uri, req.headers());



            // 向远程主机发送 请求
            FullHttpRequest request = new DefaultFullHttpRequest(req.protocolVersion(), new HttpMethod("GET"), host);
            request.headers().add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().add(HttpHeaderNames.CONTENT_LENGTH,request.content().readableBytes());
            ctx.writeAndFlush(request);

            ByteBuf cont = request.content();
            System.out.println("request: " + cont.toString());
            FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK, Unpooled.wrappedBuffer(con));

            response.headers()
                    .set("content-type", "text/plain")
                    .setInt("content-length", response.content().readableBytes());

            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set("connection", "keep-alive");
                }
            }
            ByteBuf content = response.content();
            System.out.println("response: " + content.toString());
            ChannelFuture f = ctx.writeAndFlush(response);

            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }


}
