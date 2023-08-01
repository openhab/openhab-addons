/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.CHANNEL_APP_ICON;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.CHANNEL_APP_NAME;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.GET_AVAILABLE_APP_LIST_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.GET_CURRENT_APP_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.LAUNCH_APP_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.SLASH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.TV_OFFLINE_MSG;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager.OBJECT_MAPPER;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.http.ParseException;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.ApplicationsDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.AvailableAppsDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.ComponentDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.CurrentAppDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.IntentDto;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.LaunchAppDto;
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
 */
public class AppService implements PhilipsTVService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Label , Entry<PackageName,ClassName> of App
    private Map<String, AbstractMap.SimpleEntry<String, String>> availableApps;

    private String currentPackageName = "";

    private final PhilipsTVConnectionManager connectionManager;

    public AppService(PhilipsTVConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            synchronized (this) { // TODO: avoids multiple inits at startup
                if (isAvailableAppListEmpty()) {
                    availableApps = getAvailableAppListFromTv();
                    connectionManager.updateChannelStateDescription(CHANNEL_APP_NAME, availableApps.keySet().stream()
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
                    Map.Entry<String, AbstractMap.SimpleEntry<String, String>> appEntry = app.get();
                    connectionManager.postUpdateChannel(CHANNEL_APP_NAME, new StringType(appEntry.getKey()));
                    // Get icon for current App
                    RawType image = getIconForApp(appEntry.getValue().getKey(), appEntry.getValue().getValue());
                    connectionManager.postUpdateChannel(CHANNEL_APP_ICON, (image != null) ? image : UnDefType.UNDEF);
                } else { // NA
                    connectionManager.postUpdateChannel(CHANNEL_APP_NAME, new StringType(packageName));
                    connectionManager.postUpdateChannel(CHANNEL_APP_ICON, UnDefType.UNDEF);
                }
            } else if (command instanceof StringType) {
                if (availableApps.containsKey(command.toString())) {
                    launchApp(command);
                } else {
                    logger.warn("The given App with Name: {} couldn't be found in the local App List from the tv.",
                            command);
                }
            } else {
                logger.warn("Unknown command: {} for Channel {}", command, channel);
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                logger.warn("Could not execute command for apps, the TV is offline.");
                connectionManager.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                connectionManager.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Error occurred during handling of command for apps: {}", e.getMessage(), e);
            }
        }
    }

    private boolean isAvailableAppListEmpty() {
        return (availableApps == null) || availableApps.isEmpty();
    }

    private void launchApp(Command command) throws IOException {
        Map.Entry<String, String> app = availableApps.get(command.toString());
        LaunchAppDto launchAppDto = new LaunchAppDto();

        ComponentDto componentDto = new ComponentDto();
        componentDto.setPackageName(app.getKey());
        componentDto.setClassName(app.getValue());

        IntentDto intentDto = new IntentDto();
        intentDto.setComponent(componentDto);
        intentDto.setAction("empty");

        launchAppDto.setIntent(intentDto);
        String appLaunchJson = OBJECT_MAPPER.writeValueAsString(launchAppDto);

        logger.debug("App Launch json: {}", appLaunchJson);
        connectionManager.doHttpsPost(LAUNCH_APP_PATH, appLaunchJson);
    }

    private String getCurrentApp() throws IOException, ParseException {
        CurrentAppDto currentAppDto = OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(GET_CURRENT_APP_PATH),
                CurrentAppDto.class);
        return currentAppDto.getComponent().getPackageName();
    }

    private RawType getIconForApp(String packageName, String className) throws IOException {
        String pathForIcon = String.format("%s%s-%s%sicon", SLASH, className, packageName, SLASH);
        byte[] icon = connectionManager
                .doHttpsGetForImage(String.format("%s%s", GET_AVAILABLE_APP_LIST_PATH, pathForIcon));
        if ((icon != null) && (icon.length > 0)) {
            return new RawType(icon, "image/png");
        } else {
            return null;
        }
    }

    private Map<String, AbstractMap.SimpleEntry<String, String>> getAvailableAppListFromTv() throws IOException {
        AvailableAppsDto availableAppsDto = OBJECT_MAPPER
                .readValue(connectionManager.doHttpsGet(GET_AVAILABLE_APP_LIST_PATH), AvailableAppsDto.class);

        ConcurrentMap<String, AbstractMap.SimpleEntry<String, String>> appsMap = availableAppsDto.getApplications()
                .stream()
                .collect(Collectors.toConcurrentMap(ApplicationsDto::getLabel,
                        a -> new AbstractMap.SimpleEntry<>(a.getIntent().getComponent().getPackageName(),
                                a.getIntent().getComponent().getClassName()),
                        (a1, a2) -> a1));

        logger.debug("Apps added: {}", appsMap.size());
        if (logger.isTraceEnabled()) {
            appsMap.keySet().forEach(app -> logger.trace("App found: {}", app));
        }
        return appsMap;
    }

    public void clearAvailableAppList() {
        if (availableApps != null) {
            availableApps.clear();
        }
    }
}
