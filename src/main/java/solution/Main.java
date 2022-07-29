package solution;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class Main {
    public static final String REMOTE_SERVICE_URI_KEY = "https://api.nasa.gov/planetary/apod?api_key=PGnJPQLC2uYIuYIGERcGDAEEjNuqqBsN4e5WgNnc";
    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setUserAgent("Nasa")
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        HttpGet request = new HttpGet(REMOTE_SERVICE_URI_KEY);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse response = httpClient.execute(request);

        Arrays.stream(response.getAllHeaders()).forEach(System.out::println);

        String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

        NasaResponse nasa = mapper.reader().forType(NasaResponse.class).readValue(body);

        System.out.println(nasa);

        saveResponse(nasa.getUrl(), httpClient);

    }

    private static void saveResponse(String url, CloseableHttpClient httpClient) throws IOException {

        String name = url.replaceAll(".*\\/\\.*", "");

        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(request);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getEntity().writeTo(out);
        String body = Base64.getEncoder().encodeToString(out.toByteArray());
        byte[] decoded = Base64.getDecoder().decode(body);

        OutputStream outputStream = new FileOutputStream(name);
        outputStream.write(decoded);
        outputStream.flush();
        outputStream.close();
    }
}
