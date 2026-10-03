/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipsair.internal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.mockito.invocation.InvocationOnMock;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirCipher;
import org.openhab.core.util.HexUtils;

/**
 * Simulates the HTTP API of a purifier behind a mocked {@link HttpClient}. The responses are scripted per URL and
 * method, so the outcome does not depend on the order or the number of requests of the other URLs. All requests are
 * recorded, to verify the URLs, methods and bodies that were used.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class FakeHttpDevice {

    /**
     * The URLs of the device API. These are spelled out here instead of using the constants of the binding.
     */
    public enum Endpoint {
        DEVICE("/di/v1/products/1/device"),
        STATUS("/di/v1/products/1/air"),
        FILTERS("/di/v1/products/1/fltsts"),
        SECURITY("/di/v1/products/0/security");

        private final String path;

        Endpoint(String path) {
            this.path = path;
        }
    }

    /**
     * A request sent to the device.
     */
    public record Call(String url, HttpMethod method, @Nullable String body) {
    }

    private record Reply(int status, String content) {
    }

    /**
     * The replies for a URL and method. The last reply is repeated.
     */
    private static class Script {
        private final List<Reply> replies;
        private int next;

        Script(List<Reply> replies) {
            this.replies = replies;
        }

        synchronized Reply next() {
            Reply reply = replies.get(Math.min(next, replies.size() - 1));
            next++;
            return reply;
        }
    }

    private static class RequestState {
        HttpMethod method = HttpMethod.GET;
        @Nullable
        String body;
    }

    private final String host;
    private final String key;
    private final HttpClient httpClient;
    private final Map<String, Script> scripts = new HashMap<>();
    private final List<Call> calls = new CopyOnWriteArrayList<>();
    private volatile @Nullable String statusAfterCommand;

    /**
     * @param host the host the device is requested with
     * @param key the key the device encrypts its responses with, as hex
     */
    public FakeHttpDevice(String host, String key) {
        this.host = host;
        this.key = key;
        this.httpClient = mock(HttpClient.class, withSettings().defaultAnswer(this::answerHttpClient));
    }

    public HttpClient httpClient() {
        return httpClient;
    }

    public String url(Endpoint endpoint) {
        return "http://" + host + endpoint.path;
    }

    /**
     * Encrypts a text as the device does with the given key.
     */
    public static String encrypt(String content, String key) throws GeneralSecurityException {
        PhilipsAirCipher cipher = new PhilipsAirCipher(BigInteger.ONE);
        cipher.initKey(key);
        String encrypted = cipher.encrypt(content);
        assertNotNull(encrypted);
        return encrypted;
    }

    /**
     * Builds the reply of the key exchange. With 'hellman' 1 the shared secret is 1 for any private exponent, so the
     * session key is encrypted with the first 16 bytes of its 128 byte encoding, which are all zero.
     */
    public static String trivialKeyExchangeResponse(String sessionKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES"), new IvParameterSpec(new byte[16]));
        String encryptedKey = HexUtils.bytesToHex(cipher.doFinal(HexUtils.hexToBytes(sessionKey)));
        return "{\"key\":\"" + encryptedKey + "\",\"hellman\":\"1\"}";
    }

    /**
     * Scripts successful responses, encrypted with the key of the device.
     */
    public void respond(Endpoint endpoint, HttpMethod method, String... contents) throws GeneralSecurityException {
        List<Reply> replies = new ArrayList<>();
        for (String content : contents) {
            replies.add(new Reply(200, encrypt(content, key)));
        }
        script(endpoint, method, replies);
    }

    /**
     * Scripts responses as they are sent over the wire.
     */
    public void respondRaw(Endpoint endpoint, HttpMethod method, int status, String... contents) {
        List<Reply> replies = new ArrayList<>();
        for (String content : contents) {
            replies.add(new Reply(status, content));
        }
        script(endpoint, method, replies);
    }

    /**
     * Scripts the answer to the key exchange, which makes the device use its key.
     */
    public void respondToKeyExchange() throws GeneralSecurityException {
        respondRaw(Endpoint.SECURITY, HttpMethod.PUT, 200, trivialKeyExchangeResponse(key));
    }

    /**
     * Scripts the answer to a command, which is the new status. The device reports that status from the first command
     * on, until then it reports the status that was scripted for the status requests.
     */
    public void respondToCommandWithStatus(String status) throws GeneralSecurityException {
        respond(Endpoint.STATUS, HttpMethod.PUT, status);
        statusAfterCommand = encrypt(status, key);
    }

    public List<Call> calls() {
        return List.copyOf(calls);
    }

    public List<Call> calls(Endpoint endpoint, HttpMethod method) {
        return calls.stream().filter(call -> call.url().equals(url(endpoint)) && call.method() == method).toList();
    }

    public int count(Endpoint endpoint, HttpMethod method) {
        return calls(endpoint, method).size();
    }

    /**
     * @return the text of the body of a command, decrypted with the key of the device
     */
    public String decryptedBody(Call call) throws GeneralSecurityException {
        return decryptedBody(call, key);
    }

    /**
     * @return the text of the body of a command, decrypted with the given key
     */
    public static String decryptedBody(Call call, String key) throws GeneralSecurityException {
        String body = call.body();
        assertNotNull(body);
        PhilipsAirCipher cipher = new PhilipsAirCipher(BigInteger.ONE);
        cipher.initKey(key);
        try {
            return cipher.decrypt(body);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new GeneralSecurityException(e);
        }
    }

    private synchronized void script(Endpoint endpoint, HttpMethod method, List<Reply> replies) {
        scripts.put(scriptKey(endpoint, method), new Script(replies));
    }

    private static String scriptKey(Endpoint endpoint, HttpMethod method) {
        return method + " " + endpoint;
    }

    /**
     * The answer to the calls that are not simulated: the default value of the return type.
     */
    private static @Nullable Object defaultAnswer(InvocationOnMock invocation) {
        Class<?> returnType = invocation.getMethod().getReturnType();
        return returnType.isPrimitive() && returnType != void.class ? Array.get(Array.newInstance(returnType, 1), 0)
                : null;
    }

    private @Nullable Object answerHttpClient(InvocationOnMock invocation) {
        if ("newRequest".equals(invocation.getMethod().getName()) && invocation.getArgument(0) instanceof String url) {
            return newRequest(url);
        }
        return defaultAnswer(invocation);
    }

    private Request newRequest(String url) {
        RequestState state = new RequestState();
        return mock(Request.class, withSettings().defaultAnswer(invocation -> {
            switch (invocation.getMethod().getName()) {
                case "method":
                    state.method = invocation.getArgument(0);
                    return invocation.getMock();
                case "content":
                    state.body = read(invocation.getArgument(0));
                    return invocation.getMock();
                case "timeout":
                    return invocation.getMock();
                case "send":
                    return reply(url, state.method, state.body);
                default:
                    return defaultAnswer(invocation);
            }
        }));
    }

    private static String read(ContentProvider provider) {
        StringBuilder body = new StringBuilder();
        for (ByteBuffer buffer : provider) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.duplicate().get(bytes);
            body.append(new String(bytes, StandardCharsets.UTF_8));
        }
        return body.toString();
    }

    private ContentResponse reply(String url, HttpMethod method, @Nullable String body) {
        calls.add(new Call(url, method, body));
        Reply reply = new Reply(404, "Not Found");
        for (Endpoint endpoint : Endpoint.values()) {
            if (url(endpoint).equals(url)) {
                Script script;
                synchronized (this) {
                    script = scripts.get(scriptKey(endpoint, method));
                }
                if (script != null) {
                    reply = script.next();
                }
            }
        }
        String newStatus = statusAfterCommand;
        if (method == HttpMethod.PUT && url.equals(url(Endpoint.STATUS)) && newStatus != null) {
            script(Endpoint.STATUS, HttpMethod.GET, List.of(new Reply(200, newStatus)));
        }
        Reply result = reply;
        return mock(ContentResponse.class, withSettings().defaultAnswer(invocation -> {
            switch (invocation.getMethod().getName()) {
                case "getStatus":
                    return result.status();
                case "getContentAsString":
                    return result.content();
                default:
                    return defaultAnswer(invocation);
            }
        }));
    }
}
