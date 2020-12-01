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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.net.NetUtil;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebContext;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActiveApp;
import org.openhab.binding.sony.internal.scalarweb.models.api.ApplicationList;
import org.openhab.binding.sony.internal.scalarweb.models.api.ApplicationStatusList;
import org.openhab.binding.sony.internal.scalarweb.models.api.TextFormResult;
import org.openhab.binding.sony.internal.scalarweb.models.api.WebAppStatus;
import org.openhab.binding.sony.internal.transports.SonyHttpTransport;
import org.openhab.binding.sony.internal.transports.SonyTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the protocol handles the AppControl service.
 * 
 * Note: haven't figured out text encryption yet - so I have the basics commented out for future use
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class ScalarWebAppControlProtocol<T extends ThingCallback<String>> extends AbstractScalarWebProtocol<T> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebAppControlProtocol.class);

    // Varous channel constants
    private static final String APPTITLE = "apptitle";
    private static final String APPICON = "appicon";
    private static final String APPDATA = "appdata";
    private static final String APPSTATUS = "appstatus";
    private static final String TEXTFORM = "textform";
    private static final String STATUS = "status";
    private static final String START = "start";
    private static final String STOP = "stop";

    // The intervals used for refresh
    private static final int APPLISTINTERVAL = 60000;
    private static final int ACTIVEAPPINTERVAL = 10000;

    /** The lock used to modify app information */
    private final Lock appListLock = new ReentrantLock();

    /** The last time the app list was accessed */
    private long appListLastTime = 0;

    /** The applications */
    private final List<ApplicationList> apps = new CopyOnWriteArrayList<ApplicationList>();

    /** The lock used to access activeApp */
    private final Lock webAppStatusLock = new ReentrantLock();

    /** The last time the activeApp was accessed */
    private long webAppStatusLastTime = 0;

    /** The active app. */
    private @Nullable WebAppStatus webAppStatus = null;

    /**
     * Instantiates a new scalar web app control protocol.
     *
     * @param context the non-null context to use
     * @param context the non-null context to use
     * @param service the non-null service to use
     * @param callback the non-null callback to use
     */
    ScalarWebAppControlProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
            final ScalarWebService service, final T callback) {
        super(factory, context, service, callback);
    }

    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors(final boolean dynamicOnly) {
        final Set<String> cache = new HashSet<>();
        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        // everything is dynamic so we ignore the dynamicOnly flag

        for (final ApplicationList app : getApplications()) {
            final String uri = app.getUri();
            if (uri == null || StringUtils.isEmpty(uri)) {
                logger.debug("uri cannot be empty: {}", app);
                continue;
            }

            final String title = app.getTitle();
            final String label = WordUtils.capitalize(title);

            final String origId = SonyUtil.createValidChannelUId(title);

            // Make a unique channel ID for this
            // We can't solely assume title is unique - there are bravias that have
            // different apps that are titled the same (go figure)
            // So do "-x" to the title if we have a duplicate
            String validId = origId;
            int i = 0;
            while (cache.contains(validId)) {
                validId = origId + "-" + (++i);
            }
            cache.add(validId);

            final String id = validId;

            descriptors.add(createDescriptor(createChannel(APPTITLE, id, uri), "String", "scalarappcontrolapptitle",
                    "App " + label + " Title", "Title for application " + label));
            descriptors.add(createDescriptor(createChannel(APPICON, id, uri), "Image", "scalarappcontrolappicon",
                    "App " + label + " Icon", "Icon for application " + label));
            descriptors.add(createDescriptor(createChannel(APPDATA, id, uri), "String", "scalarappcontrolappdata",
                    "App " + label + " Data", "Data for application " + label));
            descriptors.add(createDescriptor(createChannel(APPSTATUS, id, uri), "String", "scalarappcontrolappstatus",
                    "App " + label + " Status", "Status for " + label));
        }

        try {
            final List<ApplicationStatusList> statuses = execute(ScalarWebMethod.GETAPPLICATIONSTATUSLIST)
                    .asArray(ApplicationStatusList.class);
            for (final ApplicationStatusList status : statuses) {
                final String name = status.getName();
                if (name == null || StringUtils.isEmpty(name)) {
                    logger.debug("name cannot be empty: {}", status);
                    continue;
                }

                final String id = SonyUtil.createValidChannelUId(name);
                final String title = WordUtils.capitalize(name);
                descriptors.add(createDescriptor(createChannel(STATUS, id, name), "Switch", "scalarappcontrolstatus",
                        "Indicator " + title, "Indicator for " + title));
            }
        } catch (final IOException e) {
            logger.debug("Exception getting application status list: {}", e.getMessage());
        }

        // Haven't figured out encryption yet - assume non-encryption format
        // try {
        // final String textFormVersion = getService().getVersion(ScalarWebMethod.GETTEXTFORM);
        // if (VersionUtilities.equals(textFormVersion, ScalarWebMethod.V1_0)) {
        if (service.hasMethod(ScalarWebMethod.GETTEXTFORM)) {
            descriptors.add(createDescriptor(createChannel(TEXTFORM), "String", "scalarappcontroltextform"));
        }
        // } else if (VersionUtilities.equals(textFormVersion, ScalarWebMethod.V1_1)) {
        // final String localPubKey = pubKey;
        // if (localPubKey == null || StringUtils.isEmpty(localPubKey)) {
        // logger.debug("Can't get text form - no public key");
        // } else {
        // execute(ScalarWebMethod.GETTEXTFORM, new TextFormRequest_1_1(localPubKey, null));
        // descriptors.add(createDescriptor(createChannel(TEXTFORM), "String", "scalarappcontroltextform"));
        // }
        // }
        // } catch (final IOException e) {
        // logger.debug("Exception getting text form: {}", e.getMessage());
        // }

        return descriptors;
    }

    @Override
    public void refreshState(boolean initial) {
        final Set<ScalarWebChannel> appChannels = getChannelTracker().getLinkedChannelsForCategory(APPTITLE, APPICON,
                APPDATA, APPSTATUS, TEXTFORM, STATUS);
        for (final ScalarWebChannel chl : appChannels) {
            refreshChannel(chl);
        }
    }

    @Override
    public void refreshChannel(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");

        final String ctgy = channel.getCategory();
        if (StringUtils.equalsIgnoreCase(ctgy, TEXTFORM)) {
            refreshTextForm(channel.getChannelId());
        } else {
            final String[] paths = channel.getPaths();
            if (paths.length == 0) {
                logger.debug("Refresh Channel path invalid: {}", channel);
            } else {
                final String target = paths[0];

                switch (ctgy) {
                    case APPTITLE:
                        refreshAppTitle(channel.getChannelId(), target);
                        break;

                    case APPICON:
                        refreshAppIcon(channel.getChannelId(), target);
                        break;

                    case APPDATA:
                        refreshAppData(channel.getChannelId(), target);
                        break;

                    case APPSTATUS:
                        refreshAppStatus(channel.getChannelId(), target);
                        break;

                    case STATUS:
                        refreshStatus(channel.getChannelId(), target);
                        break;

                    default:
                        logger.debug("Unknown refresh channel: {}", channel);
                        break;
                }
            }
        }
    }

    /**
     * Gets the active application. Note that this method will cache the active application applications for
     * {@link #ACTIVEAPPINTERVAL} milliseconds to prevent excessive retrieving of the application list (applications
     * don't change that often!)
     *
     * @return the active application or null if none
     */
    private @Nullable WebAppStatus getWebAppStatus() {
        webAppStatusLock.lock();
        try {
            final long now = System.currentTimeMillis();
            if (webAppStatus == null || webAppStatusLastTime + ACTIVEAPPINTERVAL < now) {
                webAppStatus = execute(ScalarWebMethod.GETWEBAPPSTATUS).as(WebAppStatus.class);
                webAppStatusLastTime = now;
            }

        } catch (final IOException e) {
            // already handled by execute
        } finally {
            webAppStatusLock.unlock();
        }

        return webAppStatus;
    }

    /**
     * Gets the list of applications. Note that this method will cache the applications for {@link #APPLISTINTERVAL}
     * milliseconds to prevent excessive retrieving of the application list (applications don't change that often!)
     *
     * @return the non-null, possibly empty unmodifiable list of applications
     */
    private List<ApplicationList> getApplications() {
        appListLock.lock();
        try {
            final long now = System.currentTimeMillis();
            if (appListLastTime + APPLISTINTERVAL < now) {
                apps.clear();
                apps.addAll(execute(ScalarWebMethod.GETAPPLICATIONLIST).asArray(ApplicationList.class));
                appListLastTime = now;
            }

            return Collections.unmodifiableList(apps);
        } catch (final IOException e) {
            // already handled by execute
            return Collections.unmodifiableList(apps);
        } finally {
            appListLock.unlock();
        }
    }

    /**
     * Gets the application list for the given URI
     *
     * @param appUri the non-null, non-empty application URI
     * @return the application list or null if none
     */
    private @Nullable ApplicationList getApplicationList(final String appUri) {
        Validate.notEmpty(appUri, "appUri cannot be empty");
        for (final ApplicationList app : getApplications()) {
            if (StringUtils.equalsIgnoreCase(app.getUri(), appUri)) {
                return app;
            }
        }
        return null;
    }

    /**
     * Refresh app title for the given URI
     *
     * @param channelId the non-null, non-empty channel ID
     * @param appUri the non-null, non-empty application URI
     */
    private void refreshAppTitle(final String channelId, final String appUri) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(appUri, "appUri cannot be empty");
        final ApplicationList app = getApplicationList(appUri);
        if (app != null) {
            callback.stateChanged(channelId, SonyUtil.newStringType(app.getTitle()));
        }
    }

    /**
     * Refresh app icon for the given URI
     *
     * @param channelId the non-null, non-empty channel ID
     * @param appUri the non-null, non-empty application URI
     */
    private void refreshAppIcon(final String channelId, final String appUri) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(appUri, "appUri cannot be empty");
        final ApplicationList app = getApplicationList(appUri);
        if (app != null) {
            final String iconUrl = app.getIcon();
            if (iconUrl == null || StringUtils.isEmpty(iconUrl)) {
                callback.stateChanged(channelId, UnDefType.UNDEF);
            } else {
                try (SonyHttpTransport transport = SonyTransportFactory
                        .createHttpTransport(getService().getTransport().getBaseUri().toString())) {
                    final RawType rawType = NetUtil.getRawType(transport, iconUrl);
                    callback.stateChanged(channelId, rawType == null ? UnDefType.UNDEF : rawType);
                } catch (final URISyntaxException e) {
                    logger.debug("Exception occurred getting application icon: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Refresh app data for the given URI
     *
     * @param channelId the non-null, non-empty channel ID
     * @param appUri the non-null, non-empty application URI
     */
    private void refreshAppData(final String channelId, final String appUri) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(appUri, "appUri cannot be empty");
        final ApplicationList app = getApplicationList(appUri);
        if (app != null) {
            callback.stateChanged(channelId, SonyUtil.newStringType(app.getData()));
        }
    }

    /**
     * Refresh app status for the given URI
     *
     * @param channelId the non-null, non-empty channel ID
     * @param appUri the non-null, non-empty application URI
     */
    private void refreshAppStatus(final String channelId, final String appUri) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(appUri, "appUri cannot be empty");
        final WebAppStatus webAppStatus = getWebAppStatus();
        callback.stateChanged(channelId,
                webAppStatus != null && webAppStatus.isActive()
                        && StringUtils.equalsIgnoreCase(appUri, webAppStatus.getUrl()) ? SonyUtil.newStringType(START)
                                : SonyUtil.newStringType(STOP));
    }

    /**
     * Refresh status for the status name
     *
     * @param channelId the non-null, non-empty channel ID
     * @param statusName the non-null, non-empty status name
     */
    private void refreshStatus(final String channelId, final String statusName) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        Validate.notEmpty(statusName, "statusName cannot be empty");
        try {
            final List<ApplicationStatusList> statuses = execute(ScalarWebMethod.GETAPPLICATIONSTATUSLIST)
                    .asArray(ApplicationStatusList.class);
            for (final ApplicationStatusList status : statuses) {
                if (StringUtils.equalsIgnoreCase(statusName, status.getName())) {
                    callback.stateChanged(channelId, status.isOn() ? OnOffType.ON : OnOffType.OFF);
                }
            }
            callback.stateChanged(channelId, OnOffType.OFF);
        } catch (final IOException e) {
            logger.debug("Exception getting application status list: {}", e.getMessage());
        }
    }

    /**
     * Refresh text from a text form
     *
     * @param channelId the non-null, non-empty channel ID
     */
    private void refreshTextForm(final String channelId) {
        Validate.notEmpty(channelId, "channelId cannot be empty");

        // String pubKey = null;
        // final ScalarWebService enc = getService(ScalarWebService.ENCRYPTION);
        // if (enc != null) {
        // try {
        // final PublicKey publicKey = enc.execute(ScalarWebMethod.GETPUBLICKEY).as(PublicKey.class);
        // pubKey = publicKey.getPublicKey();
        // } catch (final IOException e) {
        // logger.debug("Exception getting public key: {}", e.getMessage());
        // }
        // }

        try {
            // if (StringUtils.isNotEmpty(pubKey)) {
            // byte[] byteKey = Base64.getDecoder().decode(pubKey);
            // X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            // KeyFactory kf = KeyFactory.getInstance("RSA");
            // java.security.PublicKey sonyPublicKey = kf.generatePublic(X509publicKey);
            // KeyGenerator gen = KeyGenerator.getInstance("AES");
            // gen.init(128); /* 128-bit AES */
            // SecretKey secretKey = gen.generateKey();
            // Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            // cipher.init(Cipher.ENCRYPT_MODE, sonyPublicKey);
            // byte[] a = cipher.doFinal(secretKey.getEncoded());
            // String encKey = new String(Base64.getEncoder().encode(a), "UTF-8");

            // final TextFormResult ff = execute(ScalarWebMethod.GETTEXTFORM, new TextFormRequest_1_1(encKey, null))
            // .as(TextFormResult.class);
            // }
            // Alwasy assume non encrypted version
            // final String localEncKey = pubKey;
            // if (localEncKey != null && StringUtils.isNotEmpty(localEncKey)) {
            final TextFormResult form = execute(ScalarWebMethod.GETTEXTFORM, version -> {
                // if (VersionUtilities.equals(version, ScalarWebMethod.V1_0)) {
                return null;
                // }
                // return new TextFormRequest_1_1(rsa encrypted aes key, null);
            }).as(TextFormResult.class);
            callback.stateChanged(channelId, SonyUtil.newStringType(form.getText()));
            // }
            // } catch (final IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
            // | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
        } catch (final IOException e) {
            logger.debug("Exception getting text form: {}", e.getMessage());
        }
    }

    @Override
    public void setChannel(final ScalarWebChannel channel, final Command command) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        switch (channel.getCategory()) {
            case TEXTFORM:
                if (command instanceof StringType) {
                    setTextForm(command.toString());
                } else {
                    logger.debug("TEXTFORM command not an StringType: {}", command);
                }

                break;

            case APPSTATUS:
                final String[] paths = channel.getPaths();
                if (paths.length == 0) {
                    logger.debug("Set APPSTATUS Channel path invalid: {}", channel);
                } else {
                    if (command instanceof StringType) {
                        setAppStatus(paths[0], StringUtils.equalsIgnoreCase(START, command.toString()));
                    } else {
                        logger.debug("APPSTATUS command not an StringType: {}", command);
                    }
                    if (command instanceof OnOffType) {
                        setAppStatus(paths[0], command == OnOffType.ON);
                    } else {
                        logger.debug("APPSTATUS command not an OnOffType: {}", command);
                    }
                }

                break;

            default:
                logger.debug("Unhandled channel command: {} - {}", channel, command);
                break;
        }
    }

    /**
     * Sets the text in a form
     *
     * @param text the possibly null, possibly empty text
     */
    private void setTextForm(final @Nullable String text) {
        // Assume non-encrypted form
        // final String version = getService().getVersion(ScalarWebMethod.SETTEXTFORM);
        // if (VersionUtilities.equals(version, ScalarWebMethod.V1_0)) {
        handleExecute(ScalarWebMethod.SETTEXTFORM, text == null ? "" : text);
        // } else if (VersionUtilities.equals(version, ScalarWebMethod.V1_0)) {
        // final String localEncKey = pubKey;
        // if (localEncKey != null && StringUtils.isNotEmpty(localEncKey)) {
        // do encryption
        // handleExecute(ScalarWebMethod.SETTEXTFORM,
        // new TextFormRequest_1_1(localEncKey, text == null ? "" : text));
        // }
        // } else {
        // logger.debug("Unknown {} method version: {}", ScalarWebMethod.SETTEXTFORM, version);
        // }
    }

    /**
     * Sets the application status
     *
     * @param appUri the non-null, non-empty application URI
     * @param on true if active, false otherwise
     */
    private void setAppStatus(final String appUri, final boolean on) {
        Validate.notEmpty(appUri, "appUri cannot be empty");
        if (on) {
            handleExecute(ScalarWebMethod.SETACTIVEAPP, new ActiveApp(appUri, null));
        } else {
            handleExecute(ScalarWebMethod.TERMINATEAPPS);
        }
    }
}
