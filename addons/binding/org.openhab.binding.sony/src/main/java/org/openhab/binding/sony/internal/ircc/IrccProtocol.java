/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sony.internal.SonyUtility;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.ircc.models.IrccContentInformation;
import org.openhab.binding.sony.internal.ircc.models.IrccContentUrl;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommand;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommands;
import org.openhab.binding.sony.internal.ircc.models.IrccState;
import org.openhab.binding.sony.internal.ircc.models.IrccStatusList;
import org.openhab.binding.sony.internal.ircc.models.IrccStatusList.IrccStatus;
import org.openhab.binding.sony.internal.ircc.models.IrccSystemInformation;
import org.openhab.binding.sony.internal.ircc.models.IrccText;
import org.openhab.binding.sony.internal.net.Header;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.net.SocketSession;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConfig;
import org.openhab.binding.sony.internal.upnp.models.UpnpService;
import org.openhab.binding.sony.internal.upnp.models.UpnpServiceActionDescriptor;
import org.openhab.binding.sony.internal.upnp.models.UpnpServiceDescriptor;
import org.osgi.framework.BundleContext;
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
class IrccProtocol<T extends ThingCallback<String>> implements AutoCloseable {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(IrccProtocol.class);

    /** The {@link IrccConfig}. */
    private final IrccConfig _config;

    /** The {@link IRCCHandlerCallback} that we can callback to set state and status. */
    private final T _callback;

    /** The http request. */
    private final HttpRequest _httpRequest;

    /** The transform service. */
    private final TransformationService transformService;

    // private final String RQST_DEVICENAME;
    // private final String RQST_DEVICEID;

    /** The ircc state. */
    private IrccState _irccState;

    /** The is in text. */
    private AtomicBoolean isInText = new AtomicBoolean(false);

    /** The is in web browse. */
    private AtomicBoolean isInWebBrowse = new AtomicBoolean(false);

    /** The is viewing. */
    private AtomicBoolean isViewing = new AtomicBoolean(false);

    /** The content id. */
    private AtomicReference<String> contentId = new AtomicReference<String>(null);

    /**
     * The Enum Status.
     */
    public enum Status {

        /** The Pre login. */
        PreLogin,

        /** The Authenticating. */
        Authenticating,

        /** The Post login. */
        PostLogin
    }

    /** The protocol status. */
    private AtomicReference<Status> protocolStatus = new AtomicReference<Status>(Status.PreLogin);

    /**
     * Constructs the protocol handler from given parameters. This constructor will create the
     * {@link #_session} to listen to notifications sent by the IRCC device (adding ourselfs as the
     * listener).
     *
     * @param bundleContext the bundle context
     * @param config a non-null {@link SimpleIpConfig} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     * @throws Exception the exception
     */
    IrccProtocol(BundleContext bundleContext, IrccConfig config, T callback) throws Exception {
        _config = config;
        _callback = callback;

        transformService = TransformationHelper.getTransformationService(bundleContext, "MAP");

        // RQST_DEVICENAME = "OpenHAB (" + deviceId + ")";
        // RQST_DEVICEID = deviceId + ":" + _config.getLocalMacAddress();
        //
        // _httpRequest = new HttpRequest();
        // _httpRequest.addHeader("User-Agent", "OpenHab/Sony/Binding");
        // _httpRequest.addHeader("X-CERS-DEVICE-INFO", "OpenHab/Sony/Binding");
        // _httpRequest.addHeader("Connection", "close");
        //
        // final Integer accessCode = _config.getAccessCodeNbr();
        // if (accessCode != null) {
        // // pre-shared key compatibility
        // _httpRequest.addHeader("X-Auth-PSK", accessCode.toString());
        // }
        _httpRequest = NetUtilities.createHttpRequest(_config.getAccessCodeNbr());
    }

