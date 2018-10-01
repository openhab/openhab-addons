/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model;

import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.slf4j.Logger;

import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

/**
* The {@link ItemKey} class is key for item identification
*
* @author Gabor Bicskei - Initial contribution
*/
public class ItemKey {
    /**
     * Class logger
     */
    private final Logger logger = getLogger(ItemKey.class);

    /**
     * Module key
     */
    private ModuleKey moduleKey;

    /**
     * IO number
     */
    private Integer ioNumber;

    /**
     * Item name if IO number is not applicable
     */
    private String name;

    public ItemKey(ModuleKey moduleKey) {
        this.moduleKey = moduleKey;
    }

    public ItemKey(ModuleKey moduleKey, Integer ioNumber) {
        this.moduleKey = moduleKey;
        this.ioNumber = ioNumber;
    }

    public ItemKey(ModuleKey moduleKey, String name) {
        this.moduleKey = moduleKey;
        this.name = name;
    }

    public ModuleKey getModuleKey() {
        return moduleKey;
    }

    public Integer getIoNumber() {
        return ioNumber;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemKey itemKey = (ItemKey) o;
        return Objects.equals(moduleKey, itemKey.moduleKey) &&
                Objects.equals(ioNumber, itemKey.ioNumber) &&
                Objects.equals(name, itemKey.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleKey, ioNumber, name);
    }

    @Override
    public String toString() {
        return toLabel(moduleKey.getModuleType(), moduleKey.getSerialNumber(), ioNumber);
    }

    public static String toLabel(ModuleType moduleType, SerialNumber serialNumber, Integer ioNumber) {
        StringBuilder sb = new StringBuilder(moduleType.toString()).append(serialNumber.toStringFix6());
        if (ioNumber != null) {
            sb.append("-").append(ioNumber);
        }
        return sb.toString();
    }

    public String toId() {
        StringBuilder sb = new StringBuilder(moduleKey.getModuleType().toString())
                .append("-")
                .append(moduleKey.getSerialNumber().getAddressHex());
        if (ioNumber != null) {
            sb.append("-").append(ioNumber);
        }
        return sb.toString();
    }
}
