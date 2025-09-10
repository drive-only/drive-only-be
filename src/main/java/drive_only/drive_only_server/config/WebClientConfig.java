package drive_only.drive_only_server.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
    private final static String TOUR_API_BASE_URL = "http://apis.data.go.kr/B551011/KorService2";
    private final static String GOOGLE_API_BASE_URL = "https://places.googleapis.com/v1";

    @Bean
    public WebClient tourApiWebClient() {
        HttpClient httpClient = createHttpClient();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(TOUR_API_BASE_URL);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return WebClient.builder()
                .baseUrl(TOUR_API_BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .uriBuilderFactory(factory)
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient googleApiWebClient() {
        HttpClient httpClient = createHttpClient();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(GOOGLE_API_BASE_URL);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return WebClient.builder()
                .baseUrl(GOOGLE_API_BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .uriBuilderFactory(factory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private HttpClient createHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 10초로 증가
                .responseTimeout(Duration.ofSeconds(15))             // 15초로 증가
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(15, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(15, TimeUnit.SECONDS))
                );
    }
}