    /**
     * Gets the protocol status.
     *
     * @return the protocol status
     */
    Status getProtocolStatus() {
        return protocolStatus.get();
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
     * @throws IOException if an exception occurs trying to connect our {@link #_session}
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws NotImplementedException the not implemented exception
     * @throws URISyntaxException the URI syntax exception
     */
    String login() throws IOException, ParserConfigurationException, SAXException, NotImplementedException,
            URISyntaxException {

        NetUtilities.sendWol(_config.getIpAddress(), _config.getDeviceMacAddress());

        _irccState = new IrccState(_httpRequest, _config.getIrccUri());

        // Add the specified deviceid header from the system info
        final IrccSystemInformation sysInfo = _irccState.getSystemInformation();
        final String deviceIdHeader = sysInfo == null || StringUtils.isEmpty(sysInfo.getActionHeader())
                ? "CERS-DEVICE-ID"
                : sysInfo.getActionHeader();
        _httpRequest.addHeader("X-" + deviceIdHeader, _httpRequest.getDeviceId());

        final HttpResponse status = _irccState.getStatus();
        if (status.getHttpCode() == HttpStatus.SC_FORBIDDEN) {
            protocolStatus.set(Status.Authenticating);
            final String accessCode = _config.getAccessCode();
            if (IrccConstants.ACCESSCODE_RQST.equalsIgnoreCase(accessCode)) {
                final HttpResponse accessCodeResponse = registerAccessCode(null);
                if (accessCodeResponse.getHttpCode() == HttpStatus.SC_OK) {
                    // GOOD! AVs are only a single step authorization process
                } else if (accessCodeResponse.getHttpCode() == HttpStatus.SC_UNAUTHORIZED) {
                    return "Access Code requested. Please update the Access Code with what is shown on the device screen";
                } else if (accessCodeResponse.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                    return "Unable to request an access code - HOME menu not displayed on device. Please display the home menu and try again.";
                } else {
                    return "Access code request error: " + accessCodeResponse.getHttpCode() + " ("
                            + accessCodeResponse.getContent() + ")";
                }

            } else if (accessCode != null && accessCode.trim().length() != 0) {
                try {
                    final int accessCodeNbr = Integer.parseInt(accessCode);
                    if (accessCodeNbr > 9999) {
                        return "Access code cannot be greater than 4 digits";
                    }
                    final HttpResponse registerResponse = registerAccessCode(accessCodeNbr);
                    if (registerResponse.getHttpCode() == HttpStatus.SC_OK) {
                        // GOOD!
                    } else if (registerResponse.getHttpCode() == HttpStatus.SC_UNAUTHORIZED) {
                        return "Access code was not accepted - please either request a new one or verify number matches what's shown on the device";
                    } else {
                        return "Access code was not accepted: " + registerResponse.getHttpCode() + " ("
                                + registerResponse.getContent() + ")";
                    }

                } catch (NumberFormatException e) {
                    return "Access code is not " + IrccConstants.ACCESSCODE_RQST + " or a number!";
                }
            }
        } else {
            final HttpResponse resp = registerRenewal();
            if (resp.getHttpCode() != HttpStatus.SC_OK && resp.getHttpCode() != HttpStatus.SC_SERVICE_UNAVAILABLE) {
                return "Error registering renewal: " + resp.getContent();
            }
        }

        protocolStatus.set(Status.PostLogin);
        postLogin();
        return null;
    }

    /**
     * Post successful login stuff - mark us online!.
     *
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void postLogin() throws ParserConfigurationException, SAXException, IOException {
        _irccState.postAuthentication();

        writeCommands();

        logger.debug("IRCC System now connected");
        _callback.statusChanged(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        refreshVersion();
        refreshRegistrationMode();
    }

    /**
     * Register access code.
     *
     * @param accessCode the access code
     * @return the http response
     */
    private HttpResponse registerAccessCode(Integer accessCode) {
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
            final HttpResponse resp = sendGetCommand(registerUrl + rqst, headers);
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
            return sendGetCommand(registerUrl + rqst, headers);
        } catch (UnsupportedEncodingException e) {
            return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, e.toString());
        }
    }

