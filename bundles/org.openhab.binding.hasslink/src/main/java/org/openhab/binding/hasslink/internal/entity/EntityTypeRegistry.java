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
package org.openhab.binding.hasslink.internal.entity;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.entity.impl.AlarmControlPanelEntity;
import org.openhab.binding.hasslink.internal.entity.impl.BinarySensorEntity;
import org.openhab.binding.hasslink.internal.entity.impl.ButtonEntity;
import org.openhab.binding.hasslink.internal.entity.impl.CalendarEntity;
import org.openhab.binding.hasslink.internal.entity.impl.CameraEntity;
import org.openhab.binding.hasslink.internal.entity.impl.ClimateEntity;
import org.openhab.binding.hasslink.internal.entity.impl.CoverEntity;
import org.openhab.binding.hasslink.internal.entity.impl.DateEntity;
import org.openhab.binding.hasslink.internal.entity.impl.DateTimeEntity;
import org.openhab.binding.hasslink.internal.entity.impl.DeviceTrackerEntity;
import org.openhab.binding.hasslink.internal.entity.impl.EventEntity;
import org.openhab.binding.hasslink.internal.entity.impl.FanEntity;
import org.openhab.binding.hasslink.internal.entity.impl.GenericEntity;
import org.openhab.binding.hasslink.internal.entity.impl.HumidifierEntity;
import org.openhab.binding.hasslink.internal.entity.impl.ImageEntity;
import org.openhab.binding.hasslink.internal.entity.impl.LawnMowerEntity;
import org.openhab.binding.hasslink.internal.entity.impl.LightEntity;
import org.openhab.binding.hasslink.internal.entity.impl.LockEntity;
import org.openhab.binding.hasslink.internal.entity.impl.MediaPlayerEntity;
import org.openhab.binding.hasslink.internal.entity.impl.NumberEntity;
import org.openhab.binding.hasslink.internal.entity.impl.RemoteEntity;
import org.openhab.binding.hasslink.internal.entity.impl.SceneEntity;
import org.openhab.binding.hasslink.internal.entity.impl.ScriptEntity;
import org.openhab.binding.hasslink.internal.entity.impl.SelectEntity;
import org.openhab.binding.hasslink.internal.entity.impl.SensorEntity;
import org.openhab.binding.hasslink.internal.entity.impl.SirenEntity;
import org.openhab.binding.hasslink.internal.entity.impl.SwitchEntity;
import org.openhab.binding.hasslink.internal.entity.impl.TextEntity;
import org.openhab.binding.hasslink.internal.entity.impl.TimeEntity;
import org.openhab.binding.hasslink.internal.entity.impl.UpdateEntity;
import org.openhab.binding.hasslink.internal.entity.impl.VacuumEntity;
import org.openhab.binding.hasslink.internal.entity.impl.ValveEntity;
import org.openhab.binding.hasslink.internal.entity.impl.WaterHeaterEntity;
import org.openhab.binding.hasslink.internal.entity.impl.WeatherEntity;

/**
 * The {@link EntityTypeRegistry} class maintains a registry of entity types and their corresponding implementations.
 * It allows for the retrieval of entity implementations based on their type, providing a default implementation
 * for unrecognized types.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class EntityTypeRegistry {

    private final Map<String, EntityType> entities = new HashMap<>();
    private final EntityType genericEntity = new GenericEntity();

    public EntityTypeRegistry() {
        register(new AlarmControlPanelEntity());
        register(new BinarySensorEntity());
        register(new ButtonEntity());
        register(new CalendarEntity());
        register(new CameraEntity());
        register(new ClimateEntity());
        register(new CoverEntity());
        register(new DateEntity());
        register(new DateTimeEntity());
        register(new GenericEntity());
        register(new DeviceTrackerEntity());
        register(new EventEntity());
        register(new FanEntity());
        register(new HumidifierEntity());
        register(new ImageEntity());
        register(new LawnMowerEntity());
        register(new LightEntity());
        register(new LockEntity());
        register(new MediaPlayerEntity());
        register(new NumberEntity());
        register(new RemoteEntity());
        register(new SceneEntity());
        register(new ScriptEntity());
        register(new SelectEntity());
        register(new SensorEntity());
        register(new SirenEntity());
        register(new SwitchEntity());
        register(new TextEntity());
        register(new TimeEntity());
        register(new UpdateEntity());
        register(new VacuumEntity());
        register(new ValveEntity());
        register(new WaterHeaterEntity());
        register(new WeatherEntity());
    }

    private void register(EntityType entity) {
        entities.put(entity.getType(), entity);
    }

    public EntityType getByType(@Nullable String type) {
        if (type == null) {
            return genericEntity;
        }
        return entities.getOrDefault(type, genericEntity);
    }

    public EntityType getByEntityId(String entityId) {
        String type = extractTypeFromEntityId(entityId);
        return getByType(type);
    }

    public EntityType getGeneric() {
        return genericEntity;
    }

    private @Nullable String extractTypeFromEntityId(String entityId) {
        int dotIndex = entityId.indexOf('.');
        if (dotIndex == -1) {
            return null;
        }

        return entityId.substring(0, dotIndex);
    }
}
