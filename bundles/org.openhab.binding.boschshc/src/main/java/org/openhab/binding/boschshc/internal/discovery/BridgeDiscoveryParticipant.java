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
 * The {@link BridgeDiscoveryParticipant} is responsible discovering the
 * Bosch Smart Home Controller as a Bridge with the mDNS services.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "discovery.boschsmarthomebridge")
public class BridgeDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private static final long TTL_SECONDS = Duration.ofHours(1).toSeconds();
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BoschSHCBindingConstants.THING_TYPE_SHC);

    private final Logger logger = LoggerFactory.getLogger(BridgeDiscoveryParticipant.class);
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    /// SHC Bridge Information, read via public REST API if bridge is detected. Otherwise, strings are empty.
    private PublicInformation bridgeInformation = new PublicInformation();

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

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        logger.trace("Bridge Discovery started for {}", serviceInfo);

        @Nullable
        final ThingUID uid = getThingUID(serviceInfo);
        if (uid == null) {
            return null;
        }

        logger.trace("Discovered Bosch Smart Home Controller at {}", bridgeInformation.shcIpAddress);

        return DiscoveryResultBuilder.create(uid)
                .withLabel("Bosch Smart Home Controller (" + bridgeInformation.shcIpAddress + ")")
                .withProperty("ipAddress", bridgeInformation.shcIpAddress)
                .withProperty("shcGeneration", bridgeInformation.shcGeneration)
                .withProperty("apiVersions", bridgeInformation.apiVersions).withTTL(TTL_SECONDS).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        String ipAddress = discoverBridge(serviceInfo).shcIpAddress;
        if (!ipAddress.isBlank()) {
            return new ThingUID(BoschSHCBindingConstants.THING_TYPE_SHC, ipAddress.replace('.', '-'));
        }
        return null;
    }

    protected PublicInformation discoverBridge(ServiceInfo serviceInfo) {
        logger.trace("Discovering serviceInfo {}", serviceInfo);

        if (serviceInfo.getHostAddresses() != null && serviceInfo.getHostAddresses().length > 0
                && !serviceInfo.getHostAddresses()[0].isEmpty()) {
            String address = serviceInfo.getHostAddresses()[0];
            logger.trace("Discovering InetAddress {}", address);
            // store all information for later access
            bridgeInformation = getPublicInformationFromPossibleBridgeAddress(address);
        }

        return bridgeInformation;
    }

    protected PublicInformation getPublicInformationFromPossibleBridgeAddress(String ipAddress) {
        String url = BoschHttpClient.getPublicInformationUrl(ipAddress);
        logger.trace("Discovering ipAddress {}", url);
        try {
            httpClient.start();
            ContentResponse contentResponse = httpClient.newRequest(url).method(HttpMethod.GET).send();
            // check HTTP status code
            if (!HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                logger.debug("Discovering failed with status code: {}", contentResponse.getStatus());
                return new PublicInformation();
            }
            // get content from response
            String content = contentResponse.getContentAsString();
            logger.trace("Discovered SHC - public info {}", content);
            PublicInformation bridgeInfo = gson.fromJson(content, PublicInformation.class);
            if (bridgeInfo.shcIpAddress != null) {
                return bridgeInfo;
            }
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Discovering failed with exception {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.debug("Discovering failed during http client request {}", e.getMessage());
        }
        return new PublicInformation();
    }
}
