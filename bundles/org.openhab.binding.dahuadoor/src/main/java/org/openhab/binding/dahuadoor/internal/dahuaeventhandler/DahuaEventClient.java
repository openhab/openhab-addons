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
package org.openhab.binding.dahuadoor.internal.dahuaeventhandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link DahuaEventClient} client polls the Dahua device
 *
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class DahuaEventClient implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(DahuaEventClient.class);

    private final String host;
    private final String username;
    private final String password;
    private int id = 0; // Our Request / Response ID that must be in all requests and initiated by us
    private int sessionId = 0; // Session ID will be returned after successful login
    private String fakeIpAddr = "(null)"; // WebGUI: mask our real IP
    private String clientType = ""; // WebGUI: We do not show up in logs or online users
    private int keepAliveInterval = 60;
    private long lastKeepAlive = 0;

    private DHIPEventListener eventListener;

    private @Nullable Socket sock;
    private final Gson gson = new Gson();
    private volatile boolean execThread = true;
    private Consumer<String> errorInformer;
    private ByteBuffer residualBuffer = ByteBuffer.allocate(0); // Buffer for incomplete frames across reads

    private static final int HTTP_TIMEOUT_MS = 750;
    private static final String SNAPSHOT_PATH = "/cgi-bin/snapshot.cgi";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final SSLSocketFactory BC_SSL_SOCKET_FACTORY = buildBcSslSocketFactory();

    /** Whether to use HTTPS (port 443) or HTTP (port 80) for snapshot and door-open requests. */
    private final boolean httpsAvailable;

    public DahuaEventClient(String host, String username, String password, boolean useHttps,
            DHIPEventListener eventListener, Consumer<String> errorInformer) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.eventListener = eventListener;
        this.errorInformer = errorInformer;
        this.execThread = true;
        this.httpsAvailable = useHttps;
        logger.debug("HTTPS {} for {}", useHttps ? "enabled" : "disabled", host);
        ThreadPoolManager.getPool("binding.dahuadoor").submit(this);
    }

    public void dispose() {
        this.execThread = false;
        // Close socket to unblock any pending read() operations
        final Socket localSock = sock;
        if (localSock != null) {
            try {
                localSock.close();
            } catch (IOException e) {
                logger.trace("Error closing socket during dispose: {}", e.getMessage());
            }
        }
    }

    /**
     * Requests a JPEG snapshot image from the device via HTTP.
     *
     * @return JPEG image bytes, or null if the request failed
     */
    public byte @Nullable [] requestImage() {
        logger.debug("Requesting snapshot from {}", host);
        try {
            return sendSnapshotRequest(SNAPSHOT_PATH);
        } catch (DahuaHttpRedirectException e) {
            errorInformer.accept(e.getRedirectMessage());
        } catch (Exception e) {
            logger.warn("Could not retrieve snapshot from {}", host, e);
        }
        return null;
    }

    /**
     * Opens the specified door via HTTP.
     *
     * @param doorNo door number (1 or 2)
     */
    public void openDoor(int doorNo) {
        try {
            String path = "/cgi-bin/accessControl.cgi?action=openDoor&UserID=101&Type=Remote&channel=" + doorNo;
            sendOpenDoorRequest(path);
        } catch (DahuaHttpRedirectException e) {
            errorInformer.accept(e.getRedirectMessage());
        } catch (Exception e) {
            logger.warn("Could not open door {} on {}", doorNo, host, e);
        }
    }

    /**
     * Sends a snapshot request with Digest auth.
     * Uses HTTPS or HTTP based on the cached {@link #httpsAvailable} flag.
     * Step 1: open connection, get 401 + challenge, close connection.
     * Step 2: open fresh connection, send authenticated request.
     */
    private byte @Nullable [] sendSnapshotRequest(String path) throws Exception {
        boolean useHttps = httpsAvailable;
        int port = useHttps ? 443 : 80;
        int maxAttempts = 3;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                // Step 1: unauthenticated request — get 401 + Digest challenge, then close socket
                String digestHeader = null;
                try (Socket socket = useHttps ? buildTlsSocket(port) : plainSocket(port)) {
                    socket.setSoTimeout(HTTP_TIMEOUT_MS);
                    writeHttpGet(socket.getOutputStream(), path, null, false);
                    SnapshotHttpResponse response = parseSnapshotResponse(socket.getInputStream());
                    if (response.statusCode == HttpStatus.OK_200) {
                        logger.debug("Snapshot OK: {} bytes from {}", response.body.length, host);
                        return response.body;
                    }
                    if (response.statusCode == HttpStatus.UNAUTHORIZED_401) {
                        String challenge = response.headers.get("www-authenticate");
                        if (challenge != null) {
                            digestHeader = createDigestAuthorizationHeader(challenge, path);
                        }
                    } else {
                        if (response.statusCode == HttpStatus.MOVED_TEMPORARILY_302) {
                            throw new DahuaHttpRedirectException("Snapshot request to " + host
                                    + " redirected (HTTP 302) - device may require HTTPS; enable 'Use HTTPS' in the thing configuration");
                        } else {
                            logger.warn("Snapshot request to {} failed with unexpected HTTP status {}", host,
                                    response.statusCode);
                        }
                        return null;
                    }
                }
                // First socket is now fully closed before opening the second.
                // Brief pause to let the device finish processing the TLS close_notify and
                // free its connection slot before we open the authenticated connection.
                Thread.sleep(300);

                // Step 2: authenticated request on a fresh connection
                if (digestHeader != null) {
                    try (Socket authSocket = useHttps ? buildTlsSocket(port) : plainSocket(port)) {
                        authSocket.setSoTimeout(HTTP_TIMEOUT_MS);
                        writeHttpGet(authSocket.getOutputStream(), path, digestHeader, false);
                        SnapshotHttpResponse authResponse = parseSnapshotResponse(authSocket.getInputStream());
                        if (authResponse.statusCode == HttpStatus.OK_200) {
                            logger.debug("Snapshot OK: {} bytes from {}", authResponse.body.length, host);
                            return authResponse.body;
                        }
                        if (authResponse.statusCode == HttpStatus.MOVED_TEMPORARILY_302) {
                            throw new DahuaHttpRedirectException("Snapshot request to " + host
                                    + " redirected (HTTP 302) - device may require HTTPS; enable 'Use HTTPS' in the thing configuration");
                        } else {
                            logger.warn("Authenticated snapshot request to {} failed with unexpected HTTP status {}",
                                    host, authResponse.statusCode);
                        }
                        return null;
                    }
                }
                logger.debug("Snapshot request failed: no Digest challenge received from {}", host);
                return null;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            } catch (IOException e) {
                if (attempt < maxAttempts - 1) {
                    long delayMs = 750L * (1 << attempt); // 750 ms, 1.5 s
                    logger.debug("Snapshot attempt {}/{} via {} to {} failed ({}), retrying in {} ms", attempt + 1,
                            maxAttempts, useHttps ? "HTTPS" : "HTTP", host, e.getMessage(), delayMs);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new IOException("All snapshot attempts failed for " + host);
    }

    /**
     * Sends an open-door request with Digest auth.
     * Same sequential two-connection pattern as sendSnapshotRequest.
     */
    private void sendOpenDoorRequest(String path) throws Exception {
        boolean useHttps = httpsAvailable;
        int port = useHttps ? 443 : 80;
        int maxAttempts = 3;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                String digestHeader = null;
                try (Socket socket = useHttps ? buildTlsSocket(port) : plainSocket(port)) {
                    socket.setSoTimeout(HTTP_TIMEOUT_MS);
                    writeHttpGet(socket.getOutputStream(), path, null, false);
                    OpenDoorHttpResponse response = parseOpenDoorResponse(socket.getInputStream());
                    if (response.statusCode == HttpStatus.OK_200) {
                        logger.debug("Open Door Success");
                        return;
                    }
                    if (response.statusCode == HttpStatus.UNAUTHORIZED_401) {
                        String challenge = response.wwwAuthenticate;
                        if (challenge != null) {
                            digestHeader = createDigestAuthorizationHeader(challenge, path);
                        }
                    } else {
                        if (response.statusCode == HttpStatus.MOVED_TEMPORARILY_302) {
                            throw new DahuaHttpRedirectException("Open door request to " + host
                                    + " redirected (HTTP 302) - device may require HTTPS; enable 'Use HTTPS' in the thing configuration");
                        } else {
                            logger.warn("Open door request to {} failed with unexpected HTTP status {}", host,
                                    response.statusCode);
                        }
                        return;
                    }
                }
                // First socket is now fully closed before opening the second
                Thread.sleep(300);

                if (digestHeader != null) {
                    try (Socket authSocket = useHttps ? buildTlsSocket(port) : plainSocket(port)) {
                        authSocket.setSoTimeout(HTTP_TIMEOUT_MS);
                        writeHttpGet(authSocket.getOutputStream(), path, digestHeader, false);
                        OpenDoorHttpResponse authResponse = parseOpenDoorResponse(authSocket.getInputStream());
                        if (authResponse.statusCode == HttpStatus.OK_200) {
                            logger.debug("Open Door Success (with authentication)");
                        } else if (authResponse.statusCode == HttpStatus.MOVED_TEMPORARILY_302) {
                            throw new DahuaHttpRedirectException("Open door request to " + host
                                    + " redirected (HTTP 302) - device may require HTTPS; enable 'Use HTTPS' in the thing configuration");
                        } else {
                            logger.warn("Open door request to {} failed with unexpected HTTP status {}", host,
                                    authResponse.statusCode);
                        }
                        return;
                    }
                }
                logger.debug("Open door request failed: no Digest challenge received from {}", host);
                return;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (IOException e) {
                if (attempt < maxAttempts - 1) {
                    long delayMs = 750L * (1 << attempt); // 750 ms, 1.5 s
                    logger.debug("Open door attempt {}/{} via {} to {} failed ({}), retrying in {} ms", attempt + 1,
                            maxAttempts, useHttps ? "HTTPS" : "HTTP", host, e.getMessage(), delayMs);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new IOException("All open door attempts failed for " + host);
    }

    private Socket plainSocket(int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), HTTP_TIMEOUT_MS);
        return socket;
    }

    private Socket buildTlsSocket(int port) throws IOException {
        // Dahua firmware uses TLS_RSA_WITH_AES_256_GCM_SHA384, removed from Java 21 built-in TLS.
        // BCJSSE supports this cipher and provides standard SSLSocket close() semantics:
        // graceful TLS close_notify + TCP FIN (no TCP RST), which is required for the device
        // to immediately accept the next connection for the authenticated request.
        //
        // OSGi/Karaf may already have a BCJSSE bundle installed whose class loader differs from our
        // embedded copy. BCJSSE loads internal classes (e.g. SSLSocketUtil) via the thread context
        // class loader. We temporarily set it to our embedded BouncyCastleJsseProvider's class
        // loader so that BCJSSE's internal class lookups stay within our bundle.
        Socket tcpSocket = new Socket();
        ClassLoader savedCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(BouncyCastleJsseProvider.class.getClassLoader());
            tcpSocket.connect(new InetSocketAddress(host, port), HTTP_TIMEOUT_MS);
            SSLSocket sslSocket = (SSLSocket) BC_SSL_SOCKET_FACTORY.createSocket(tcpSocket, host, port, true);
            sslSocket.setSoTimeout(HTTP_TIMEOUT_MS);
            sslSocket.setEnabledProtocols(new String[] { "TLSv1.2" });
            sslSocket.setEnabledCipherSuites(new String[] { "TLS_RSA_WITH_AES_256_GCM_SHA384" });
            sslSocket.startHandshake();
            logger.debug("BCJSSE TLS connected to {}:{} using TLS_RSA_WITH_AES_256_GCM_SHA384", host, port);
            return sslSocket;
        } catch (ConnectException e) {
            // Port unreachable: re-throw directly so callers can detect it without unwrapping
            tcpSocket.close();
            throw e;
        } catch (Exception e) {
            tcpSocket.close();
            throw new IOException("TLS handshake failed to " + host + ":" + port + ": " + e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(savedCl);
        }
    }

    /**
     * Builds the BCJSSE {@link SSLSocketFactory} once at class load time.
     * Uses directly instantiated BouncyCastle provider instances with the {@link SSLContext},
     * without registering them globally, so that TLS_RSA_WITH_AES_256_GCM_SHA384 (removed from
     * Java 21 built-in TLS) is available.
     */
    private static SSLSocketFactory buildBcSslSocketFactory() {
        // Use our embedded BouncyCastleJsseProvider instance directly for both the TrustManager
        // and the SSLContext (not via the global Security registry).
        //
        // Why not TrustManagerFactory.getInstance(alg) / Security.getProviders() approach:
        // - JCA provider selection uses the global registry; Karaf globally registers its own
        // bctls bundle's BouncyCastleJsseProvider at higher priority than SunJSSE.
        // - Using Karaf's globally-registered BCJSSE factory fails with NoClassDefFoundError
        // (ProvTrustManagerFactorySpi is in Karaf's bundle, invisible to our classloader).
        // - Using SunJSSE's X509TrustManagerImpl fails because BCJSSE passes authType "KE:RSA"
        // to checkServerTrusted(); SunJSSE does not recognise that format → certificate_unknown.
        //
        // Solution: supply our local embedded bcjsse instance as the explicit Provider to both
        // TrustManagerFactory.getInstance() and SSLContext.getInstance(). Class loading then
        // goes through bcjsse's own classloader (our embedded classes), not Karaf's bundle.
        // BCJSSE's trust manager also speaks BCJSSE's own authType format natively.
        ClassLoader savedCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(BouncyCastleJsseProvider.class.getClassLoader());
            BouncyCastleJsseProvider bcjsse = new BouncyCastleJsseProvider(new BouncyCastleProvider());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm(),
                    bcjsse);
            tmf.init((KeyStore) null);
            SSLContext ctx = SSLContext.getInstance("TLS", bcjsse);
            ctx.init(null, tmf.getTrustManagers(), SECURE_RANDOM);
            return ctx.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new IllegalStateException("Failed to initialize BCJSSE SSL context", e);
        } finally {
            Thread.currentThread().setContextClassLoader(savedCl);
        }
    }

    private void writeHttpGet(OutputStream out, String path, @Nullable String authorizationHeader, boolean keepAlive)
            throws IOException {
        StringBuilder req = new StringBuilder();
        req.append("GET ").append(path).append(" HTTP/1.1\r\n");
        req.append("Host: ").append(host).append("\r\n");
        if (authorizationHeader != null) {
            req.append("Authorization: ").append(authorizationHeader).append("\r\n");
        }
        req.append("Connection: ").append(keepAlive ? "keep-alive" : "close").append("\r\n\r\n");
        out.write(req.toString().getBytes(StandardCharsets.US_ASCII));
        out.flush();
    }

    private SnapshotHttpResponse parseSnapshotResponse(InputStream in) throws IOException {
        byte[] raw = readHttpResponse(in);
        int headerEnd = findHeaderEnd(raw);
        if (headerEnd < 0) {
            return new SnapshotHttpResponse(0, Map.of(), new byte[0]);
        }
        String headerSection = new String(raw, 0, headerEnd, StandardCharsets.US_ASCII);
        String[] lines = headerSection.split("\r\n");
        int statusCode = parseStatusCode(lines[0]);
        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0) {
                headers.put(lines[i].substring(0, colon).trim().toLowerCase(Locale.ROOT),
                        lines[i].substring(colon + 1).trim());
            }
        }
        byte[] body = Arrays.copyOfRange(raw, headerEnd + 4, raw.length);
        return new SnapshotHttpResponse(statusCode, headers, body);
    }

    private OpenDoorHttpResponse parseOpenDoorResponse(InputStream in) throws IOException {
        byte[] raw = readHttpResponse(in);
        int headerEnd = findHeaderEnd(raw);
        if (headerEnd < 0) {
            return new OpenDoorHttpResponse(0, null);
        }
        String headerSection = new String(raw, 0, headerEnd, StandardCharsets.US_ASCII);
        String[] lines = headerSection.split("\r\n");
        int statusCode = parseStatusCode(lines[0]);
        String wwwAuthenticate = null;
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0 && "www-authenticate".equals(lines[i].substring(0, colon).trim().toLowerCase(Locale.ROOT))) {
                wwwAuthenticate = lines[i].substring(colon + 1).trim();
                break;
            }
        }
        return new OpenDoorHttpResponse(statusCode, wwwAuthenticate);
    }

    /**
     * Reads an HTTP response in a TLS-safe way: reads headers until the blank line,
     * then reads the body via Content-Length or chunked transfer encoding.
     * Avoids waiting for EOF/close_notify which old Dahua firmware may never send.
     */
    private byte[] readHttpResponse(InputStream in) throws IOException {
        // Read byte-by-byte until \r\n\r\n (end of headers)
        ByteArrayOutputStream headerBuf = new ByteArrayOutputStream(4096);
        int b;
        int last4 = 0;
        while ((b = in.read()) != -1) {
            headerBuf.write(b);
            last4 = ((last4 << 8) | b) & 0xFFFFFFFF;
            if (last4 == 0x0D0A0D0A) { // \r\n\r\n
                break;
            }
            if (headerBuf.size() > 65536) {
                break; // safety limit
            }
        }
        byte[] headerBytes = headerBuf.toByteArray();
        String headerSection = new String(headerBytes, StandardCharsets.US_ASCII);
        // Check for chunked transfer encoding
        boolean isChunked = false;
        int contentLength = -1;
        for (String line : headerSection.split("\r\n")) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.startsWith("transfer-encoding:") && lower.contains("chunked")) {
                isChunked = true;
            } else if (lower.startsWith("content-length:")) {
                try {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                } catch (NumberFormatException e) {
                    // ignore malformed header
                }
            }
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream(headerBytes.length + Math.max(contentLength, 0));
        result.write(headerBytes);

        if (isChunked) {
            // Read chunked body: each chunk is preceded by hex size line
            byte[] chunkBody = readChunkedBody(in);
            result.write(chunkBody);
        } else if (contentLength > 0) {
            // Read exactly contentLength bytes
            byte[] body = new byte[contentLength];
            int offset = 0;
            while (offset < contentLength) {
                int n = in.read(body, offset, contentLength - offset);
                if (n == -1) {
                    break;
                }
                offset += n;
            }
            result.write(body, 0, offset);
        }
        return result.toByteArray();
    }

    private byte[] readChunkedBody(InputStream in) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream(65536);
        byte[] lineBuf = new byte[64];
        while (true) {
            // Read chunk size line (hex digits followed by \r\n)
            int lineLen = 0;
            int prev = 0;
            int cur;
            while ((cur = in.read()) != -1) {
                if (prev == '\r' && cur == '\n') {
                    break;
                }
                if (cur != '\r') {
                    if (lineLen < lineBuf.length) {
                        lineBuf[lineLen++] = (byte) cur;
                    }
                }
                prev = cur;
            }
            if (lineLen == 0) {
                break;
            }
            String sizeLine = new String(lineBuf, 0, lineLen, StandardCharsets.US_ASCII).trim();
            // Strip chunk extensions (e.g. "1a;extension")
            int semi = sizeLine.indexOf(';');
            if (semi >= 0) {
                sizeLine = sizeLine.substring(0, semi).trim();
            }
            int chunkSize;
            try {
                chunkSize = Integer.parseInt(sizeLine, 16);
            } catch (NumberFormatException e) {
                break;
            }
            if (chunkSize == 0) {
                // Last chunk — consume trailing \r\n
                in.read();
                in.read();
                break;
            }
            byte[] chunk = new byte[chunkSize];
            int offset = 0;
            while (offset < chunkSize) {
                int n = in.read(chunk, offset, chunkSize - offset);
                if (n == -1) {
                    break;
                }
                offset += n;
            }
            body.write(chunk, 0, offset);
            // Consume trailing \r\n after chunk data
            in.read();
            in.read();
        }
        return body.toByteArray();
    }

    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private int parseStatusCode(String statusLine) {
        String[] parts = statusLine.split(" ", 3);
        if (parts.length >= 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                logger.trace("Could not parse HTTP status code from status line: {}", statusLine);
            }
        }
        return 0;
    }

    private @Nullable String createDigestAuthorizationHeader(String challenge, String requestPath) throws Exception {
        if (!challenge.toLowerCase(Locale.ROOT).startsWith("digest")) {
            return null;
        }
        Map<String, String> digestParams = parseDigestChallenge(challenge.substring(6).trim());
        String realm = digestParams.get("realm");
        String nonce = digestParams.get("nonce");
        if (realm == null || nonce == null) {
            return null;
        }
        String qop = digestParams.get("qop");
        if (qop != null && qop.contains(",")) {
            qop = qop.split(",")[0].trim();
        }
        String ha1 = md5Hex(username + ":" + realm + ":" + password);
        String ha2 = md5Hex("GET:" + requestPath);
        StringBuilder auth = new StringBuilder("Digest ");
        auth.append("username=\"").append(escapeDigestValue(username)).append("\"");
        auth.append(", realm=\"").append(escapeDigestValue(realm)).append("\"");
        auth.append(", nonce=\"").append(escapeDigestValue(nonce)).append("\"");
        auth.append(", uri=\"").append(requestPath).append("\"");
        String response;
        if (qop != null && !qop.isBlank()) {
            String nonceCount = "00000001";
            String cnonce = randomHex(16);
            response = md5Hex(ha1 + ":" + nonce + ":" + nonceCount + ":" + cnonce + ":" + qop + ":" + ha2);
            auth.append(", qop=").append(qop);
            auth.append(", nc=").append(nonceCount);
            auth.append(", cnonce=\"").append(cnonce).append("\"");
        } else {
            response = md5Hex(ha1 + ":" + nonce + ":" + ha2);
        }
        String opaque = digestParams.get("opaque");
        if (opaque != null) {
            auth.append(", opaque=\"").append(escapeDigestValue(opaque)).append("\"");
        }
        String algorithm = digestParams.get("algorithm");
        if (algorithm != null) {
            auth.append(", algorithm=").append(algorithm);
        }
        auth.append(", response=\"").append(response).append("\"");
        return auth.toString();
    }

    private Map<String, String> parseDigestChallenge(String challenge) {
        Map<String, String> result = new HashMap<>();
        int index = 0;
        while (index < challenge.length()) {
            while (index < challenge.length()
                    && (challenge.charAt(index) == ',' || Character.isWhitespace(challenge.charAt(index)))) {
                index++;
            }
            int equals = challenge.indexOf('=', index);
            if (equals < 0) {
                break;
            }
            String key = challenge.substring(index, equals).trim().toLowerCase(Locale.ROOT);
            index = equals + 1;
            String value;
            if (index < challenge.length() && challenge.charAt(index) == '"') {
                index++;
                int end = challenge.indexOf('"', index);
                if (end < 0) {
                    break;
                }
                value = challenge.substring(index, end);
                index = end + 1;
            } else {
                int comma = challenge.indexOf(',', index);
                if (comma < 0) {
                    value = challenge.substring(index).trim();
                    index = challenge.length();
                } else {
                    value = challenge.substring(index, comma).trim();
                    index = comma + 1;
                }
            }
            result.put(key, value);
        }
        return result;
    }

    private String md5Hex(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.ISO_8859_1));
        StringBuilder builder = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private String randomHex(int length) {
        byte[] bytes = new byte[length / 2];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder builder = new StringBuilder(length);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private String escapeDigestValue(String value) {
        return value.replace("\\\\", "\\\\\\\\").replace("\"", "\\\"");
    }

    private record SnapshotHttpResponse(int statusCode, Map<String, String> headers, byte[] body) {
    }

    private record OpenDoorHttpResponse(int statusCode, @Nullable String wwwAuthenticate) {
    }

    public String generateMd5Hash(String dahuaRandom, String dahuaRealm, String username, String password)
            throws Exception {
        final String passwordDbHash = md5(String.join(":", username, dahuaRealm, password)).toUpperCase(Locale.ROOT);
        final String pass = String.join(":", username, dahuaRandom, passwordDbHash);
        return md5(pass).toUpperCase(Locale.ROOT);
    }

    /**
     * MD5 hash function for Dahua DHIP protocol authentication.
     * Note: MD5 is cryptographically weak, but is required by the Dahua device API
     * for digest authentication. This is a protocol limitation, not a design choice.
     */
    private String md5(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void keepAlive(int delay) {
        final Socket localSock = sock;
        if (localSock == null) {
            return;
        }

        logger.trace("Started keepAlive thread");
        while (execThread) {
            Map<String, Object> queryArgs = new HashMap<>();
            queryArgs.put("method", "global.keepAlive");
            queryArgs.put("magic", "0x1234");
            queryArgs.put("params", Map.of("timeout", delay, "active", true));
            queryArgs.put("id", this.id);
            queryArgs.put("session", this.sessionId);

            try {
                send(new Gson().toJson(queryArgs));
            } catch (IOException e) {
                logger.trace("Error sending keepAlive", e);
                return;
            }
            lastKeepAlive = System.currentTimeMillis();
            boolean keepAliveReceived = false;

            while (lastKeepAlive + delay * 1000 > System.currentTimeMillis()) {
                ArrayList<String> data;
                try {
                    data = receive();
                    for (String packet : data) {
                        JsonObject jsonPacket = gson.fromJson(packet, JsonObject.class);
                        if (jsonPacket != null) {
                            if (jsonPacket.has("result")) {
                                logger.trace("keepAlive back");
                                keepAliveReceived = true;
                            } else if ("client.notifyEventStream".equals(jsonPacket.get("method").getAsString())) {
                                eventListener.onEvent(jsonPacket);
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Read timeout: no data yet, keep waiting until the keep-alive deadline.
                    logger.trace("keepAlive receive timeout, continuing to wait");
                } catch (IOException e) {
                    logger.trace("Error receiving keepAlive response", e);
                    return;
                }
            }

            if (!keepAliveReceived) {
                logger.trace("keepAlive failed");
                return;
            }
        }
    }

    public void send(String packet) throws IOException {
        byte[] payloadBytes = packet.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(32 + payloadBytes.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(0x20000000);
        buffer.putInt(0x44484950);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(sessionId);
        buffer.putInt(id);
        buffer.putInt(payloadBytes.length);
        buffer.putInt(0);
        buffer.putInt(payloadBytes.length);
        buffer.putInt(0);

        if (buffer.position() != 32) {
            logger.trace("Binary header != 32");
            return;
        }

        id += 1;

        final Socket localSock = sock;
        if (localSock == null) {
            throw new IOException("Socket is not connected");
        }

        buffer.put(payloadBytes);
        localSock.getOutputStream().write(buffer.array());
    }

    public ArrayList<String> receive() throws IOException {
        ArrayList<String> p2pReturnData = new ArrayList<String>();
        byte[] buffer = new byte[8192];
        byte[] header = new byte[32];
        ByteBuffer bbuffer;
        int lenRecved = 1;
        int timeout = 5;

        final Socket localSock = sock;
        if (localSock == null) {
            logger.debug("Socket is not connected");
            throw new IOException("Socket is not connected");
        }

        try {
            localSock.setSoTimeout(timeout * 1000); // Set timeout in milliseconds
            InputStream input = localSock.getInputStream();
            int bytesRead = input.read(buffer);
            if (bytesRead < 0) {
                // End of stream - connection closed
                throw new IOException("Connection closed by remote host");
            }

            // Combine residual buffer with new data
            if (residualBuffer.hasRemaining()) {
                ByteBuffer combined = ByteBuffer.allocate(residualBuffer.remaining() + bytesRead);
                combined.put(residualBuffer);
                combined.put(buffer, 0, bytesRead);
                combined.flip();
                bbuffer = combined;
                residualBuffer = ByteBuffer.allocate(0); // Clear residual
            } else {
                bbuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            logger.trace("IOException in receive(): {}", e.getMessage());
            throw e;
        }

        while (bbuffer.hasRemaining()) {
            // Ensure we have enough bytes for at least the magic value
            if (bbuffer.remaining() < Long.BYTES) {
                // Save remaining bytes for next read
                residualBuffer = ByteBuffer.allocate(bbuffer.remaining());
                residualBuffer.put(bbuffer);
                residualBuffer.flip();
                break;
            }

            bbuffer.order(ByteOrder.BIG_ENDIAN);
            if (bbuffer.getLong(bbuffer.position()) == 0x2000000044484950L) {
                // Ensure we have a full header before reading it
                if (bbuffer.remaining() < 32) {
                    // Save remaining bytes for next read
                    residualBuffer = ByteBuffer.allocate(bbuffer.remaining());
                    residualBuffer.put(bbuffer);
                    residualBuffer.flip();
                    break;
                }
                bbuffer.order(ByteOrder.LITTLE_ENDIAN);
                // DHIP Protocol Header Structure (32 bytes):
                // Offset 0-7: Magic value (0x2000000044484950)
                // Offset 8-11: Session ID
                // Offset 12-15: Sequence number
                // Offset 16-19: Length of received data (lenRecved)
                // Offset 20-23: Reserved
                // Offset 24-27: Expected length for multi-part messages
                // Offset 28-31: Reserved
                int frameStart = bbuffer.position();
                lenRecved = bbuffer.getInt(frameStart + 16);
                bbuffer.get(header, 0, 32); // advances position by 32 to payload start
            } else {
                if (lenRecved > 0) {
                    // Ensure we have the full payload before reading it
                    if (bbuffer.remaining() < lenRecved) {
                        // Save from header start to preserve full frame for the next read.
                        ByteBuffer residualSlice = bbuffer.duplicate();
                        int headerStartPosition = Math.max(0, bbuffer.position() - 32);
                        residualSlice.position(headerStartPosition);
                        residualBuffer = ByteBuffer.allocate(residualSlice.remaining());
                        residualBuffer.put(residualSlice);
                        residualBuffer.flip();
                        break;
                    }
                    String p2pData = new String(bbuffer.array(), bbuffer.arrayOffset() + bbuffer.position(), lenRecved,
                            StandardCharsets.UTF_8);
                    bbuffer.position(bbuffer.position() + lenRecved);
                    p2pReturnData.add(p2pData);
                    lenRecved = 0;
                } else {
                    break;
                }
            }
        }
        return p2pReturnData;
    }

    @SuppressWarnings("unchecked")
    public boolean login() {
        logger.trace("Start login");

        Map<String, Object> queryArgs = new HashMap<>();
        queryArgs.put("id", this.id);
        queryArgs.put("magic", "0x1234");
        queryArgs.put("method", "global.login");
        queryArgs.put("params", Map.of("clientType", this.clientType, "ipAddr", this.fakeIpAddr, "loginType", "Direct",
                "password", "", "userName", this.username));
        queryArgs.put("session", 0);

        try {
            send(new Gson().toJson(queryArgs));
            ArrayList<String> data = receive();
            if (data.isEmpty()) {
                logger.trace("global.login [random]");
                return false;
            }
            Map<String, Object> jsonData = new Gson().fromJson(data.get(0), Map.class);
            if (jsonData == null || !jsonData.containsKey("session") || !jsonData.containsKey("params")) {
                logger.trace("Invalid JSON response from login");
                return false;
            }
            Object sessionObj = jsonData.get("session");
            if (sessionObj instanceof Number) {
                this.sessionId = ((Number) sessionObj).intValue();
            } else {
                logger.trace("Invalid session type in response");
                return false;
            }
            Map<String, Object> params = (Map<String, Object>) jsonData.get("params");
            if (params == null) {
                logger.trace("Missing params in response");
                return false;
            }
            String random = (String) params.get("random");
            String realm = (String) params.get("realm");

            if (random == null || realm == null) {
                logger.trace("Login failed: missing random or realm");
                return false;
            }

            String hashedCredentials = generateMd5Hash(random, realm, this.username, this.password);

            queryArgs = new HashMap<>();
            queryArgs.put("id", this.id);
            queryArgs.put("magic", "0x1234");
            queryArgs.put("method", "global.login");
            queryArgs.put("session", this.sessionId);
            queryArgs.put("params", Map.of("userName", this.username, "password", hashedCredentials, "clientType",
                    this.clientType, "ipAddr", this.fakeIpAddr, "loginType", "Direct", "authorityType", "Default"));

            send(new Gson().toJson(queryArgs));
            data = receive();
            if (data.isEmpty()) {
                return false;
            }
            jsonData = new Gson().fromJson(data.get(0), Map.class);
            if (jsonData != null) {
                if (Boolean.TRUE.equals(jsonData.get("result"))) {
                    logger.trace("Login success");
                    Object paramsObj = jsonData.get("params");
                    if (paramsObj instanceof Map) {
                        Map<String, Object> paramsMap = (Map<String, Object>) paramsObj;
                        Object intervalObj = paramsMap.get("keepAliveInterval");
                        if (intervalObj instanceof Number) {
                            this.keepAliveInterval = ((Number) intervalObj).intValue();
                        }
                    }
                    return true;
                }
                Object errorObj = jsonData.get("error");
                if (errorObj instanceof Map) {
                    Map<?, ?> errorMap = (Map<?, ?>) errorObj;
                    Object code = errorMap.get("code");
                    Object message = errorMap.get("message");
                    logger.trace("Login failed: {} {}", code, message);
                } else {
                    logger.trace("Login failed: {}", jsonData);
                }
            } else {
                logger.trace("Login failed: empty or invalid JSON response");
            }
        } catch (Exception e) {
            logger.trace("Login error: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public void run() {
        boolean error = false;
        int loginTries = 0;
        while (execThread) {
            if (error) {
                try {
                    for (int i = 0; i < 12 && execThread; i++) {
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    logger.debug("Thread interrupted during error wait", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            error = true;
            try {
                Socket localSock = new Socket();
                localSock.connect(new InetSocketAddress(host, 5000), 5000); // 5s connect timeout
                sock = localSock;
                localSock.setSoTimeout(5000); // Set timeout to 5 seconds
                error = false;

                if (!login()) {
                    loginTries++;
                    if (loginTries > 4) {
                        errorInformer.accept("Login failed. Verify the host configuration and credentials.");
                        execThread = false;
                    }
                    try {
                        Socket sockToClose = sock;
                        if (sockToClose != null) {
                            sockToClose.close();
                        }
                    } catch (IOException e) {
                        logger.trace("Error closing socket after login failure", e);
                    }
                    continue;
                }

                Map<String, Object> queryArgs = new HashMap<>();
                queryArgs.put("id", this.id);
                queryArgs.put("magic", "0x1234");
                queryArgs.put("method", "eventManager.attach");
                queryArgs.put("params", Map.of("codes", new String[] { "All" }));
                queryArgs.put("session", this.sessionId);

                send(new Gson().toJson(queryArgs));
                ArrayList<String> data = receive();
                JsonObject jsonData = data.isEmpty() ? null : gson.fromJson(data.get(0), JsonObject.class);
                if (jsonData == null || !jsonData.has("result")) {
                    logger.trace("Failure eventManager.attach");
                    try {
                        Socket sockToClose = sock;
                        if (sockToClose != null) {
                            sockToClose.close();
                        }
                    } catch (IOException e) {
                        logger.trace("Error closing socket after attach failure", e);
                    }
                    continue;
                } else {
                    for (String packet : data) {
                        JsonObject jsonPacket = gson.fromJson(packet, JsonObject.class);
                        if (jsonPacket != null && jsonPacket.has("method")) {
                            String method = jsonPacket.get("method").getAsString();
                            if ("client.notifyEventStream".equals(method)) {
                                eventListener.onEvent(jsonPacket);
                            }
                        }
                    }
                }
                keepAlive(this.keepAliveInterval);
                logger.trace("Failure no keep alive received");
            } catch (Exception e) {
                logger.trace("Socket open failed", e);
            }
        }
        try {
            Socket sockToClose = sock;
            if (sockToClose != null) {
                sockToClose.close();
            }
        } catch (Exception e) {
            logger.trace("Error while closing socket", e);
        }
    }
}
