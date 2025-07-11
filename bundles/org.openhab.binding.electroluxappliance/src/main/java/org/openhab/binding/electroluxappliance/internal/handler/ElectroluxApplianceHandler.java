/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.electroluxappliance.internal.handler;

import static org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceBindingConstants.CHANNEL_STATUS;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.electroluxappliance.internal.ElectroluxApplianceConfiguration;
import org.openhab.binding.electroluxappliance.internal.api.ElectroluxGroupAPI;
import org.openhab.binding.electroluxappliance.internal.dto.ApplianceDTO;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElectroluxApplianceHandler} is
 *
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public abstract class ElectroluxApplianceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ElectroluxApplianceHandler.class);

    private ElectroluxApplianceConfiguration config = new ElectroluxApplianceConfiguration();

    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    public ElectroluxApplianceHandler(Thing thing, @Reference TranslationProvider translationProvider,
            @Reference LocaleProvider localeProvider) {
        super(thing);
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
    }

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Command received: {} on channelID: {}", command, channelUID);
        if (CHANNEL_STATUS.equals(channelUID.getId()) || command instanceof RefreshType) {
            final Bridge bridge = getBridge();
            if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
                bridgeHandler.handleCommand(channelUID, command);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ElectroluxApplianceConfiguration.class);
        if (config.getSerialNumber().isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getLocalizedText("error.electroluxappliance.all-devices.missing-serial-number"));
        } else {
            updateStatus(ThingStatus.UNKNOWN);

            // The bridge updates, then attempts to read the config, this fails, then refreshes the token and try's
            // again
            // while this is going on this kicks in where the DTO data is not yet read, so this allows for these initial
            // delays.
            scheduler.schedule(() -> {
                update();
                Map<String, String> properties = refreshProperties();
                updateProperties(properties);
            }, 5, TimeUnit.SECONDS);
        }
    }

    public void update() {
        ApplianceDTO dto = getApplianceDTO();
        if (dto != null) {
            update(dto);
        } else {
            logger.warn("{}", getLocalizedText("warning.electroluxappliance.application-dto-null"));
        }
    }

    protected @Nullable ElectroluxGroupAPI getElectroluxGroupAPI() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
            return bridgeHandler.getElectroluxDeltaAPI();
        }
        return null;
    }

    protected @Nullable ApplianceDTO getApplianceDTO() {
        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof ElectroluxApplianceBridgeHandler bridgeHandler) {
            return bridgeHandler.getElectroluxApplianceThings().get(config.getSerialNumber());
        }
        return null;
    }

    public abstract void update(@Nullable ApplianceDTO dto);

    public abstract Map<String, String> refreshProperties();

    protected ElectroluxApplianceConfiguration getApplianceConfig() {
        return config;
    }
}