    /**
     * Register renewal.
     *
     * @return the http response
     */
    private HttpResponse registerRenewal() {
        try {
            final String registerUrl = _irccState.getUrlForAction(IrccState.AN_REGISTER);
            if (registerUrl == null) {
                return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, "Register action is not supported");
            }

            final String parms = "?name=" + URLEncoder.encode(_httpRequest.getDeviceName(), "UTF-8")
                    + "&registrationType=renewal&deviceId=" + URLEncoder.encode(_httpRequest.getDeviceId(), "UTF-8");
            return sendGetCommand(registerUrl + parms);
        } catch (UnsupportedEncodingException e) {
            return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, e.toString());
        }
    }

    /**
     * Write commands.
     *
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeCommands() throws ParserConfigurationException, SAXException, IOException {
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

        final IrccRemoteCommands remoteCmds = _irccState.getRemoteCommands();
        if (remoteCmds == null) {
            logger.info("Remote commands are undefined");
        } else {
            final List<String> lines = new ArrayList<String>();
            for (IrccRemoteCommand v : remoteCmds.getRemoteCommands().values()) {
                // Note: encode value in case it's a URL type
                lines.add(v.getName() + "=" + v.getType() + ":" + URLEncoder.encode(v.getCmd(), "UTF-8"));
            }
            Collections.sort(lines, String.CASE_INSENSITIVE_ORDER);

            if (lines.size() > 0) {
                logger.info("Writing remote commands to {}", file);
                Files.write(file, lines, Charset.forName("UTF-8"));
            }
        }
    }

    /**
     * Send get command.
     *
     * @param uri the uri
     * @param headers the headers
     * @return the http response
     */
    private HttpResponse sendGetCommand(String uri, Header... headers) {
        return _httpRequest.sendGetCommand(uri, headers);
    }

    /**
     * Send post command.
     *
     * @param uri the uri
     * @param body the body
     * @param headers the headers
     * @return the http response
     */
    private HttpResponse sendPostCommand(String uri, String body, Header... headers) {
        return _httpRequest.sendPostXmlCommand(uri, body, headers);
    }

    /**
     * Refresh state.
     */
    public void refreshState() {
        refreshStatus();

        // logger.debug(sendGetCommand(_irccState.getUrlForAction(IrccState.AN_GETCONTENTINFORMATION)).toString());
        // logger.debug(sendGetCommand(_irccState.getUrlForAction(IrccState.AN_GETHISTORYLIST)).toString());

    }

    /**
     * Refresh status.
     */
    public void refreshStatus() {
        final String getStatusUrl = _irccState.getUrlForAction(IrccState.AN_GETSTATUS);
        if (StringUtils.isEmpty(getStatusUrl)) {
            logger.debug("{} is not implemented", IrccState.AN_GETSTATUS);
            return;
        }

        final HttpResponse resp = sendGetCommand(getStatusUrl);
        if (resp.getHttpCode() == HttpStatus.SC_OK) {
            _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_POWER),
                    OnOffType.ON);

            try {
                final IrccStatusList irccStatusList = new IrccStatusList(resp.getContentAsXml());

                if (irccStatusList.isTextInput()) {
                    if (!isInText.getAndSet(true)) {
                        refreshText();
                        refreshInText();
                    }
                } else {
                    if (isInText.getAndSet(false)) {
                        refreshText();
                        refreshInText();
                    }
                }

                if (irccStatusList.isWebBrowse()) {
                    if (!isInWebBrowse.getAndSet(true)) {
                        refreshInBrowser();
                    }
                    refreshContentUrl(); // always refresh in case they change urls
                } else {
                    if (isInWebBrowse.getAndSet(false)) {
                        refreshInBrowser();
                        refreshContentUrl();
                    }
                }

                final IrccStatus viewing = irccStatusList.getViewing();

                if (viewing == null) {
                    if (isViewing.getAndSet(false)) {
                        refreshIsViewing();
                    }
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_ID),
                            UnDefType.UNDEF);
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_SOURCE),
                            UnDefType.UNDEF);
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_SOURCE2),
                            UnDefType.UNDEF);
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_CLASS),
                            UnDefType.UNDEF);
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_TITLE),
                            UnDefType.UNDEF);
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_DURATION),
                            UnDefType.UNDEF);
                } else {
                    if (!isViewing.getAndSet(true)) {
                        refreshIsViewing();
                    }
                    final String id = viewing.getItemValue(IrccStatus.ID);
                    final String source = viewing.getItemValue(IrccStatus.SOURCE);
                    final String source2 = viewing.getItemValue(IrccStatus.SOURCE2);
                    final String clazz = viewing.getItemValue(IrccStatus.CLASS);
                    final String title = viewing.getItemValue(IrccStatus.TITLE);
                    final String dur = viewing.getItemValue(IrccStatus.DURATION);

                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_ID),
                            id == null ? UnDefType.NULL : new StringType(id));
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_SOURCE),
                            source == null ? UnDefType.NULL : new StringType(source));
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_SOURCE2),
                            source == null ? UnDefType.NULL : new StringType(source2));
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_CLASS),
                            clazz == null ? UnDefType.NULL : new StringType(clazz));
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_TITLE),
                            title == null ? UnDefType.NULL : new StringType(title));
                    if (StringUtils.isEmpty(dur)) {
                        _callback.stateChanged(
                                SonyUtility.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_DURATION),
                                UnDefType.NULL);
                    } else {
                        try {
                            _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_VIEWING,
                                    IrccConstants.CHANNEL_DURATION), new DecimalType(Integer.parseInt(dur)));
                        } catch (NumberFormatException e) {
                            logger.error("Could not convert {} into an integer", dur);
                        }
                    }

                    final String cId = contentId.get();
                    if (cId == null || !cId.equals(id)) {
                        refreshContentInformation();
                    }
                }

            } catch (ParserConfigurationException | SAXException | IOException e) {
                logger.warn("Exception occurred trying to {}: {}", IrccState.AN_GETTEXT, e);
            }
        } else if (resp.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_POWER),
                    OnOffType.OFF);
        } else {
            logger.warn("Unknown code from {}: {}", IrccState.AN_GETSTATUS, resp);
        }
    }

    /**
     * Refresh in text.
     */
    public void refreshInText() {
        _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_INTEXT),
                isInText.get() ? OnOffType.ON : OnOffType.OFF);

    }

    /**
     * Refresh in browser.
     */
    public void refreshInBrowser() {
        _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_INBROWSER),
                isInWebBrowse.get() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Refresh is viewing.
     */
    public void refreshIsViewing() {
        _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_ISVIEWING),
                isViewing.get() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Refresh version.
     */
    public void refreshVersion() {
        _callback.setProperty(IrccConstants.PROP_VERSION, _irccState.getUnrDeviceInformation().getVersion());
    }

    /**
     * Refresh registration mode.
     */
    public void refreshRegistrationMode() {
        _callback.setProperty(IrccConstants.PROP_REGISTRATIONMODE, Integer.toString(_irccState.getRegistrationMode()));
    }

    /**
     * Refresh text.
     */
    public void refreshText() {
        final String getTextUrl = _irccState.getUrlForAction(IrccState.AN_GETTEXT);
        if (StringUtils.isEmpty(getTextUrl)) {
            logger.debug("{} is not implemented", IrccState.AN_GETTEXT);
            return;
        }

        final HttpResponse resp = sendGetCommand(getTextUrl);
        if (resp.getHttpCode() == HttpStatus.SC_OK) {

            try {
                final IrccText irccText = new IrccText(resp.getContentAsXml());
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_TEXT),
                        new StringType(irccText.getText()));
            } catch (ParserConfigurationException | SAXException | IOException e) {
                logger.warn("Exception occurred trying to {}: {}", IrccState.AN_GETTEXT, e);
            }
        } else if (resp.getHttpCode() == HttpStatus.SC_NOT_ACCEPTABLE) {
            _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_TEXT),
                    UnDefType.UNDEF);
        } else if (resp.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_TEXT),
                    UnDefType.UNDEF);
        } else {
            logger.warn("Unknown code from {}: {}", IrccState.AN_GETTEXT, resp);
        }
    }

    /**
     * Refresh content url.
     */
    public void refreshContentUrl() {
        final String getContentUrl = _irccState.getUrlForAction(IrccState.AN_GETCONTENTURL);
        if (StringUtils.isEmpty(getContentUrl)) {
            logger.debug("{} is not implemented", IrccState.AN_GETCONTENTURL);
            return;
        }

        final HttpResponse resp = sendGetCommand(getContentUrl);
        if (resp.getHttpCode() == HttpStatus.SC_OK) {
            try {
                final IrccContentUrl irccContent = new IrccContentUrl(resp.getContentAsXml().getDocumentElement());

                final String url = irccContent.getUrl();
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_CONTENTURL),
                        url == null ? UnDefType.NULL : new StringType(url));

                if (irccContent.getContentInformation() == null) {
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                            UnDefType.UNDEF);
                } else {
                    final String urlTitle = irccContent.getContentInformation()
                            .getInfoItemValue(IrccContentInformation.TITLE);
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                            urlTitle == null ? UnDefType.NULL : new StringType(urlTitle));
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                logger.warn("Exception occurred trying to {}: {}", IrccState.AN_GETCONTENTURL, e);
            }
        } else if (resp.getHttpCode() == HttpStatus.SC_NOT_ACCEPTABLE) {
            _callback.stateChanged(
                    SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_CONTENTURL),
                    UnDefType.UNDEF);
            _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                    UnDefType.UNDEF);
        } else if (resp.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            _callback.stateChanged(
                    SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_CONTENTURL),
                    UnDefType.UNDEF);
            _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                    UnDefType.UNDEF);
        } else {
            logger.warn("Unknown code from {}: {}", IrccState.AN_GETCONTENTURL, resp);
        }
    }

    /**
     * Refresh content information.
     */
    public void refreshContentInformation() {
        final String getContentUrl = _irccState.getUrlForAction(IrccState.AN_GETCONTENTINFORMATION);
        if (StringUtils.isEmpty(getContentUrl)) {
            logger.debug("{} is not implemented", IrccState.AN_GETCONTENTINFORMATION);
            return;
        }

        final HttpResponse resp = sendGetCommand(getContentUrl);
        if (resp.getHttpCode() == HttpStatus.SC_OK) {
            try {
                final IrccContentInformation irccContent = new IrccContentInformation(
                        resp.getContentAsXml().getDocumentElement());

                final String id = irccContent.getInfoItemValue(IrccContentInformation.ID);
                final String title = irccContent.getInfoItemValue(IrccContentInformation.TITLE);
                final String clazz = irccContent.getInfoItemValue(IrccContentInformation.CLASS);
                final String source = irccContent.getInfoItemValue(IrccContentInformation.SOURCE);
                final String mediaType = irccContent.getInfoItemValue(IrccContentInformation.MEDIATYPE);
                final String mediaFormat = irccContent.getInfoItemValue(IrccContentInformation.MEDIAFORMAT);
                final String edition = irccContent.getInfoItemValue(IrccContentInformation.EDITION);
                final String description = irccContent.getInfoItemValue(IrccContentInformation.DESCRIPTION);
                final String genre = irccContent.getInfoItemValue(IrccContentInformation.GENRE);
                final String dur = irccContent.getInfoItemValue(IrccContentInformation.DURATION);
                final String rating = irccContent.getInfoItemValue(IrccContentInformation.RATING);
                final String daterelease = irccContent.getInfoItemValue(IrccContentInformation.DATERELEASE);
                final String director = irccContent.getInfoItemValue(IrccContentInformation.DIRECTOR);
                final String producer = irccContent.getInfoItemValue(IrccContentInformation.PRODUCER);
                final String screen = irccContent.getInfoItemValue(IrccContentInformation.SCREENWRITER);
                final String iconData = irccContent.getInfoItemValue(IrccContentInformation.ICONDATA);

                contentId.set(id);

                _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_ID),
                        id == null ? UnDefType.NULL : new StringType(id));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                        title == null ? UnDefType.NULL : new StringType(title));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_CLASS),
                        clazz == null ? UnDefType.NULL : new StringType(clazz));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_SOURCE),
                        source == null ? UnDefType.NULL : new StringType(source));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_MEDIATYPE),
                        mediaType == null ? UnDefType.NULL : new StringType(mediaType));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_MEDIAFORMAT),
                        mediaFormat == null ? UnDefType.NULL : new StringType(mediaFormat));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_EDITION),
                        edition == null ? UnDefType.NULL : new StringType(edition));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DESCRIPTION),
                        description == null ? UnDefType.NULL : new StringType(description));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_GENRE),
                        genre == null ? UnDefType.NULL : new StringType(genre));

                if (StringUtils.isEmpty(dur)) {
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DURATION),
                            UnDefType.NULL);
                } else {
                    try {
                        _callback.stateChanged(
                                SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DURATION),
                                new DecimalType(Integer.parseInt(dur)));
                    } catch (NumberFormatException e) {
                        logger.error("Could not convert {} into an integer", dur);
                    }
                }

                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_RATING),
                        rating == null ? UnDefType.NULL : new StringType(rating));

                if (StringUtils.isEmpty(daterelease)) {
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DATERELEASE),
                            UnDefType.NULL);
                } else {
                    try {
                        _callback.stateChanged(SonyUtility.createChannelId(IrccConstants.GRP_CONTENT,
                                IrccConstants.CHANNEL_DATERELEASE), new DateTimeType(daterelease));
                    } catch (IllegalArgumentException e) {
                        logger.error("Could not convert {} into an valid date", daterelease);

                    }
                }

                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DIRECTOR),
                        director == null ? UnDefType.NULL : new StringType(director));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_PRODUCER),
                        producer == null ? UnDefType.NULL : new StringType(producer));
                _callback.stateChanged(
                        SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_SCREENWRITER),
                        screen == null ? UnDefType.NULL : new StringType(screen));

                if (StringUtils.isEmpty(iconData)) {
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_ICONDATA),
                            UnDefType.NULL);
                } else {
                    final byte[] rawBytes = java.util.Base64.getDecoder().decode(iconData);
                    _callback.stateChanged(
                            SonyUtility.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_ICONDATA),
                            new RawType(rawBytes, RawType.DEFAULT_MIME_TYPE));
                }

            } catch (ParserConfigurationException | SAXException | IOException e) {
                logger.warn("Exception occurred trying to {}: {}", IrccState.AN_GETCONTENTINFORMATION, e);
            }
        } else if (resp.getHttpCode() == HttpStatus.SC_NOT_ACCEPTABLE) {
            _callback.stateChanged(
                    SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_CONTENTURL),
                    UnDefType.UNDEF);
        } else if (resp.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            _callback.stateChanged(
                    SonyUtility.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_CONTENTURL),
                    UnDefType.UNDEF);
        } else {
            logger.warn("Unknown code from {}: {}", IrccState.AN_GETCONTENTINFORMATION, resp);
        }
    }

    /**
     * Send power.
     *
     * @param turnOn the turn on
     */
    public void sendPower(boolean turnOn) {
        final IrccRemoteCommands cmds = _irccState.getRemoteCommands();
        if (cmds == null) {
            logger.debug("Remote commands are not supported!");
            return;
        }

        if (turnOn) {
            if (_config.isWOL()) {
                try {
                    NetUtilities.sendWol(_config.getIpAddress(), _config.getDeviceMacAddress());
                    logger.debug("WOL packet sent to {}", _config.getDeviceMacAddress());
                } catch (IOException e) {
                    logger.warn("Exception occurred sending WOL packet to {}: {}", _config.getDeviceMacAddress(), e);
                }
            } else {
                final IrccRemoteCommand powerOn = cmds.getPowerOn();
                if (powerOn == null) {
                    logger.info("WOL packet is not supported nor could we find a power on IRCC cmd");
                } else {
                    sendIrccCommand(powerOn.getCmd());
                }

            }
        } else {
            final IrccRemoteCommand cmd = cmds.getPowerOff();
            if (cmd == null) {
                logger.info("No power off (or power toggle) remote command was found");
            } else {
                sendIrccCommand(cmd.getCmd());
            }
        }
    }

    /**
     * Send command.
     *
     * @param cmd the cmd
     */
    public void sendCommand(String cmd) {

        if (StringUtils.isEmpty(cmd)) {
            return;
        }

        if (cmd.equalsIgnoreCase("test")) {
            doTest();
        }

        final String cmdMap = _config.getCommandsMapFile();
        if (transformService != null) {
            String code;
            try {
                code = transformService.transform(cmdMap, cmd);
                if (!StringUtils.isEmpty(code)) {
                    logger.debug("Transformed {} with map file '{}' to {}", cmd, cmdMap, code);
                    cmd = code;
                }

            } catch (TransformationException e) {
                logger.error("Failed to transform {} using map file '{}', exception={}", cmd, cmdMap, e.getMessage());
                return;
            }

        }

        String decodedCmd;
        try {
            decodedCmd = URLDecoder.decode(cmd, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            decodedCmd = cmd;
        }
        final int idx = decodedCmd.indexOf(':');

        String protocol = IrccRemoteCommands.IRCC;
        String cmdToSend = decodedCmd;
        if (idx >= 0) {
            protocol = decodedCmd.substring(0, idx);
            cmdToSend = decodedCmd.substring(idx + 1);
        }

        if (IrccRemoteCommands.IRCC.equalsIgnoreCase(protocol)) {
            sendIrccCommand(cmdToSend);
        } else if (IrccRemoteCommands.URL.equalsIgnoreCase(protocol)) {
            final HttpResponse resp = sendGetCommand(cmdToSend);
            if (resp.getHttpCode() == HttpStatus.SC_OK) {
                // yay!
            } else if (resp.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                logger.debug("URL service is unavailable (power off?)");
            } else {
                logger.error("Bad return code from {}: {}", IrccState.SRV_ACTION_SENDIRCC, resp);
            }
        } else {
            logger.warn("Unknown protocol found for the send command: {}", cmd);
        }
    }

    private void doTest() {
        String json = "{\"id\" : 2, \"method\" : \"getPowerStatus()\",  \"params\" : [ ], \"version\" : \"1.0\"}";
        String url = "http://192.168.1.32:10000/sony/system";
        HttpResponse resp = _httpRequest.sendPostJsonCommand(url, json);
        logger.debug(">>> resp: {}", resp);
    }

    /**
     * Send ircc command.
     *
     * @param cmdToSend the cmd to send
     */
    private void sendIrccCommand(String cmdToSend) {
        final UpnpService service = _irccState.getService(IrccState.SRV_IRCC);
        if (service == null) {
            logger.error("IRCC Service was not found");
            return;
        }

        final UpnpServiceDescriptor desc = service.getServiceDescriptor();
        if (desc == null) {
            logger.error("IRCC Service Descriptor was not found");
            return;
        }
        final UpnpServiceActionDescriptor actionDescriptor = desc.getActionDescriptor(IrccState.SRV_ACTION_SENDIRCC);
        if (actionDescriptor == null) {
            logger.error("IRCC Service Action Descriptior was not found");
            return;
        }

        final String soap = actionDescriptor.getSoap(cmdToSend);
        final HttpResponse resp = sendPostCommand(service.getControlUrl(), soap,
                new Header("SOAPACTION", "\"" + service.getServiceType() + "#" + IrccState.SRV_ACTION_SENDIRCC + "\""));
        if (resp.getHttpCode() == HttpStatus.SC_OK) {
            // yay!
        } else if (resp.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            logger.debug("IRCC service is unavailable (power off?)");
        } else {
            logger.error("Bad return code from {}: {}", IrccState.SRV_ACTION_SENDIRCC, resp);
        }
    }

    /**
     * Send content url.
     *
     * @param contentUrl the content url
     */
    public void sendContentUrl(String contentUrl) {
        final String sendContentUrl = _irccState.getUrlForAction(IrccState.AN_SENDCONTENTURL);
        if (StringUtils.isEmpty(sendContentUrl)) {
            logger.warn("{} action was not implmented", IrccState.AN_SENDCONTENTURL);
            return;
        }
        final String body = "<contentUrl><url>" + contentUrl + "</url></contentUrl>";
        final HttpResponse resp = sendPostCommand(sendContentUrl, body);
        if (resp.getHttpCode() == HttpStatus.SC_OK) {
            // Do nothing
        } else if (resp.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
            logger.debug("IRCC service is unavailable (power off?)");
        } else {
            logger.warn("Bad return code from {}: {}", IrccState.AN_SENDCONTENTURL, resp);
        }
    }

    /**
     * Send text.
     *
     * @param string the string
     */
    public void sendText(String string) {
        final String sendTextUrl = _irccState.getUrlForAction(IrccState.AN_SENDTEXT);
        if (StringUtils.isEmpty(sendTextUrl)) {
            logger.warn("{} action was not implmented", IrccState.AN_SENDTEXT);
            return;
        }

        try {
            final String text = "?text=" + URLEncoder.encode(string, "UTF-8");
            final HttpResponse resp = sendGetCommand(sendTextUrl + text);
            if (resp.getHttpCode() == HttpStatus.SC_OK) {
                // yeah!
            } else if (resp.getHttpCode() == HttpStatus.SC_NOT_ACCEPTABLE) {
                logger.debug("{} was sent but 'not acceptable' was returned (ie no input field to accept text)",
                        IrccState.AN_SENDTEXT);
            } else if (resp.getHttpCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                logger.debug("IRCC service is unavailable (power off?)");
            } else {
                logger.warn("Unknown code for {}:L {}", IrccState.AN_SENDTEXT, resp);
            }
        } catch (UnsupportedEncodingException e) {
            logger.warn("UTF-8 is not supported on this platform: {}", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        _httpRequest.close();
    }
}
