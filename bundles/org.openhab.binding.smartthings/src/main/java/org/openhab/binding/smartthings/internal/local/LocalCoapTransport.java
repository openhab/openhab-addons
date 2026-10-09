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
package org.openhab.binding.smartthings.internal.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.config.UdpConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConfig;
import org.eclipse.californium.scandium.config.DtlsConfig.DtlsRole;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.eclipse.californium.scandium.dtls.PskPublicInformation;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.californium.scandium.dtls.cipher.XECDHECryptography.SupportedGroup;
import org.eclipse.californium.scandium.dtls.pskstore.AdvancedSinglePskStore;
import org.eclipse.californium.scandium.dtls.x509.SingleCertificateProvider;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * DTLS 1.2 CoAP client with explicitly imported credentials and bounded CBOR exchanges.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class LocalCoapTransport implements LocalTransport {
    private final InetAddress host;
    private final int port;
    private final long timeoutMillis;
    private final Endpoint endpoint;
    private final Object lifecycle = new Object();
    private final Set<Request> pending = new HashSet<>();
    private boolean closed;

    LocalCoapTransport(LocalApplianceConfiguration config) throws IOException {
        try {
            config.validate();
            host = InetAddress.getByName(config.host);
            if (host.isAnyLocalAddress() || host.isMulticastAddress()) {
                throw new IOException("An individual appliance address is required");
            }
            timeoutMillis = TimeUnit.SECONDS.toMillis(config.timeout);
            if (config.port == 0) {
                LocalDiscovery.Descriptor descriptor = LocalDiscovery.discover(host, config.timeout);
                if (!config.deviceId.isBlank() && !config.deviceId.equalsIgnoreCase(descriptor.deviceId())) {
                    throw new IOException("Discovered appliance identity differs from the configured device");
                }
                port = descriptor.securePort();
            } else {
                port = config.port;
            }
            endpoint = startEndpoint(dtlsConfiguration(config, host));
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            throw new IOException("Invalid local appliance credentials or connection configuration");
        }
    }

    LocalCoapTransport(InetAddress host, int port, long timeoutMillis, Endpoint endpoint) {
        this.host = host;
        this.port = port;
        this.timeoutMillis = timeoutMillis;
        this.endpoint = endpoint;
    }

    private static Endpoint startEndpoint(DtlsConnectorConfig configuration) throws IOException {
        DTLSConnector connector = new DTLSConnector(configuration);
        @Nullable
        CoapEndpoint coapEndpoint = null;
        try {
            ScheduledExecutorService scheduler = scheduler();
            connector.setExecutor(scheduler);
            coapEndpoint = new CoapEndpoint.Builder().setConfiguration(networkConfiguration()).setConnector(connector)
                    .build();
            coapEndpoint.setExecutors(scheduler, scheduler);
            coapEndpoint.start();
            return coapEndpoint;
        } catch (IOException | RuntimeException e) {
            if (coapEndpoint != null) {
                coapEndpoint.destroy();
            } else {
                connector.destroy();
            }
            throw new IOException("Cannot start local appliance transport");
        }
    }

    static ScheduledExecutorService scheduler() {
        return ThreadPoolManager.getScheduledPool("smartthings-local");
    }

    static Configuration networkConfiguration() {
        CoapConfig.register();
        UdpConfig.register();
        DtlsConfig.register();
        return new Configuration().set(CoapConfig.MAX_RESOURCE_BODY_SIZE, LocalCbor.MAX_BODY_SIZE)
                .set(CoapConfig.BLOCKWISE_REUSE_TOKEN, true)
                // A rejected POST must not be resubmitted automatically as a different blockwise exchange.
                .set(CoapConfig.BLOCKWISE_ENTITY_TOO_LARGE_AUTO_FAILOVER, false).set(CoapConfig.MAX_ACTIVE_PEERS, 1);
    }

    static int localPort(LocalApplianceConfiguration config, InetAddress host) {
        return config.localPort != 0 ? config.localPort
                : 40000 + Math.floorMod(31 * Arrays.hashCode(host.getAddress()) + config.deviceId.hashCode(), 20000);
    }

    static DtlsConnectorConfig dtlsConfiguration(LocalApplianceConfiguration config, InetAddress host)
            throws IOException, GeneralSecurityException {
        DtlsConnectorConfig.Builder builder = DtlsConnectorConfig.builder(networkConfiguration())
                .setAddress(new InetSocketAddress(localPort(config, host)))
                .set(DtlsConfig.DTLS_ROLE, DtlsRole.CLIENT_ONLY).set(DtlsConfig.DTLS_MAX_CONNECTIONS, 1)
                .set(DtlsConfig.DTLS_USE_SERVER_NAME_INDICATION, false)
                .setAsList(DtlsConfig.DTLS_CURVES, SupportedGroup.secp256r1);
        if (!config.ownerPsk.isBlank()) {
            UUID owner = LocalApplianceConfiguration.uuid(config.ownerId);
            byte[] identity = ByteBuffer.allocate(16).putLong(owner.getMostSignificantBits())
                    .putLong(owner.getLeastSignificantBits()).array();
            byte[] key = config.psk();
            try {
                builder.setAdvancedPskStore(
                        new AdvancedSinglePskStore(new PskPublicInformation(owner.toString(), identity), key))
                        // Samsung's imported OwnerPSK profile requires this specific non-recommended CBC suite.
                        .set(DtlsConfig.DTLS_RECOMMENDED_CIPHER_SUITES_ONLY, false)
                        .setAsList(DtlsConfig.DTLS_CIPHER_SUITES, CipherSuite.TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256);
            } finally {
                Arrays.fill(key, (byte) 0);
            }
        } else {
            char[] password = config.keyStorePassword.toCharArray();
            try (InputStream input = Files.newInputStream(Path.of(config.keyStore))) {
                KeyStore store = KeyStore.getInstance("PKCS12");
                store.load(input, password);
                List<String> aliases = new ArrayList<>();
                var entries = store.aliases();
                while (entries.hasMoreElements()) {
                    String alias = entries.nextElement();
                    if (store.isKeyEntry(alias)) {
                        aliases.add(alias);
                    }
                }
                if (aliases.size() != 1 || !(store.getKey(aliases.getFirst(), password) instanceof PrivateKey key)) {
                    throw new GeneralSecurityException("A single client private key is required");
                }
                Certificate[] chain = store.getCertificateChain(aliases.getFirst());
                if (chain == null || chain.length == 0) {
                    throw new GeneralSecurityException("A client certificate chain is required");
                }
                builder.setCertificateIdentityProvider(new SingleCertificateProvider(key, chain, CertificateType.X_509))
                        .setAdvancedCertificateVerifier(new PinnedCertificateVerifier(config.fingerprint()))
                        // Samsung advertises CA names even when it authorizes a self-signed client UUID.
                        // Truncating against those names would silently send an empty client certificate.
                        .set(DtlsConfig.DTLS_TRUNCATE_CLIENT_CERTIFICATE_PATH, false)
                        .setAsList(DtlsConfig.DTLS_CIPHER_SUITES, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256);
            } catch (IOException | GeneralSecurityException | RuntimeException e) {
                throw new GeneralSecurityException("Cannot load local appliance client credentials");
            } finally {
                Arrays.fill(password, '\0');
            }
        }
        return builder.build();
    }

    @Override
    public JsonElement get(String path) throws IOException {
        Request request = Request.newGet().setURI(resourceUri(host, port, path));
        request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_CBOR);
        return representation(exchange(request));
    }

    @Override
    public void post(String href, JsonObject fields) throws IOException {
        Request request = Request.newPost().setURI(resourceUri(host, port, href));
        request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_CBOR)
                .setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
        request.setPayload(LocalCbor.encode(fields));
        exchange(request);
    }

    static URI resourceUri(InetAddress host, int port, String path) throws IOException {
        try {
            URI relative = new URI(path);
            String resource = relative.getRawPath();
            if (relative.isAbsolute() || relative.getRawAuthority() != null || relative.getRawFragment() != null
                    || resource == null || !resource.matches("/(?:[A-Za-z0-9_.~-]+/)*[A-Za-z0-9_.~-]+")
                    || "/oic/sec".equals(resource) || resource.startsWith("/oic/sec/")) {
                throw new IOException("Invalid appliance resource path");
            }
            for (String segment : resource.split("/")) {
                if (".".equals(segment) || "..".equals(segment)) {
                    throw new IOException("Invalid appliance resource path");
                }
            }
            String query = relative.getRawQuery();
            if (query != null
                    && (!query.matches("[A-Za-z0-9_.~-]+=[A-Za-z0-9_.~-]+(?:&[A-Za-z0-9_.~-]+=[A-Za-z0-9_.~-]+)*")
                            || query.matches(".*(?:^|[=&])oic\\.r\\.(?:doxm|pstat|cred|acl|acl2)(?:&|$).*"))) {
                throw new IOException("Invalid appliance resource query");
            }
            return new URI("coaps", null, host.getHostAddress(), port, resource, query, null);
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new IOException("Invalid appliance resource path");
        }
    }

    private Response exchange(Request request) throws IOException {
        synchronized (lifecycle) {
            if (closed) {
                throw new IOException("Local appliance transport is closed");
            }
            pending.add(request);
        }
        try {
            request.send(endpoint);
            Response response = request.waitForResponse(timeoutMillis);
            synchronized (lifecycle) {
                if (closed) {
                    throw new IOException("Local appliance transport is closed");
                }
            }
            if (response == null) {
                throw new IOException("Local appliance request failed or timed out");
            }
            if (!response.isSuccess()) {
                throw new IOException("Local appliance request rejected with CoAP code " + response.getCode());
            }
            if (response.getPayloadSize() > LocalCbor.MAX_BODY_SIZE) {
                throw new IOException("Local appliance response is too large");
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Local appliance request interrupted");
        } catch (RuntimeException e) {
            throw new IOException("Local appliance request failed");
        } finally {
            request.cancel();
            synchronized (lifecycle) {
                pending.remove(request);
            }
        }
    }

    static JsonElement representation(Response response) throws IOException {
        if (response.getOptions().getContentFormat() != MediaTypeRegistry.APPLICATION_CBOR) {
            throw new IOException("Expected an appliance CBOR response");
        }
        return LocalCbor.decode(response.getPayload());
    }

    @Override
    public void close() {
        List<Request> requests;
        synchronized (lifecycle) {
            if (closed) {
                return;
            }
            closed = true;
            requests = List.copyOf(pending);
        }
        requests.forEach(Request::cancel);
        // Endpoint destruction owns connector shutdown; the shared openHAB executors remain available.
        endpoint.destroy();
    }
}
