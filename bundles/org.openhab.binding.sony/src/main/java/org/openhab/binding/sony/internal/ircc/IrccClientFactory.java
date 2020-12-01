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
package org.openhab.binding.sony.internal.ircc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.sony.internal.ircc.models.IrccActionList;
import org.openhab.binding.sony.internal.ircc.models.IrccClient;
import org.openhab.binding.sony.internal.ircc.models.IrccCodeList;
import org.openhab.binding.sony.internal.ircc.models.IrccDevice;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommands;
import org.openhab.binding.sony.internal.ircc.models.IrccRoot;
import org.openhab.binding.sony.internal.ircc.models.IrccSystemInformation;
import org.openhab.binding.sony.internal.ircc.models.IrccUnrDeviceInfo;
import org.openhab.binding.sony.internal.ircc.models.IrccXmlReader;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.openhab.binding.sony.internal.upnp.models.UpnpScpd;
import org.openhab.binding.sony.internal.upnp.models.UpnpService;
import org.openhab.binding.sony.internal.upnp.models.UpnpXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class represents a factory for creating {@link IrccClient} classes and will attempt to detect basic information
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class IrccClientFactory {
    /** The default service type for IRCC */
    public static final String SRV_IRCC_SERVICETYPE = "urn:schemas-sony-com:service:IRCC:1";

    /** The typical IRCC values for TV and blurays */
    private static final String LIKELY_TVAVR_SCPD = "/sony/ircc/IRCCSCPD.xml";
    private static final String LIKELY_TVAVR_IRCC = "/sony/ircc";
    private static final int LIKELY_BLURAY_PORT = 50001;
    private static final String LIKELY_BLURAY_SCPD = "/IRCCSCPD.xml";
    private static final String LIKELY_BLURAY_IRCC = "/upnp/control/IRCC";
    private static final String LIKELY_SCPD_RESULT = "<?xml version=\"1.0\"?><scpd xmlns=\"urn:schemas-upnp-org:service-1-0\"><actionList><action><name>X_SendIRCC</name><argumentList><argument><name>IRCCCode</name><direction>in</direction><relatedStateVariable>X_A_ARG_TYPE_IRCCCode</relatedStateVariable></argument></argumentList></action></actionList></scpd>";

    /**
     * Creates an {@link IrccClient} for the given URL. If the URL is a basic URL (ie host name only), will attempt to
     * detect the correct settings
     * 
     * @param irccUrl a non-null, non-empty IRCC URL
     * @return a non-null {@link IrccClient}
     * @throws IOException if an IO exception occurs
     * @throws URISyntaxException if a URL has an incorrect syntax
     */
    public static IrccClient get(final String irccUrl) throws IOException, URISyntaxException {
        Validate.notEmpty(irccUrl, "irccUrl cannot be empty");
        return get(new URL(irccUrl));
    }

    /**
     * Instantiates a new IRCC client give the IRCC URL
     *
     * @param irccUrl the non-null IRCC URL
     * @throws IOException if an IO exception occurs getting information from the client
     * @throws URISyntaxException if a URL has an incorrect syntax
     */
    public static IrccClient get(final URL irccUrl) throws IOException, URISyntaxException {
        Objects.requireNonNull(irccUrl, "irccUrl cannot be null");

        final Logger logger = LoggerFactory.getLogger(IrccClientFactory.class);

        if (StringUtils.isEmpty(irccUrl.getPath())) {
            return getDefaultClient(irccUrl, logger);
        } else {
            try {
                final IrccClient client = queryIrccClient(irccUrl, logger);
                return client == null ? getDefaultClient(irccUrl, logger) : client;
            } catch (final IOException | URISyntaxException e) {
                logger.debug("Exception occurred querying IRCC client - trying default client: {}", e.getMessage(), e);
                return getDefaultClient(irccUrl, logger);
            }
        }
    }

    /**
     * Helper method to generate a default {@link IrccClient}. A 'default' client is one where we try to detect and use
     * standard URLs/ports
     * 
     * @param irccUrl a non-null IRCC url
     * @param logger a non-null logger
     * @return a non-null {@link IrccClient}
     * @throws URISyntaxException if a URL has an inccorect syntax
     * @throws MalformedURLException if a URL is malformed
     */
    private static IrccClient getDefaultClient(final URL irccUrl, final Logger logger)
            throws URISyntaxException, MalformedURLException {
        Objects.requireNonNull(irccUrl, "irccUrl cannot be null");
        Objects.requireNonNull(logger, "logger cannot be null");

        logger.debug("Creating default IRCC client for {}", irccUrl);

        final IrccActionList actions = new IrccActionList();
        final IrccSystemInformation sysInfo = new IrccSystemInformation();
        final IrccRemoteCommands remoteCommands = new IrccRemoteCommands();
        final IrccUnrDeviceInfo irccDeviceInfo = new IrccUnrDeviceInfo();

        final Map<String, UpnpService> services = new HashMap<>();
        final Map<String, UpnpScpd> scpdByService = new HashMap<>();

        URL baseUrl = irccUrl;

        logger.debug("Testing Default IRCC client to see if it's a TV/AVR or BLURAY: {}{}", baseUrl, LIKELY_TVAVR_SCPD);
        try (SonyHttpTransport transport = SonyTransportFactory.createHttpTransport(baseUrl)) {
            final HttpResponse tvavr = transport.executeGet(new URL(baseUrl, LIKELY_TVAVR_SCPD).toExternalForm());

            String irccScpdResponse = null;
            if (tvavr.getHttpCode() == HttpStatus.OK_200) {
                logger.debug("Default IRCC client likely a TV/AVR: {}{}", baseUrl, LIKELY_TVAVR_IRCC);
                services.put(IrccClient.SRV_IRCC, new UpnpService(IrccClient.SRV_IRCC, SRV_IRCC_SERVICETYPE,
                        LIKELY_TVAVR_SCPD, LIKELY_TVAVR_IRCC));

                irccScpdResponse = tvavr.getContent();
            } else {
                final URL blurayURL = new URL(baseUrl.getProtocol(), baseUrl.getHost(), LIKELY_BLURAY_PORT,
                        LIKELY_BLURAY_SCPD);

                logger.debug("Default IRCC client may not be a TV/AVR - trying BLURAY: {}", blurayURL);
                final HttpResponse bluray = transport.executeGet(blurayURL.toExternalForm());
                if (bluray.getHttpCode() == HttpStatus.OK_200) {
                    logger.debug("Default IRCC client likely a BLURAY: {}{}", baseUrl, LIKELY_BLURAY_IRCC);
                    irccScpdResponse = bluray.getContent();
                    services.put(IrccClient.SRV_IRCC, new UpnpService(IrccClient.SRV_IRCC, SRV_IRCC_SERVICETYPE,
                            LIKELY_BLURAY_SCPD, LIKELY_BLURAY_IRCC));

                    baseUrl = blurayURL; // override to get the port
                }
            }

            if (irccScpdResponse != null && StringUtils.isNotEmpty(irccScpdResponse)) {
                logger.debug("Default IRCC client using SCPD: {}", irccScpdResponse);
                final UpnpScpd scpd = UpnpXmlReader.SCPD.fromXML(irccScpdResponse);
                if (scpd != null) {
                    scpdByService.put(IrccClient.SRV_IRCC, scpd);
                }
            }
        }

        if (services.isEmpty()) {
            logger.debug("Default IRCC detection failed - assuming TV/AVR: {}{}", baseUrl, LIKELY_TVAVR_IRCC);
            services.put(IrccClient.SRV_IRCC,
                    new UpnpService(IrccClient.SRV_IRCC, SRV_IRCC_SERVICETYPE, LIKELY_TVAVR_SCPD, LIKELY_TVAVR_IRCC));
        }

        if (scpdByService.isEmpty()) {
            logger.debug("Default SCPD detection failed - assuming result: {}", LIKELY_SCPD_RESULT);
            final UpnpScpd scpd = UpnpXmlReader.SCPD.fromXML(LIKELY_SCPD_RESULT);
            scpdByService.put(IrccClient.SRV_IRCC, scpd);
        }

        return new IrccClient(baseUrl, services, actions, sysInfo, remoteCommands, irccDeviceInfo, scpdByService);
    }

    /**
     * Helper method to create a {@link IrccClient} from a URL discovered via UPNP
     * 
     * @param irccUrl a non-null URL pointing to the UPNP description
     * @param logger a non-null logger
     * @return a non-null {@link IrccClient}
     * @throws URISyntaxException if a URL has an inccorect syntax
     * @throws MalformedURLException if a URL is malformed
     */
    private static IrccClient queryIrccClient(final URL irccUrl, final Logger logger)
            throws IOException, URISyntaxException {
        Objects.requireNonNull(irccUrl, "irccUrl cannot be null");
        Objects.requireNonNull(logger, "logger cannot be null");

        try (SonyHttpTransport transport = SonyTransportFactory.createHttpTransport(irccUrl)) {
            logger.debug("Querying IRCC client {}", irccUrl);
            final HttpResponse resp = transport.executeGet(irccUrl.toExternalForm());
            if (resp.getHttpCode() != HttpStatus.OK_200) {
                throw resp.createException();
            }

            final String irccResponse = resp.getContent();
            final IrccRoot irccRoot = IrccXmlReader.ROOT.fromXML(irccResponse);
            if (irccRoot == null) {
                throw new IOException("IRCC response (" + irccUrl + ") was not valid: " + irccResponse);
            }
            logger.debug("Querying IRCC client {} and got IRCCRoot response: {}", irccUrl, irccResponse);

            final IrccDevice irccDevice = irccRoot.getDevice();
            if (irccDevice == null) {
                throw new IOException("IRCC response (" + irccUrl + ") didn't contain an IRCC device");
            }

            final Map<String, UpnpService> services = new HashMap<>();
            final Map<String, UpnpScpd> scpdByService = new HashMap<>();

            for (final UpnpService service : irccDevice.getServices()) {
                final String serviceId = service.getServiceId();

                if (serviceId == null || StringUtils.isEmpty(serviceId)) {
                    logger.debug("Querying IRCC client {} and found a service with no service id - ignoring: {}",
                            irccUrl, service);
                    continue;
                }

                logger.debug("Querying IRCC client {} and found service: {} -- {}", irccUrl, serviceId, service);
                services.put(serviceId, service);

                final URL scpdUrl = service.getScpdUrl(irccUrl);
                if (scpdUrl != null) {
                    logger.debug("Querying IRCC client {} -- {} and getting SCPD: {}", irccUrl, serviceId, scpdUrl);
                    final HttpResponse spcdResponse = transport.executeGet(scpdUrl.toExternalForm());

                    final int httpCode = spcdResponse.getHttpCode();
                    if (httpCode == HttpStatus.NOT_FOUND_404) {
                        logger.debug("Querying IRCC client {} -- {} -- {} -- wasn't found - skipping", irccUrl,
                                serviceId, scpdUrl);
                        continue;
                    } else if (spcdResponse.getHttpCode() != HttpStatus.OK_200) {
                        throw spcdResponse.createException();
                    }

                    final String scpdResponse = spcdResponse.getContent();
                    final UpnpScpd scpd = UpnpXmlReader.SCPD.fromXML(scpdResponse);
                    if (scpd == null) {
                        logger.debug("spcd url '{}' didn't contain a valid response (and is being ignored): {}",
                                scpdUrl, spcdResponse);
                    } else {
                        logger.debug("Querying IRCC client {} -- {} and adding SCPD: {} -- {}", irccUrl, serviceId,
                                scpdUrl, scpd);
                        scpdByService.put(serviceId, scpd);
                    }
                }
            }

            final IrccUnrDeviceInfo unrDeviceInfo = irccDevice.getUnrDeviceInfo();
            final IrccUnrDeviceInfo irccDeviceInfo = unrDeviceInfo == null ? new IrccUnrDeviceInfo() : unrDeviceInfo;

            final String actionsUrl = irccDeviceInfo.getActionListUrl();

            IrccActionList actionsList;
            IrccSystemInformation sysInfo;

            // If empty - likely version 1.0 or 1.1
            if (actionsUrl == null || StringUtils.isEmpty(actionsUrl)) {
                logger.debug("Querying IRCC client {} and found no actionsUrl - generating default", irccUrl);
                actionsList = new IrccActionList();
                sysInfo = new IrccSystemInformation();
            } else {
                logger.debug("Querying IRCC client {} and finding action: {}", irccUrl, actionsUrl);
                final HttpResponse actionsResp = transport.executeGet(actionsUrl);
                if (actionsResp.getHttpCode() == HttpStatus.OK_200) {
                    final String actionXml = actionsResp.getContent();
                    final IrccActionList actionList = IrccXmlReader.ACTIONS.fromXML(actionXml);
                    if (actionList == null) {
                        throw new IOException(
                                "IRCC Actions response (" + actionsUrl + ")  was not valid: " + actionXml);
                    }
                    logger.debug("Querying IRCC client {} and found action: {} -- {}", irccUrl, actionsUrl, actionList);
                    actionsList = actionList;
                } else {
                    logger.debug("Querying IRCC client {} for actions url {} -- got error {} and defaulting to none",
                            irccUrl, actionsUrl, actionsResp.getHttpCode());
                    actionsList = new IrccActionList();
                }

                final String sysUrl = actionsList.getUrlForAction(IrccClient.AN_GETSYSTEMINFORMATION);
                if (sysUrl == null || StringUtils.isEmpty(sysUrl)) {
                    logger.debug("Querying IRCC client {} but found no system information actions URL: {} - defaulting",
                            irccUrl, actionsList);
                    sysInfo = new IrccSystemInformation();
                } else {
                    logger.debug("Querying IRCC client {} and getting system information: {}", irccUrl, sysUrl);
                    final HttpResponse sysResp = transport.executeGet(sysUrl);
                    if (sysResp.getHttpCode() == HttpStatus.OK_200) {
                        final String sysXml = sysResp.getContent();
                        final IrccSystemInformation sys = IrccXmlReader.SYSINFO.fromXML(sysXml);
                        if (sys == null) {
                            throw new IOException(
                                    "IRCC systems info response (" + sysUrl + ")  was not valid: " + sysXml);
                        }
                        logger.debug("Querying IRCC client {} and found system information: {} -- {}", irccUrl, sysUrl,
                                sys);
                        sysInfo = sys;
                    } else {
                        logger.debug(
                                "Querying IRCC client {} for sysinfo url {} -- got error {} and defaulitn system information",
                                irccUrl, sysUrl, sysResp.getHttpCode());
                        sysInfo = new IrccSystemInformation();
                    }
                }
            }

            IrccRemoteCommands remoteCommands;

            final IrccCodeList codeList = irccDevice.getCodeList();
            final String remoteCommandsUrl = actionsList.getUrlForAction(IrccClient.AN_GETREMOTECOMMANDLIST);
            if (remoteCommandsUrl == null || StringUtils.isEmpty(remoteCommandsUrl)) {
                logger.debug("Querying IRCC client {} and found no remote commands - using default code list", irccUrl);
                remoteCommands = new IrccRemoteCommands().withCodeList(codeList);
            } else {
                logger.debug("Querying IRCC client {} and getting remote commands: {}", irccUrl, remoteCommandsUrl);
                final HttpResponse rcResp = transport.executeGet(remoteCommandsUrl);
                if (rcResp.getHttpCode() == HttpStatus.OK_200) {
                    final String rcXml = rcResp.getContent();
                    final IrccRemoteCommands rcCmds = IrccXmlReader.REMOTECOMMANDS.fromXML(rcXml);
                    if (rcCmds == null) {
                        throw new IOException(
                                "IRCC systems info response (" + remoteCommandsUrl + ")  was not valid: " + rcXml);
                    }
                    logger.debug("Querying IRCC client {} and getting remote commands: {} -- {}", irccUrl,
                            remoteCommandsUrl, rcCmds);
                    remoteCommands = rcCmds;
                } else {
                    logger.debug(
                            "Querying IRCC client {} and encountered an error getting remote commands (using default now): {}",
                            irccUrl, rcResp);
                    remoteCommands = new IrccRemoteCommands().withCodeList(codeList);
                }
            }

            return new IrccClient(irccUrl, services, actionsList, sysInfo, remoteCommands, irccDeviceInfo,
                    scpdByService);
        }
    }
}
