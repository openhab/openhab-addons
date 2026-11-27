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
package org.openhab.io.homekit.internal.accessories;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.io.homekit.internal.HomekitAccessoryType;
import org.openhab.io.homekit.internal.HomekitAccessoryUpdater;
import org.openhab.io.homekit.internal.HomekitOHItemProxy;
import org.openhab.io.homekit.internal.HomekitSettings;
import org.openhab.io.homekit.internal.HomekitTaggedItem;

import io.github.hapjava.characteristics.impl.lightbulb.HueCharacteristic;
import io.github.hapjava.characteristics.impl.lightbulb.SaturationCharacteristic;

/**
 * Tests commands for explicitly configured lighting group characteristics.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
class HomekitLightbulbImplTest {
    @ParameterizedTest
    @CsvSource({ "true,false", "false,true", "true,true" })
    void colorGroupCharacteristicsSendCommandsToMembers(boolean writeHue, boolean writeSaturation) throws Exception {
        GroupItem lighting = new GroupItem("lighting");
        SwitchItem power = new SwitchItem("power");
        GroupItem colors = new GroupItem("colors", new ColorItem("base"));
        colors.setState(new HSBType("30,40,60"));
        colors.addMember(new ColorItem("first"));
        colors.addMember(new ColorItem("second"));
        lighting.addMember(power);
        lighting.addMember(colors);
        lighting.setLabel("Lighting");

        MetadataRegistry metadata = mock(MetadataRegistry.class);
        MetadataKey powerKey = new MetadataKey("homekit", power.getName());
        MetadataKey colorsKey = new MetadataKey("homekit", colors.getName());
        when(metadata.get(powerKey)).thenReturn(new Metadata(powerKey, "Lighting.OnState", Map.of()));
        when(metadata.get(colorsKey)).thenReturn(new Metadata(colorsKey, "Lighting.Hue,Lighting.Saturation", Map.of()));
        EventPublisher publisher = mock(EventPublisher.class);
        colors.setEventPublisher(publisher);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        doReturn(future).when(scheduler).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS));

        try (var pools = mockStatic(ThreadPoolManager.class)) {
            pools.when(() -> ThreadPoolManager.getScheduledPool(ThreadPoolManager.THREAD_POOL_NAME_COMMON))
                    .thenReturn(scheduler);
            var accessory = HomekitAccessoryFactory.create(
                    new HomekitTaggedItem(new HomekitOHItemProxy(lighting), HomekitAccessoryType.LIGHTBULB, Map.of()),
                    metadata, mock(HomekitAccessoryUpdater.class), new HomekitSettings());

            if (writeHue) {
                accessory.getCharacteristic(HueCharacteristic.class).orElseThrow().setValue(120.0);
            }
            if (writeSaturation) {
                accessory.getCharacteristic(SaturationCharacteristic.class).orElseThrow().setValue(70.0);
            }
            if (!(writeHue && writeSaturation)) {
                ArgumentCaptor<Runnable> pending = ArgumentCaptor.forClass(Runnable.class);
                verify(scheduler).schedule(pending.capture(), anyLong(), eq(TimeUnit.MILLISECONDS));
                pending.getValue().run();
            }

            ArgumentCaptor<ItemCommandEvent> commands = ArgumentCaptor.forClass(ItemCommandEvent.class);
            verify(publisher, times(2)).post(commands.capture());
            assertEquals(List.of("first", "second"),
                    commands.getAllValues().stream().map(ItemCommandEvent::getItemName).toList());
            HSBType expected = new HSBType((writeHue ? "120" : "30") + "," + (writeSaturation ? "70" : "40") + ",60");
            commands.getAllValues().forEach(command -> {
                assertEquals(expected, command.getItemCommand());
                assertEquals(HomekitTaggedItem.HOMEKIT_SOURCE, command.getSource());
            });
        }
    }
}
