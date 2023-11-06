/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.discovery;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.DsAPI;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.serverconnection.impl.DsAPIImpl;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeDiscoveryService} is responsible for discovering digitalSTROM-Server, if the server is in the
 * local network and is reachable through "dss.local." with default port number "8080". It uses the central
 * {@link AbstractDiscoveryService}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.digitalstrom")
public class BridgeDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(BridgeDiscoveryService.class);
    public static final String HOST_ADDRESS = "dss.local.";

    private final Runnable resultCreater = new Runnable() {

        @Override
        public void run() {
            createResult();
        }

        private void createResult() {
            ThingUID uid = getThingUID();

            if (uid != null) {
                Map<String, Object> properties = new HashMap<>(2);
                properties.put(DigitalSTROMBindingConstants.HOST, HOST_ADDRESS);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                        .withLabel("digitalSTROM-Server").build();
                thingDiscovered(result);
            }
        }

        private ThingUID getThingUID() {
            DsAPI digitalSTROMClient = new DsAPIImpl(HOST_ADDRESS, Config.DEFAULT_CONNECTION_TIMEOUT,
                    Config.DEFAULT_READ_TIMEOUT, true);
            String dSID = null;
            switch (digitalSTROMClient.checkConnection("123")) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                case HttpURLConnection.HTTP_FORBIDDEN:
                    Map<String, String> dsidMap = digitalSTROMClient.getDSID(null);
                    if (dsidMap != null) {
                        dSID = dsidMap.get(JSONApiResponseKeysEnum.DSID.getKey());
                    }
                    if (dSID != null && !dSID.isBlank()) {
                        return new ThingUID(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE, dSID);
                    } else {
                        logger.error("Can't get server dSID to generate ThingUID. Please add the server manually.");
                    }
            }
            return null;
        }
    };

    /**
     * Creates a new {@link BridgeDiscoveryService}.
     */
    public BridgeDiscoveryService() {
        super(new HashSet<>(Arrays.asList(DigitalSTROMBindingConstants.THING_TYPE_DSS_BRIDGE)), 10, false);
    }

    @Activate
    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Deactivate
    @Override
    protected void deactivate() {
        super.deactivate();
    }

    @Modified
    @Override
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        scheduler.execute(resultCreater);
    }
}
