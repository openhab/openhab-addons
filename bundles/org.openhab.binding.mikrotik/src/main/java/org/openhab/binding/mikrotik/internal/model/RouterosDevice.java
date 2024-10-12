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
package org.openhab.binding.mikrotik.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private @Nullable ApiConnection connection;

    public static final String PROP_ID_KEY = ".id";
    public static final String PROP_TYPE_KEY = "type";
    public static final String PROP_NAME_KEY = "name";
    public static final String PROP_SSID_KEY = "ssid";
    public static final String PROP_CONFIG_SSID_KEY = "configuration.ssid";
    private static final String CMD_PRINT_IFACES = "/interface/print";
    private static final String CMD_PRINT_IFACE_TYPE_TPL = "/interface/%s/print";
    private static final String CMD_MONTOR_IFACE_MONITOR_TPL = "/interface/%s/monitor numbers=%s once";
    private static final String CMD_PRINT_CAPS_IFACES = "/caps-man/interface/print";
    private static final String CMD_PRINT_CAPSMAN_REGS = "/caps-man/registration-table/print";
    private static final String CMD_PRINT_WIFI_REGS = "/interface/wifi/registration-table/print";
    private static final String CMD_PRINT_WIRELESS_REGS = "/interface/wireless/registration-table/print";
    private static final String CMD_PRINT_RESOURCE = "/system/resource/print";
    private static final String CMD_PRINT_RB_INFO = "/system/routerboard/print";

    private final List<RouterosInterfaceBase> interfaceCache = new ArrayList<>();
    private final List<RouterosCapsmanRegistration> capsmanRegistrationCache = new ArrayList<>();
    private final List<RouterosWirelessRegistration> wirelessRegistrationCache = new ArrayList<>();
    private final Set<String> monitoredInterfaces = new HashSet<>();
    private final Map<String, String> wlanSsid = new HashMap<>();

    private @Nullable RouterosSystemResources resourcesCache;
    private @Nullable RouterosRouterboardInfo rbInfo;

    private static Optional<RouterosInterfaceBase> createTypedInterface(Map<String, String> interfaceProps) {
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
            case WIFI:
                return Optional.of(new RouterosWifiInterface(interfaceProps));
            case PPPOE_CLIENT:
                return Optional.of(new RouterosPPPoECliInterface(interfaceProps));
            case PPP_CLIENT:
                return Optional.of(new RouterosPPPCliInterface(interfaceProps));
            case L2TP_SERVER:
                return Optional.of(new RouterosL2TPSrvInterface(interfaceProps));
            case L2TP_CLIENT:
                return Optional.of(new RouterosL2TPCliInterface(interfaceProps));
            case LTE:
                return Optional.of(new RouterosLTEInterface(interfaceProps));
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
        ApiConnection conn = this.connection;
        return conn != null && conn.isConnected();
    }

    public void start() throws MikrotikApiException {
        login();
        updateRouterboardInfo();
        if (rbInfo != null) {
            logger.debug("RouterOS Version = {}", rbInfo.getFirmwareVersion());
        }
    }

    public void stop() {
        ApiConnection conn = this.connection;
        if (conn != null && conn.isConnected()) {
            logout();
        }
    }

    public void login() throws MikrotikApiException {
        logger.debug("Attempting login to {} ...", host);
        ApiConnection conn = ApiConnection.connect(SocketFactory.getDefault(), host, port, connectionTimeout);
        conn.login(login, password);
        logger.debug("Logged in to RouterOS at {} !", host);
        this.connection = conn;
    }

    public void logout() {
        ApiConnection conn = this.connection;
        logger.debug("Logging out of {}", host);
        if (conn != null) {
            logger.debug("Closing connection to {}", host);
            try {
                conn.close();
            } catch (ApiConnectionException e) {
                logger.debug("Logout error", e);
            } finally {
                this.connection = null;
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
            try {
                updateCapsmanRegistrations();
            } catch (MikrotikApiException e) {
                logger.debug(
                        "MikrotikApiException: Device may have the CAPsMAN feature for wireless management disabled.");
            }
            wirelessRegistrationCache.clear();
            try {
                updateWirelessRegistrations();
            } catch (MikrotikApiException e) {
                logger.debug(
                        "MikrotikApiException: Device does not appear to have any built in CAPsMAN wireless devices.");
            }
            try {
                updateWifiRegistrations();
            } catch (MikrotikApiException e) {
                logger.debug("MikrotikApiException: Device does not appear to have any built in wifi.");
            }
            logger.trace("There are {} wirelessRegistration's registered in cache.", wirelessRegistrationCache.size());
        }
    }

    public @Nullable RouterosRouterboardInfo getRouterboardInfo() {
        return rbInfo;
    }

    public @Nullable RouterosSystemResources getSysResources() {
        return resourcesCache;
    }

    public @Nullable RouterosCapsmanRegistration findCapsmanRegistration(String macAddress) {
        Optional<RouterosCapsmanRegistration> searchResult = capsmanRegistrationCache.stream()
                .filter(registration -> macAddress.equalsIgnoreCase(registration.getMacAddress())).findFirst();
        return searchResult.orElse(null);
    }

    public @Nullable RouterosWirelessRegistration findWirelessRegistration(String macAddress) {
        Optional<RouterosWirelessRegistration> searchResult = wirelessRegistrationCache.stream()
                .filter(registration -> macAddress.equalsIgnoreCase(registration.getMacAddress())).findFirst();
        return searchResult.orElse(null);
    }

    @SuppressWarnings("null")
    public @Nullable RouterosInterfaceBase findInterface(String name) {
        Optional<RouterosInterfaceBase> searchResult = interfaceCache.stream()
                .filter(iface -> iface.getName() != null && iface.getName().equalsIgnoreCase(name)).findFirst();
        return searchResult.orElse(null);
    }

    @SuppressWarnings("null")
    private void updateInterfaceData() throws MikrotikApiException {
        ApiConnection conn = this.connection;
        if (conn == null) {
            return;
        }

        List<Map<String, String>> ifaceResponse = conn.execute(CMD_PRINT_IFACES);

        Set<String> interfaceTypesToPoll = new HashSet<>();
        this.wlanSsid.clear();
        this.interfaceCache.clear();
        ifaceResponse.forEach(props -> {
            logger.trace("Interface Details:{}", props.toString());
            Optional<RouterosInterfaceBase> ifaceOpt = createTypedInterface(props);
            if (ifaceOpt.isPresent()) {
                RouterosInterfaceBase iface = ifaceOpt.get();
                if (iface.hasDetailedReport()) {
                    interfaceTypesToPoll.add(iface.getApiType());
                }
                this.interfaceCache.add(iface);
            }
        });

        // Checks if any new interfaces have been setup since last check
        Map<String, Map<String, String>> typedIfaceResponse = new HashMap<>();
        for (String ifaceApiType : interfaceTypesToPoll) {
            String cmd = String.format(CMD_PRINT_IFACE_TYPE_TPL, ifaceApiType);
            if (ifaceApiType.compareTo("cap") == 0) {
                cmd = CMD_PRINT_CAPS_IFACES;
            }
            logger.debug("Command used for updating the interfaces is:{}", cmd);
            connection.execute(cmd).forEach(propMap -> {
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
            Map<String, String> additionalIfaceProps = typedIfaceResponse.get(ifaceModel.getName());
            if (additionalIfaceProps != null) {
                ifaceModel.mergeProps(additionalIfaceProps);
            }
            // Get monitor data, runs if you have added an interface thing.
            if (ifaceModel.hasMonitor() && monitoredInterfaces.contains(ifaceModel.getName())) {
                String cmd = String.format(CMD_MONTOR_IFACE_MONITOR_TPL, ifaceModel.getApiType(), ifaceModel.getName());
                if (logger.isDebugEnabled()) {
                    logger.debug("Getting detailed data for Interface:{}, with command:{}", ifaceModel.getName(), cmd);
                }
                List<Map<String, String>> monitorProps = connection.execute(cmd);
                ifaceModel.mergeProps(monitorProps.get(0));
            }
            // Note SSIDs for non-CAPsMAN wireless clients
            String ifaceName = ifaceModel.getName();
            String ifaceSsid;
            switch (ifaceModel.getApiType()) {
                case "wifi":
                    ifaceSsid = ifaceModel.getProperty(PROP_CONFIG_SSID_KEY);
                    break;
                case "caps":
                case "wireless":
                default:
                    ifaceSsid = ifaceModel.getProperty(PROP_SSID_KEY);
            }
            if (ifaceName != null && ifaceSsid != null && !ifaceName.isBlank() && !ifaceSsid.isBlank()) {
                this.wlanSsid.put(ifaceName, ifaceSsid);
            }
        }
        logger.debug("Found the following SSID's:{}", wlanSsid.toString());
    }

    private void updateCapsmanRegistrations() throws MikrotikApiException {
        ApiConnection conn = this.connection;
        if (conn == null) {
            return;
        }
        List<Map<String, String>> response = conn.execute(CMD_PRINT_CAPSMAN_REGS);
        if (response != null) {
            capsmanRegistrationCache.clear();
            response.forEach(reg -> capsmanRegistrationCache.add(new RouterosCapsmanRegistration(reg)));
        }
    }

    private void updateWirelessRegistrations() throws MikrotikApiException {
        ApiConnection conn = this.connection;
        if (conn == null) {
            return;
        }
        List<Map<String, String>> response = conn.execute(CMD_PRINT_WIRELESS_REGS);
        response.forEach(props -> {
            String wlanIfaceName = props.get("interface");
            String wlanSsidName = wlanSsid.get(wlanIfaceName);

            if (wlanSsidName != null && wlanIfaceName != null && !wlanIfaceName.isBlank() && !wlanSsidName.isBlank()) {
                props.put(PROP_SSID_KEY, wlanSsidName);
            }
            wirelessRegistrationCache.add(new RouterosWirelessRegistration(props));
        });
    }

    private void updateWifiRegistrations() throws MikrotikApiException {
        ApiConnection conn = this.connection;
        if (conn == null) {
            return;
        }
        List<Map<String, String>> response = conn.execute(CMD_PRINT_WIFI_REGS);
        response.forEach(props -> {
            String wlanIfaceName = props.get("interface");
            String wlanSsidName = wlanSsid.get(wlanIfaceName);

            if (wlanSsidName != null && wlanIfaceName != null && !wlanIfaceName.isBlank() && !wlanSsidName.isBlank()) {
                props.put("configuration.ssid", wlanSsidName);
            }
            wirelessRegistrationCache.add(new RouterosWirelessRegistration(props));
        });
    }

    private void updateResources() throws MikrotikApiException {
        ApiConnection conn = this.connection;
        if (conn == null) {
            return;
        }
        List<Map<String, String>> response = conn.execute(CMD_PRINT_RESOURCE);
        this.resourcesCache = new RouterosSystemResources(response.get(0));
    }

    private void updateRouterboardInfo() throws MikrotikApiException {
        ApiConnection conn = this.connection;
        if (conn == null) {
            return;
        }
        List<Map<String, String>> response = conn.execute(CMD_PRINT_RB_INFO);
        this.rbInfo = new RouterosRouterboardInfo(response.get(0));
    }
}
