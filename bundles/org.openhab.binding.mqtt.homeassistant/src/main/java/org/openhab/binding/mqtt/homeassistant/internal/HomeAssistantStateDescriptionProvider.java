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
package org.openhab.binding.mqtt.homeassistant.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.type.DynamicCommandDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.util.BundleResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This subclass exists solely so that the I18n provider can find the correct bundle
 * for translations.
 *
 * @author Cody Cutrer - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, DynamicCommandDescriptionProvider.class,
        HomeAssistantStateDescriptionProvider.class })
@NonNullByDefault
public class HomeAssistantStateDescriptionProvider extends MqttChannelStateDescriptionProvider {
    @Activate
    public HomeAssistantStateDescriptionProvider(@Reference TranslationProvider i18nProvider,
            @Reference BundleResolver bundleResolver) {
        super(i18nProvider, bundleResolver);
    }
}
