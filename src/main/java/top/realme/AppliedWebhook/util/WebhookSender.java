package top.realme.AppliedWebhook.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 用于发送 Webhook 请求的工具类。
 */
public final class WebhookSender {
    private WebhookSender() {}

    private static Logger LOGGER = LogUtils.getLogger();

    // 延迟初始化，避免在 mod 类加载时触发
    private static class Holder {
        static final HttpClient CLIENT = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }
    private static HttpClient client() { return Holder.CLIENT; }

    /**
     * 异步发送 Webhook 请求。
     *
     * @param url  Webhook 接收端的 URL
     * @param json 要发送的 JSON 字符串
     * @param token 用于认证的 token
     * @return 一个 CompletableFuture，包含 HttpResponse<String> 结果
     */
    public static CompletableFuture<HttpResponse<String>> sendAsync(String url, String json, String token) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        return client().sendAsync(req, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * 同步阻塞发送 Webhook 请求。
     *
     * @param url  Webhook 接收端的 URL
     * @param json 要发送的 JSON 字符串
     * @param token 用于认证的 token
     * @return HttpResponse<String> 包含响应结果，或 null 如果发生异常
     */
    public static  HttpResponse<String> sendBlocking(String url, String json, String token) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();


        try {
            return client().send(req, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return null;
        }


    }
}
