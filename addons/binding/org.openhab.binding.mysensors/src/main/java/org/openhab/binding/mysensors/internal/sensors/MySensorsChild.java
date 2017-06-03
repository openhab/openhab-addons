/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.sensors;

import java.util.Date;

/**
 * Every thing/node may have one ore more childs in the MySensors context.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsChild<T> {

    private Integer childId = 0;
    private T childValue = null;

    private Date childLastUpdate = null;

    public MySensorsChild(int childId, T initialValue) {
        this.childId = childId;
        this.childValue = initialValue;
    }

    public int getChildId() {
        return childId;
    }

    public void setChildValue(T newValue) {
        childLastUpdate = new Date();
        childValue = newValue;
    }

    public T getChildValue() {
        return childValue;
    }

    @Override
    public String toString() {
        return "MySensorsChild [childId=" + childId + ", nodeValue=" + childValue + "]";
    }

}
