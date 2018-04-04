/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.processor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.io.imperihome.internal.model.Room;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The device registry stores created devices by ID.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class DeviceRegistry implements Iterable<AbstractDevice> {

    private final Logger logger = LoggerFactory.getLogger(DeviceRegistry.class);

    private final Map<String, AbstractDevice> devices;
    private Set<Room> rooms;

    public DeviceRegistry() {
        devices = new ConcurrentHashMap<>();
    }

    public AbstractDevice getDevice(String deviceId) {
        return devices.get(deviceId);
    }

    public Map<String, AbstractDevice> getDevices() {
        return new HashMap<>(devices);
    }

    public Collection<Room> getRooms() {
        return new HashSet<>(rooms);
    }

    public boolean hasDevices() {
        return !devices.isEmpty();
    }

    public boolean hasDevice(String deviceId) {
        return devices.containsKey(deviceId);
    }

    public void add(AbstractDevice device) {
        //Workaround for Eclipse SH bug: ignore add-event for same item
        //https://github.com/eclipse/smarthome/issues/3160
        if (devices.containsKey(device.getId())) {
            logger.warn("Ignoring duplicate device #{}, name={}, item={}", device.getId(), device.getName(), device.getItemName());
            return;
        }

        devices.put(device.getId(), device);
        updateRooms();

        logger.debug("Device {} added, registry now contains {} total", device.getName(), devices.size());
    }

    public AbstractDevice remove(String deviceId) {
        AbstractDevice removed = devices.remove(deviceId);
        if (removed != null) {
            updateRooms();
            logger.debug("Device {} removed, registry now contains {} total", removed.getName(), devices.size());
        }
        return removed;
    }

    @Override
    public Iterator<AbstractDevice> iterator() {
        return devices.values().iterator();
    }

    public void clear() {
        for (AbstractDevice device : devices.values()) {
            device.destroy();
        }
        devices.clear();

        if (rooms != null) {
            rooms.clear();
        }

        logger.debug("Device registry cleared");
    }

    private void updateRooms() {
        Set<Room> newRooms = new HashSet<>();
        for (AbstractDevice device : devices.values()) {
            Room room = new Room();
            room.setId(device.getRoom());
            room.setName(device.getRoomName());
            newRooms.add(room);
        }
        rooms = newRooms;
    }

}
