/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoDeviceType;
import org.openhab.io.neeo.internal.models.NeeoThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * This class manages device definitions. Device definitions map openHAB things/channels to NEEO device/capabilities.
 * All device definition changes are saved to a JSON file in the user data folder.
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceDefinitions {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceDefinitions.class);

    /** Cross reference between a ThingUID and a NeeoDevice */
    private final ConcurrentHashMap<NeeoThingUID, NeeoDevice> uidToDevice = new ConcurrentHashMap<>();

    /** The gson used to save/restore device definitions */
    private final Gson gson = NeeoUtil.createNeeoDeviceGsonBuilder().create();

    /** The service context */
    private final ServiceContext context;

    /** The openHAB to NEEO converter */
    private final OpenHabToDeviceConverter converter;

    /** Whether the default is to expose all devices (as accessories) */
    private final boolean exposeAll;

    /** Whether the default is to expose all NEEO bindings devices (as accessories) */
    private final boolean exposeNeeoBinding;

    /** The file we store definitions in */
    private final File file = new File(NeeoConstants.FILENAME_DEVICEDEFINITIONS);

    /**
     * Create the object based on the {@link ServiceContext} and will read the definitions from the {@link #file}
     *
     * @param context the non-null {@link ServiceContext}
     */
    NeeoDeviceDefinitions(ServiceContext context) {
        Objects.requireNonNull(context, "context cannot be null");

        this.context = context;
        this.converter = new OpenHabToDeviceConverter(context);

        exposeAll = context.isExposeAllThings();
        exposeNeeoBinding = context.isExposeNeeoBinding();

        if (file.exists()) {
            try {
                logger.debug("Reading contents of {}", file.getAbsolutePath());
                final byte[] contents = Files.readAllBytes(file.toPath());
                final String json = new String(contents, StandardCharsets.UTF_8);

                // devices can be null on an empty file regardless if Eclipse doesn't think so
                final NeeoDevice[] devices = gson.fromJson(json, NeeoDevice[].class);
                if (devices != null) {
                    for (NeeoDevice device : devices) {
                        // merge to get the latest
                        uidToDevice.put(device.getUid(), device);
                    }
                }
            } catch (JsonParseException | UnsupportedOperationException e) {
                logger.debug("JsonParseException reading {}: {}", file.toPath(), e.getMessage(), e);
            } catch (IOException e) {
                logger.debug("IOException reading {}: {}", file.toPath(), e.getMessage(), e);
            }
        }
    }

    /**
     * Saves the current definitions to the {@link #file}. Any {@link IOException} will be logged and ignored.
     */
    public void save() {
        logger.debug("Saving devices to {}", file.toPath());
        try {
            // ensure full path exists
            file.getParentFile().mkdirs();

            final List<NeeoDevice> devices = new ArrayList<>();

            // filter for only things that are still valid
            final ThingRegistry thingRegistry = context.getThingRegistry();
            for (NeeoDevice device : uidToDevice.values()) {
                if (NeeoConstants.NEEOIO_BINDING_ID.equalsIgnoreCase(device.getUid().getBindingId())) {
                    devices.add(device);
                } else {
                    if (thingRegistry.get(device.getUid().asThingUID()) != null) {
                        devices.add(device);
                    }
                }
            }

            final String json = gson.toJson(devices);
            final byte[] contents = json.getBytes(StandardCharsets.UTF_8);
            Files.write(file.toPath(), contents);
        } catch (IOException e) {
            logger.debug("IOException writing {}: {}", file.toPath(), e.getMessage(), e);
        }
    }

    /**
     * Adds/Replaces the specified device definition and then {@link #save()}
     *
     * @param device the non-null device definition
     */
    public void put(NeeoDevice device) {
        Objects.requireNonNull(device, "device cannot be null");

        uidToDevice.put(device.getUid(), device);
        save();
    }

    /**
     * Removed the device definition for the specified {@link NeeoThingUID}
     *
     * @param uid the non-null uid
     * @return true if found and removed, false otherwise
     */
    public boolean remove(NeeoThingUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final boolean found = uidToDevice.remove(uid) != null;
        if (found) {
            save();
        }
        return found;
    }

    /**
     * Returns a list of {@link NeeoDevice} that have been exposed (where the type isn't {@link NeeoDeviceType#EXCLUDE})
     *
     * @return a non-null, possibly empty list of {@link NeeoDevice}
     */
    public List<NeeoDevice> getExposed() {
        final List<NeeoDevice> devices = new ArrayList<>();
        for (NeeoDevice device : exposeAll || exposeNeeoBinding ? getAllDevices() : uidToDevice.values()) {
            if (device.getExposedChannels().length > 0 && !NeeoDeviceType.EXCLUDE.equals(device.getType())
                    && !device.getType().toString().isEmpty()) {
                devices.add(device);
            }
        }

        return devices;
    }

    /**
     *
     * Checks to see if the specified itemName is bound given the {@link NeeoDeviceKeys}. This method will find any
     * {@link NeeoDevice} that is bound (according to the {@link NeeoDeviceKeys}) and then will determine if the item
     * name has been bound on that {@link NeeoDevice}.
     *
     * @param keys a non-null {@link NeeoDeviceKeys}
     * @param itemName a non-null, non-empty item name to use
     * @return true if bound, false otherwise
     */
    public boolean isBound(NeeoDeviceKeys keys, String itemName) {
        Objects.requireNonNull(keys, "keys cannot be null");
        NeeoUtil.requireNotEmpty(itemName, "itemName must not be empty");

        logger.trace("isBound: {} --- {}", itemName, keys);
        for (NeeoDevice device : uidToDevice.values()) {
            final NeeoThingUID uid = new NeeoThingUID(device.getUid());
            final boolean isBound = keys.isBound(uid);

            logger.trace("isBound(device): {} --- {} --- {} --- {}", uid, itemName, isBound, keys);
            if (isBound) {
                if (device.isExposed(itemName)) {
                    logger.trace("isBound(YES!): {} --- {} --- {} --- {}", uid, itemName, isBound, keys);
                    return true;
                }
            }
        }

        logger.trace("isBound(NO): {} --- {}", itemName, keys);
        return false;
    }

    /**
     * Gets the list of {@link NeeoDevice} and {@link NeeoDeviceChannel} that are currently bound. This list will
     * include ALL bound devices/channels
     *
     * @param keys a non-null {@link NeeoDeviceKeys}
     * @return a non-null, possibly empty list
     */
    public List<Map.Entry<NeeoDevice, NeeoDeviceChannel>> getBound(NeeoDeviceKeys keys) {
        return getBound(keys, null);
    }

    /**
     * Gets the list of {@link NeeoDevice} and {@link NeeoDeviceChannel} that are currently bound for the given itemName
     * (or all if the itemName is null)
     *
     * @param keys a non-null {@link NeeoDeviceKeys}
     * @param itemName a possibly null, possibly empty item name to use
     * @return a non-null, possibly empty list
     */
    public List<Map.Entry<NeeoDevice, NeeoDeviceChannel>> getBound(NeeoDeviceKeys keys, @Nullable String itemName) {
        Objects.requireNonNull(keys, "keys cannot be null");

        final List<Map.Entry<NeeoDevice, NeeoDeviceChannel>> channels = new ArrayList<>();
        for (NeeoDevice device : uidToDevice.values()) {
            if (keys.isBound(device.getUid())) {
                for (NeeoDeviceChannel channel : device.getExposedChannels()) {
                    if (itemName == null || itemName.equalsIgnoreCase(channel.getItemName())) {
                        channels.add(new AbstractMap.SimpleImmutableEntry<>(device, channel));
                    }
                }
            }
        }
        return channels;
    }

    /**
     * Gets the {@link NeeoDevice} for the given {@link NeeoThingUID}. If no definition has been created yet, the
     * definition
     * will be created (but not saved) and returned. Note that any saved definition will be merged with the latest
     * openHAB thing definition to pick up on any channel changes (added or removed)
     *
     * @param uid the non-null uid
     * @return the neeo device or null if unknown (or a neeo uid)
     */
    @Nullable
    public NeeoDevice getDevice(NeeoThingUID uid) {
        Objects.requireNonNull(uid, "uid cannot be null");

        final NeeoDevice device = uidToDevice.get(uid);
        if (device == null) {
            final Thing thing = context.getThingRegistry().get(uid.asThingUID());
            if (thing == null) {
                logger.debug("Unknown thing uid {}", uid);
                return null;
            } else {
                return converter.convert(thing);
            }
        } else {
            return device;
        }
    }

    /**
     * Get's all things defined in openHAB. This will include virtual devices defined by the user plus all things in the
     * registry (merged with our definition)
     *
     * @return a non-null, possibly empty list of {@link NeeoDevice}
     */
    public List<NeeoDevice> getAllDevices() {
        final List<NeeoDevice> devices = new ArrayList<>();
        for (Entry<NeeoThingUID, NeeoDevice> entry : uidToDevice.entrySet()) {
            if (NeeoConstants.NEEOIO_BINDING_ID.equalsIgnoreCase(entry.getKey().getBindingId())) {
                devices.add(entry.getValue());
            } else {
                final Thing thing = context.getThingRegistry().get(entry.getKey().asThingUID());
                if (thing == null) {
                    logger.debug("Thing {} doesn't exist in registry anymore", entry.getKey());
                } else {
                    final NeeoDevice mergedDevice = entry.getValue().merge(context);
                    if (mergedDevice != null) {
                        devices.add(mergedDevice);
                    }
                }
            }
        }

        for (Thing thing : context.getThingRegistry().getAll()) {
            if (!uidToDevice.containsKey(new NeeoThingUID(thing.getUID()))) {
                final NeeoDevice device = converter.convert(thing);
                if (device != null) {
                    devices.add(device);
                }
            }
        }

        return devices;
    }

    /**
     * Returns a {@link NeeoDeviceChannel} that represents the given itemname (or null if itemname is not found)
     *
     * @param itemName a possibly empty, possibly null item name
     * @return a {@link NeeoDeviceChannel} representing the item name or null if not found
     */
    public @Nullable List<NeeoDeviceChannel> getNeeoDeviceChannel(String itemName) {
        return converter.getNeeoDeviceChannel(itemName);
    }
}
