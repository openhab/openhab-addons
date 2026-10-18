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
package org.openhab.binding.bluetooth.generic.internal;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.bluetooth.gattparser.BluetoothGattParserFactory;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GenericBluetoothHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.bluetooth.generic", service = ThingHandlerFactory.class)
public class GenericBluetoothHandlerFactory extends BaseThingHandlerFactory {

    /**
     * Binding configuration key for the folder holding custom GATT specification XML files.
     * A relative value is resolved against the openHAB config folder ({@link OpenHAB#getConfigFolder()},
     * e.g. {@code conf/} or {@code /etc/openhab}), which keeps it portable across operating systems;
     * an absolute value is used as-is. The folder must contain {@code characteristic/} and/or
     * {@code service/} sub-folders with XML files in the Bluetooth SIG GATT XML format.
     */
    private static final String CONFIG_GATT_EXTENSIONS_FOLDER = "gattExtensionsFolder";

    /**
     * Default (relative) location for the custom GATT extensions folder when not configured.
     */
    private static final String DEFAULT_GATT_EXTENSIONS_FOLDER = "gatt-extensions";

    private final Logger logger = LoggerFactory.getLogger(GenericBluetoothHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(GenericBindingConstants.THING_TYPE_GENERIC);

    private final CharacteristicChannelTypeProvider channelTypeProvider;

    @Activate
    public GenericBluetoothHandlerFactory(@Reference CharacteristicChannelTypeProvider channelTypeProvider,
            Map<String, Object> config) {
        this.channelTypeProvider = channelTypeProvider;
        loadGattExtensions(config);
    }

    /**
     * Loads custom GATT specifications from the configured folder (if present) into the shared
     * default parser. The parser is a singleton used by both the channel-type provider and the
     * handlers, so loading here — once, before any handler is created — makes custom characteristics
     * known for both channel building and value parsing.
     */
    private void loadGattExtensions(Map<String, Object> config) {
        File folder = resolveGattExtensionsFolder(config, OpenHAB.getConfigFolder());
        if (!folder.isDirectory()) {
            logger.debug("No custom GATT extensions folder at {}; skipping.", folder);
            return;
        }
        try {
            BluetoothGattParserFactory.getDefault().loadExtensionsFromFolder(folder.getAbsolutePath());
            logger.info("Loaded custom GATT extensions from {}", folder);
        } catch (RuntimeException e) {
            logger.warn("Failed to load custom GATT extensions from {}", folder, e);
        }
    }

    /**
     * Resolves the custom GATT extensions folder from the binding configuration. A blank/missing
     * value falls back to {@link #DEFAULT_GATT_EXTENSIONS_FOLDER}. A relative location is resolved
     * against {@code configFolder} (the openHAB config folder) so the same configuration is portable
     * across operating systems; an absolute location is returned as-is.
     *
     * @param config the binding configuration
     * @param configFolder the openHAB configuration folder to resolve relative locations against
     * @return the folder to load custom GATT specifications from
     */
    static File resolveGattExtensionsFolder(Map<String, Object> config, String configFolder) {
        Object configured = config.get(CONFIG_GATT_EXTENSIONS_FOLDER);
        String location = configured instanceof String s && !s.isBlank() ? s.trim() : DEFAULT_GATT_EXTENSIONS_FOLDER;
        File folder = new File(location);
        return folder.isAbsolute() ? folder : new File(configFolder, location);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (GenericBindingConstants.THING_TYPE_GENERIC.equals(thingTypeUID)) {
            return new GenericBluetoothHandler(thing, channelTypeProvider);
        }

        return null;
    }
}
