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
package org.openhab.binding.mercedesme.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.MercedesMeStateOptionProvider;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/**
 * {@link MercedesMeDynamicStateDescriptionProviderMock} Mock to collect StatePattern settings
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MercedesMeDynamicStateDescriptionProviderMock<V> extends MercedesMeStateOptionProvider {
    public MercedesMeDynamicStateDescriptionProviderMock(EventPublisher eventPublisher,
            ItemChannelLinkRegistry itemChannelLinkRegistry,
            ChannelTypeI18nLocalizationService channelTypeI18nLocalizationService) {
        super(eventPublisher, itemChannelLinkRegistry, channelTypeI18nLocalizationService);
    }

    public Map<String, String> patternMap = new HashMap<>();

    @Override
    public void setStatePattern(ChannelUID channelUID, String pattern) {
        patternMap.put(channelUID.toString(), pattern);
    }
}
