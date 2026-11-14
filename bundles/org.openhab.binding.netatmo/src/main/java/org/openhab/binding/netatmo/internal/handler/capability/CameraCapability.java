/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toRawType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.AlimentationStatus;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SdCardStatus;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CameraCapability} give to handle Welcome Camera specifics
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class CameraCapability extends HomeSecurityThingCapability {
    private static final String IP_ADDRESS = "ipAddress";

    private final Logger logger = LoggerFactory.getLogger(CameraCapability.class);

    private final CameraChannelHelper cameraHelper;
    private final ChannelUID personChannelUID;

    protected @Nullable String localUrl;
    protected @Nullable String vpnUrl;

    public CameraCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler, descriptionProvider, channelHelpers);
        this.personChannelUID = new ChannelUID(thingUID, GROUP_LAST_EVENT, CHANNEL_EVENT_PERSON_ID);
        this.cameraHelper = channelHelpers.stream().filter(CameraChannelHelper.class::isInstance)
                .map(CameraChannelHelper.class::cast).findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "CameraCapability must find a CameraChannelHelper, please file a bug report."));
    }

    @Override
    public void updateHomeStatusModule(HomeStatusModule newData) {
        super.updateHomeStatusModule(newData);
        // Per documentation vpn_url expires every 3 hours and on camera reboot. So useless to reping it if not changed
        String newVpnUrl = newData.getVpnUrl();
        if (newVpnUrl != null && !newVpnUrl.equals(vpnUrl)) {
            // This will also decrease the number of requests emitted toward Netatmo API.
            localUrl = newData.isLocal() ? ping(newVpnUrl) : null;
            logger.debug("localUrl set to {} for camera {}", localUrl, thingUID);
            cameraHelper.setUrls(newVpnUrl, localUrl);
            eventHelper.setUrls(newVpnUrl, localUrl);
        }
        vpnUrl = newVpnUrl;
        if (!SdCardStatus.SD_CARD_WORKING.equals(newData.getSdStatus())) {
            statusReason = newData.getSdStatus().toString();
        } else if (!AlimentationStatus.ALIM_CORRECT_POWER.equals(newData.getAlimStatus())) {
            statusReason = newData.getAlimStatus().toString();
        }
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        if (command instanceof OnOffType && CHANNEL_MONITORING.equals(channelName)) {
            getSecurityCapability().ifPresent(cap -> cap.changeStatus(localUrl, OnOffType.ON.equals(command)));
        } else if (command instanceof RefreshType && CHANNEL_LIVEPICTURE.equals(channelName)) {
            handler.updateState(GROUP_CAM_LIVE, CHANNEL_LIVEPICTURE,
                    toRawType(cameraHelper.getLivePictureURL(localUrl != null, true)));
        } else {
            super.handleCommand(channelName, command);
        }
    }

    @Override
    protected void beforeNewData() {
        super.beforeNewData();
        getSecurityCapability().ifPresent(cap -> {
            NAObjectMap<HomeDataPerson> persons = cap.getPersons();
            descriptionProvider.setStateOptions(personChannelUID,
                    persons.values().stream().map(p -> new StateOption(p.getId(), p.getName())).toList());
        });
    }

    @Override
    protected void afterNewData(@Nullable Object newData) {
        // The channel should get triggered at last (after super and sub methods), because this allows rules to access
        // the new updated data from the other channels.
        if (newData instanceof WebhookEvent event) {
            String eventType = event.getEventType().name();
            handler.recurseUpToHomeHandler(handler).ifPresent(
                    homeHandler -> homeHandler.triggerChannel(GROUP_SECURITY_EVENT, CHANNEL_HOME_EVENT, eventType));
            handler.triggerChannel(GROUP_SECURITY_EVENT, CHANNEL_HOME_EVENT, eventType);
        }
        super.afterNewData(newData);
    }

    @Override
    public List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        getSecurityCapability().ifPresent(cap -> {
            HomeEvent event = cap.getDeviceLastEvent(handler.getId(), moduleType.apiName);
            if (event != null) {
                result.add(event);
                result.addAll(event.getSubEvents());
            }
        });
        return result;
    }

    public @Nullable String ping(String vpnUrl) {
        return getSecurityCapability().map(cap -> {
            URI apiLocalUri = null;
            String localUrl = cap.ping(vpnUrl);
            if (localUrl != null) {
                try {
                    UriBuilder builder = UriBuilder.fromPath(localUrl);
                    apiLocalUri = builder.build();
                    if (apiLocalUri.getHost().startsWith("169.254.")) {
                        logger.warn("Suspicious local IP address received: {}", apiLocalUri);
                        Configuration config = handler.getThing().getConfiguration();
                        if (config.containsKey(IP_ADDRESS)) {
                            String provided = (String) config.get(IP_ADDRESS);
                            apiLocalUri = builder.host(provided).build();
                            logger.info("Using {} as local url for '{}'", apiLocalUri, thingUID);
                        } else {
                            logger.debug("No alternative ip Address provided, keeping API answer");
                        }
                    }
                } catch (UriBuilderException e) { // Crashed at first URI build
                    logger.warn("API returned a badly formatted local url address for '{}': {}", thingUID,
                            e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid fallback address provided in configuration for '{}' keeping API answer: {}",
                            thingUID, e.getMessage());
                }
            }
            return apiLocalUri != null ? apiLocalUri.toString() : null;
        }).orElse(null);
    }
}
