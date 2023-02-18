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
package org.openhab.binding.echonetlite.internal;

import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.DEFAULT_POLL_INTERVAL_MS;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.DEFAULT_RETRY_TIMEOUT_MS;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetProfileNode extends EchonetObject implements EchonetDeviceListener {

    private final Consumer<EchonetDevice> newDeviceListener;
    private final EchonetDiscoveryListener echonetDiscoveryListener;
    private long lastPollMs = 0;

    public EchonetProfileNode(final InstanceKey instanceKey, Consumer<EchonetDevice> newDeviceListener,
            EchonetDiscoveryListener echonetDiscoveryListener) {
        super(instanceKey, Epc.NodeProfile.SELF_NODE_INSTANCE_LIST_S);
        this.newDeviceListener = newDeviceListener;
        this.echonetDiscoveryListener = echonetDiscoveryListener;
        setTimeouts(DEFAULT_POLL_INTERVAL_MS, DEFAULT_RETRY_TIMEOUT_MS);
    }

    @Override
    public void applyProperty(InstanceKey sourceInstanceKey, Esv esv, int epcCode, int pdc, ByteBuffer edt) {
        final Epc epc = Epc.lookup(instanceKey().klass.groupCode(), instanceKey().klass.classCode(), epcCode);

        if (EchonetClass.NODE_PROFILE == sourceInstanceKey.klass && Epc.NodeProfile.SELF_NODE_INSTANCE_LIST_S == epc) {
            final int selfNodeInstanceCount = edt.get() & 0xFF;

            for (int i = 0; i < selfNodeInstanceCount && edt.hasRemaining(); i++) {
                final byte groupCode = edt.get();
                final byte classCode = edt.get();
                final byte instance = edt.get();
                final EchonetClass itemClass = EchonetClassIndex.INSTANCE.lookup(groupCode, classCode);

                final InstanceKey newItemKey = new InstanceKey(sourceInstanceKey.address, itemClass, instance);
                final EchonetDevice discoveredDevice = new EchonetDevice(newItemKey, this);
                discoveredDevice.setTimeouts(DEFAULT_POLL_INTERVAL_MS, DEFAULT_RETRY_TIMEOUT_MS);
                newDeviceListener.accept(discoveredDevice);
            }
        }
    }

    @Override
    public boolean buildPollMessage(EchonetMessageBuilder messageBuilder, ShortSupplier tidSupplier, long nowMs,
            InstanceKey managementControllerKey) {
        boolean result = false;
        if (lastPollMs + pollIntervalMs <= nowMs) {
            result = super.buildPollMessage(messageBuilder, tidSupplier, nowMs, managementControllerKey);

            if (result) {
                lastPollMs = nowMs;
            }
        }

        return result;
    }

    @Override
    public void onInitialised(String identifier, InstanceKey instanceKey, Map<String, String> channelIdAndType) {
        echonetDiscoveryListener.onDeviceFound(identifier, instanceKey);
    }
}
