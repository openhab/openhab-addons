/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.*;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.ParseException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.ApplicationsDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.AvailableAppsDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.ComponentDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.CurrentAppDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.ExtrasDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.IntentDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.LaunchAppDTO;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AppService} is responsible for handling key code commands, which emulate a button
 * press on a remote control.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class AppService implements PhilipsTVService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Label , Entry<PackageName,ClassName> of App
    private @Nullable Map<String, AbstractMap.SimpleEntry<String, String>> availableApps;

    private String currentPackageName = "";

    private final PhilipsTVConnectionManager handler;

    private final ConnectionManager connectionManager;

    public AppService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            synchronized (this) {
                if (isAvailableAppListEmpty()) {
                    getAvailableAppListFromTv();
                    handler.updateChannelStateDescription(CHANNEL_APPNAME, availableApps.keySet().stream()
                            .collect(Collectors.toMap(Function.identity(), Function.identity())));
                }
            }
            if (command instanceof RefreshType) {
                // Get current App name
                String packageName = getCurrentApp();
                if (currentPackageName.equals(packageName)) {
                    return;
                } else {
                    currentPackageName = packageName;
                }
                Optional<Map.Entry<String, AbstractMap.SimpleEntry<String, String>>> app = availableApps.entrySet()
                        .stream().filter(e -> e.getValue().getKey().equalsIgnoreCase(packageName)).findFirst();
                if (app.isPresent()) {
                    handler.postUpdateChannel(CHANNEL_APP, new StringType(packageName));
                    Map.Entry<String, AbstractMap.SimpleEntry<String, String>> appEntry = app.get();
                    handler.postUpdateChannel(CHANNEL_APPNAME, new StringType(appEntry.getKey()));
                    // Get icon for current App
                    RawType image = getIconForApp(appEntry.getValue().getKey(), appEntry.getValue().getValue());
                    handler.postUpdateChannel(CHANNEL_APP_ICON, (image != null) ? image : UnDefType.UNDEF);
                } else { // NA
                    handler.postUpdateChannel(CHANNEL_APP, new StringType(packageName));
                    handler.postUpdateChannel(CHANNEL_APPNAME, new StringType(packageName));
                    handler.postUpdateChannel(CHANNEL_APP_ICON, UnDefType.UNDEF);
                }
            } else if (command instanceof StringType) {
                String appName = "";
                if (CHANNEL_APPNAME.equals(channel) && availableApps.containsKey(command.toString())) {
                    launchApp(command.toString());
                } else if (CHANNEL_APP.equals(channel)) {
                    launchDNApp(command.toString());
                } else {
                    logger.warn("The given App with Name: {} {} couldn't be found in the local App List from the tv.",
                            command, appName);
                }
            } else {
                logger.warn("Unknown command: {} for Channel {}", command, channel);
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                logger.debug("Could not execute command for apps, the TV is offline.");
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Error occurred during handling of command for apps: {}", e.getMessage(), e);
            }
        }
    }

    private boolean isAvailableAppListEmpty() {
        return (availableApps == null) || availableApps.isEmpty();
    }

    private void launchDNApp(String appName) throws IOException {
        for (Map.Entry<String, AbstractMap.SimpleEntry<String, String>> entry : availableApps.entrySet()) {
            Map.Entry<String, String> app = entry.getValue();
            if (app.getKey().equals(appName)) {
                logger.debug("Found app by dn: {} {} {}", entry.getKey(), app.getKey(), app.getValue());
                launchApp(entry.getKey());
                return;
            }
        }
        logger.warn("The given App with DN: {} couldn't be found in the local App List from the tv.", appName);
    }

    private void launchApp(String appName) throws IOException {
        Map.Entry<String, String> app = availableApps.get(appName);

        ComponentDTO componentDTO = new ComponentDTO();
        componentDTO.setPackageName(app.getKey());
        componentDTO.setClassName(app.getValue());

        IntentDTO intentDTO = new IntentDTO(componentDTO, new ExtrasDTO());
        intentDTO.setAction("empty");
        LaunchAppDTO launchAppDTO = new LaunchAppDTO(intentDTO);
        String appLaunchJson = OBJECT_MAPPER.writeValueAsString(launchAppDTO);

        logger.debug("App Launch json: {}", appLaunchJson);
        connectionManager.doHttpsPost(LAUNCH_APP_PATH, appLaunchJson);
    }

    private String getCurrentApp() throws IOException, ParseException {
        CurrentAppDTO currentAppDTO = OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(GET_CURRENT_APP_PATH),
                CurrentAppDTO.class);
        return currentAppDTO.getComponent().getPackageName();
    }

    private @Nullable RawType getIconForApp(String packageName, String className) throws IOException {
        String pathForIcon = String.format("%s%s-%s%sicon", SLASH, className, packageName, SLASH);
        byte[] icon = connectionManager
                .doHttpsGetForImage(String.format("%s%s", GET_AVAILABLE_APP_LIST_PATH, pathForIcon));
        if ((icon != null) && (icon.length > 0)) {
            return new RawType(icon, "image/png");
        } else {
            return null;
        }
    }

    private void getAvailableAppListFromTv() throws IOException {
        AvailableAppsDTO availableAppsDTO = OBJECT_MAPPER
                .readValue(connectionManager.doHttpsGet(GET_AVAILABLE_APP_LIST_PATH), AvailableAppsDTO.class);

        ConcurrentMap<String, AbstractMap.SimpleEntry<String, String>> appsMap = availableAppsDTO.getApplications()
                .stream()
                .collect(Collectors.toConcurrentMap(ApplicationsDTO::getLabel,
                        a -> new AbstractMap.SimpleEntry<>(a.getIntent().getComponent().getPackageName(),
                                a.getIntent().getComponent().getClassName()),
                        (a1, a2) -> a1));

        logger.debug("appsMap - Apps added: {}", appsMap.size());
        if (logger.isTraceEnabled()) {
            appsMap.keySet().forEach(app -> logger.trace("appsMap - App found: {}", app));
        }

        this.availableApps = appsMap;
    }

    public void clearAvailableAppList() {
        if (availableApps != null) {
            availableApps.clear();
        }
    }
}
