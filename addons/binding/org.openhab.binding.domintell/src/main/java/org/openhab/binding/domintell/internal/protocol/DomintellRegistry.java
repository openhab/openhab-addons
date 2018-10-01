/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol;

import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroupType;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
* The {@link DomintellRegistry} class is a registry for all Domintell modules and groups
*
* @author Gabor Bicskei - Initial contribution
*/
public class DomintellRegistry {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellRegistry.class);

    /**
     * Module cache
     */
    private final HashMap<ModuleKey, Module> moduleCache = new HashMap<>();

    /**
     * Item groups
     */
    private final HashMap<ItemGroupType, ItemGroup> itemGroups = new HashMap<>();

    /**
     * Configuration event listener
     */
    private ConfigurationEventHandler configEventListener;

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    public DomintellRegistry(DomintellConnection connection) {
        this.connection = connection;
    }

    /**
     * Setter.
     *
     * @param configEventListener Config event listener instance
     */
    void setConfigEventListener(ConfigurationEventHandler configEventListener) {
        this.configEventListener = configEventListener;
    }

    /**
     * Retrieves Domintell module from the cache. The module is created if missing.
     *
     * @param serialNumber Module serialNumber
     * @return Requested module
     */
    public Module getDomintellModule(ModuleType moduleType, SerialNumber serialNumber) {
        Class<? extends Module> moduleClass = moduleType.getClazz();
        ModuleKey key = new ModuleKey(moduleType, serialNumber);
        Module module = moduleCache.get(key);
        if (module == null) {
            //missing the module - creating
            try {
                Constructor<? extends Module> constructor = moduleClass.getConstructor(DomintellConnection.class, SerialNumber.class);
                module = constructor.newInstance(connection, serialNumber);
            } catch (Exception e) {
                logger.error("Unable to instantiate module: {}", serialNumber, e);
            }
            if (module != null && module.getConfigChangeListener() == null) {
                configEventListener.handleNewDiscoverable(module);
            }
        }
        if (!(moduleClass.isInstance(module))) {
            logger.error("Invalid module type found at given serialNumber: {}", serialNumber);
        }
        moduleCache.put(key, module);
        return module;
    }

    /**
     * Find or create new item group by type
     *
     * @param type Group type
     * @return Requested item group
     */
    public ItemGroup getItemGroup(ItemGroupType type) {
        ItemGroup itemGroup = itemGroups.get(type);
        if (itemGroup == null) {
            itemGroup = new ItemGroup(connection, type);
            itemGroups.put(type, itemGroup);
            configEventListener.handleNewDiscoverable(itemGroup);
        }
        return itemGroup;
    }

    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(moduleCache.values());
    }

    Collection<ItemGroup> getGroups() {
        return Collections.unmodifiableCollection(itemGroups.values());
    }
}
