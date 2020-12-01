/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.openhab.binding.sony.internal.AccessResult;
import org.openhab.binding.sony.internal.CheckResult;
import org.openhab.binding.sony.internal.SonyAuth;
import org.openhab.binding.sony.internal.SonyAuthChecker;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.ircc.IrccClientFactory;
import org.openhab.binding.sony.internal.ircc.models.IrccClient;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommand;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommands;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebClient;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConfig;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConstants;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebError;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.InterfaceInformation;
import org.openhab.binding.sony.internal.scalarweb.models.api.NetIf;
import org.openhab.binding.sony.internal.scalarweb.models.api.NetworkSetting;
import org.openhab.binding.sony.internal.scalarweb.models.api.RemoteControllerInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.RemoteControllerInfo.RemoteCommand;
import org.openhab.binding.sony.internal.scalarweb.models.api.SystemInformation;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConfig;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.openhab.binding.sony.internal.transports.TransportOptionAutoAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * This is the login protocol handler for scalar web systems. The login handler will handle both registration and login.
 * Additionally, the handler will also perform initial connection logic (like writing scalar/IRCC commands to the file).
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type callback
 */
@NonNullByDefault
public class ScalarWebLoginProtocol<T extends ThingCallback<String>> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebLoginProtocol.class);

    /** The configuration */
    private final ScalarWebConfig config;

    /** The callback to set state and status. */
    private final T callback;

    /** The scalar state */
    private final ScalarWebClient scalarClient;

    /** The transformation service */
    private final @Nullable TransformationService transformService;

    /** The sony authentication */
    private final SonyAuth sonyAuth;

    /**
     * Constructs the protocol handler from given parameters.
     *
     * @param client a non-null {@link ScalarWebClient}
     * @param config a non-null {@link SimpleIpConfig} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     * @param transformService a potentially null transformation service
     * @throws IOException
     */
    public ScalarWebLoginProtocol(final ScalarWebClient client, final ScalarWebConfig config, final T callback,
            final @Nullable TransformationService transformService) throws IOException {
        Objects.requireNonNull(client, "client cannot be null");
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        this.scalarClient = client;
        this.config = config;
        this.callback = callback;
        this.transformService = transformService;

        final ScalarWebService accessControlService = scalarClient.getService(ScalarWebService.ACCESSCONTROL);

        sonyAuth = new SonyAuth(() -> {
            final String irccUrl = config.getIrccUrl();
            try {
                SonyUtil.sendWakeOnLan(logger, config.getDeviceIpAddress(), config.getDeviceMacAddress());
                return irccUrl == null || StringUtils.isEmpty(irccUrl) ? null : IrccClientFactory.get(irccUrl);
            } catch (IOException | URISyntaxException e) {
                logger.debug("Cannot create IRCC Client: {}", e.getMessage());
                return null;
            }
        }, accessControlService);
    }

    /**
     * Gets the callback.
     *
     * @return the callback
     */
    T getCallback() {
        return callback;
    }

    /**
     * Attempts to log into the system.
     *
     * @return the access check result
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws URISyntaxException the URI syntax exception
     */
    public AccessResult login() throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        final ScalarWebService systemService = scalarClient.getService(ScalarWebService.SYSTEM);
        if (systemService == null) {
            return AccessResult.SERVICEMISSING;
        }

        final URL baseUrl = scalarClient.getDevice().getBaseUrl();
        if (baseUrl == null) {
            return new AccessResult(AccessResult.OTHER, "missing base url");
        }

        final SonyTransport[] transports = scalarClient.getDevice().getServices().stream()
                .map(srv -> srv.getTransport()).toArray(SonyTransport[]::new);

        // turn off auto authorization for all services
        Arrays.stream(transports).forEach(t -> t.setOption(TransportOptionAutoAuth.FALSE));

        SonyUtil.sendWakeOnLan(logger, config.getDeviceIpAddress(), config.getDeviceMacAddress());

        final String accessCode = config.getAccessCode();
        final SonyAuthChecker authChecker = new SonyAuthChecker(systemService.getTransport(), accessCode);
        final CheckResult checkResult = authChecker.checkResult(() -> {

            // Default to a bad access result
            AccessResult ar = new AccessResult("unknown", "Unknown Result!");

            // If we have a power status - execute it first
            if (systemService.hasMethod(ScalarWebMethod.GETPOWERSTATUS)) {
                final ScalarWebResult result = systemService.execute(ScalarWebMethod.GETPOWERSTATUS);
                ar = getAccessResult(result);
                if (ar == AccessResult.NEEDSPAIRING || ar == AccessResult.DISPLAYOFF) {
                    return ar;
                }
            }

            // Now try the get system information
            if (systemService.hasMethod(ScalarWebMethod.GETSYSTEMINFORMATION)) {
                final ScalarWebResult result = systemService.execute(ScalarWebMethod.GETSYSTEMINFORMATION);
                ar = getAccessResult(result);
                if (ar == AccessResult.NEEDSPAIRING || ar == AccessResult.DISPLAYOFF) {
                    return ar;
                }
            }

            // and finally the get device mode
            if (systemService.hasMethod(ScalarWebMethod.GETDEVICEMODE)) {
                final ScalarWebResult result = systemService.execute(ScalarWebMethod.GETDEVICEMODE);
                // getDeviceMode takes an unknown "value" argument - if we get back
                // illegal arugment - then it executed fine and we are OK
                ar = getAccessResult(result);
                if (ar == AccessResult.NEEDSPAIRING || ar == AccessResult.DISPLAYOFF) {
                    return ar;
                }
                if (result.getDeviceErrorCode() == ScalarWebError.ILLEGALARGUMENT) {
                    ar = AccessResult.OK;
                }
            }

            // Finally - return our last result (probably either AccessResult.OK or some http error)
            return ar;
        });

        if (CheckResult.OK_HEADER.equals(checkResult)) {
            if (accessCode == null || StringUtils.isEmpty(accessCode)) {
                // This shouldn't happen - if our check result is OK_HEADER, then
                // we had a valid (non-null, non-empty) accessCode. Unfortunately
                // nullable checking thinks this can be null now.
                logger.debug("This shouldn't happen - access code is blank!: {}", accessCode);
                return new AccessResult(AccessResult.OTHER, "Access code cannot be blank");
            } else {
                SonyAuth.setupHeader(accessCode, transports);
            }
        } else if (CheckResult.OK_COOKIE.equals(checkResult)) {
            SonyAuth.setupCookie(transports);
        } else if (AccessResult.DISPLAYOFF.equals(checkResult)) {
            return checkResult;
        } else if (AccessResult.NEEDSPAIRING.equals(checkResult)) {
            if (StringUtils.isEmpty(accessCode)) {
                return new AccessResult(AccessResult.OTHER, "Access code cannot be blank");
            } else {
                try (SonyHttpTransport httpTransport = SonyTransportFactory.createHttpTransport(baseUrl,
                        ScalarWebService.ACCESSCONTROL)) {
                    final AccessResult res = sonyAuth.requestAccess(httpTransport,
                            StringUtils.equalsIgnoreCase(ScalarWebConstants.ACCESSCODE_RQST, accessCode) ? null
                                    : accessCode);
                    if (AccessResult.OK.equals(res)) {
                        SonyAuth.setupCookie(transports);
                    } else {
                        return res;
                    }
                }
            }
        }

        postLogin();
        return AccessResult.OK;
    }

    /**
     * Get's the access result for the result
     * 
     * @param result a non-null result
     * @return a non-null if there is a bad result, null if okay
     */
    private static @Nullable AccessResult getAccessResult(ScalarWebResult result) {
        Objects.requireNonNull(result, "result cannot be null");

        if (result.getDeviceErrorCode() == ScalarWebError.DISPLAYISOFF) {
            return AccessResult.DISPLAYOFF;
        }

        final int deviceErrorCode = result.getDeviceErrorCode();
        final int httpCode = result.getHttpResponse().getHttpCode();

        if (deviceErrorCode == ScalarWebError.NOTIMPLEMENTED || deviceErrorCode == ScalarWebError.FORBIDDEN
                || httpCode == HttpStatus.UNAUTHORIZED_401 || httpCode == HttpStatus.FORBIDDEN_403) {
            return AccessResult.NEEDSPAIRING;
        }

        if (httpCode == HttpStatus.OK_200) {
            return AccessResult.OK;
        }

        return new AccessResult(result.getHttpResponse());
    }

    /**
     * Post successful login stuff - set the properties and write out the commands.
     *
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void postLogin() throws ParserConfigurationException, SAXException, IOException {
        logger.debug("WebScalar System now connected");

        final ScalarWebService sysService = scalarClient.getService(ScalarWebService.SYSTEM);
        Objects.requireNonNull(sysService, "sysService is null - shouldn't happen since it's checked in login()");

        if (sysService.hasMethod(ScalarWebMethod.GETSYSTEMINFORMATION)) {
            final ScalarWebResult sysResult = sysService.execute(ScalarWebMethod.GETSYSTEMINFORMATION);
            if (!sysResult.isError() && sysResult.hasResults()) {
                final SystemInformation sysInfo = sysResult.as(SystemInformation.class);
                callback.setProperty(ScalarWebConstants.PROP_PRODUCT, sysInfo.getProduct());
                callback.setProperty(ScalarWebConstants.PROP_NAME, sysInfo.getName());

                final String modelName = sysInfo.getModel();
                if (modelName != null && SonyUtil.isValidModelName(modelName)) {
                    callback.setProperty(ScalarWebConstants.PROP_MODEL, modelName);
                }

                callback.setProperty(ScalarWebConstants.PROP_GENERATION, sysInfo.getGeneration());
                callback.setProperty(ScalarWebConstants.PROP_SERIAL, sysInfo.getSerial());
                callback.setProperty(ScalarWebConstants.PROP_MACADDR, sysInfo.getMacAddr());
                callback.setProperty(ScalarWebConstants.PROP_AREA, sysInfo.getArea());
                callback.setProperty(ScalarWebConstants.PROP_REGION, sysInfo.getRegion());
            }
        }

        if (sysService.hasMethod(ScalarWebMethod.GETINTERFACEINFORMATION)) {
            final ScalarWebResult intResult = sysService.execute(ScalarWebMethod.GETINTERFACEINFORMATION);
            if (!intResult.isError() && intResult.hasResults()) {
                final InterfaceInformation intInfo = intResult.as(InterfaceInformation.class);
                callback.setProperty(ScalarWebConstants.PROP_INTERFACEVERSION, intInfo.getInterfaceVersion());
                callback.setProperty(ScalarWebConstants.PROP_PRODUCTCATEGORY, intInfo.getProductCategory());
                callback.setProperty(ScalarWebConstants.PROP_SERVERNAME, intInfo.getServerName());
            }
        }

        if (sysService.hasMethod(ScalarWebMethod.GETNETWORKSETTINGS)) {
            for (final String netIf : new String[] { "eth0", "wlan0", "eth1", "wlan1" }) {
                final ScalarWebResult swr = sysService.execute(ScalarWebMethod.GETNETWORKSETTINGS, new NetIf(netIf));
                if (!swr.isError() && swr.hasResults()) {
                    final NetworkSetting netSetting = swr.as(NetworkSetting.class);
                    callback.setProperty(ScalarWebConstants.PROP_NETIF, netSetting.getNetif());
                    callback.setProperty(ScalarWebConstants.PROP_HWADDRESS, netSetting.getHwAddr());
                    callback.setProperty(ScalarWebConstants.PROP_IPV4, netSetting.getIpAddrV4());
                    callback.setProperty(ScalarWebConstants.PROP_IPV6, netSetting.getIpAddrV6());
                    callback.setProperty(ScalarWebConstants.PROP_NETMASK, netSetting.getNetmask());
                    callback.setProperty(ScalarWebConstants.PROP_GATEWAY, netSetting.getGateway());
                    break;
                }
            }
        }

        writeCommands(sysService);
    }

    /**
     * Write commands
     *
     * @param service the non-null service
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeCommands(final ScalarWebService service)
            throws ParserConfigurationException, SAXException, IOException {
        Objects.requireNonNull(service, "service cannot be null");

        if (transformService == null) {
            logger.debug("No MAP transformation service - skipping writing a map file");
        } else {
            final String cmdMap = config.getCommandsMapFile();
            if (StringUtils.isEmpty(cmdMap)) {
                logger.debug("No command map defined - ignoring");
                return;
            }

            final String filePath = ConfigConstants.getConfigFolder() + File.separator
                    + TransformationService.TRANSFORM_FOLDER_NAME + File.separator + cmdMap;
            final Path file = Paths.get(filePath);
            if (file.toFile().exists()) {
                logger.debug("Command map already defined - ignoring: {}", file);
                return;
            }

            try {
                final Set<String> cmds = new HashSet<String>();
                final List<String> lines = new ArrayList<String>();
                if (service.hasMethod(ScalarWebMethod.GETREMOTECONTROLLERINFO)) {
                    final RemoteControllerInfo rci = service.execute(ScalarWebMethod.GETREMOTECONTROLLERINFO)
                            .as(RemoteControllerInfo.class);

                    for (final RemoteCommand v : rci.getCommands()) {
                        // Note: encode value in case it's a URL type
                        final String name = v.getName();
                        final String value = v.getValue();
                        if (name != null && value != null && !cmds.contains(name)) {
                            cmds.add(name);
                            lines.add(name + "=" + URLEncoder.encode(value, "UTF-8"));
                        }
                    }
                } else {
                    logger.debug("No {} method found", ScalarWebMethod.GETREMOTECONTROLLERINFO);
                }

                // add any ircc extended commands
                final String irccUrl = config.getIrccUrl();
                if (irccUrl != null && StringUtils.isNotEmpty(irccUrl)) {
                    try {
                        final IrccClient irccClient = IrccClientFactory.get(irccUrl);
                        final IrccRemoteCommands remoteCmds = irccClient.getRemoteCommands();
                        for (final IrccRemoteCommand v : remoteCmds.getRemoteCommands().values()) {
                            // Note: encode value in case it's a URL type
                            final String name = v.getName();
                            if (!cmds.contains(name)) {
                                cmds.add(name);
                                lines.add(v.getName() + "=" + URLEncoder.encode(v.getCmd(), "UTF-8"));
                            }
                        }
                    } catch (IOException | URISyntaxException e) {
                        logger.debug("Exception creating IRCC client: {}", e.getMessage(), e);
                    }
                }
                Collections.sort(lines, String.CASE_INSENSITIVE_ORDER);

                if (!lines.isEmpty()) {
                    logger.debug("Writing remote commands to {}", file);
                    Files.write(file, lines, Charset.forName("UTF-8"));
                }
            } catch (final IOException e) {
                logger.debug("Remote commands are undefined: {}", e.getMessage());
            }
        }
    }
}
