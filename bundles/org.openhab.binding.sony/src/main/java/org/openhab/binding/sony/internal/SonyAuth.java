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
package org.openhab.binding.sony.internal;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sony.internal.ircc.models.IrccClient;
import org.openhab.binding.sony.internal.ircc.models.IrccSystemInformation;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.scalarweb.gson.GsonUtilities;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebError;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebRequest;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActRegisterId;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActRegisterOptions;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.TransportOption;
import org.openhab.binding.sony.internal.transports.TransportOptionAutoAuth;
import org.openhab.binding.sony.internal.transports.TransportOptionHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This class contains all the logic to authorized against a sony device (either Scalar or IRCC)
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyAuth {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(SonyAuth.class);

    /** The GSON to use */
    private final Gson gson = GsonUtilities.getApiGson();

    /** The callback to get an IRCC client instance */
    private final @Nullable IrccClientProvider irccClientProvider;

    /** The activation URL */
    private final String activationUrl;

    /** The activation URL version */
    private final String activationVersion;

    /**
     * Constructs the authentication from a SCALAR URL
     * 
     * @param url a non-null URL
     */
    public SonyAuth(final URL url) {
        Objects.requireNonNull(url, "url cannot be null");

        activationUrl = NetUtil.getSonyUrl(url, ScalarWebService.ACCESSCONTROL);
        activationVersion = ScalarWebMethod.V1_0;
        irccClientProvider = null;
    }

    /**
     * Constructs the authentication with a callback for a IRCC client
     * 
     * @param irccClientProvider a non-null callback
     */
    public SonyAuth(final IrccClientProvider irccClientProvider) {
        this(irccClientProvider, null);
    }

    /**
     * Constructs the authentication witha callback for a IRCC client and a scalar access control service
     * 
     * @param getIrccClient a non-null IRCC client
     * @param accessControlService a possibly null access control service
     */
    public SonyAuth(final IrccClientProvider getIrccClient, final @Nullable ScalarWebService accessControlService) {
        Objects.requireNonNull(getIrccClient, "getIrccClient cannot be null");
        this.irccClientProvider = getIrccClient;

        String actUrl = null, actVersion = null;

        if (accessControlService != null) {
            actUrl = accessControlService == null ? null : accessControlService.getTransport().getBaseUri().toString();
            actVersion = accessControlService == null ? null
                    : accessControlService.getVersion(ScalarWebMethod.ACTREGISTER);
        }

        this.activationUrl = actUrl;
        this.activationVersion = StringUtils.defaultIfEmpty(actVersion, ScalarWebMethod.V1_0);
    }

    /**
     * Helper method to get the device id header name (X-CERS-DEVICE-ID generally)
     * 
     * @return a non-null device id header name
     */
    private String getDeviceIdHeaderName() {
        final IrccClient irccClient = irccClientProvider == null ? null : irccClientProvider.getClient();
        final IrccSystemInformation sysInfo = irccClient == null ? null : irccClient.getSystemInformation();
        final String actionHeader = sysInfo == null ? null : sysInfo.getActionHeader();
        return "X-" + StringUtils.defaultIfEmpty(actionHeader, "CERS-DEVICE-ID");
    }

    /**
     * Helper method to get the IRCC registration mode (or null if none)
     * 
     * @return a integer specifying the registration mode or null if none
     */
    private @Nullable Integer getRegistrationMode() {
        final IrccClient irccClient = irccClientProvider == null ? null : irccClientProvider.getClient();
        return irccClient == null ? null : irccClient.getRegistrationMode();
    }

    /**
     * Helper method to get the IRCC registration URL (or null if none)
     * 
     * @return a non-empty URL if found, null if not
     */
    private @Nullable String getRegistrationUrl() {
        final IrccClient irccClient = irccClientProvider == null ? null : irccClientProvider.getClient();
        return irccClient == null ? null
                : StringUtils.defaultIfEmpty(irccClient.getUrlForAction(IrccClient.AN_REGISTER), null);
    }

    /**
     * Helper method to get the IRCC activation URL (or null if none)
     * 
     * @return a non-empty URL if found, null if not
     */
    private @Nullable String getActivationUrl() {
        if (activationUrl != null && StringUtils.isNotEmpty(activationUrl)) {
            return activationUrl;
        }

        final IrccClient irccClient = irccClientProvider == null ? null : irccClientProvider.getClient();
        return irccClient == null ? null : NetUtil.getSonyUrl(irccClient.getBaseUrl(), ScalarWebService.ACCESSCONTROL);
    }

    /**
     * Request access by initiating the registration or doing the activation if on the second step
     *
     * @param transport a non-null transport to use
     * @param accessCode the access code (null for initial setup)
     * @return the http response
     */
    public AccessResult requestAccess(final SonyHttpTransport transport, final @Nullable String accessCode) {
        Objects.requireNonNull(transport, "transport cannot be null");

        logger.debug("Requesting access: {}", StringUtils.defaultIfEmpty(accessCode, "(initial)"));

        if (accessCode != null) {
            transport.setOption(new TransportOptionHeader(NetUtil.createAccessCodeHeader(accessCode)));
        }
        transport.setOption(new TransportOptionHeader(getDeviceIdHeaderName(), NetUtil.getDeviceId()));

        final ScalarWebResult result = scalarActRegister(transport, accessCode);
        final HttpResponse httpResponse = result.getHttpResponse();

        final String registrationUrl = getRegistrationUrl();
        if (httpResponse.getHttpCode() == HttpStatus.UNAUTHORIZED_401) {
            if (registrationUrl == null || StringUtils.isEmpty(registrationUrl)) {
                return accessCode == null ? AccessResult.PENDING : AccessResult.NOTACCEPTED;
            }
        }

        if (result.getDeviceErrorCode() == ScalarWebError.NOTIMPLEMENTED
                || (result.getDeviceErrorCode() == ScalarWebError.HTTPERROR
                        && httpResponse.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503)
                || httpResponse.getHttpCode() == HttpStatus.UNAUTHORIZED_401
                || httpResponse.getHttpCode() == HttpStatus.FORBIDDEN_403) {
            if (registrationUrl != null && StringUtils.isNotEmpty(registrationUrl)) {
                final HttpResponse irccResponse = irccRegister(transport, accessCode);
                if (irccResponse.getHttpCode() == HttpStatus.OK_200) {
                    return AccessResult.OK;
                } else if (irccResponse.getHttpCode() == HttpStatus.UNAUTHORIZED_401) {
                    return AccessResult.PENDING;
                } else {
                    return new AccessResult(irccResponse);
                }
            }
        }

        if (result.getDeviceErrorCode() == ScalarWebError.DISPLAYISOFF) {
            return AccessResult.DISPLAYOFF;
        }

        if (httpResponse.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
            return AccessResult.HOMEMENU;
        }

        if (httpResponse.getHttpCode() == HttpStatus.OK_200
                || result.getDeviceErrorCode() == ScalarWebError.ILLEGALARGUMENT) {
            return AccessResult.OK;
        }

        return new AccessResult(httpResponse);
    }

    /**
     * Register an access renewal
     *
     * @param transport a non-null transport to use
     * @return the non-null {@link HttpResponse}
     */
    public AccessResult registerRenewal(final SonyHttpTransport transport) {
        Objects.requireNonNull(transport, "transport cannot be null");

        logger.debug("Registering Renewal");

        transport.setOption(new TransportOptionHeader(getDeviceIdHeaderName(), NetUtil.getDeviceId()));

        final ScalarWebResult response = scalarActRegister(transport, null);

        // if good response, return it
        if (response.getHttpResponse().getHttpCode() == HttpStatus.OK_200) {
            return AccessResult.OK;
        }

        // If we got a 401 (unauthorized) and there is no ircc registration url
        // return it as well
        final String registrationUrl = getRegistrationUrl();
        if (response.getHttpResponse().getHttpCode() == HttpStatus.UNAUTHORIZED_401
                && (registrationUrl == null || StringUtils.isEmpty(registrationUrl))) {
            return AccessResult.NEEDSPAIRING;
        }

        final HttpResponse irccResponse = irccRenewal(transport);
        if (irccResponse.getHttpCode() == HttpStatus.OK_200) {
            return AccessResult.OK;
        } else {
            return new AccessResult(irccResponse);
        }
    }

    /**
     * Register the specified access code
     *
     * @param transport a non-null transport to use
     * @param accessCode the possibly null access code
     * @return the non-null {@link HttpResponse}
     */
    private HttpResponse irccRegister(final SonyHttpTransport transport, final @Nullable String accessCode) {
        Objects.requireNonNull(transport, "transport cannot be null");

        final String registrationUrl = getRegistrationUrl();
        if (registrationUrl == null || StringUtils.isEmpty(registrationUrl)) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, "No registration URL");
        }

        // Do the registration first with what the mode says,
        // then try it again with the other mode (so registration mode sometimes lie)
        final String[] registrationTypes = new String[3];
        if (getRegistrationMode() == 2) {
            registrationTypes[0] = "new";
            registrationTypes[1] = "initial";
            registrationTypes[2] = "renewal";
        } else {
            registrationTypes[0] = "initial";
            registrationTypes[1] = "new";
            registrationTypes[2] = "renewal";
        }

        final TransportOption[] headers = accessCode == null ? new TransportOption[0]
                : new TransportOption[] { new TransportOptionHeader(NetUtil.createAuthHeader(accessCode)) };

        HttpResponse resp = new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, "unavailable");
        for (String rType : registrationTypes) {
            try {
                final String rqst = "?name=" + URLEncoder.encode(NetUtil.getDeviceName(), "UTF-8")
                        + "&registrationType=" + rType + "&deviceId="
                        + URLEncoder.encode(NetUtil.getDeviceId(), "UTF-8");
                resp = transport.executeGet(registrationUrl + rqst, headers);
                if (resp.getHttpCode() == HttpStatus.OK_200 || resp.getHttpCode() == HttpStatus.UNAUTHORIZED_401) {
                    return resp;
                }
            } catch (final UnsupportedEncodingException e) {
                resp = new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.toString());
            }
        }

        return resp;
    }

    /**
     * Helper method to initiate an IRCC renewal
     * 
     * @param transport a non-null transport to use
     * @return the non-null HttpResponse of the renewal
     */
    private HttpResponse irccRenewal(final SonyHttpTransport transport) {
        Objects.requireNonNull(transport, "transport cannot be null");

        final String registrationUrl = getRegistrationUrl();
        if (registrationUrl == null || StringUtils.isEmpty(registrationUrl)) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, "No registration URL");
        }

        try {
            final String parms = "?name=" + URLEncoder.encode(NetUtil.getDeviceName(), "UTF-8")
                    + "&registrationType=renewal&deviceId=" + URLEncoder.encode(NetUtil.getDeviceId(), "UTF-8");
            return transport.executeGet(registrationUrl + parms);
        } catch (final UnsupportedEncodingException e) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.toString());
        }
    }

    /**
     * Helper method to execute an ActRegister to register the system
     *
     * @param transport a non-null transport to use
     * @param accessCode the access code to use (or null to initiate the first step of ActRegister)
     * @return the scalar web result
     */
    private ScalarWebResult scalarActRegister(final SonyHttpTransport transport, final @Nullable String accessCode) {
        Objects.requireNonNull(transport, "transport cannot be null");

        final String actReg = gson.toJson(new ScalarWebRequest(ScalarWebMethod.ACTREGISTER, activationVersion,
                new ActRegisterId(), new Object[] { new ActRegisterOptions() }));

        final String actUrl = getActivationUrl();
        if (actUrl == null) {
            return ScalarWebResult.createNotImplemented(ScalarWebMethod.ACTREGISTER);
        }

        final HttpResponse r = transport.executePostJson(actUrl, actReg, accessCode == null ? new TransportOption[0]
                : new TransportOption[] { new TransportOptionHeader(NetUtil.createAuthHeader(accessCode)) });

        if (r.getHttpCode() == HttpStatus.OK_200) {
            return gson.fromJson(r.getContent(), ScalarWebResult.class);
        } else {
            return new ScalarWebResult(r);
        }
    }

    /**
     * Sets the authentication header for all specified transports (generally used for preshared keys)
     * 
     * @param accessCode a non-null, non-empty access code
     * @param transports the transports to set header authentication
     */
    public static void setupHeader(final String accessCode, final SonyTransport... transports) {
        Validate.notEmpty(accessCode, "accessCode cannot be empty");
        for (final SonyTransport transport : transports) {
            transport.setOption(TransportOptionAutoAuth.FALSE);
            transport.setOption(new TransportOptionHeader(NetUtil.createAccessCodeHeader(accessCode)));
        }
    }

    /**
     * Sets up cookie authorization on all specified transports
     * 
     * @param transports the transports to set cookie authentication
     */
    public static void setupCookie(final SonyTransport... transports) {
        for (final SonyTransport transport : transports) {
            transport.setOption(TransportOptionAutoAuth.TRUE);
        }
    }

    /**
     * Functional interface to retrive an IRCC client
     */
    @NonNullByDefault
    public interface IrccClientProvider {
        /**
         * Called when an IRCC client is needed
         * 
         * @return a potentially null IRCC client
         */
        @Nullable
        IrccClient getClient();
    }
}
