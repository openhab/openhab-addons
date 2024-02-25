package org.openhab.binding.salus.internal.rest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryHttpClient implements RestClient {
    private final Logger logger = LoggerFactory.getLogger(RetryHttpClient.class);
    private final RestClient restClient;
    private final int maxRetries;

    public RetryHttpClient(RestClient restClient, int maxRetries) {
        this.restClient = restClient;
        if (maxRetries <= 0) {
            throw new IllegalArgumentException("maxRetries cannot be lower or equal to 0, but was " + maxRetries);
        }
        this.maxRetries = maxRetries;
    }

    @Override
    public Response<@Nullable String> get(String url, @Nullable Header... headers)
            throws ExecutionException, InterruptedException, TimeoutException {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return restClient.get(url, headers);
            } catch (RuntimeException | ExecutionException | InterruptedException | TimeoutException ex) {
                if (i < maxRetries - 1) {
                    logger.debug("Error while calling GET {}. Retrying {}/{}...", i + 1, maxRetries, url, ex);
                } else {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Should not happen!");
    }

    @Override
    public Response<@Nullable String> post(String url, Content content, @Nullable Header... headers)
            throws ExecutionException, InterruptedException, TimeoutException {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return restClient.post(url, content, headers);
            } catch (RuntimeException | ExecutionException | InterruptedException | TimeoutException ex) {
                if (i < maxRetries - 1) {
                    logger.debug("Error while calling POST {}. Retrying {}/{}...", i + 1, maxRetries, url, ex);
                } else {
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Should not happen!");
    }
}
