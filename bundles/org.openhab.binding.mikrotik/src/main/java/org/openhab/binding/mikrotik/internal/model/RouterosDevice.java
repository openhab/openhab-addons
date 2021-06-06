/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.SocketFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.ApiConnectionException;
import me.legrange.mikrotik.MikrotikApiException;

/**
 * The {@link RouterosDevice} class is wrapped inside a bridge thing and responsible for communication with
 * Mikrotik device, data fetching, caching and aggregation.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class RouterosDevice {
    private final Logger logger = LoggerFactory.getLogger(RouterosDevice.class);

    private final String host;
    private final int port;
    private final int connectionTimeout;
    private final String login;
    private final String password;
    @Nullable
    private ApiConnection connection;

    public static final String PROP_ID_KEY = ".id";
    public static final String PROP_TYPE_KEY = "type";
    public static final String PROP_NAME_KEY = "name";
    public static final String PROP_SSID_KEY = "ssid";

    private static final String CMD_PRINT_IFACES = "/interface/print";
    private static final String CMD_PRINT_IFACE_TYPE_TPL = "/interface/%s/print";
    private static final String CMD_MONTOR_IFACE_MONITOR_TPL = "/interface/%s/monitor numbers=%s once";
    private static final String CMD_PRINT_CAPS_IFACES = "/caps-man/interface/print";
    private static final String CMD_PRINT_CAPSMAN_REGS = "/caps-man/registration-table/print";
    private static final String CMD_PRINT_WIRELESS_REGS = "/interface/wireless/registration-table/print";
    private static final String CMD_PRINT_RESOURCE = "/system/resource/print";
    private static final String CMD_PRINT_RB_INFO = "/system/routerboard/print";

    @Nullable
    private List<RouterosInterfaceBase> interfaceCache;
    @Nullable
    private List<RouterosCapsmanRegistration> capsmanRegistrationCache;
    @Nullable
    private List<RouterosWirelessRegistration> wirelessRegistrationCache;
    private Set<String> monitoredInterfaces = new HashSet<>();
    private Map<String, String> wlanSsid = new HashMap<>();

    @Nullable
    private RouterosSystemResources resourcesCache;
    @Nullable
    private RouterosRouterboardInfo rbInfo;

    private static Optional<RouterosInterfaceBase> createTypedInterface(Map<String, String> interfaceProps) {
        @Nullable
        RouterosInterfaceType ifaceType = RouterosInterfaceType.resolve(interfaceProps.get(PROP_TYPE_KEY));
        if (ifaceType == null) {
            return Optional.empty();
        }
        switch (ifaceType) {
            case ETHERNET:
                return Optional.of(new RouterosEthernetInterface(interfaceProps));
            case BRIDGE:
                return Optional.of(new RouterosBridgeInterface(interfaceProps));
            case CAP:
                return Optional.of(new RouterosCapInterface(interfaceProps));
            case WLAN:
                return Optional.of(new RouterosWlanInterface(interfaceProps));
            case PPPOE_CLIENT:
                return Optional.of(new RouterosPPPoECliInterface(interfaceProps));
            case L2TP_SERVER:
                return Optional.of(new RouterosL2TPSrvInterface(interfaceProps));
            case L2TP_CLIENT:
                return Optional.of(new RouterosL2TPCliInterface(interfaceProps));
            default:
                return Optional.empty();
        }
    }

    public RouterosDevice(String host, int port, String login, String password) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.connectionTimeout = ApiConnection.DEFAULT_CONNECTION_TIMEOUT;
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public void start() throws MikrotikApiException {
        login();
        updateRouterboardInfo();
    }

    public void stop() {
        if (connection != null && connection.isConnected()) {
            logout();
        }
    }

    public void login() throws MikrotikApiException {
        logger.debug("Attempting login to {} ...", host);
        this.connection = ApiConnection.connect(SocketFactory.getDefault(), host, port, connectionTimeout);
        connection.login(login, password);
        logger.debug("Logged in to RouterOS at {} !", host);
    }

    public void logout() {
        logger.debug("Logging out of {}", host);
        if (connection != null) {
            logger.debug("Closing connection to {}", host);
            try {
                connection.close();
            } catch (ApiConnectionException e) {
                logger.debug("Logout error", e);
            } finally {
                connection = null;
            }
        }
    }

    public boolean registerForMonitoring(String interfaceName) {
        return monitoredInterfaces.add(interfaceName);
    }

    public boolean unregisterForMonitoring(String interfaceName) {
        return monitoredInterfaces.remove(interfaceName);
    }

    public void refresh() throws MikrotikApiException {
        synchronized (this) {
            updateResources();
            updateInterfaceData();
            updateCapsmanRegistrations();
            updateWirelessRegistrations();
        }
    }

    public @Nullable RouterosRouterboardInfo getRouterboardInfo() {
        return rbInfo;
    }

    public @Nullable RouterosSystemResources getSysResources() {
        return resourcesCache;
    }

    public @Nullable RouterosCapsmanRegistration findCapsmanRegistration(String macAddress) {
        logger.trace("findCapsmanRegistration({}) called for {}", macAddress, host);
        Optional<RouterosCapsmanRegistration> searchResult = capsmanRegistrationCache.stream()
                .filter(registration -> registration.getMacAddress().equalsIgnoreCase(macAddress)).findFirst();
        return searchResult.orElse(null);
    }

    public @Nullable RouterosWirelessRegistration findWirelessRegistration(String macAddress) {
        logger.trace("findWirelessRegistration({}) called for {}", macAddress, host);
        Optional<RouterosWirelessRegistration> searchResult = wirelessRegistrationCache.stream()
                .filter(registration -> registration.getMacAddress().equalsIgnoreCase(macAddress)).findFirst();
        return searchResult.orElse(null);
    }

    public @Nullable RouterosInterfaceBase findInterface(String name) {
        logger.trace("findInterface({}) called for {}", name, host);
        Optional<RouterosInterfaceBase> searchResult = interfaceCache.stream()
                .filter(iface -> iface.getName() != null && iface.getName().equalsIgnoreCase(name)).findFirst();
        return searchResult.orElse(null);
    }

    private void updateInterfaceData() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_IFACES, host);
        List<Map<String, String>> ifaceResponse = connection.execute(CMD_PRINT_IFACES);

        Set<String> interfaceTypesToPoll = new HashSet<>();
        this.wlanSsid.clear();
        interfaceCache = ifaceResponse.stream().map(props -> {
            logger.trace("Got interface props from {}: {}", host, props);
            Optional<RouterosInterfaceBase> ifaceOpt = createTypedInterface(props);
            if (ifaceOpt.isPresent() && ifaceOpt.get().hasDetailedReport()) {
                interfaceTypesToPoll.add(ifaceOpt.get().getApiType());
            } else {
                logger.trace("Skipping unsupported interface type: {}", props.get(PROP_TYPE_KEY));
            }
            return ifaceOpt;
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        Map<String, Map<String, String>> typedIfaceResponse = new HashMap<>();
        for (String ifaceApiType : interfaceTypesToPoll) {
            String cmd = String.format(CMD_PRINT_IFACE_TYPE_TPL, ifaceApiType);
            if (ifaceApiType.compareTo("cap") == 0) {
                cmd = CMD_PRINT_CAPS_IFACES;
            }
            logger.trace("Executing '{}' on {}...", cmd, host);
            connection.execute(cmd).forEach(propMap -> {
                @Nullable
                String ifaceName = propMap.get(PROP_NAME_KEY);
                if (ifaceName != null) {
                    if (typedIfaceResponse.containsKey(ifaceName)) {
                        typedIfaceResponse.get(ifaceName).putAll(propMap);
                    } else {
                        typedIfaceResponse.put(ifaceName, propMap);
                    }
                }
            });
        }

        for (RouterosInterfaceBase ifaceModel : interfaceCache) {
            // Enrich with detailed data
            @Nullable
            Map<String, String> additionalIfaceProps = typedIfaceResponse.get(ifaceModel.getName());
            if (additionalIfaceProps != null) {
                ifaceModel.mergeProps(additionalIfaceProps);
            }
            // Get monitor data
            if (ifaceModel.hasMonitor() && monitoredInterfaces.contains(ifaceModel.getName())) {
                String cmd = String.format(CMD_MONTOR_IFACE_MONITOR_TPL, ifaceModel.getApiType(), ifaceModel.getName());
                logger.trace("Executing '{}' on {}...", cmd, host);
                List<Map<String, String>> monitorProps = connection.execute(cmd);
                ifaceModel.mergeProps(monitorProps.get(0));
            }
            // Note SSIDs for non-CAPsMAN wireless clients
            @Nullable
            String ifaceName = ifaceModel.getName();
            @Nullable
            String ifaceSsid = ifaceModel.getProperty(PROP_SSID_KEY);
            if (ifaceName != null && ifaceSsid != null && !ifaceName.isBlank() && !ifaceSsid.isBlank()) {
                this.wlanSsid.put(ifaceName, ifaceSsid);
            }
        }
    }

    private void updateCapsmanRegistrations() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_CAPSMAN_REGS, host);
        List<Map<String, String>> response = connection.execute(CMD_PRINT_CAPSMAN_REGS);
        capsmanRegistrationCache = response.stream().map(props -> {
            logger.trace("Got capsman registration props from {}: {}", host, props);
            return new RouterosCapsmanRegistration(props);
        }).collect(Collectors.toList());
    }

    private void updateWirelessRegistrations() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_WIRELESS_REGS, host);
        List<Map<String, String>> response = connection.execute(CMD_PRINT_WIRELESS_REGS);
        logger.trace("wlanSsid = {}", wlanSsid);
        wirelessRegistrationCache = response.stream().map(props -> {
            logger.trace("Got wireless registration props from {}: {}", host, props);

            @Nullable
            String wlanIfaceName = props.get("interface");
            @Nullable
            String wlanSsidName = wlanSsid.get(wlanIfaceName);

            if (wlanSsidName != null && wlanIfaceName != null && !wlanIfaceName.isBlank() && !wlanSsidName.isBlank()) {
                props.put(PROP_SSID_KEY, wlanSsidName);
            }
            return new RouterosWirelessRegistration(props);
        }).collect(Collectors.toList());
    }

    private void updateResources() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_RESOURCE, host);
        List<Map<String, String>> response = connection.execute(CMD_PRINT_RESOURCE);
        logger.trace("Got resource props from {}: {}", host, response.get(0));
        this.resourcesCache = new RouterosSystemResources(response.get(0));
    }

    private void updateRouterboardInfo() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_RB_INFO, host);
        List<Map<String, String>> response = connection.execute(CMD_PRINT_RB_INFO);
        this.rbInfo = new RouterosRouterboardInfo(response.get(0));
    }
}
