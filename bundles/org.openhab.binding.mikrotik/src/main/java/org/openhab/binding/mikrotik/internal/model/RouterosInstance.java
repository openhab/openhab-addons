package org.openhab.binding.mikrotik.internal.model;

import me.legrange.mikrotik.*;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RouterosInstance {
    private final Logger logger = LoggerFactory.getLogger(RouterosInstance.class);

    private final String host;
    private final int port;
    private final int connectionTimeout;
    private final String login;
    private final String password;
    private ApiConnection connection;

    public static final String PROP_ID_KEY = ".id";

    private static final String CMD_PRINT_IFACES = "/interface/print";
    private static final String CMD_PRINT_CAPS_IFACES = "/caps-man/interface/print";
    private static final String CMD_PRINT_CAPSMAN_REGS = "/caps-man/registration-table/print";
    private static final String CMD_PRINT_WIRELESS_REGS = "/interface/wireless/registration-table/print";
    private static final String CMD_PRINT_RESOURCE = "/system/resource/print";
    private static final String CMD_PRINT_RB_INFO = "/system/routerboard/print";

    private List<RouterosInterfaceBase> interfaceCache;
    private List<RouterosCapsmanRegistration> capsmanRegistrationCache;
    private List<RouterosWirelessRegistration> wirelessRegistrationCache;

    private RouterosSystemResources resourcesCache;
    private RouterosRouterboardInfo rbInfo;

    private static Optional<RouterosInterfaceBase> createTypedInterface(Map<String,String> interfaceProps){
        RouterosInterfaceType ifaceType = RouterosInterfaceType.resolve(interfaceProps.get("type"));
        switch (ifaceType){
            case ETHERNET:
            case BRIDGE:
            case CAP:
                return Optional.of(new RouterosEthernetInterface(interfaceProps));
            case PPPOE_CLIENT: return Optional.of(new RouterosPPPoECliInterface(interfaceProps));
            case L2TP_SERVER: return Optional.of(new RouterosL2TPSrvInterface(interfaceProps));
            default: return Optional.empty();
        }
    }

    public RouterosInstance(String host, int port, String login, String password){
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        this.connectionTimeout = ApiConnection.DEFAULT_CONNECTION_TIMEOUT;
    }

    public boolean isConnected(){
        return connection != null && connection.isConnected();
    }

    public void start() throws MikrotikApiException {
        login();
        updateRouterboardInfo();
    }

    public void stop() throws ApiConnectionException {
        logout();
    }

    public void login() throws MikrotikApiException {
        logger.debug("Attempting login to {} ...", host);
        this.connection = ApiConnection.connect(SocketFactory.getDefault(), host, port, connectionTimeout);
        connection.login(login, password);
        logger.debug("Logged in to RouterOS at {} !", host);
    }

    public void logout() throws ApiConnectionException {
        logger.debug("Logging out of {}", host);
        if(connection != null){
            logger.debug("Closing connection to {}", host);
            connection.close();
        }
    }

    public void refresh() throws MikrotikApiException {
        synchronized (this) {
            updateResources();
            updateInterfaces();
            updateCapsmanRegistrations();
            updateWirelessRegistrations();
        }
    }

    public @Nullable RouterosRouterboardInfo getRouterboardInfo(){
        return rbInfo;
    }
    public @Nullable RouterosSystemResources getSysResources(){ return resourcesCache; }

    public @Nullable RouterosCapsmanRegistration findCapsmanRegistration(String macAddress){
        logger.trace("findCapsmanRegistration({}) called for {}", macAddress, host);
        Optional<RouterosCapsmanRegistration> searchResult = capsmanRegistrationCache.stream()
                .filter(registration -> registration.getMacAddress().equalsIgnoreCase(macAddress))
                .findFirst();
        return searchResult.orElse(null);
    }

    public @Nullable RouterosWirelessRegistration findWirelessRegistration(String macAddress){
        logger.trace("findWirelessRegistration({}) called for {}", macAddress, host);
        Optional<RouterosWirelessRegistration> searchResult = wirelessRegistrationCache.stream()
                .filter(registration -> registration.getMacAddress().equalsIgnoreCase(macAddress))
                .findFirst();
        return searchResult.orElse(null);
    }

    public @Nullable RouterosInterfaceBase findInterface(String name){
        logger.trace("findInterface({}) called for {}", name, host);
        Optional<RouterosInterfaceBase> searchResult = interfaceCache.stream()
                .filter(iface -> iface.getName() != null && iface.getName().equalsIgnoreCase(name))
                .findFirst();
        return searchResult.orElse(null);
    }


    private void updateInterfaces() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_IFACES, host);
        List<Map<String, String>> ifaceResponse = connection.execute(CMD_PRINT_IFACES);
        logger.trace("Executing '{}' on {}...", CMD_PRINT_CAPS_IFACES, host);
        List<Map<String, String>> capsResponse = connection.execute(CMD_PRINT_CAPS_IFACES);

        interfaceCache = ifaceResponse.stream().map(props -> {
            logger.trace("Got interface props from {}: {}", host, props);
            Optional<RouterosInterfaceBase> ifaceOpt = createTypedInterface(props);
            if(ifaceOpt.isPresent()){
                RouterosInterfaceBase iface = ifaceOpt.get();
                // Enrich with CAPsMAN data
                Optional<Map<String, String>> capsProps = capsResponse.stream()
                        .filter(sp -> sp.get(PROP_ID_KEY).equals(iface.getId()))
                        .findFirst();
                if(capsProps.isPresent()){
                    iface.mergeProps(capsProps.get());
                } else {
                    logger.trace("No CAPsMAN props found for interface #{} {}", props.get(PROP_ID_KEY), props.get("name"));
                }
            } else {
                logger.trace("Skipping unsupported interface type: {}", props.get("type"));
            }
            return ifaceOpt;
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    private void updateCapsmanRegistrations() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_CAPSMAN_REGS, host);
        List<Map<String, String>> response = connection.execute(CMD_PRINT_CAPSMAN_REGS);
        capsmanRegistrationCache = response.stream()
                .map(RouterosCapsmanRegistration::new)
                .collect(Collectors.toList());
    }

    private void updateWirelessRegistrations() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_WIRELESS_REGS, host);
        List<Map<String, String>> response = connection.execute(CMD_PRINT_WIRELESS_REGS);
        wirelessRegistrationCache = response.stream()
                .map(RouterosWirelessRegistration::new)
                .collect(Collectors.toList());
    }

    private void updateResources() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_RESOURCE, host);
        List<Map<String, String>> response = connection.execute(CMD_PRINT_RESOURCE);
        this.resourcesCache = new RouterosSystemResources(response.get(0));
    }

    private void updateRouterboardInfo() throws MikrotikApiException {
        logger.trace("Executing '{}' on {}...", CMD_PRINT_RB_INFO, host);
        List<Map<String, String>> response = connection.execute(CMD_PRINT_RB_INFO);
        this.rbInfo = new RouterosRouterboardInfo(response.get(0));
    }

}
