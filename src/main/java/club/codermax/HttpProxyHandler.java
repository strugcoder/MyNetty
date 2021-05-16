package club.codermax;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;

public class HttpProxyHandler extends SimpleChannelInboundHandler<HttpObject> {
    private String host;
    private int port;

    private static final byte[] con = {'h', 'e'};
    public HttpProxyHandler (String host, int port) {
        this.host = host;
        this.port = port;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            boolean keepAlive = HttpUtil.isKeepAlive(req);
            // 向远程主机发送 请求
            FullHttpRequest request = new DefaultFullHttpRequest(req.protocolVersion(), new HttpMethod("GET"), host);
            ByteBuf cont = request.content();
            System.out.println("request: " + cont.toString());
            FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK, Unpooled.wrappedBuffer(con));

            response.headers()
                    .set("content_type", "text/plain")
                    .setInt("content-length", cont.readableBytes());

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
