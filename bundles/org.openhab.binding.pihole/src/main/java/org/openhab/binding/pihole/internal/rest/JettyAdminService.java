package org.openhab.binding.pihole.internal.rest;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pihole.internal.rest.model.DnsStatistics;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;

@NonNullByDefault
public class JettyAdminService implements AdminService {
    private final String token;
    private final URI baseUrl;
    private final HttpClient client;
    private final Gson gson = new Gson();

    public JettyAdminService(String token, URI baseUrl, HttpClient client) {
        this.token = token;
        this.baseUrl = baseUrl;
        this.client = client;
        if (this.client.isStopped()) {
            throw new IllegalStateException("HttpClient is stopped");
        }
    }

  @Override
  public Optional<DnsStatistics> summary() throws ExecutionException, InterruptedException, TimeoutException {
        var url = baseUrl.resolve("/admin/api.php?summaryRaw&auth=" + token);
        var request = client.newRequest(url);
        var response = request.send();
        var content = response.getContentAsString();
        return Optional.ofNullable(gson.fromJson(content, DnsStatistics.class));
    }

    @Override
    public void disableBlocking(long seconds) throws ExecutionException, InterruptedException, TimeoutException {
        var url = baseUrl.resolve(format("/admin/api.php?disable=%s&auth=%s",seconds, token));
        var request = client.newRequest(url);
        request.send();
    }

    @Override
    public void enableBlocking() throws ExecutionException, InterruptedException, TimeoutException {
        var url = baseUrl.resolve(format("/admin/api.php?disable&auth=%s", token));
        var request = client.newRequest(url);
        request.send();
    }
}
