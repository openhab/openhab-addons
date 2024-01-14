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
package org.openhab.binding.liquidcheck.internal.discovery;

import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.liquidcheck.internal.LiquidCheckBindingConstants.THING_TYPE_LIQUID_CHECK;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.liquidcheck.internal.json.CommData;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link LiquidCheckDiscoveryService} class defines discovery service for the LiquidCheckBinding
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.liquidcheck")
public class LiquidCheckDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVER_TIMEOUT_SECONDS = 300;
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HttpClient httpClient;

    @Activate
    public LiquidCheckDiscoveryService(@Reference HttpClientFactory httpClientFactory) {
        super(SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS, false);
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    /**
     * Method for starting the scan
     */
    @Override
    protected void startScan() {
        scheduler.execute(liquidCheckDiscoveryRunnable());
    }

    /**
     * Method to stop the scan
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Method for creating a Runnable to start a scan
     * 
     * @return the Runnable
     */
    protected Runnable liquidCheckDiscoveryRunnable() {
        return () -> {
            try {
                List<InetAddress> addresses = getIPv4Addresses();
                List<InetAddress> hosts = findActiveHosts(addresses);
                for (InetAddress host : hosts) {
                    Request request = httpClient.newRequest("http://" + host.getHostAddress() + "/infos.json")
                            .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).method(HttpMethod.GET)
                            .followRedirects(false);
                    try {
                        ContentResponse response = request.send();
                        if (response.getStatus() == 200) {
                            CommData json = null;
                            try {
                                json = new Gson().fromJson(response.getContentAsString(), CommData.class);
                            } catch (JsonSyntaxException e) {
                                logger.debug("Json Syntax Exception!");
                            }
                            if (null != json) {
                                buildDiscoveryResult(json,
                                        InetAddress.getByName(json.payload.wifi.station.hostname).isReachable(50));
                            } else {
                                logger.debug("Response Object is null!");
                            }
                        }
                    } catch (TimeoutException e) {
                        logger.debug("TimeOut: {}", e.getMessage());
                    } catch (ExecutionException e) {
                        logger.debug("ExecutionException: {}", e.getMessage());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                }
            } catch (IOException e) {
                logger.debug("Message: {}", e.getMessage());
            }
        };
    }

    /**
     * This Method retrieves all IPv4 addresses of the server
     * 
     * @return A list of all available IPv4 addresses that are registered
     * @throws SocketException
     */
    private List<InetAddress> getIPv4Addresses() throws SocketException {
        Iterator<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces().asIterator();
        List<InetAddress> addresses = new ArrayList<>();
        // Get IPv4 addresses from all network interfaces
        if (null != networkInterfaces) {
            while (networkInterfaces.hasNext()) {
                NetworkInterface currentNetworkInterface = networkInterfaces.next();
                Iterator<InetAddress> inetAddresses = currentNetworkInterface.getInetAddresses().asIterator();
                while (inetAddresses.hasNext()) {
                    InetAddress currentAddress = inetAddresses.next();
                    if (currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()) {
                        addresses.add(currentAddress);
                    }
                }
            }
        }
        return addresses;
    }

    /**
     * This method will find any active host in the network and return a list of them
     * 
     * @param addresses
     * @return List of hosts
     * @throws UnknownHostException
     * @throws IOException
     */
    private List<InetAddress> findActiveHosts(List<InetAddress> addresses) throws UnknownHostException, IOException {
        List<InetAddress> hosts = new ArrayList<>();
        for (InetAddress inetAddress : addresses) {
            String[] addressStrings = inetAddress.getHostAddress().split("[.]");
            String subnet = addressStrings[0] + "." + addressStrings[1] + "." + addressStrings[2];
            int timeout = 50;
            for (int i = 0; i < 255; i++) {
                String host = subnet + "." + i;
                if (!inetAddress.getHostAddress().equals(host)) {
                    if (InetAddress.getByName(host).isReachable(timeout)) {
                        hosts.add(InetAddress.getByName(host));
                    }
                }
            }
        }
        return hosts;
    }

    /**
     * This method builds a thing based on the response from the device
     * 
     * @param response
     */
    private void buildDiscoveryResult(CommData response, Boolean isHostname) {
        ThingUID thingUID = new ThingUID(THING_TYPE_LIQUID_CHECK, response.payload.device.uuid);
        DiscoveryResult dResult = DiscoveryResultBuilder.create(thingUID)
                .withProperties(response.createPropertyMap(isHostname)).withLabel(response.payload.device.name).build();
        thingDiscovered(dResult);
    }
}
