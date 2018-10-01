/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.model;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.domintell.internal.protocol.StateChangeListener;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ItemType;

/**
* The {@link Item} class is a Domintell item representation in the binding
*
* @author Gabor Bicskei - Initial contribution
*/
public class Item<T> {
    /**
     * Parent module
     */
    private Module module;

    /**
     * Item identification key
     */
    private ItemKey itemKey;

    /**
     * Item type
     */
    private ItemType type;

    /**
     * Listener for state changes
     */
    private StateChangeListener stateChangeListener;

    /**
     * Item value
     */
    private T value;

    /**
     * Item description
     */
    private Description description;

    public Item(@NonNull ItemKey itemkey, @NonNull Module module, @NonNull ItemType type) {
        this.itemKey = itemkey;
        this.module = module;
        this.type = type;
    }

    public void setStateChangeListener(StateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }

    public Module getModule() {
        return module;
    }

    public String getDescription() {
        ModuleKey moduleKey = itemKey.getModuleKey();
        StringBuilder sb = new StringBuilder(ItemKey.toLabel(moduleKey.getModuleType(), moduleKey.getSerialNumber(), itemKey.getIoNumber()));
        Description descr = description != null ? description: module.getDescription();
        if (descr != null) {
            sb.append(descr.getLocation());
        }
        return sb.toString();
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public ItemType getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public ItemKey getItemKey() {
        return itemKey;
    }

    public String getLabel() {
        return description != null ? description.getName():
                module.getDescription().getName() + (itemKey.getIoNumber() != null ? " " + itemKey.getIoNumber(): "");
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemKey=" + itemKey +
                ", type=" + type +
                ", value=" + value +
                '}';
    }

    public void notifyStateUpdate() {
        if (stateChangeListener != null) {
            stateChangeListener.itemStateChanged(this);
        }
    }
}
