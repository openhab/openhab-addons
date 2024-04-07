package org.openhab.binding.pihole.internal.rest;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@NonNullByDefault
public class AdminService {
    private final String token;
    private final URI baseUrl;
    private final HttpClient client;
    private final Gson gson = new Gson();

    public AdminService(String token, URI baseUrl, HttpClient client) {
        this.token = token;
        this.baseUrl = baseUrl;
        this.client = client;
        if (this.client.isStopped()) {
            throw new IllegalStateException("HttpClient is stopped");
        }
    }

  public Optional<DnsStatistics> summary() throws ExecutionException, InterruptedException, TimeoutException {
        var url = baseUrl.resolve("/admin/api.php?summaryRaw&auth=" + token);
        var request = client.newRequest(url);
        var response = request.send();
        var content = response.getContentAsString();
        return Optional.ofNullable(gson.fromJson(content, DnsStatistics.class));
    }
}
