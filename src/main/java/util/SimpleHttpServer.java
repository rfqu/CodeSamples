package util;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ForkJoinPool;

public class SimpleHttpServer {
    public static int port = 8765;
    static InetSocketAddress inetSocketAddress = new InetSocketAddress(port);

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create();
        server.bind(inetSocketAddress, 0);

        HttpContext context = server.createContext("/", new EchoHandler());
        context.setAuthenticator(new Auth());

        server.setExecutor(ForkJoinPool.commonPool());
        server.start();
    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
/*            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
  */
            StringBuilder builder = new StringBuilder();

            URI requestURI = exchange.getRequestURI();
            builder.append("<h1>URI: ").append(requestURI).append("</h1>");

            Headers headers = exchange.getRequestHeaders();
            for (String header : headers.keySet()) {
                builder.append("<p>").append(header).append("=")
                        .append(headers.getFirst(header)).append("</p>");
            }

            String reply = builder.toString();
            byte[] bytes = reply.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class Auth extends Authenticator {
        @Override
        public Result authenticate(HttpExchange httpExchange) {
            if ("/forbidden".equals(httpExchange.getRequestURI().toString()))
                return new Failure(403);
            else
                return new Success(new HttpPrincipal("c0nst", "realm"));
        }
    }
}