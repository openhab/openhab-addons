/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Dynamic provider of state options for VehicleHandler.
 *
 * @author Gregory Moyer - Initial contribution
 * @author Gaël L'hopital - Copied as-is in VolvoOnCall binding
 */
@Component(service = { DynamicStateDescriptionProvider.class, VehicleStateDescriptionProvider.class })
@NonNullByDefault
public class VehicleStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {

    @Reference
    protected void setChannelTypeI18nLocalizationService(
            final ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.channelTypeI18nLocalizationService = channelTypeI18nLocalizationService;
    }

    protected void unsetChannelTypeI18nLocalizationService(
            final ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        this.channelTypeI18nLocalizationService = null;
    }
}
