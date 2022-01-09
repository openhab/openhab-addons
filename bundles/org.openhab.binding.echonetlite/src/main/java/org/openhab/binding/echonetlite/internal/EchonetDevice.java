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

import static org.openhab.binding.echonetlite.internal.HexUtil.hex;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Barker - Initial contribution
 */
public class EchonetDevice extends EchonetObject {
    private static final long UPDATE_RESEND_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(1);

    private final LinkedHashMap<Epc, State> pendingSets = new LinkedHashMap<>();
    private final HashMap<Epc, State> stateFields = new HashMap<>();
    private final HashMap<String, Epc> epcByChannelId = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(EchonetDevice.class);
    private EchonetPropertyMap getPropertyMap = null;
    private EchonetDeviceListener listener;
    private boolean initialised = false;

    private long lastPollMs = 0;

    public EchonetDevice(final InstanceKey instanceKey, EchonetDeviceListener listener) {
        super(instanceKey, Epc.Device.GET_PROPERTY_MAP);
        this.listener = listener;
    }

    public void applyProperty(InstanceKey sourceInstanceKey, Esv esv, final int epcCode, final int pdc,
            final ByteBuffer edt) {
        final Epc epc = Epc.lookup(instanceKey().klass.groupCode(), instanceKey().klass.classCode(), epcCode);

        if ((Esv.Get_Res == esv || Esv.Get_SNA == esv || Esv.INF == esv) && 0 < pdc) {
            pendingGets.remove(epc);

            int edtPosition = edt.position();

            final StateDecode decoder = epc.decoder();
            State state = null;
            if (null != decoder) {
                state = decoder.decodeState(edt);
                if (null == stateFields.put(epc, state)) {
                    epcByChannelId.put(epc.channelId(), epc);
                }

                final State pendingState = pendingSets.get(epc);
                if (null != pendingState && pendingState.equals(state)) {
                    logger.debug("pendingSet - removing: {} {}", epc, state);
                    pendingSets.remove(epc);
                } else if (null != pendingState) {
                    logger.debug("pendingSet - state mismatch: {} {} {}", epc, pendingState, state);
                }

                if (initialised) {
                    listener.onUpdated(epc.channelId(), state);
                } else if (pendingGets.isEmpty()) {
                    initialised = true;
                    listener.onInitialised(identifier(), instanceKey, channelIds());
                    stateFields.forEach((e, s) -> listener.onUpdated(e.channelId(), s));
                }

            } else {
                if (Epc.Device.GET_PROPERTY_MAP == epc && null == getPropertyMap) {
                    getPropertyMap = new EchonetPropertyMap(epc);
                    getPropertyMap.update(edt);
                    getPropertyMap.getProperties(instanceKey().klass.groupCode(), instanceKey().klass.classCode(),
                            Set.of(Epc.Device.GET_PROPERTY_MAP), pendingGets);
                }
            }

            if (logger.isDebugEnabled()) {
                String value = null != state ? state.toString() : "";
                edt.position(edtPosition);
                logger.debug("Applying: {}({},{}) {} {}", epc, hex(epc.code()), pdc, value, hex(edt));
            }
        } else if (esv == Esv.Set_Res) {
            pendingSets.remove(epc);
            // if (pendingSets.containsKey(epc)) {
            // logger.debug("Set value {} received, getting latest from device", epc);
            // pendingGets.add(epc);
            // }
        }
    }

    public String identifier() {
        return stateFields.get(Epc.Device.IDENTIFICATION_NUMBER).toString();
    }

    public boolean buildUpdateMessage(final EchonetMessageBuilder messageBuilder, final ShortSupplier tidSupplier,
            final long nowMs) {
        if (pendingSets.isEmpty()) {
            return false;
        }

        if (hasInflight(nowMs, inflightSetRequest)) {
            return false;
        }

        final short tid = tidSupplier.getAsShort();
        messageBuilder.start(tid, EchonetLiteBindingConstants.MANAGEMENT_CONTROLLER_KEY, instanceKey, Esv.SetC);

        pendingSets.forEach((k, v) -> {
            if (null != k.encoder()) {
                final ByteBuffer buffer = messageBuilder.edtBuffer();
                k.encoder().encodeState(v, buffer);
                messageBuilder.appendEpcUpdate(k.code(), buffer.flip());
            }
        });

        inflightSetRequest.requestSent(tid, nowMs);

        return true;
    }

    public void update(String channelId, State state) {
        final Epc epc = epcByChannelId.get(channelId);
        if (null == epc) {
            logger.warn("Unable to find epc for channelId: {}", channelId);
            return;
        }

        pendingSets.put(epc, state);
    }

    @Override
    public void removed() {
        listener.onRemoved();
    }

    public void refreshAll(long nowMs) {
        if (lastPollMs + pollIntervalMs <= nowMs && null != getPropertyMap) {
            getPropertyMap.getProperties(instanceKey().klass.groupCode(), instanceKey().klass.classCode(),
                    Set.of(Epc.Device.GET_PROPERTY_MAP), pendingGets);
            lastPollMs = nowMs;
        }
    }

    @Override
    public void refresh(String channelId) {
        final Epc epc = epcByChannelId.get(channelId);
        if (null == epc) {
            return;
        }

        final State state = stateFields.get(epc);
        if (null == state) {
            return;
        }

        listener.onUpdated(channelId, state);
    }

    public void setListener(EchonetDeviceListener listener) {
        final boolean isNewListener = !Objects.equals(this.listener, listener);
        this.listener = listener;

        if (isNewListener && initialised) {
            listener.onInitialised(identifier(), instanceKey(), channelIds());
            stateFields.forEach((e, s) -> listener.onUpdated(e.channelId(), s));
        }
    }

    private Map<String, String> channelIds() {
        final HashMap<String, String> channelIdAndType = new HashMap<>();
        stateFields.keySet().stream().filter(e -> null != e.decoder())
                .forEach(e -> channelIdAndType.put(e.channelId(), e.decoder().itemType()));
        return channelIdAndType;
    }
}
