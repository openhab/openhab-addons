/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.echonetlite.internal;

import java.nio.ByteBuffer;
import java.util.HashSet;

import org.openhab.core.types.State;

/**
 * @author Michael Barker - Initial contribution
 */
public abstract class EchonetObject {

    protected final InstanceKey instanceKey;
    protected final HashSet<Epc> pendingGets = new HashSet<>();

    public EchonetObject(final InstanceKey instanceKey, final Epc initialProperty) {
        this.instanceKey = instanceKey;
        pendingGets.add(initialProperty);
    }

    public InstanceKey instanceKey() {
        return instanceKey;
    }

    public void applyResponse(InstanceKey sourceInstanceKey, Esv esv, final int epcCode, final int pdc,
            final ByteBuffer edt) {
    }

    public boolean buildPollMessage(final EchonetMessageBuilder messageBuilder, final ShortSupplier tid, long nowMs) {
        if (pendingGets.isEmpty()) {
            return false;
        }

        messageBuilder.start(tid.getAsShort(), EchonetLiteBindingConstants.MANAGEMENT_CONTROLLER_KEY, instanceKey(),
                Esv.Get);

        for (Epc pendingProperty : pendingGets) {
            messageBuilder.appendEpcRequest(pendingProperty.code());
        }

        return true;
    }

    public boolean buildUpdateMessage(final EchonetMessageBuilder messageBuilder, final ShortSupplier tid,
            final long nowMs) {
        return false;
    }

    public void refreshAll(long nowMs) {
    }

    public String toString() {
        return "ItemBase{" + "instanceKey=" + instanceKey + ", pendingProperties=" + pendingGets + '}';
    }

    public void update(String channelId, State state) {
    }

    public void removed() {
    }

    public void refresh(String channelId) {
    }
}
