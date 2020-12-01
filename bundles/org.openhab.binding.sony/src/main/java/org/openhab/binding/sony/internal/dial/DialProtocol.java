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
package org.openhab.binding.sony.internal.dial;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sony.internal.AccessResult;
import org.openhab.binding.sony.internal.CheckResult;
import org.openhab.binding.sony.internal.LoginUnsuccessfulResponse;
import org.openhab.binding.sony.internal.SonyAuth;
import org.openhab.binding.sony.internal.SonyAuthChecker;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.dial.models.DialApp;
import org.openhab.binding.sony.internal.dial.models.DialAppState;
import org.openhab.binding.sony.internal.dial.models.DialClient;
import org.openhab.binding.sony.internal.dial.models.DialDeviceInfo;
import org.openhab.binding.sony.internal.dial.models.DialService;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.openhab.binding.sony.internal.transports.TransportOptionAutoAuth;
import org.openhab.binding.sony.internal.transports.TransportOptionHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the DIAL System. This handler will issue the protocol commands and will
 * process the responses from the DIAL system.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class DialProtocol<T extends ThingCallback<String>> implements AutoCloseable {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(DialProtocol.class);

    /** The DIAL device full address */
    private final String deviceUrlStr;

    /** The {@link ThingCallback} that we can callback to set state and status */
    private final T callback;

    /** The {@link SonyTransport} used to make http requests */
    private final SonyHttpTransport transport;

    /** The {@link DialClient} representing the DIAL application */
    private final DialClient dialClient;

    /** The configuration for the dial device */
    private final DialConfig config;

    /** The authorization service */
    private final SonyAuth sonyAuth;

    /**
     * Constructs the protocol handler from the configuration and callback
     *
     * @param config a non-null {@link DialConfig} (may be connected or disconnected)
     * @param callback a non-null {@link ThingCallback} to callback
     * @throws IOException if an ioexception is thrown
     * @throws URISyntaxException if a uri is malformed
     */
    DialProtocol(final DialConfig config, final T callback) throws IOException, URISyntaxException {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        // Confirm the address is a valid URL
        final String deviceAddress = config.getDeviceAddress();
        final URL deviceURL = new URL(deviceAddress);

        this.config = config;
        this.deviceUrlStr = deviceURL.toExternalForm();

        this.callback = callback;

        transport = SonyTransportFactory.createHttpTransport(deviceUrlStr);

        final DialClient dialClient = DialClientFactory.get(this.deviceUrlStr);
        if (dialClient == null) {
            throw new IOException("DialState could not be retrieved from " + deviceAddress);
        }
        this.dialClient = dialClient;

        this.sonyAuth = new SonyAuth(deviceURL);
    }

    /**
     * Attempts to log into the system. This method will attempt to get the current applications list. If the current
     * application list is forbidden, we attempt to register the device (either by registring the access code or
     * requesting an access code). If we get the current application list, we simply renew our registration code.
     *
     * @return a non-null {@link LoginUnsuccessfulResponse} if we can't login (usually pending access) or null if the
     *         login was successful
     *
     * @throws IOException if an io exception occurs to the IRCC device
     */
    @Nullable
    LoginUnsuccessfulResponse login() throws IOException {
        final String accessCode = config.getAccessCode();

        transport.setOption(TransportOptionAutoAuth.FALSE);
        final SonyAuthChecker authChecker = new SonyAuthChecker(transport, accessCode);

        final CheckResult checkResult = authChecker.checkResult(() -> {
            for (final DialDeviceInfo info : dialClient.getDeviceInfos()) {
                final String appsListUrl = info.getAppsListUrl();
                if (appsListUrl == null || StringUtils.isEmpty(appsListUrl)) {
                    return new AccessResult(Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR_500),
                            "No application list URL to check");
                }
                final HttpResponse resp = getApplicationList(appsListUrl);
                if (resp.getHttpCode() == HttpStatus.FORBIDDEN_403) {
                    return AccessResult.NEEDSPAIRING;
                }
            }
            return AccessResult.OK;
        });

        if (CheckResult.OK_HEADER.equals(checkResult)) {
            if (accessCode == null || StringUtils.isEmpty(accessCode)) {
                // This shouldn't happen - if our check result is OK_HEADER, then
                // we had a valid (non-null, non-empty) accessCode. Unfortunately
                // nullable checking thinks this can be null now.
                logger.debug("This shouldn't happen - access code is blank!: {}", accessCode);
                return new LoginUnsuccessfulResponse(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Access code cannot be blank");
            } else {
                SonyAuth.setupHeader(accessCode, transport);
            }
        } else if (CheckResult.OK_COOKIE.equals(checkResult)) {
            SonyAuth.setupCookie(transport);
        } else if (AccessResult.NEEDSPAIRING.equals(checkResult)) {
            if (StringUtils.isEmpty(accessCode)) {
                return new LoginUnsuccessfulResponse(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Access code cannot be blank");
            } else {
                final AccessResult result = sonyAuth.requestAccess(transport,
                        StringUtils.equalsIgnoreCase(DialConstants.ACCESSCODE_RQST, accessCode) ? null : accessCode);
                if (AccessResult.OK.equals(result)) {
                    SonyAuth.setupCookie(transport);
                } else {
                    return new LoginUnsuccessfulResponse(ThingStatusDetail.CONFIGURATION_ERROR, result.getMsg());
                }
            }
        } else {
            return new LoginUnsuccessfulResponse(ThingStatusDetail.CONFIGURATION_ERROR, checkResult.getMsg());
        }

        return null;
    }

    /**
     * Returns the callback used by this protocol
     *
     * @return the non-null callback used by this protocol
     */
    T getCallback() {
        return callback;
    }

    /**
     * Sets the 'state' channel for a specific application id. on to start the app, off to turn it off (off generally
     * isn't supported by SONY devices - but we try as per the protocol anyway)
     *
     * @param channelId the non-null, non-empty channel id
     * @param applId the non-null, non-empty application id
     * @param start true to start, false otherwise
     */
    public void setState(final String channelId, final String applId, final boolean start) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(applId, "applId cannot be empty");

        final URL urr = NetUtil.getUrl(dialClient.getAppUrl(), applId);
        if (urr == null) {
            logger.debug("Could not combine {} and {}", dialClient.getAppUrl(), applId);
        } else {
            final HttpResponse resp = start ? transport.executePostXml(urr.toString(), "")
                    : transport.executeDelete(urr.toString());
            if (resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
                logger.debug("Cannot start {}, another application is currently running.", applId);
            } else if (resp.getHttpCode() != HttpStatus.CREATED_201) {
                logger.debug("Error setting the 'state' of the application: {}", resp.getHttpCode());
            }
        }
    }

    /**
     * Refresh state of a specific DIAL application
     *
     * @param channelId the non-null non-empty channel ID
     * @param applId the non-null, non-empty application ID
     */
    public void refreshState(final String channelId, final String applId) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(applId, "applId cannot be empty");

        try {
            final URL urr = NetUtil.getUrl(dialClient.getAppUrl(), applId);
            if (urr == null) {
                logger.debug("Could not combine {} and {}", dialClient.getAppUrl(), applId);
            } else {
                final HttpResponse resp = transport.executeGet(urr.toExternalForm());
                if (resp.getHttpCode() != HttpStatus.OK_200) {
                    throw resp.createException();
                }

                final DialAppState state = DialAppState.get(resp.getContent());
                if (state != null) {
                    callback.stateChanged(channelId, state.isRunning() ? OnOffType.ON : OnOffType.OFF);
                }
            }
        } catch (final IOException e) {
            logger.debug("Error refreshing the 'state' of the application: {}", e.getMessage());
        }
    }

    /**
     * Refresh the name of the application
     *
     * @param channelId the non-null non-empty channel ID
     * @param applId the non-null application id
     */
    public void refreshName(final String channelId, final String applId) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(applId, "applId cannot be empty");

        final DialApp app = getDialApp(applId);
        callback.stateChanged(channelId, SonyUtil.newStringType(app == null ? null : app.getName()));
    }

    /**
     * Refresh the icon for the application
     *
     * @param channelId the non-null non-empty channel ID
     * @param applId the non-null application id
     */
    public void refreshIcon(final String channelId, final String applId) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(applId, "applId cannot be empty");

        final DialApp app = getDialApp(applId);
        final String url = app == null ? null : app.getIconUrl();

        final RawType rawType = NetUtil.getRawType(transport, url);
        callback.stateChanged(channelId, rawType == null ? UnDefType.UNDEF : rawType);
    }

    /**
     * Helper method to get the dial application for the given application id
     * 
     * @param appId a non-null, non-empty appication id
     * @return the DialApp for the applId or null if not found
     */
    private @Nullable DialApp getDialApp(final String appId) {
        Validate.notEmpty(appId, "appId cannot be empty");
        final Map<String, DialApp> apps = getDialApps();
        return apps.get(appId);
    }

    /**
     * Returns the list of dial apps on the sony device
     *
     * @return a non-null, maybe empty list of dial apps
     */
    public Map<String, DialApp> getDialApps() {
        final Map<String, DialApp> apps = new HashMap<>();
        for (final DialDeviceInfo info : dialClient.getDeviceInfos()) {
            final String appsListUrl = info.getAppsListUrl();
            if (appsListUrl != null && StringUtils.isNotBlank(appsListUrl)) {
                final HttpResponse appsResp = getApplicationList(appsListUrl);
                if (appsResp.getHttpCode() == HttpStatus.OK_200) {
                    final DialService service = DialService.get(appsResp.getContent());
                    if (service != null) {
                        service.getApps().forEach(a -> {
                            final String id = a.getId();
                            if (id != null) {
                                apps.putIfAbsent(id, a);
                            }
                        });
                    }
                } else {
                    logger.debug("Exception getting dial service from {}: {}", appsListUrl, appsResp);
                }
            }
        }
        return Collections.unmodifiableMap(apps);
    }

    /**
     * Gets the application list for a given url
     * 
     * @param appsListUrl a non-null, non-empty application list url
     * @return the http response for the call
     */
    private HttpResponse getApplicationList(final String appsListUrl) {
        Validate.notEmpty(appsListUrl, "appsListUrl cannot be empty");
        return transport.executeGet(appsListUrl,
                new TransportOptionHeader("Content-Type", "text/xml; charset=\"utf-8\""));
    }

    @Override
    public void close() {
        transport.close();
    }
}
