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
package org.openhab.io.yamlcomposer.internal.dynamic;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingRegistryChangeListener;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.dto.ThingDTO;
import org.openhab.core.thing.dto.ThingDTOMapper;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides Thing Registry entities to dynamic sources and notifies listeners of discrete Thing changes,
 * including enable/disable transitions via status events.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicSourceProvider.class, ThingRegistrySourceProvider.class, EventSubscriber.class })
public class ThingRegistrySourceProvider
        implements DynamicSourceProvider<Thing>, ThingRegistryChangeListener, EventSubscriber {
    private static final String SOURCE_NAME = "THINGS";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ThingRegistry thingRegistry;
    private volatile Consumer<EntityChange> onChangeListener = change -> {
    };

    @Activate
    public ThingRegistrySourceProvider(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
        thingRegistry.addRegistryChangeListener(this);
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public boolean supportsSource(String source) {
        return SOURCE_NAME.equals(source);
    }

    @Override
    public Collection<Thing> getAllEntities() {
        return thingRegistry.getAll();
    }

    @Override
    public String getKey(Thing entity) {
        return entity.getUID().toString();
    }

    @Override
    public Map<String, @Nullable Object> adaptToMap(Thing thing) {
        ThingDTO dto = ThingDTOMapper.map(thing);
        Map<String, @Nullable Object> dtoMap = OBJECT_MAPPER.convertValue(dto,
                new TypeReference<Map<String, @Nullable Object>>() {
                });

        Map<String, @Nullable Object> adapted = new LinkedHashMap<>();

        // Enrich with enabled status, id, and lowercase uid alias
        adapted.put("enabled", thing.isEnabled());
        adapted.put("id", thing.getUID().getId());
        adapted.put("uid", thing.getUID().toString());

        dtoMap.forEach((key, value) -> {
            if ("configuration".equals(key)) {
                // Rename thing-level 'configuration' to 'config'
                adapted.put("config", value);
            } else if ("channels".equals(key) && value instanceof List<?> channelList) {
                adapted.put("channels", adaptChannels(channelList));
            } else {
                adapted.put(key, value);
            }
        });

        return RegistryEntityUtils.immutableMap(adapted);
    }

    private Map<String, @Nullable Object> adaptChannels(List<?> channelList) {
        Map<String, @Nullable Object> channelMap = new LinkedHashMap<>();

        for (Object chObj : channelList) {
            if (chObj instanceof Map<?, ?> chMap) {
                Map<String, @Nullable Object> adaptedChannel = new LinkedHashMap<>();
                String channelId = null;

                for (Map.Entry<?, ?> entry : chMap.entrySet()) {
                    String chKey = String.valueOf(entry.getKey());
                    Object chVal = entry.getValue();

                    if (chVal == null) {
                        continue;
                    }

                    if ("id".equals(chKey) && chVal instanceof String idStr) {
                        channelId = idStr;
                        adaptedChannel.put("id", idStr);
                        continue;
                    }

                    if ("uid".equals(chKey) && chVal instanceof String uidStr) {
                        adaptedChannel.put("uid", uidStr);
                        adaptedChannel.put("UID", uidStr); // Alias for template convenience
                        continue;
                    }

                    if ("configuration".equals(chKey)) {
                        // Rename channel-level 'configuration' to 'config'
                        adaptedChannel.put("config", chVal);
                    } else {
                        adaptedChannel.put(chKey, chVal);
                    }
                }

                if (channelId != null) {
                    channelMap.put(channelId, adaptedChannel);
                }
            }
        }

        return channelMap;
    }

    @Override
    public void setOnChangeListener(Consumer<EntityChange> listener) {
        this.onChangeListener = listener;
    }

    @Override
    public void added(Thing element) {
        onChangeListener.accept(new EntityChange(SOURCE_NAME, null, adaptToMap(element)));
    }

    @Override
    public void removed(Thing element) {
        onChangeListener.accept(new EntityChange(SOURCE_NAME, adaptToMap(element), null));
    }

    @Override
    public void updated(Thing oldElement, Thing element) {
        onChangeListener.accept(new EntityChange(SOURCE_NAME, adaptToMap(oldElement), adaptToMap(element)));
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return Set.of(ThingStatusInfoChangedEvent.TYPE);
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ThingStatusInfoChangedEvent statusEvent) {
            ThingStatusInfo statusInfo = statusEvent.getStatusInfo();
            ThingStatusInfo oldStatusInfo = statusEvent.getOldStatusInfo();

            boolean wasDisabled = oldStatusInfo.getStatusDetail() == ThingStatusDetail.DISABLED;
            boolean isDisabled = statusInfo.getStatusDetail() == ThingStatusDetail.DISABLED;

            if (wasDisabled != isDisabled) {
                ThingUID thingUID = statusEvent.getThingUID();
                Thing thing = thingRegistry.get(thingUID);
                if (thing != null) {
                    Map<String, @Nullable Object> currentMap = adaptToMap(thing);
                    Map<String, @Nullable Object> previousMap = new LinkedHashMap<>(currentMap);
                    previousMap.put("enabled", !thing.isEnabled());
                    onChangeListener.accept(new EntityChange(SOURCE_NAME, previousMap, currentMap));
                }
            }
        }
    }

    @Deactivate
    public void deactivate() {
        thingRegistry.removeRegistryChangeListener(this);
    }
}
