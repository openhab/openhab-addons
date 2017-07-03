/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.models.api.ActiveApp;
import org.openhab.binding.sony.internal.scalarweb.models.api.ApplicationList;
import org.openhab.binding.sony.internal.scalarweb.models.api.ApplicationStatusList;
import org.openhab.binding.sony.internal.scalarweb.models.api.PublicKey;
import org.openhab.binding.sony.internal.scalarweb.models.api.TextFormRequest;
import org.openhab.binding.sony.internal.scalarweb.models.api.TextFormResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebAppControlProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
class ScalarWebAppControlProtocol<T extends ThingCallback<ScalarWebChannel>> extends AbstractScalarWebProtocol<T> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebAppControlProtocol.class);

    /** The Constant APPTITLE. */
    private final static String APPTITLE = "apptitle";

    /** The Constant APPICON. */
    private final static String APPICON = "appicon";

    /** The Constant APPDATA. */
    private final static String APPDATA = "appdata";

    /** The Constant APPSTATUS. */
    private final static String APPSTATUS = "appstatus";

    /** The Constant TEXTFORM. */
    private final static String TEXTFORM = "textform";

    /** The Constant STATUS. */
    private final static String STATUS = "status";

    /** The enc key. */
    private final String encKey;

    /** The app list lock. */
    private final Lock appListLock = new ReentrantLock();

    /** The app list last time. */
    private long appListLastTime = 0;

    /** The Constant appListInterval. */
    private final static int appListInterval = 5000;

    /** The apps. */
    private final List<ApplicationList> apps = new CopyOnWriteArrayList<ApplicationList>();

    /** The active app lock. */
    private final Lock activeAppLock = new ReentrantLock();

    /** The active app last time. */
    private long activeAppLastTime = 0;

    /** The Constant activeAppInterval. */
    private final static int activeAppInterval = 1000;

    /** The active app. */
    private ActiveApp activeApp = null;

    /**
     * Instantiates a new scalar web app control protocol.
     *
     * @param tracker the tracker
     * @param state the state
     * @param service the service
     * @param callback the callback
     */
    ScalarWebAppControlProtocol(ScalarWebChannelTracker tracker, ScalarWebState state, ScalarWebService service,
            T callback) {

        super(tracker, state, service, callback);

        String pubKey = null;
        try {
            final ScalarWebService enc = state.getService(ScalarWebService.Encryption);
            if (enc != null) {
                final PublicKey publicKey = enc.execute(ScalarWebMethod.GetPublicKey).as(PublicKey.class);
                pubKey = publicKey.getPublicKey();
            } else {
                pubKey = null;
            }
        } catch (IOException e) {
            pubKey = null;
        }
        encKey = pubKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#getChannelDescriptors()
     */
    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors() {

        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        for (final ApplicationList app : getApplications()) {
            final String title = WordUtils.capitalize(app.getTitle());
            final String uri = app.getUri();

            descriptors.add(createDescriptor(createChannel(APPTITLE, uri), "String", "scalarappcontrolapptitle",
                    "App " + title + " Title", "Title for application " + title));
            descriptors.add(createDescriptor(createChannel(APPICON, uri), "String", "scalarappcontrolappicon",
                    "App " + title + " Icon", "Icon for application " + title));
            descriptors.add(createDescriptor(createChannel(APPDATA, uri), "String", "scalarappcontrolappdata",
                    "App " + title + " Data", "Data for application " + title));
            descriptors.add(createDescriptor(createChannel(APPSTATUS, uri), "Switch", "scalarappcontrolappstatus",
                    "App " + title + " Status", "Status for " + title));
        }

        try {
            final List<ApplicationStatusList> statuses = execute(ScalarWebMethod.GetApplicationStatusList)
                    .asArray(ApplicationStatusList.class);
            for (final ApplicationStatusList status : statuses) {
                final String title = WordUtils.capitalize(status.getName());
                descriptors.add(createDescriptor(createChannel(STATUS, status.getName()), "Switch",
                        "scalarappcontrolstatus", "Indicator " + title, "Indicator for " + title));
            }
        } catch (IOException e) {
            // not implemented
        }

        try {
            if (StringUtils.isNotEmpty(encKey)) {
                execute(ScalarWebMethod.GetTextForm, new PublicKey(encKey));
                descriptors.add(createDescriptor(createChannel(TEXTFORM), "String", "scalarappcontroltextform"));
            }
        } catch (IOException e) {
            // not implemented
        }

        return descriptors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshState()
     */
    @Override
    public void refreshState() {
        if (isIdLinked(APPTITLE, APPICON, APPDATA, APPSTATUS)) {
            for (final ApplicationList app : getApplications()) {
                final String uri = app.getUri();

                if (isLinked(APPTITLE, uri)) {
                    refreshAppTitle(uri);
                }
                if (isLinked(APPICON, uri)) {
                    refreshAppIcon(uri);
                }
                if (isLinked(APPDATA, uri)) {
                    refreshAppData(uri);
                }
                if (isLinked(APPSTATUS, uri)) {
                    refreshAppStatus(uri);
                }
            }
        }

        try {
            if (isIdLinked(STATUS)) {
                final List<ApplicationStatusList> statuses = execute(ScalarWebMethod.GetApplicationStatusList)
                        .asArray(ApplicationStatusList.class);
                for (final ApplicationStatusList status : statuses) {
                    if (isLinked(STATUS, status.getName())) {
                        refreshStatus(status.getName());
                    }
                }
            }
        } catch (IOException e) {
            // handled by execute
        }

        if (StringUtils.isNotEmpty(encKey) && isLinked(TEXTFORM)) {
            refreshTextForm();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel)
     */
    @Override
    public void refreshChannel(ScalarWebChannel channel) {
        if (StringUtils.equalsIgnoreCase(channel.getId(), TEXTFORM)) {
            refreshTextForm();
        } else {
            refreshState();
            final String[] paths = channel.getPaths();
            if (paths.length == 0) {
                logger.debug("Refresh Channel path invalid: {}", channel);
            } else {
                final String target = paths[0];

                switch (channel.getId()) {
                    case APPTITLE:
                        refreshAppTitle(target);
                        break;

                    case APPICON:
                        refreshAppIcon(target);
                        break;

                    case APPDATA:
                        refreshAppData(target);
                        break;

                    case APPSTATUS:
                        refreshAppStatus(target);
                        break;

                    case STATUS:
                        refreshStatus(target);
                        break;

                    default:
                        logger.debug("Unknown refresh channel: {}", channel);
                        break;
                }
            }
        }
    }

    /**
     * Gets the active app.
     *
     * @return the active app
     */
    private ActiveApp getActiveApp() {
        activeAppLock.lock();
        try {
            final long now = System.currentTimeMillis();
            if (activeAppLastTime + activeAppInterval < now || activeApp == null) {
                activeApp = execute(ScalarWebMethod.GetWebAppStatus).as(ActiveApp.class);
                activeAppLastTime = now;
            }

        } catch (IOException e) {
            // already handled by execute
        } finally {
            activeAppLock.unlock();
        }

        return activeApp;
    }

    /**
     * Gets the applications.
     *
     * @return the applications
     */
    private List<ApplicationList> getApplications() {
        appListLock.lock();
        try {
            final long now = System.currentTimeMillis();
            if (appListLastTime + appListInterval < now) {
                apps.clear();
                apps.addAll(execute(ScalarWebMethod.GetApplicationList).asArray(ApplicationList.class));
                appListLastTime = now;
            }

        } catch (IOException e) {
            // already handled by execute
        } finally {
            appListLock.unlock();
        }

        return apps;
    }

    /**
     * Gets the application list.
     *
     * @param appUri the app uri
     * @return the application list
     */
    private ApplicationList getApplicationList(String appUri) {
        for (final ApplicationList app : getApplications()) {
            if (StringUtils.equalsIgnoreCase(app.getUri(), appUri)) {
                return app;
            }
        }
        return null;
    }

    /**
     * Refresh app title.
     *
     * @param appUri the app uri
     */
    private void refreshAppTitle(String appUri) {
        final ApplicationList app = getApplicationList(appUri);
        if (app != null) {
            callback.stateChanged(createChannel(APPTITLE, appUri), new StringType(app.getTitle()));
        }
    }

    /**
     * Refresh app icon.
     *
     * @param appUri the app uri
     */
    private void refreshAppIcon(String appUri) {
        final ApplicationList app = getApplicationList(appUri);
        if (app != null) {
            callback.stateChanged(createChannel(APPICON, appUri), new StringType(app.getIcon()));
        }
    }

    /**
     * Refresh app data.
     *
     * @param appUri the app uri
     */
    private void refreshAppData(String appUri) {
        final ApplicationList app = getApplicationList(appUri);
        if (app != null) {
            callback.stateChanged(createChannel(APPDATA, appUri), new StringType(app.getData()));
        }
    }

    /**
     * Refresh app status.
     *
     * @param appUri the app uri
     */
    private void refreshAppStatus(String appUri) {
        final ActiveApp app = getActiveApp();
        if (app != null) {
            callback.stateChanged(createChannel(APPSTATUS, appUri),
                    StringUtils.equalsIgnoreCase(appUri, app.getUri()) ? OnOffType.ON : OnOffType.OFF);
        }
    }

    /**
     * Refresh status.
     *
     * @param statusName the status name
     */
    private void refreshStatus(String statusName) {
        try {
            final List<ApplicationStatusList> statuses = execute(ScalarWebMethod.GetApplicationStatusList)
                    .asArray(ApplicationStatusList.class);
            for (final ApplicationStatusList status : statuses) {
                if (StringUtils.equalsIgnoreCase(statusName, status.getName())) {
                    callback.stateChanged(createChannel(STATUS, statusName),
                            status.isOn() ? OnOffType.ON : OnOffType.OFF);

                }
            }
            callback.stateChanged(createChannel(STATUS, statusName), OnOffType.OFF);
        } catch (IOException e) {
            // not implemented
        }

    }

    /**
     * Refresh text form.
     */
    private void refreshTextForm() {
        try {
            final TextFormResult form = execute(ScalarWebMethod.GetTextForm, new TextFormRequest(encKey, null))
                    .as(TextFormResult.class);
            callback.stateChanged(createChannel(TEXTFORM), new StringType(form.getText()));
        } catch (IOException e) {
            // not implemented
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#setChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel, org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void setChannel(ScalarWebChannel channel, Command command) {
        switch (channel.getId()) {
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
     * Sets the text form.
     *
     * @param text the new text form
     */
    private void setTextForm(String text) {
        handleExecute(ScalarWebMethod.SetTextForm, new TextFormRequest(encKey, text));
    }

    /**
     * Sets the app status.
     *
     * @param appUri the app uri
     * @param on the on
     */
    private void setAppStatus(String appUri, boolean on) {
        if (on) {
            handleExecute(ScalarWebMethod.SetActiveApp, new ActiveApp(appUri, null));
        } else {
            handleExecute(ScalarWebMethod.TerminateApps);
        }
    }
}
