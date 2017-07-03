/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommand;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommands;
import org.openhab.binding.sony.internal.ircc.models.IrccState;
import org.openhab.binding.sony.internal.net.Header;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.net.SocketSession;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConfig;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConstants;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.models.api.InterfaceInformation;
import org.openhab.binding.sony.internal.scalarweb.models.api.NetIf;
import org.openhab.binding.sony.internal.scalarweb.models.api.NetworkSetting;
import org.openhab.binding.sony.internal.scalarweb.models.api.RemoteControllerInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.RemoteControllerInfo.RemoteCommand;
import org.openhab.binding.sony.internal.scalarweb.models.api.SystemInformation;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * This is the protocol handler for the IRCC System. This handler will issue the protocol commands and will
 * process the responses from the IRCC system. The IRCC system is a little flacky and doesn't seem to handle
 * multiple commands in a single session. For this reason, we create a single {@link SocketSession} to listen for any
 * notifications (whose lifetime matches that of this handler) and then create separate {@link SocketSession} for each
 * request. Special care must be taken to differentiate between a Control request result and the Enquiry/Notification
 * results to avoid misinterpreting the result (the control "success" message will have all zeroes - which has a form
 * that matches some enquery/notification results (like volume could be interpreted as 0!).
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
public class ScalarWebLoginProtocol<T extends ThingCallback<ScalarWebChannel>> {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(ScalarWebLoginProtocol.class);

    /** The {@link ScalarWebConfig}. */
    private final ScalarWebConfig _config;

    /** The {@link IRCCHandlerCallback} that we can callback to set state and status. */
    private final T _callback;

    /** The scalar state. */
    private final ScalarWebState _scalarState;

    /**
     * Constructs the protocol handler from given parameters. This constructor will create the
     * {@link #_session} to listen to notifications sent by the IRCC device (adding ourselfs as the
     * listener).
     *
     * @param scalarState the scalar state
     * @param config a non-null {@link SimpleIpConfig} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     */
    public ScalarWebLoginProtocol(ScalarWebState scalarState, ScalarWebConfig config, T callback) {
        Objects.requireNonNull(scalarState, "scalarState cannot be null");
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        _scalarState = scalarState;
        _config = config;
        _callback = callback;

    }

    /**
     * Gets the callback.
     *
     * @return the callback
     */
    T getCallback() {
        return _callback;
    }

    /**
     * Attempts to log into the system. The login will connect the {@link #_session} and immediately call
     * {@link #postLogin()} since there is no authentication mechanisms
     *
     * @return always null to indicate a successful login
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     */
    public String login() throws IOException, ParserConfigurationException, SAXException {

        NetUtilities.sendWol(_config.getIpAddress(), _config.getDeviceMacAddress());

        final ScalarWebService systemWebService = _scalarState.getService(ScalarWebService.System);
        if (systemWebService == null) {
            return "Device doesn't implement the system web service and is required";
        }

        final HttpResponse status = checkDeviceMode();

        switch (status.getHttpCode()) {
            case HttpStatus.SC_OK:
                postLogin();
                return null;

            case 12:
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_FORBIDDEN:
                String accessCode = _config.getAccessCode();
                if (StringUtils.equalsIgnoreCase(ScalarWebConstants.ACCESSCODE_RQST, accessCode)) {
                    final HttpResponse accessCodeResponse = requestAccess(null);
                    if (accessCodeResponse.getHttpCode() == HttpStatus.SC_OK) {
                        // already registered!
                        accessCode = null;
                    } else if (accessCodeResponse.getHttpCode() == HttpStatus.SC_UNAUTHORIZED) {
                        return "Access Code requested. Please update the Access Code with what is shown on the device screen";
                    } else if (accessCodeResponse.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                        return "Unable to request an access code - HOME menu not displayed on device. Please display the home menu and try again.";
                    } else if (accessCodeResponse.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                        return "Unable to request an access code - HOME menu not displayed on device. Please display the home menu and try again.";
                    } else if (accessCodeResponse.getHttpCode() == ScalarWebResult.DisplayIsOff) {
                        return "Unable to request an access code - Display is turned off (must be on to see code).";
                    } else {
                        return "Access code request error: " + accessCodeResponse + ")";
                    }

                }

                if (accessCode != null && accessCode.trim().length() != 0) {
                    try {
                        final int accessCodeNbr = Integer.parseInt(accessCode);
                        if (accessCodeNbr > 9999) {
                            return "Access code cannot be greater than 4 digits";
                        }
                        final HttpResponse registerResponse = requestAccess(accessCodeNbr);
                        if (registerResponse.getHttpCode() == HttpStatus.SC_OK) {
                            // GOOD!
                        } else if (registerResponse.getHttpCode() == HttpStatus.SC_UNAUTHORIZED) {
                            return "Access code was not accepted - please either request a new one or verify number matches what's shown          on the device";
                        } else {
                            return "Access code was not accepted: " + registerResponse.getHttpCode() + " ("
                                    + registerResponse.getContent() + ")";
                        }

                    } catch (NumberFormatException e) {
                        return "Access code is not " + ScalarWebConstants.ACCESSCODE_RQST + " or a number!";
                    }
                }

                postLogin();
                return null;
            case ScalarWebResult.DisplayIsOff:
                return "Display must be on to start pairing process - please turn on to start pairing process";

            default:
                return status.toString();
        }
    }

    /**
     * Post successful login stuff - mark us online!.
     *
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void postLogin() throws ParserConfigurationException, SAXException, IOException {
        logger.debug("WebScalar System now connected");

        _scalarState.postAuthentication();

        final ScalarWebService sysService = _scalarState.getService(ScalarWebService.System);
        Objects.requireNonNull(sysService, "sysService is null - shouldn't happen since it's checked in login()");

        final SystemInformation sysInfo = sysService.execute(ScalarWebMethod.GetSystemInformation)
                .as(SystemInformation.class);
        if (sysInfo == null) {
            logger.debug("System information service unavailable");
        } else {
            _callback.setProperty(ScalarWebConstants.PROP_PRODUCT, sysInfo.getProduct());
            _callback.setProperty(ScalarWebConstants.PROP_NAME, sysInfo.getName());
            _callback.setProperty(ScalarWebConstants.PROP_MODEL, sysInfo.getModel());
            _callback.setProperty(ScalarWebConstants.PROP_GENERATION, sysInfo.getGeneration());
            _callback.setProperty(ScalarWebConstants.PROP_SERIAL, sysInfo.getSerial());
            _callback.setProperty(ScalarWebConstants.PROP_MACADDR, sysInfo.getMacAddr());
            _callback.setProperty(ScalarWebConstants.PROP_AREA, sysInfo.getArea());
            // _callback.setProperty("", sysInfo.getLanguage()); // has a set langauge
            _callback.setProperty(ScalarWebConstants.PROP_REGION, sysInfo.getRegion());
        }

        final InterfaceInformation intInfo = sysService.execute(ScalarWebMethod.GetInterfaceInformation)
                .as(InterfaceInformation.class);
        if (intInfo == null) {
            logger.debug("Interface information service is unavailable");
        } else {
            _callback.setProperty(ScalarWebConstants.PROP_INTERFACEVERSION, intInfo.getInterfaceVersion());
            _callback.setProperty(ScalarWebConstants.PROP_PRODUCTCATEGORY, intInfo.getProductCategory());
            _callback.setProperty(ScalarWebConstants.PROP_SERVERNAME, intInfo.getServerName());
        }

        // final ScalarWebService excService = _scalarState.getService(ScalarWebService.Encryption);
        // if (excService == null) {
        // logger.debug("Encryption service is unavailable");
        // } else {
        // publicKey.set(excService.execute(ScalarWebMethod.GetPublicKey).as(String.class));
        // }

        for (String netIf : new String[] { "eth0", "wlan0", "eth1", "wlan1" }) {
            final ScalarWebResult swr = sysService.execute(ScalarWebMethod.GetNetworkSettings, new NetIf(netIf));
            if (!swr.isError() && swr.hasResults()) {
                final NetworkSetting netSetting = swr.as(NetworkSetting.class);
                _callback.setProperty(ScalarWebConstants.PROP_NETIF, netSetting.getNetif());
                _callback.setProperty(ScalarWebConstants.PROP_HWADDRESS, netSetting.getHwAddr());
                _callback.setProperty(ScalarWebConstants.PROP_IPV4, netSetting.getIpAddrV4());
                _callback.setProperty(ScalarWebConstants.PROP_IPV6, netSetting.getIpAddrV6());
                _callback.setProperty(ScalarWebConstants.PROP_NETMASK, netSetting.getNetmask());
                _callback.setProperty(ScalarWebConstants.PROP_GATEWAY, netSetting.getGateway());
                break;
            }
        }

        writeCommands(sysService);

        // final ScalarWebService excService = _scalarState.getService(ScalarWebService.AppControl);
        // if (excService == null) {
        // } else {
        // logger.debug(">> applist: {}", excService.execute(ScalarWebMethod.GetApplicationList));
        // logger.debug(">> appstatus: {}", excService.execute(ScalarWebMethod.GetApplicationStatusList));
        // logger.debug(">> webapp: {}", excService.execute(ScalarWebMethod.Get));
        // logger.debug(">> led: {}", excService.execute(ScalarWebMethod.GetLedIndicatorStatus));
        // logger.debug(">> device: {}", excService.execute(ScalarWebMethod.GetDeviceMode));
        // logger.debug(">> currenttime: {}", excService.execute(ScalarWebMethod.GetCurrentTime));
        //
        // }

    }

    /**
     * Check device mode.
     *
     * @return the http response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private HttpResponse checkDeviceMode() throws IOException {
        final ScalarWebService systemService = _scalarState.getService(ScalarWebService.System);
        if (systemService == null) {
            return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, "Device doesn't implement the system service");
        }

        return systemService.execute(ScalarWebMethod.GetDeviceMode).getHttpResponse();
    }

    /**
     * Request access.
     *
     * @param accessCode the access code
     * @return the http response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private HttpResponse requestAccess(Integer accessCode) throws IOException {
        final ScalarWebService accessControlService = _scalarState.getService(ScalarWebService.AccessControl);
        if (accessControlService == null) {
            return registerAccessCode(null);
            // return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE,
            // "Device doesn't implement the access control service");
        }

        return accessControlService.actRegister(accessCode).getHttpResponse();
    }

    /**
     * Register access code.
     *
     * @param accessCode the access code
     * @return the http response
     */
    private HttpResponse registerAccessCode(Integer accessCode) {
        try (HttpRequest _httpRequest = NetUtilities.createHttpRequest()) {
            IrccState _irccState = _scalarState.getIrccState();
            final String registerUrl = _irccState.getUrlForAction(IrccState.AN_REGISTER);
            if (registerUrl == null) {
                return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, "Register action is not supported");
            }

            // Do the registration first with what the mode says,
            // then try it again with the other mode (so registration mode sometimes lie)
            final String[] registrationTypes = new String[2];
            if (_irccState.getRegistrationMode() == 2) {
                registrationTypes[0] = "new";
                registrationTypes[1] = "initial";
            } else {
                registrationTypes[0] = "initial";
                registrationTypes[1] = "new";
            }

            final Header[] headers = accessCode == null ? new Header[0]
                    : new Header[] { NetUtilities.createAuthHeader(accessCode) };
            try {
                final String rqst = "?name=" + URLEncoder.encode(_httpRequest.getDeviceName(), "UTF-8")
                        + "&registrationType=" + registrationTypes[0] + "&deviceId="
                        + URLEncoder.encode(_httpRequest.getDeviceId(), "UTF-8");
                final HttpResponse resp = _httpRequest.sendGetCommand(registerUrl + rqst, headers);
                if (resp.getHttpCode() != HttpStatus.SC_BAD_REQUEST) {
                    return resp;
                }
            } catch (UnsupportedEncodingException e) {
                // do nothing for now
            }

            try {
                final String rqst = "?name=" + URLEncoder.encode(_httpRequest.getDeviceName(), "UTF-8")
                        + "&registrationType=" + registrationTypes[1] + "&deviceId="
                        + URLEncoder.encode(_httpRequest.getDeviceId(), "UTF-8");
                return _httpRequest.sendGetCommand(registerUrl + rqst, headers);
            } catch (UnsupportedEncodingException e) {
                return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, e.toString());
            }
        } catch (Exception e1) {
            return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, e1.toString());
        }
    }

    /**
     * Write commands.
     *
     * @param sysInfo the sys info
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeCommands(ScalarWebService sysInfo)
            throws ParserConfigurationException, SAXException, IOException {
        final String cmdMap = _config.getCommandsMapFile();
        if (StringUtils.isEmpty(cmdMap)) {
            logger.debug("No command map defined - ignoring");
            return;
        }

        final String filePath = ConfigConstants.getConfigFolder() + File.separator
                + TransformationService.TRANSFORM_FOLDER_NAME + File.separator + cmdMap;
        Path file = Paths.get(filePath);
        if (file.toFile().exists()) {
            logger.info("Command map already defined - ignoring: {}", file);
            return;
        }

        try {
            final RemoteControllerInfo rci = sysInfo.execute(ScalarWebMethod.GetRemoteControllerInfo)
                    .as(RemoteControllerInfo.class);
            if (rci == null) {
                logger.debug("Remote controller information service is unavailable");
            } else {

                final Set<String> cmds = new HashSet<String>();

                final List<String> lines = new ArrayList<String>();
                for (RemoteCommand v : rci.getCommands()) {
                    // Note: encode value in case it's a URL type
                    final String name = v.getName();
                    if (!cmds.contains(name)) {
                        cmds.add(name);
                        lines.add(v.getName() + "=" + URLEncoder.encode(v.getValue(), "UTF-8"));
                    }
                }

                // add any ircc extended commands
                final IrccState irccState = _scalarState.getIrccState();
                if (irccState != null) {

                    final IrccRemoteCommands remoteCmds = irccState.getRemoteCommands();
                    if (remoteCmds != null) {
                        for (IrccRemoteCommand v : remoteCmds.getRemoteCommands().values()) {
                            // Note: encode value in case it's a URL type
                            final String name = v.getName();
                            if (!cmds.contains(name)) {
                                cmds.add(name);
                                lines.add(v.getName() + "=" + URLEncoder.encode(v.getCmd(), "UTF-8"));
                            }
                        }
                    }
                }

                Collections.sort(lines, String.CASE_INSENSITIVE_ORDER);

                if (lines.size() > 0) {
                    logger.info("Writing remote commands to {}", file);
                    Files.write(file, lines, Charset.forName("UTF-8"));
                }
            }
        } catch (IOException e) {
            logger.info("Remote commands are undefined: {}", e.getMessage(), e);
        }
    }
}
