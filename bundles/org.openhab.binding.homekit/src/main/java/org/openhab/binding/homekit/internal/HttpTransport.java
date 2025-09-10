package org.openhab.binding.homekit.internal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpTransport {

    private final HttpClient client;

    public HttpTransport() {
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    public byte[] post(String url, byte[] payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/pairing+tlv8").header("Accept", "application/pairing+tlv8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(payload)).build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Pairing failed: HTTP " + response.statusCode());
        }

        return response.body();
    }
}