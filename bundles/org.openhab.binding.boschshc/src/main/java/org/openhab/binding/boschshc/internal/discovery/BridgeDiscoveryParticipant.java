/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.discovery;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.BINDING_ID;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.BoschHttpClient;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.PublicInformation;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link BridgeDiscoveryParticipant} is responsible discovering the Bosch
 * Smart Home Controller as a Bridge with the mDNS services.
 *
 * @author Gerd Zanker - Initial contribution
 * @author David Pace - Discovery result caching
 */
@NonNullByDefault
@Component(configurationPid = "discovery.boschsmarthomebridge")
public class BridgeDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final String NAME_PREFIX_BOSCH_SHC = "Bosch SHC";
    private static final Duration TTL_DURATION = Duration.ofMinutes(10);
    private static final long TTL_SECONDS = TTL_DURATION.toSeconds();

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BoschSHCBindingConstants.THING_TYPE_SHC);

    private final Logger logger = LoggerFactory.getLogger(BridgeDiscoveryParticipant.class);
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    /**
     * Cache for bridge discovery results. Uses the IP address of mDNS events as
     * key. If the value is <code>null</code>, no Bosch SHC controller could be
     * identified at the corresponding IP address.
     */
    private ExpiringCacheMap<String, @Nullable PublicInformation> discoveryResultCache = new ExpiringCacheMap<>(
            TTL_DURATION);

    @Activate
    public BridgeDiscoveryParticipant(@Reference HttpClientFactory httpClientFactory) {
        // create http client upfront to later request public information from SHC
        SslContextFactory sslContextFactory = new SslContextFactory.Client.Client(true); // Accept all certificates
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        httpClient = httpClientFactory.createHttpClient(BINDING_ID, sslContextFactory);
    }

    protected BridgeDiscoveryParticipant(HttpClient customHttpClient) {
        httpClient = customHttpClient;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    /**
     * This method is frequently called by the mDNS discovery framework in different
     * threads with individual service info instances.
     * <p>
     * Different service info objects can refer to the same Bosch SHC controller,
     * e.g. the controller may be reachable via a <code>192.168.*.*</code> IP and an
     * IP in the <code>169.254.*.*</code> range. The response from the controller
     * contains the actual resolved IP address.
     * <p>
     * We ignore mDNS events if they do not contain any IP addresses or if the name
     * property does not start with <code>Bosch SHC</code>.
     */
    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        if (logger.isTraceEnabled()) {
            logger.trace("Bridge discovery invoked with mDNS service info {}", serviceInfo);
        }

        String name = serviceInfo.getName();
        if (name == null || !name.startsWith(NAME_PREFIX_BOSCH_SHC)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Ignoring mDNS service event because name '{}' does not start with '{}')", name,
                        NAME_PREFIX_BOSCH_SHC);
            }
            return null;
        }

        @Nullable
        String ipAddress = getFirstIPAddress(serviceInfo);
        if (ipAddress == null || ipAddress.isBlank()) {
            return null;
        }

        PublicInformation publicInformation = getOrComputePublicInformation(ipAddress);
        if (publicInformation == null) {
            return null;
        }

        @Nullable
        final ThingUID uid = getThingUID(serviceInfo);
        if (uid == null) {
            return null;
        }

        return DiscoveryResultBuilder.create(uid)
                .withLabel("Bosch Smart Home Controller (" + publicInformation.shcIpAddress + ")")
                .withProperty("ipAddress", publicInformation.shcIpAddress)
                .withProperty("shcGeneration", publicInformation.shcGeneration)
                .withProperty("apiVersions", publicInformation.apiVersions).withTTL(TTL_SECONDS).build();
    }

    private @Nullable String getFirstIPAddress(ServiceInfo serviceInfo) {
        String[] hostAddresses = serviceInfo.getHostAddresses();
        if (hostAddresses != null && hostAddresses.length > 0 && !hostAddresses[0].isEmpty()) {
            return hostAddresses[0];
        }

        return null;
    }

    /**
     * Provides a cached discovery result if available, or performs an actual
     * communication attempt to the device with the given IP address.
     * <p>
     * This method is synchronized because multiple threads try to access discovery
     * results concurrently. We only want one thread to "win" and to invoke the
     * actual HTTP communication.
     * 
     * @param ipAddress IP address to contact if no cached result is available
     * @return the {@link PublicInformation} of the Bosch Smart Home Controller or
     *         <code>null</code> if the device with the given IP address could not
     *         be identified as Bosch Smart Home Controller
     */
    protected synchronized @Nullable PublicInformation getOrComputePublicInformation(String ipAddress) {
        return discoveryResultCache.putIfAbsentAndGet(ipAddress, () -> {
            logger.trace("No cached bridge discovery result available for IP {}, trying to contact SHC", ipAddress);
            return discoverBridge(ipAddress);
        });
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        String ipAddress = getFirstIPAddress(serviceInfo);
        if (ipAddress != null) {
            @Nullable
            PublicInformation publicInformation = getOrComputePublicInformation(ipAddress);
            if (publicInformation != null) {
                String resolvedIpAddress = publicInformation.shcIpAddress;
                return new ThingUID(BoschSHCBindingConstants.THING_TYPE_SHC, resolvedIpAddress.replace('.', '-'));
            }
        }
        return null;
    }

    protected @Nullable PublicInformation discoverBridge(String ipAddress) {
        logger.debug("Attempting to contact Bosch Smart Home Controller at IP {}", ipAddress);
        PublicInformation bridgeInformation = getPublicInformationFromPossibleBridgeAddress(ipAddress);
        if (bridgeInformation != null && bridgeInformation.shcIpAddress != null
                && !bridgeInformation.shcIpAddress.isBlank()) {
            return bridgeInformation;
        }

        return null;
    }

    /**
     * Attempts to send a HTTP request to the given IP address in order to determine
     * if the device is a Bosch Smart Home Controller.
     * 
     * @param ipAddress the IP address of the potential Bosch Smart Home Controller
     * @return a {@link PublicInformation} object if the bridge was successfully
     *         contacted or <code>null</code> if the communication failed
     */
    protected @Nullable PublicInformation getPublicInformationFromPossibleBridgeAddress(String ipAddress) {
        String url = BoschHttpClient.getPublicInformationUrl(ipAddress);
        logger.trace("Requesting SHC information via URL {}", url);
        try {
            httpClient.start();
            ContentResponse contentResponse = httpClient.newRequest(url).method(HttpMethod.GET)
                    .timeout(BoschHttpClient.DEFAULT_TIMEOUT_SECONDS, BoschHttpClient.DEFAULT_TIMEOUT_UNIT).send();

            // check HTTP status code
            if (!HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                logger.debug("Discovery failed with status code {}: {}", contentResponse.getStatus(),
                        contentResponse.getContentAsString());
                return null;
            }
            // get content from response
            String content = contentResponse.getContentAsString();
            logger.debug("Discovered SHC at IP {}, public info: {}", ipAddress, content);
            PublicInformation bridgeInfo = gson.fromJson(content, PublicInformation.class);
            if (bridgeInfo != null && bridgeInfo.shcIpAddress != null && !bridgeInfo.shcIpAddress.isBlank()) {
                return bridgeInfo;
            }
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Discovery could not reach SHC at IP {}: {}", ipAddress, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warn("Discovery failed during HTTP client request: {}", e.getMessage(), e);
        }
        return null;
    }
}
