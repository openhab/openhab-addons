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

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.MercedesMeCommandOptionProvider;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.CommandOption;

/**
 * {@link MercedesMeCommandOptionProviderMock} Mock to collect CommandOption settings
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MercedesMeCommandOptionProviderMock extends MercedesMeCommandOptionProvider {
    public Map<String, List<CommandOption>> commands = new HashMap<>();

    public MercedesMeCommandOptionProviderMock() {
        super(mock(EventPublisher.class), mock(ItemChannelLinkRegistry.class),
                mock(ChannelTypeI18nLocalizationService.class));
    }

    @Override
    public void setCommandOptions(ChannelUID cuid, List<CommandOption> col) {
        commands.put(cuid.toString(), col);
    }

    public List<CommandOption> getCommandList(String channel) {
        List<CommandOption> commandList = commands.get(channel);
        if (commandList != null) {
            return commandList;
        }
        return List.of();
    }
}
