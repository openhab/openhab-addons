/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.dial.models.DialApp;
import org.openhab.binding.sony.internal.dial.models.DialAppState;
import org.openhab.binding.sony.internal.dial.models.DialState;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.net.SocketSession;
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
class DialProtocol<T extends ThingCallback<String>> implements AutoCloseable {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(DialProtocol.class);

    /** The {@link DialConfig}. */
    private final DialConfig _config;

    /** The {@link IRCCHandlerCallback} that we can callback to set state and status. */
    private final T _callback;

    /** The http request. */
    private final HttpRequest _httpRequest;

    /** The dial state. */
    private DialState _dialState;

    /**
     * Constructs the protocol handler from given parameters. This constructor will create the
     * {@link #_session} to listen to notifications sent by the IRCC device (adding ourselfs as the
     * listener).
     *
     * @param config a non-null {@link SimpleIpConfig} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     * @throws Exception the exception
     */
    DialProtocol(DialConfig config, T callback) throws Exception {
        _config = config;
        _callback = callback;

        _httpRequest = NetUtilities.createHttpRequest();
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

        try {
            NetUtilities.sendWol(_config.getIpAddress(), _config.getDeviceMacAddress());
        } catch (Exception e) {
            // do nothing
        }

        _dialState = new DialState(_httpRequest, _config.getDialUri());

        // Add the specified deviceid header from the system info
        // final IrccSystemInformation sysInfo = _irccState.getSystemInformation();
        // final String deviceIdHeader = sysInfo == null || StringUtils.isEmpty(sysInfo.getActionHeader())
        // ? "CERS-DEVICE-ID" : sysInfo.getActionHeader();
        // _httpRequest.addHeader("X-" + deviceIdHeader, RQST_DEVICEID);
        //
        // final HttpResponse status = checkStatus();
        // if (status.getHttpCode() == HttpStatus.FORBIDDEN_403) {
        // final String accessCode = _config.getAccessCode();
        // if (DialConstants.ACCESSCODE_RQST.equalsIgnoreCase(accessCode)) {
        // final HttpResponse accessCodeResponse = requestAccessCode();
        // if (accessCodeResponse.getHttpCode() == HttpStatus.UNAUTHORIZED_401) {
        // return "Access Code requested. Please update the Access Code with what is shown on the device screen";
        // } else if (accessCodeResponse.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
        // return "Unable to request an access code - HOME menu not displayed on device. Please display the home menu
        // and try again.";
        // } else {
        // return "Access code request error: " + accessCodeResponse.getHttpCode() + " ("
        // + accessCodeResponse.getContent() + ")";
        // }
        //
        // }
        //
        // if (accessCode != null && accessCode.trim().length() != 0) {
        // try {
        // final int accessCodeNbr = Integer.parseInt(accessCode);
        // if (accessCodeNbr > 9999) {
        // return "Access code cannot be greater than 4 digits";
        // }
        // final HttpResponse registerResponse = registerAccessCode(accessCodeNbr);
        // if (registerResponse.getHttpCode() == HttpStatus.OK_200) {
        // // GOOD!
        // } else if (registerResponse.getHttpCode() == HttpStatus.UNAUTHORIZED_401) {
        // return "Access code was not accepted - please either request a new one or verify number matches what's shown
        // on the device";
        // } else {
        // return "Access code was not accepted: " + registerResponse.getHttpCode() + " ("
        // + registerResponse.getContent() + ")";
        // }
        //
        // } catch (NumberFormatException e) {
        // return "Access code is not " + DialConstants.ACCESSCODE_RQST + " or a number!";
        // }
        // }
        // } else {
        // final HttpResponse resp = registerRenewal();
        // if (resp.getHttpCode() != HttpStatus.OK_200 && resp.getHttpCode() != HttpStatus.SERVICE_UNAVAILABLE_503) {
        // return "Error registering renewal: " + resp.getContent();
        // }
        // }

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
        logger.debug("DIAL System now connected");
        _callback.statusChanged(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    /**
     * Gets the dial apps.
     *
     * @return the dial apps
     */
    public Map<String, DialApp> getDialApps() {
        return _dialState.getDialApps();
    }

    /**
     * Sets the state.
     *
     * @param appId the app id
     * @param start the start
     */
    public void setState(String appId, boolean start) {
        final URI urr = _dialState.getAppUri().resolve(appId);
        final HttpResponse resp = start ? _httpRequest.sendPostXmlCommand(urr.toString(), null)
                : _httpRequest.sendDeleteCommand(urr.toString(), null);
        if (resp.getHttpCode() != HttpStatus.SC_CREATED) {

        }

        refreshState();
    }

    /**
     * Refresh state.
     */
    public void refreshState() {
        for (String appId : _dialState.getDialApps().keySet()) {
            refreshState(appId);
        }
    }

    /**
     * Refresh state.
     *
     * @param appId the app id
     */
    public void refreshState(String appId) {
        try {
            final URI urr = _dialState.getAppUri().resolve(appId);
            final HttpResponse resp = _httpRequest.sendGetCommand(urr.toString());
            if (resp.getHttpCode() == HttpStatus.SC_OK) {
                final DialAppState state = new DialAppState(resp.getContentAsXml());
                _callback.stateChanged(DialConstants.CHANNEL_STATE + "-" + appId.replace('.', '-'),
                        state.isRunning() ? OnOffType.ON : OnOffType.OFF);
            } else {
                throw resp.createException();
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            // ignore - app may not actually be installed
            // logger.debug("Exception refreshing state: " + e.getMessage(), e);
        }
    }

    /**
     * Refresh icon.
     *
     * @param appId the app id
     */
    public void refreshIcon(String appId) {
        final DialApp app = _dialState.getDialApps().get(appId);
        final String url = app == null ? null : app.getIconUrl();

        byte[] iconData = null;
        if (StringUtils.isNotEmpty(url)) {
            final HttpResponse resp = _httpRequest.sendGetCommand(url);
            if (resp.getHttpCode() == HttpStatus.SC_OK) {
                iconData = resp.getContentAsBytes();
            }
        }

        if (iconData == null) {
            _callback.stateChanged(DialConstants.CHANNEL_ICON + "-" + appId.replace('.', '-'), UnDefType.NULL);
        } else {
            _callback.stateChanged(DialConstants.CHANNEL_ICON + "-" + appId.replace('.', '-'), new RawType(iconData));
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
