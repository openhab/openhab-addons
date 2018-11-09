/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.netutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An asynchronous API for HTTP interaction. Uses Javas {@link HttpURLConnection} internally.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class AsyncHttpClient {
    /**
     * Perform a GET request
     *
     * @param address The address
     * @param timeout A timeout
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public static Result get(String address, int timeout) throws IOException {
        return doNetwork(new Parameters(address, "GET", "", timeout));
    }

    /**
     * Perform a POST request
     *
     * @param address The address
     * @param body The message body
     * @param timeout A timeout
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public static Result post(String address, String body, int timeout) throws IOException {
        return doNetwork(new Parameters(address, "POST", body, timeout));
    }

    /**
     * Perform a PUT request
     *
     * @param address The address
     * @param body The message body
     * @param timeout A timeout
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public static Result put(String address, String body, int timeout) throws IOException {
        return doNetwork(new Parameters(address, "PUT", body, timeout));
    }

    /**
     * Perform an asynchronous PUT request
     *
     * @param address The address
     * @param body The message body
     * @param scheduler A scheduler for the thread creation
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public static CompletableFuture<Result> putAsync(String address, String body, ScheduledExecutorService scheduler) {
        return CompletableFuture.supplyAsync(() -> new Parameters(address, "PUT", body, 1000), scheduler)
                .thenApply(AsyncHttpClient::doNetwork);
    }

    /**
     * Perform an asynchronous POST request
     *
     * @param address The address
     * @param body The message body
     * @param scheduler A scheduler for the thread creation
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public static CompletableFuture<Result> postAsync(String address, String body, ScheduledExecutorService scheduler) {
        return CompletableFuture.supplyAsync(() -> new Parameters(address, "POST", body, 1000), scheduler)
                .thenApply(AsyncHttpClient::doNetwork);
    }

    /**
     * Perform an asynchronous GET request
     *
     * @param address The address
     * @param scheduler A scheduler for the thread creation
     * @return The result
     * @throws IOException Any IO exception in an error case.
     */
    public static CompletableFuture<Result> getAsync(String address, ScheduledExecutorService scheduler) {
        return CompletableFuture.supplyAsync(() -> new Parameters(address, "GET", "", 1000), scheduler)
                .thenApply(AsyncHttpClient::doNetwork);
    }

    public static Result delete(String address, int timeout) throws IOException {
        return doNetwork(new Parameters(address, "DELETE", "", timeout));
    }

    private static Result doNetwork(Parameters p) {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) new URL(p.address).openConnection();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        try (AutoCloseable conc = () -> conn.disconnect()) {
            conn.setRequestMethod(p.requestMethod);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(p.timeout);
            conn.setReadTimeout(p.timeout);

            String body = p.body;
            if (body != null && !body.equals("")) {
                conn.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(body);
                out.close();
            }

            if (conn.getResponseCode() / 100 == 2) {
                String output = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                return new Result(output, conn.getResponseCode());
            } else {
                String output = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                return new Result(output, conn.getResponseCode());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Result {
        private final String body;
        private final int responseCode;

        public Result(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }

        public String getBody() {
            return body;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }

    public static final class Parameters {
        public final String requestMethod;
        public final String address;
        public final @Nullable String body;
        public final CompletableFuture<Result> future;
        public final Integer timeout;

        public Parameters(String address, String requestMethod, @Nullable String body, Integer timeout) {
            this.address = address;
            this.requestMethod = requestMethod;
            this.body = body;
            this.future = new CompletableFuture<Result>();
            this.timeout = timeout;
        }
    }
}
