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
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sony.internal.AccessResult;
import org.openhab.binding.sony.internal.CheckResult;
import org.openhab.binding.sony.internal.LoginUnsuccessfulResponse;
import org.openhab.binding.sony.internal.SonyAuth;
import org.openhab.binding.sony.internal.SonyAuthChecker;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.ircc.models.IrccClient;
import org.openhab.binding.sony.internal.ircc.models.IrccContentInformation;
import org.openhab.binding.sony.internal.ircc.models.IrccContentUrl;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommand;
import org.openhab.binding.sony.internal.ircc.models.IrccRemoteCommands;
import org.openhab.binding.sony.internal.ircc.models.IrccStatus;
import org.openhab.binding.sony.internal.ircc.models.IrccStatusItem;
import org.openhab.binding.sony.internal.ircc.models.IrccStatusList;
import org.openhab.binding.sony.internal.ircc.models.IrccText;
import org.openhab.binding.sony.internal.ircc.models.IrccUnrDeviceInfo;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.SocketSession;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.openhab.binding.sony.internal.transports.TransportOptionAutoAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class IrccProtocol<T extends ThingCallback<String>> implements AutoCloseable {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(IrccProtocol.class);

    /** The reference to the associated {@link IrccConfig} */
    private final IrccConfig config;

    /** The callback that we use to (ehm) callback */
    private final T callback;

    /** The {@link SonyTransport} to use */
    private final SonyHttpTransport transport;

    /** The transform service to use to transform commands with */
    private final @Nullable TransformationService transformService;

    /** The {@link IrccClient} to use */
    private final IrccClient irccClient;

    // ---------------------- The following variables are state variables ------------------
    /** Whether the devices is in a text field or not */
    private final AtomicBoolean isInText = new AtomicBoolean(false);

    /** Whether the device is in a web browser or not */
    private final AtomicBoolean isInWebBrowse = new AtomicBoolean(false);

    /** Whether the device is viewing some content */
    private final AtomicBoolean isViewing = new AtomicBoolean(false);

    /** If viewing content, the current content identifier */
    private final AtomicReference<@Nullable String> contentId = new AtomicReference<>();

    /** The authorization service */
    private final SonyAuth sonyAuth;

    /**
     * Constructs the protocol handler from given parameters.
     *
     * @param config a non-null {@link IrccConfig}
     * @param transformService a possibly null {@link TransformationService} to use
     * @param callback a non-null {@link ThingCallback} to use as a callback
     * @throws IOException if an io exception occurs to the IRCC device
     */
    IrccProtocol(final IrccConfig config, final @Nullable TransformationService transformService, final T callback)
            throws IOException, URISyntaxException {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        this.config = config;
        this.callback = callback;

        this.transformService = transformService;

        this.irccClient = IrccClientFactory.get(config.getDeviceUrl());
        this.transport = SonyTransportFactory.createHttpTransport(irccClient.getBaseUrl().toExternalForm());
        this.sonyAuth = new SonyAuth(() -> irccClient);
    }

    /**
     * Gets the callback being using by the protocol
     *
     * @return the non-null callback
     */
    T getCallback() {
        return callback;
    }

    /**
     * Attempts to log into the system. This method will attempt to get the current status. If the current status is
     * forbidden, we attempt to register the device (either by registring the access code or requesting an access code).
     * If we get the current state, we simply renew our registration code.
     *
     * @return a non-null {@link LoginUnsuccessfulResponse} if we can't login (usually pending access) or null if the
     *         login was successful
     *
     * @throws IOException if an io exception occurs to the IRCC device
     */
    @Nullable
    LoginUnsuccessfulResponse login() throws IOException {
        transport.setOption(TransportOptionAutoAuth.FALSE);

        final String accessCode = config.getAccessCode();

        final SonyAuthChecker authChecker = new SonyAuthChecker(transport, accessCode);
        final CheckResult checkResult = authChecker.checkResult(() -> {
            // To check our authorization, we execute a non-existent command.
            // If it worked (200), we need to check further if we can getstatus (BDVs will respond 200
            // on non-existent command and not authorized)
            //
            // If we have 200 (good) or 500 (command not found), we return OK
            // If we have 403 (unauthorized) or 503 (service not available), we need pairing
            HttpResponse status = irccClient.executeSoap(transport, "nonexistentcommand");
            if (status.getHttpCode() == HttpStatus.OK_200) {
                status = getStatus();
            }

            if (status.getHttpCode() == HttpStatus.OK_200
                    || status.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR_500) {
                return AccessResult.OK;
            }
            if (status.getHttpCode() == HttpStatus.FORBIDDEN_403
                    || status.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
                return AccessResult.NEEDSPAIRING;
            }
            return new AccessResult(status);
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
                        StringUtils.equalsIgnoreCase(IrccConstants.ACCESSCODE_RQST, accessCode) ? null : accessCode);
                if (AccessResult.OK.equals(result)) {
                    SonyAuth.setupCookie(transport);
                } else {
                    return new LoginUnsuccessfulResponse(ThingStatusDetail.CONFIGURATION_ERROR, result.getMsg());
                }
            }
        } else {
            final AccessResult resp = sonyAuth.registerRenewal(transport);
            if (AccessResult.OK.equals(resp)) {
                SonyAuth.setupCookie(transport);
            } else {
                // Use configuration_error - prevents handler from continually trying to
                // reconnect
                return new LoginUnsuccessfulResponse(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Error registering renewal: " + resp.getMsg());
            }
        }

        writeCommands();

        callback.statusChanged(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        refreshVersion();
        refreshRegistrationMode();

        return null;
    }

    /**
     * Gets the current status from the IRCC device
     *
     * @return the non-null HttpResponse of the request
     */
    public HttpResponse getStatus() {
        final String statusUrl = irccClient.getUrlForAction(IrccClient.AN_GETSTATUS);
        if (statusUrl == null) {
            return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, "No GETSTATUS url");
        } else {
            return transport.executeGet(statusUrl);
        }
    }

    /**
     * Write the various IRCC commands the device reports
     *
     * @throws IOException if an IO exception occurs writing the map file
     */
    private void writeCommands() throws IOException {
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

            final IrccRemoteCommands remoteCmds = irccClient.getRemoteCommands();
            final List<String> lines = new ArrayList<>();
            for (final IrccRemoteCommand v : remoteCmds.getRemoteCommands().values()) {
                // Note: encode value in case it's a URL type
                lines.add(v.getName() + "=" + v.getType() + ":" + URLEncoder.encode(v.getCmd(), "UTF-8"));
            }
            Collections.sort(lines, String.CASE_INSENSITIVE_ORDER);

            if (!lines.isEmpty()) {
                logger.debug("Writing remote commands to {}", file);
                Files.write(file, lines, Charset.forName("UTF-8"));
            }
        }
    }

    /**
     * Refresh the state for this protocol (currently only calls {@link #refreshStatus})
     */
    public void refreshState() {
        refreshStatus();
    }

    /**
     * Refresh the status of the device
     */
    public void refreshStatus() {
        final String getStatusUrl = irccClient.getUrlForAction(IrccClient.AN_GETSTATUS);
        if (getStatusUrl == null || StringUtils.isEmpty(getStatusUrl)) {
            logger.debug("{} is not implemented", IrccClient.AN_GETSTATUS);
            return;
        }

        final HttpResponse resp = transport.executeGet(getStatusUrl);
        if (resp.getHttpCode() == HttpStatus.OK_200) {
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_POWER),
                    OnOffType.ON);

            final String irccStatusXml = resp.getContent();
            final IrccStatusList irccStatusList = IrccStatusList.get(irccStatusXml);
            if (irccStatusList == null) {
                logger.debug("IRCC Status response ({}) was not valid: {}", getStatusUrl, irccStatusXml);
                return;
            }

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
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_ID),
                        UnDefType.UNDEF);
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_SOURCE),
                        UnDefType.UNDEF);
                callback.stateChanged(
                        SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_SOURCE2),
                        UnDefType.UNDEF);
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_CLASS),
                        UnDefType.UNDEF);
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_TITLE),
                        UnDefType.UNDEF);
                callback.stateChanged(
                        SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_DURATION),
                        UnDefType.UNDEF);

                if (irccStatusList.isDisk()) {
                    refreshContentInformation();
                }

            } else {
                if (!isViewing.getAndSet(true)) {
                    refreshIsViewing();
                }
                final String id = viewing.getItemValue(IrccStatusItem.ID);
                final String source = viewing.getItemValue(IrccStatusItem.SOURCE);
                final String source2 = viewing.getItemValue(IrccStatusItem.SOURCE2);
                final String clazz = viewing.getItemValue(IrccStatusItem.CLASS);
                final String title = viewing.getItemValue(IrccStatusItem.TITLE);
                final String dur = viewing.getItemValue(IrccStatusItem.DURATION);

                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_ID),
                        SonyUtil.newStringType(id));
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_SOURCE),
                        SonyUtil.newStringType(source));
                callback.stateChanged(
                        SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_SOURCE2),
                        SonyUtil.newStringType(source2));
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_CLASS),
                        SonyUtil.newStringType(clazz));
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_TITLE),
                        SonyUtil.newStringType(title));
                if (StringUtils.isEmpty(dur)) {
                    callback.stateChanged(
                            SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_DURATION),
                            UnDefType.NULL);
                } else {
                    try {
                        callback.stateChanged(
                                SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_DURATION),
                                SonyUtil.newDecimalType(Integer.parseInt(dur)));
                    } catch (final NumberFormatException e) {
                        logger.debug("Could not convert {} into an integer", dur);
                        callback.stateChanged(
                                SonyUtil.createChannelId(IrccConstants.GRP_VIEWING, IrccConstants.CHANNEL_DURATION),
                                UnDefType.NULL);
                    }
                }

                final String cId = contentId.get();
                if (cId == null || !cId.equals(id)) {
                    refreshContentInformation();
                }
            }
        } else if (resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_POWER),
                    OnOffType.OFF);
        } else {
            logger.debug("Unknown code from {}: {}", IrccClient.AN_GETSTATUS, resp);
        }
    }

    /**
     * Refresh whether the device is in a text field or not
     */
    public void refreshInText() {
        callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_INTEXT),
                isInText.get() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Refresh whether the device is in a browser or not
     */
    public void refreshInBrowser() {
        callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_INBROWSER),
                isInWebBrowse.get() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Refresh whether the device is viewing content
     */
    public void refreshIsViewing() {
        callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_ISVIEWING),
                isViewing.get() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Set's the version property of the thing based on the device
     */
    private void refreshVersion() {
        final String version = irccClient.getUnrDeviceInformation().getVersion();
        if (version == null || StringUtils.isEmpty(version)) {
            callback.setProperty(IrccConstants.PROP_VERSION, IrccUnrDeviceInfo.NOTSPECIFIED);
        } else {
            callback.setProperty(IrccConstants.PROP_VERSION, version);
        }
    }

    /**
     * Set's the registration mode property of the thing based on the device
     */
    private void refreshRegistrationMode() {
        callback.setProperty(IrccConstants.PROP_REGISTRATIONMODE, Integer.toString(irccClient.getRegistrationMode()));
    }

    /**
     * Refresh the current text field's text
     */
    public void refreshText() {
        final String getTextUrl = irccClient.getUrlForAction(IrccClient.AN_GETTEXT);
        if (getTextUrl == null || StringUtils.isEmpty(getTextUrl)) {
            logger.debug("{} is not implemented", IrccClient.AN_GETTEXT);
            return;
        }

        final HttpResponse resp = transport.executeGet(getTextUrl);
        if (resp.getHttpCode() == HttpStatus.OK_200) {
            final String irccTextXml = resp.getContent();
            final IrccText irccText = IrccText.get(irccTextXml);
            if (irccText == null) {
                logger.debug("IRCC get text response ({}) was not valid: {}", getTextUrl, irccTextXml);
            } else {
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_TEXT),
                        SonyUtil.newStringType(irccText.getText()));
            }
        } else if (resp.getHttpCode() == HttpStatus.NOT_ACCEPTABLE_406
                || resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_TEXT),
                    UnDefType.UNDEF);
        } else {
            logger.debug("Unknown code from {}: {}", IrccClient.AN_GETTEXT, resp);
        }
    }

    /**
     * Refresh the device's content URL
     */
    public void refreshContentUrl() {
        final String getContentUrl = irccClient.getUrlForAction(IrccClient.AN_GETCONTENTURL);
        if (getContentUrl == null || StringUtils.isEmpty(getContentUrl)) {
            logger.debug("{} is not implemented", IrccClient.AN_GETCONTENTURL);
            return;
        }

        final HttpResponse resp = transport.executeGet(getContentUrl);
        if (resp.getHttpCode() == HttpStatus.OK_200) {
            final String irccContentUrlXml = resp.getContent();
            final IrccContentUrl irccContent = IrccContentUrl.get(irccContentUrlXml);
            if (irccContent == null) {
                logger.debug("IRCC content url response ({}) was not valid: {}", getContentUrl, irccContentUrlXml);
                return;
            }

            final String url = irccContent.getUrl();
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_CONTENTURL),
                    SonyUtil.newStringType(url));

            final IrccContentInformation ici = irccContent.getContentInformation();
            if (ici == null) {
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                        UnDefType.UNDEF);
            } else {
                final String urlTitle = ici.getInfoItemValue(IrccContentInformation.TITLE);
                callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                        SonyUtil.newStringType(urlTitle));
            }
        } else if (resp.getHttpCode() == HttpStatus.NOT_ACCEPTABLE_406
                || resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_CONTENTURL),
                    UnDefType.UNDEF);
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                    UnDefType.UNDEF);
        } else {
            logger.debug("Unknown code from {}: {}", IrccClient.AN_GETCONTENTURL, resp);
        }
    }

    /**
     * Refresh the device's current content information
     */
    public void refreshContentInformation() {
        final String getContentUrl = irccClient.getUrlForAction(IrccClient.AN_GETCONTENTINFORMATION);
        if (getContentUrl == null || StringUtils.isEmpty(getContentUrl)) {
            logger.debug("{} is not implemented", IrccClient.AN_GETCONTENTINFORMATION);
            return;
        }

        final HttpResponse resp = transport.executeGet(getContentUrl);
        if (resp.getHttpCode() == HttpStatus.OK_200) {
            final String irccContentXml = resp.getContent();
            final IrccContentInformation irccContent = IrccContentInformation.get(irccContentXml);
            if (irccContent == null) {
                logger.debug("IRCC get content url response ({}) was invalid: {}", getContentUrl, irccContentXml);
                return;
            }

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

            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_ID),
                    SonyUtil.newStringType(id));
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_TITLE),
                    SonyUtil.newStringType(title));
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_CLASS),
                    SonyUtil.newStringType(clazz));
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_SOURCE),
                    SonyUtil.newStringType(source));
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_MEDIATYPE),
                    SonyUtil.newStringType(mediaType));
            callback.stateChanged(
                    SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_MEDIAFORMAT),
                    SonyUtil.newStringType(mediaFormat));
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_EDITION),
                    SonyUtil.newStringType(edition));
            callback.stateChanged(
                    SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DESCRIPTION),
                    SonyUtil.newStringType(description));
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_GENRE),
                    SonyUtil.newStringType(genre));

            if (StringUtils.isEmpty(dur)) {
                callback.stateChanged(
                        SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DURATION),
                        UnDefType.NULL);
            } else {
                try {
                    callback.stateChanged(
                            SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DURATION),
                            SonyUtil.newDecimalType(Integer.parseInt(dur)));
                } catch (final NumberFormatException e) {
                    logger.debug("Could not convert {} into an integer", dur);
                    callback.stateChanged(
                            SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DURATION),
                            UnDefType.NULL);
                }
            }

            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_RATING),
                    SonyUtil.newStringType(rating));

            if (daterelease == null || StringUtils.isEmpty(daterelease)) {
                callback.stateChanged(
                        SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DATERELEASE),
                        UnDefType.NULL);
            } else {
                try {
                    callback.stateChanged(
                            SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DATERELEASE),
                            new DateTimeType(daterelease));
                } catch (final IllegalArgumentException e) {
                    logger.debug("Could not convert {} into an valid date", daterelease);
                    callback.stateChanged(
                            SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DATERELEASE),
                            UnDefType.NULL);
                }
            }

            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_DIRECTOR),
                    SonyUtil.newStringType(director));
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_PRODUCER),
                    SonyUtil.newStringType(producer));
            callback.stateChanged(
                    SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_SCREENWRITER),
                    SonyUtil.newStringType(screen));

            if (StringUtils.isEmpty(iconData)) {
                callback.stateChanged(
                        SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_ICONDATA),
                        UnDefType.NULL);
            } else {
                final byte[] rawBytes = Base64.getDecoder().decode(iconData);
                callback.stateChanged(
                        SonyUtil.createChannelId(IrccConstants.GRP_CONTENT, IrccConstants.CHANNEL_ICONDATA),
                        new RawType(rawBytes, RawType.DEFAULT_MIME_TYPE));
            }

        } else if (resp.getHttpCode() == HttpStatus.NOT_ACCEPTABLE_406
                || resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
            callback.stateChanged(SonyUtil.createChannelId(IrccConstants.GRP_PRIMARY, IrccConstants.CHANNEL_CONTENTURL),
                    UnDefType.UNDEF);
        } else {
            logger.debug("Unknown code from {}: {}", IrccClient.AN_GETCONTENTINFORMATION, resp);
        }
    }

    /**
     * Set's the power status of the device
     *
     * @param turnOn true to turn on, false otherwise
     */
    public void sendPower(final boolean turnOn) {
        final IrccRemoteCommands cmds = irccClient.getRemoteCommands();
        if (turnOn) {
            SonyUtil.sendWakeOnLan(logger, config.getDeviceIpAddress(), config.getDeviceMacAddress());
            final IrccRemoteCommand powerOn = cmds.getPowerOn();
            if (powerOn != null) {
                sendIrccCommand(powerOn.getCmd());
            }
        } else {
            final IrccRemoteCommand cmd = cmds.getPowerOff();
            if (cmd == null) {
                logger.debug("No power off (or power toggle) remote command was found");
            } else {
                sendIrccCommand(cmd.getCmd());
            }
        }
    }

    /**
     * Send command to the device (if the command is empty, nothing occurs)
     *
     * @param cmd a non-null, non-empty command to send
     */
    public void sendCommand(final String cmd) {
        Validate.notEmpty(cmd, "cmd cannot be null");

        final String cmdMap = config.getCommandsMapFile();

        String cmdToSend = cmd;

        final TransformationService localTransformService = transformService;
        if (localTransformService == null) {
            logger.debug("No MAP transformation service - cannot transform command");
        } else {
            try {
                if (cmdMap != null && StringUtils.isNotBlank(cmdMap)) {
                    cmdToSend = localTransformService.transform(cmdMap, cmd);
                    if (!StringUtils.equalsIgnoreCase(cmdToSend, cmd)) {
                        logger.debug("Transformed {} with map file '{}' to {}", cmd, cmdMap, cmdToSend);
                    }
                }
            } catch (final TransformationException e) {
                logger.debug("Failed to transform {} using map file '{}', exception={} - ignoring error", cmd, cmdMap,
                        e.getMessage());
            }
        }

        try {
            cmdToSend = URLDecoder.decode(cmdToSend, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            logger.debug("Failed to decode {}, exception={} - ignoring error", cmdToSend, e.getMessage());
        }

        final int idx = cmdToSend == null ? -1 : cmdToSend.indexOf(':');

        String protocol = IrccRemoteCommand.IRCC;
        if (cmdToSend != null && idx >= 0) {
            protocol = cmdToSend.substring(0, idx);
            cmdToSend = cmdToSend.substring(idx + 1);
        }

        if (cmdToSend == null || StringUtils.isEmpty(cmdToSend)) {
            logger.debug("Command was empty - ignoring");
        } else if (StringUtils.equalsIgnoreCase(IrccRemoteCommand.IRCC, protocol)) {
            sendIrccCommand(cmdToSend);
        } else if (StringUtils.equalsIgnoreCase(IrccRemoteCommand.URL, protocol)) {
            final HttpResponse resp = transport.executeGet(cmdToSend);
            if (resp.getHttpCode() == HttpStatus.OK_200) {
                logger.trace("Send of command {} was successful", cmdToSend);
            } else if (resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
                logger.debug("URL service is unavailable (power off?)");
            } else {
                logger.debug("Bad return code from {}: {}", IrccClient.SRV_ACTION_SENDIRCC, resp);
            }
        } else {
            logger.debug("Unknown protocol found for the send command: {}", cmd);
        }
    }

    /**
     * Send an IRCC command to the device
     *
     * @param cmdToSend the non-null, non-empty IRCC command to send
     */
    private void sendIrccCommand(final String cmdToSend) {
        Validate.notEmpty(cmdToSend, "cmdToSend cannot be empty");

        final HttpResponse resp = irccClient.executeSoap(transport, cmdToSend);
        if (resp.getHttpCode() == HttpStatus.OK_200) {
            logger.trace("Sending of IRCC command {} was successful", cmdToSend);
        } else if (resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
            logger.debug("IRCC service is unavailable (power off?)");
        } else if (resp.getHttpCode() == HttpStatus.INTERNAL_SERVER_ERROR_500) {
            logger.debug("IRCC service returned a 500 - probably an unknown command: {}", cmdToSend);
        } else {
            logger.debug("Bad return code from {}: {}", IrccClient.SRV_ACTION_SENDIRCC, resp);
        }
    }

    /**
     * Set's the content URL of the device
     *
     * @param contentUrl the non-null, non-empty content url
     */
    public void sendContentUrl(final String contentUrl) {
        Validate.notEmpty(contentUrl, "contentUrl cannot be empty");

        final String sendContentUrl = irccClient.getUrlForAction(IrccClient.AN_SENDCONTENTURL);
        if (sendContentUrl == null || StringUtils.isEmpty(sendContentUrl)) {
            logger.debug("{} action was not implmented", IrccClient.AN_SENDCONTENTURL);
            return;
        }
        final String body = "<contentUrl><url>" + contentUrl + "</url></contentUrl>";
        final HttpResponse resp = transport.executePostXml(sendContentUrl, body);
        if (resp.getHttpCode() == HttpStatus.OK_200) {
            logger.trace("Send of URL {} was successful", contentUrl);
            // Do nothing
        } else if (resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
            logger.debug("IRCC service is unavailable (power off?)");
        } else {
            logger.debug("Bad return code from {}: {}", IrccClient.AN_SENDCONTENTURL, resp);
        }
    }

    /**
     * Set's the text field with the specified text
     *
     * @param text the non-null, non-empty text
     */
    public void sendText(final String text) {
        Validate.notEmpty(text, "text cannot be empty");
        final String sendTextUrl = irccClient.getUrlForAction(IrccClient.AN_SENDTEXT);
        if (StringUtils.isEmpty(sendTextUrl)) {
            logger.debug("{} action was not implmented", IrccClient.AN_SENDTEXT);
            return;
        }

        try {
            final String textParm = "?text=" + URLEncoder.encode(text, "UTF-8");
            final HttpResponse resp = transport.executeGet(sendTextUrl + textParm);
            if (resp.getHttpCode() == HttpStatus.OK_200) {
                logger.trace("Send of text {} was successful", text);
            } else if (resp.getHttpCode() == HttpStatus.NOT_ACCEPTABLE_406) {
                logger.debug("{} was sent but 'not acceptable' was returned (ie no input field to accept text)",
                        IrccClient.AN_SENDTEXT);
            } else if (resp.getHttpCode() == HttpStatus.SERVICE_UNAVAILABLE_503) {
                logger.debug("IRCC service is unavailable (power off?)");
            } else {
                logger.debug("Unknown code for {}:L {}", IrccClient.AN_SENDTEXT, resp);
            }
        } catch (final UnsupportedEncodingException e) {
            logger.debug("UTF-8 is not supported on this platform: {}", e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        transport.close();
    }
}
