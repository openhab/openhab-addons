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
package org.openhab.binding.smartthings.internal.ocf;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.elements.EndpointContext;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Read-only OIC endpoint discovery. Public advertisements are not authentication evidence.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class Discovery {
    record Descriptor(String deviceId, String name, int securePort) {
    }

    private Discovery() {
    }

    static Descriptor discover(InetAddress host, int timeoutSeconds) throws IOException {
        Set<Integer> ports = new LinkedHashSet<>(List.of(5683, 49154, 49153));
        for (int port = 49152; port <= 49160; port++) {
            ports.add(port);
        }
        return discover(host, ports, timeoutSeconds);
    }

    static Descriptor discover(InetAddress host, int publicPort, int timeoutSeconds) throws IOException {
        if (publicPort < 1 || publicPort > 65535) {
            throw new IOException("Invalid appliance discovery port");
        }
        return discover(host, Set.of(publicPort), timeoutSeconds);
    }

    static Descriptor discover(InetAddress host, Set<Integer> ports, int timeoutSeconds) throws IOException {
        return discover(host, ports, timeoutSeconds, false);
    }

    static Descriptor discoverSamsung(InetAddress host, int timeoutSeconds) throws IOException {
        return discoverSamsung(host, new LinkedHashSet<>(List.of(5683, 49154, 49153)), timeoutSeconds);
    }

    static Descriptor discoverSamsung(InetAddress host, Set<Integer> ports, int timeoutSeconds) throws IOException {
        return discover(host, ports, timeoutSeconds, true);
    }

    private static Descriptor discover(InetAddress host, Set<Integer> ports, int timeoutSeconds, boolean samsungOnly)
            throws IOException {
        if (timeoutSeconds < 1 || timeoutSeconds > 60 || host.isAnyLocalAddress() || host.isMulticastAddress()) {
            throw new IOException("Invalid appliance discovery address or timeout");
        }
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
        var scheduler = CoapTransport.scheduler();
        int remainingProbes = ports.size();
        for (int port : ports) {
            if (Thread.currentThread().isInterrupted()) {
                throw new IOException("Appliance discovery interrupted");
            }
            if (System.nanoTime() >= deadline) {
                break;
            }
            // A closed UDP port can leave an ICMP socket error for the next send on macOS.
            // Isolate probes so an unavailable port cannot poison discovery of a working one.
            CoapEndpoint endpoint = new CoapEndpoint.Builder()
                    .setConfiguration(CoapTransport.networkConfiguration().set(CoapConfig.MAX_ACTIVE_PEERS, 16))
                    .setPort(0).build();
            try {
                endpoint.setExecutors(scheduler, scheduler);
                endpoint.start();
                JsonElement resources = get(endpoint, host, port, "/oic/res", "rt=oic.r.doxm", deadline,
                        remainingProbes--);
                JsonElement device = get(endpoint, host, port, "/oic/d", "", deadline, 0);
                Descriptor descriptor = descriptor(resources, device, host);
                if (samsungOnly) {
                    verifySamsung(get(endpoint, host, port, "/oic/p", "", deadline, 0), descriptor.deviceId());
                }
                return descriptor;
            } catch (IOException e) {
                if (Thread.currentThread().isInterrupted()) {
                    throw e;
                }
                // Probe other public ports, but never guess a secure port or modify ownership.
            } catch (RuntimeException e) {
                throw new IOException("Appliance discovery failed");
            } finally {
                endpoint.destroy();
            }
        }
        throw new IOException("No usable appliance endpoint was advertised");
    }

    private static JsonElement get(CoapEndpoint endpoint, InetAddress host, int port, String path, String query,
            long deadline, int remainingProbes) throws IOException {
        Request request = Request.newGet();
        try {
            request.setURI(
                    new URI("coap", null, host.getHostAddress(), port, path, query.isEmpty() ? null : query, null));
            request.getOptions().setAccept(MediaTypeRegistry.APPLICATION_CBOR);
            long remaining = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
            if (remaining <= 0) {
                throw new IOException("Appliance discovery timed out");
            }
            request.send(endpoint);
            long wait = remainingProbes > 1 ? Math.max(1, Math.min(remaining / remainingProbes, 1000)) : remaining;
            Response response = request.waitForResponse(wait);
            EndpointContext source = response == null ? null : response.getSourceContext();
            if (response == null || !response.isSuccess() || source == null
                    || !source.getPeerAddress().getAddress().equals(host)
                    || source.getPeerAddress().getPort() != port) {
                throw new IOException("Appliance discovery request failed or timed out");
            }
            return CoapTransport.representation(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Appliance discovery interrupted");
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new IOException("Invalid appliance discovery address");
        } finally {
            request.cancel();
        }
    }

    static Descriptor descriptor(JsonElement resources, JsonElement device, InetAddress host) throws IOException {
        try {
            List<JsonObject> devices = objects(device);
            String deviceId = "";
            String name = "Samsung Appliance";
            for (JsonObject representation : devices) {
                if (representation.has("di")) {
                    String identity = ApplianceConfiguration.uuid(string(representation, "di")).toString();
                    if (!deviceId.isEmpty() && !deviceId.equals(identity)) {
                        throw new IOException("Conflicting appliance identities");
                    }
                    deviceId = identity;
                    if (representation.has("n")) {
                        name = string(representation, "n");
                    }
                }
            }
            if (deviceId.isEmpty()) {
                throw new IOException("No appliance identity was advertised");
            }
            Set<Integer> securePorts = new LinkedHashSet<>();
            for (JsonObject listing : objects(resources)) {
                if (listing.has("di") && !deviceId.equalsIgnoreCase(string(listing, "di"))) {
                    throw new IOException("Conflicting appliance identities");
                }
                if (listing.has("links")) {
                    for (JsonObject link : objects(listing.get("links"))) {
                        endpoints(link, host, securePorts);
                    }
                } else {
                    endpoints(listing, host, securePorts);
                }
            }
            if (securePorts.size() != 1) {
                throw new IOException("No unique secure appliance endpoint was advertised");
            }
            return new Descriptor(deviceId, name, securePorts.iterator().next());
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException e) {
            throw new IOException("Invalid appliance discovery representation");
        }
    }

    static void verifySamsung(JsonElement platform, String deviceId) throws IOException {
        boolean found = false;
        try {
            for (JsonObject representation : objects(platform)) {
                // A platform UUID (pi) is distinct from the device UUID (di).
                if (representation.has("di")
                        && !deviceId.equals(ApplianceConfiguration.uuid(string(representation, "di")).toString())) {
                    throw new IOException("Conflicting appliance platform identity");
                }
                if (representation.has("mnmn")) {
                    String manufacturer = string(representation, "mnmn").trim();
                    if (!manufacturer.matches("(?i)Samsung(?: Electronics(?: Co\\.,? Ltd\\.?)?)?")) {
                        throw new IOException("Not a Samsung appliance");
                    }
                    found = true;
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new IOException("Invalid appliance platform representation");
        }
        if (!found) {
            throw new IOException("No Samsung manufacturer was advertised");
        }
    }

    private static void endpoints(JsonObject link, InetAddress host, Set<Integer> ports) throws IOException {
        if (link.has("eps")) {
            for (JsonObject endpoint : objects(link.get("eps"))) {
                String ep = string(endpoint, "ep");
                if (ep.startsWith("coaps:")) {
                    ports.add(securePort(host, ep));
                }
            }
        }
        if (link.has("p")) {
            if (!link.get("p").isJsonObject()) {
                throw new IOException("Invalid legacy appliance endpoint");
            }
            JsonObject policy = link.getAsJsonObject("p");
            if (policy.has("sec")) {
                JsonElement secure = policy.get("sec");
                if (!secure.isJsonPrimitive() || !secure.getAsJsonPrimitive().isBoolean()) {
                    throw new IOException("Invalid legacy appliance endpoint");
                }
                if (secure.getAsBoolean()) {
                    JsonElement value = policy.get("port");
                    if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                        throw new IOException("Invalid legacy appliance endpoint");
                    }
                    int port = value.getAsBigDecimal().intValueExact();
                    if (port < 1 || port > 65535) {
                        throw new IOException("Invalid legacy appliance endpoint");
                    }
                    ports.add(port);
                }
            }
        }
    }

    static int securePort(InetAddress host, String advertisement) throws IOException {
        try {
            URI uri = new URI(advertisement);
            String advertisedHost = uri.getHost();
            if (!"coaps".equals(uri.getScheme()) || advertisedHost == null || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null || uri.getRawPath() == null
                    || !uri.getRawPath().isEmpty()
                    || !(advertisedHost.matches("[0-9.]+") || advertisedHost.contains(":"))
                    || !InetAddress.getByName(advertisedHost).equals(host)) {
                throw new IOException("Invalid secure appliance endpoint advertisement");
            }
            int port = uri.getPort() == -1 ? 5684 : uri.getPort();
            if (port < 1 || port > 65535) {
                throw new IOException("Invalid secure appliance endpoint advertisement");
            }
            return port;
        } catch (URISyntaxException | IllegalArgumentException | IOException e) {
            throw new IOException("Invalid secure appliance endpoint advertisement");
        }
    }

    private static List<JsonObject> objects(JsonElement value) throws IOException {
        List<JsonObject> result = new ArrayList<>();
        if (value.isJsonObject()) {
            result.add(value.getAsJsonObject());
        } else if (value.isJsonArray()) {
            for (JsonElement entry : value.getAsJsonArray()) {
                if (!entry.isJsonObject()) {
                    throw new IOException("Invalid appliance discovery representation");
                }
                result.add(entry.getAsJsonObject());
            }
        } else {
            throw new IOException("Invalid appliance discovery representation");
        }
        return result;
    }

    private static String string(JsonObject object, String key) throws IOException {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IOException("Invalid appliance discovery representation");
        }
        return value.getAsString();
    }
}
