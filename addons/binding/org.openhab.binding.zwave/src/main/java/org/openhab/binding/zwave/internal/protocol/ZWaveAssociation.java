/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * @author Chris Jackson - Initial contribution
 *
 */
@XStreamAlias("multiAssociationCommandClass")
public class ZWaveAssociation {
    private int node;
    private int endpoint;

    public ZWaveAssociation(int node) {
        this.node = node;
        this.endpoint = 0;
    }

    public ZWaveAssociation(int node, int endpoint) {
        this.node = node;
        this.endpoint = endpoint;
    }

    public int getNode() {
        return node;
    }

    public int getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean equals(Object checker) {
        ZWaveAssociation assoc = (ZWaveAssociation) checker;
        if (this.node == assoc.node && this.endpoint == assoc.endpoint) {
            return true;
        }
        return false;
    }
}