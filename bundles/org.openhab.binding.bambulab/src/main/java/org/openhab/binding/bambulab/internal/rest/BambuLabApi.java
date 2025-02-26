package org.openhab.binding.bambulab.internal.rest;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.jetty.http.HttpHeader.AUTHORIZATION;

/**
 * Here you can find Bambu API - https://github.com/Doridian/OpenBambuAPI/blob/main/cloud-http.md
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class BambuLabApi {
    private static final String LOGIN = "/user-service/user/login";
    private static final String MY_PREFERENCE = "/design-user-service/my/preference";
    private static final int TIMEOUT = 10;
    private static final int IDLE_TIMEOUT = TIMEOUT;
    private final Gson gson = new Gson();
    private final HttpClient client;
    private final String baseUrl;

    public BambuLabApi(HttpClient client, String baseUrl) {
        this.client = client;
        if (this.client.isStopped()) {
            throw new IllegalStateException("HttpClient is stopped");
        }
        this.baseUrl = baseUrl;
    }

    public void login(String username, String password) {
        var uri = baseUrl + LOGIN;
        var request = client.POST(uri);
        var loginForm = "{\"account\": \"%s\",\"password\":\"%s\"}".formatted(username, password);
        request.content(new StringContentProvider(loginForm, UTF_8));
        request.header("Content-Type", "application/json");
        execute(request, uri);
    }

    @Nullable
    public TokenResponse verificationCode(String username, String verificationCode) {
        var url = baseUrl + LOGIN;
        var request = client.POST(url);
        var loginForm = "{\"account\": \"%s\",\"code\":\"%s\"}".formatted(username, verificationCode);
        request.content(new StringContentProvider(loginForm, UTF_8));
        request.header("Content-Type", "application/json");
        var response = execute(request, url);
        return gson.fromJson(response, TokenResponse.class);
    }

    @Nullable
    public UserProfile queryMyPreference(String accessToken) {
        var url = baseUrl + MY_PREFERENCE;
        var request = client.newRequest(url);
        request.header(AUTHORIZATION, "Bearer " + accessToken);
        var response = execute(request, url);
        return gson.fromJson(response, UserProfile.class);
    }

    private @Nullable String execute(Request request, String url)/* throws SalusApiException*/ {
        try {
            request.timeout(TIMEOUT, SECONDS);
            request.idleTimeout(IDLE_TIMEOUT, SECONDS);
            var response = request.send();
            var status = response.getStatus();
            if (status < 200 || status >= 399) {
                throw new RuntimeException(status + " " + response.getReason());
            }
            return response.getContentAsString();
        } catch (RuntimeException | TimeoutException | ExecutionException | InterruptedException ex) {
            Throwable cause = ex;
            while (cause != null) {
                if (cause instanceof HttpResponseException hte) {
                    var response = hte.getResponse();
                    throw new RuntimeException(response.getStatus() + " " + response.getReason(), hte);
                }
                cause = cause.getCause();
            }
            throw new RuntimeException("Error while executing request to " + url, ex);
        }
    }

}
