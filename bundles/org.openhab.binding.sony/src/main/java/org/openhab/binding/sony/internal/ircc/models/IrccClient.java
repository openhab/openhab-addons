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
package org.openhab.binding.sony.internal.ircc.models;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.TransportOptionHeader;
import org.openhab.binding.sony.internal.upnp.models.UpnpScpd;
import org.openhab.binding.sony.internal.upnp.models.UpnpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class represents an IRCC client. The client will gather all the necessary about an IRCC device and provide an
 * interface to query information and to manipulate the IRCC device.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class IrccClient {

    /** The logger used by the client */
    private final Logger logger = LoggerFactory.getLogger(IrccClient.class);

    /** The constant representing the REGISTER action name */
    public static final String AN_REGISTER = "register";

    /** The constant representing the GET TEXT action name */
    public static final String AN_GETTEXT = "getText";

    /** The constant representing the SEND TEXT action name */
    public static final String AN_SENDTEXT = "sendText";

    /** The constant representing the GET CONTENT INFORMATION action name */
    public static final String AN_GETCONTENTINFORMATION = "getContentInformation";

    /** The constant representing the GET SYSTEM INFORMATION action name */
    public static final String AN_GETSYSTEMINFORMATION = "getSystemInformation";

    /** The constant representing the GET REMOTE COMMANDS action name */
    public static final String AN_GETREMOTECOMMANDLIST = "getRemoteCommandList";

    /** The constant representing the GET STATUS action name */
    public static final String AN_GETSTATUS = "getStatus";

    /** The constant representing the GET CONTENT URL action name */
    public static final String AN_GETCONTENTURL = "getContentUrl";

    /** The constant representing the SEND CONTENT URL action name */
    public static final String AN_SENDCONTENTURL = "sendContentUrl";

    /** The constant representing the IRCC service name */
    public static final String SRV_IRCC = "urn:schemas-sony-com:serviceId:IRCC";

    /** The constant representing the IRCC service action to send an IRCC command */
    public static final String SRV_ACTION_SENDIRCC = "X_SendIRCC";

    /** The base URL for the IRCC device */
    private final URL baseUrl;

    /** The services mapped by service id */
    private final Map<String, UpnpService> services;

    /** The action list for the IRCC device */
    private final IrccActionList actions;

    /** The system information for the IRCC device */
    private final IrccSystemInformation sysInfo;

    /** The remote commands supported by the IRCC device */
    private final IrccRemoteCommands remoteCommands;

    /** The IRCC UNR Device information */
    private final IrccUnrDeviceInfo irccDeviceInfo;

    /** The scpd mapped by service id */
    private final Map<String, UpnpScpd> scpdByService;

    /**
     * Constructs the IRCC client with the essential information
     * 
     * @param baseUrl a non-null base URL
     * @param services a non-null, possibly empty map of services by service id
     * @param actions the non-null ircc action list
     * @param sysInfo the non-null ircc system information
     * @param remoteCommands the non-null ircc remote commands
     * @param irccDeviceInfo the non-null ircc device information
     * @param scpdByService a non-null, possibly empty map of scpd (service control point information) by service id
     */
    public IrccClient(final URL baseUrl, final Map<String, UpnpService> services, final IrccActionList actions,
            final IrccSystemInformation sysInfo, final IrccRemoteCommands remoteCommands,
            final IrccUnrDeviceInfo irccDeviceInfo, final Map<String, UpnpScpd> scpdByService) {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        Objects.requireNonNull(services, "services cannot be null");
        Objects.requireNonNull(actions, "actions cannot be null");
        Objects.requireNonNull(sysInfo, "sysInfo cannot be null");
        Objects.requireNonNull(remoteCommands, "remoteCommands cannot be null");
        Objects.requireNonNull(irccDeviceInfo, "irccDeviceInfo cannot be null");
        Objects.requireNonNull(scpdByService, "scpdByService cannot be null");

        this.baseUrl = baseUrl;
        this.services = Collections.unmodifiableMap(services);
        this.actions = actions;
        this.sysInfo = sysInfo;
        this.remoteCommands = remoteCommands;
        this.irccDeviceInfo = irccDeviceInfo;
        this.scpdByService = Collections.unmodifiableMap(scpdByService);
    }

    /**
     * Returns the base URL of the IRCC device
     *
     * @return the non-null, non-empty base URL
     */
    public URL getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the URL for the given action name or null if not found
     *
     * @param actionName the non-null, non-empty action name
     * @return the url for action or null if not found
     */
    public @Nullable String getUrlForAction(final String actionName) {
        Validate.notEmpty(actionName, "actionName cannot be empty");
        return actions.getUrlForAction(actionName);
    }

    /**
     * Gets the registration mode of the IRCC device
     *
     * @return the registration mode (most likely >= 0)
     */
    public int getRegistrationMode() {
        return actions.getRegistrationMode();
    }

    /**
     * Gets the service related to the service ID
     *
     * @param serviceId the non-null, non-empty service id
     * @return the service or null if not found
     */
    private @Nullable UpnpService getService(final String serviceId) {
        Validate.notEmpty(serviceId, "serviceId cannot be empty");
        return services.get(serviceId);
    }

    /**
     * Gets the system information for the IRCC device
     *
     * @return the non-null system information
     */
    public IrccSystemInformation getSystemInformation() {
        return sysInfo;
    }

    /**
     * Gets the UNR Device information for the IRCC device
     *
     * @return the non-null unr device information
     */
    public IrccUnrDeviceInfo getUnrDeviceInformation() {
        return irccDeviceInfo;
    }

    /**
     * Gets the remote commands supported by the IRCC device
     *
     * @return the non-null remote commands
     */
    public IrccRemoteCommands getRemoteCommands() {
        return remoteCommands;
    }

    /**
     * Get's the SOAP command for the given service ID, action name and possibly parameters
     *
     * @param serviceId the non-null, non-empty service id
     * @param actionName the non-null, non-empty action name
     * @param parms the possibly null, possibly empty list of action parameters
     * @return the possibly null (if not service/action is not found) SOAP command
     */
    public @Nullable String getSOAP(final String serviceId, final String actionName, final String... parms) {
        Validate.notEmpty(serviceId, "serviceId cannot be empty");
        Validate.notEmpty(actionName, "actionName cannot be empty");

        final UpnpService service = services.get(serviceId);

        if (service == null) {
            logger.debug("Unable to getSOAP for service id {} - service not found", serviceId);
            return null;
        }
        final UpnpScpd scpd = scpdByService.get(serviceId);
        if (scpd == null) {
            logger.debug("Unable to getSOAP for service id {} - scpd not found", serviceId);
            return null;
        }
        final String serviceType = service.getServiceType();
        if (serviceType == null || StringUtils.isEmpty(serviceType)) {
            logger.debug("Unable to getSOAP for service id {} - serviceType was empty", serviceId);
            return null;
        }
        return scpd.getSoap(serviceType, actionName, parms);
    }

    /**
     * Executes a SOAP command
     * 
     * @param transport a non-null transport to use
     * @param cmd a non-null, non-empty cmd to execute
     * @return an HttpResponse indicating the results of the execution
     */
    public HttpResponse executeSoap(final SonyHttpTransport transport, final String cmd) {
        Objects.requireNonNull(transport, "transport cannot be null");
        Validate.notEmpty(cmd, "cmd cannot be empty");

        final UpnpService service = getService(IrccClient.SRV_IRCC);
        if (service == null) {
            logger.debug("IRCC Service was not found");
            return new HttpResponse(HttpStatus.NOT_FOUND_404, "IRCC Service was not found");
        }

        final String soap = getSOAP(IrccClient.SRV_IRCC, IrccClient.SRV_ACTION_SENDIRCC, cmd);
        if (soap == null || StringUtils.isEmpty(soap)) {
            logger.debug("Unable to find the IRCC service/action to send IRCC");
            return new HttpResponse(HttpStatus.NOT_FOUND_404, "Unable to find the IRCC service/action to send IRCC");
        }

        final URL baseUrl = getBaseUrl();
        final URL controlUrl = service.getControlUrl(baseUrl);
        if (controlUrl == null) {
            logger.debug("ControlURL for IRCC service wasn't found: {}", baseUrl);
            return new HttpResponse(HttpStatus.NOT_FOUND_404, "ControlURL for IRCC service wasn't found: " + baseUrl);
        } else {
            return transport.executePostXml(controlUrl.toExternalForm(), soap, new TransportOptionHeader("SOAPACTION",
                    "\"" + service.getServiceType() + "#" + IrccClient.SRV_ACTION_SENDIRCC + "\""));
        }
    }
}
