/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal.dto;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A static factory for registering packet types that may be received and
 * dispatched to client cod * request types, like {@code PowerStateRequest}) or types received only via UDP
 * e. Packet handlers (used to construct actual packet
 * instances) may be retrieved via their packet type.
 *
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 * @author Wouter Born - Support LIFX 2016 product line-up and infrared functionality
 */
@NonNullByDefault
public class PacketFactory {

    private static @Nullable PacketFactory instance;

    public static synchronized PacketFactory getInstance() {
        PacketFactory result = instance;
        if (result == null) {
            result = new PacketFactory();
            instance = result;
        }
        return result;
    }

    private final Map<Integer, PacketHandler<?>> handlers;

    private PacketFactory() {
        handlers = new HashMap<>();

        register(AcknowledgementResponse.class);
        register(EchoRequestResponse.class);
        register(GetColorZonesRequest.class);
        register(GetEchoRequest.class);
        register(GetGroupRequest.class);
        register(GetHevCycleConfigurationRequest.class);
        register(GetHevCycleRequest.class);
        register(GetHostFirmwareRequest.class);
        register(GetHostInfoRequest.class);
        register(GetInfoRequest.class);
        register(GetLabelRequest.class);
        register(GetLastHevCycleResultRequest.class);
        register(GetLightInfraredRequest.class);
        register(GetLightPowerRequest.class);
        register(GetLocationRequest.class);
        register(GetPowerRequest.class);
        register(GetRequest.class);
        register(GetServiceRequest.class);
        register(GetTagLabelsRequest.class);
        register(GetTagsRequest.class);
        register(GetTileEffectRequest.class);
        register(GetVersionRequest.class);
        register(GetWifiFirmwareRequest.class);
        register(GetWifiInfoRequest.class);
        register(SetColorRequest.class);
        register(SetColorZonesRequest.class);
        register(SetDimAbsoluteRequest.class);
        register(SetHevCycleRequest.class);
        register(SetHevCycleConfigurationRequest.class);
        register(SetLabelRequest.class);
        register(SetLightInfraredRequest.class);
        register(SetLightPowerRequest.class);
        register(SetPowerRequest.class);
        register(SetTagsRequest.class);
        register(StateGroupResponse.class);
        register(StateHevCycleConfigurationResponse.class);
        register(StateHevCycleResponse.class);
        register(StateHostFirmwareResponse.class);
        register(StateHostInfoResponse.class);
        register(StateInfoResponse.class);
        register(StateLabelResponse.class);
        register(StateLastHevCycleResultResponse.class);
        register(StateLightInfraredResponse.class);
        register(StateLightPowerResponse.class);
        register(StateLocationResponse.class);
        register(StateMultiZoneResponse.class);
        register(StatePowerResponse.class);
        register(StateResponse.class);
        register(StateServiceResponse.class);
        register(StateTileEffectResponse.class);
        register(StateVersionResponse.class);
        register(StateWifiFirmwareResponse.class);
        register(StateWifiInfoResponse.class);
        register(StateZoneResponse.class);
        register(TagLabelsResponse.class);
        register(TagsResponse.class);
    }

    /**
     * Registers a packet handler for the given packet type.
     *
     * @param type the type to register
     * @param handler the packet handler to associate with the type
     */
    public final void register(int type, PacketHandler<?> handler) {
        handlers.put(type, handler);
    }

    /**
     * Registers a new generic packet handler for the given packet class. The
     * packet class must meet the criteria for {@link GenericHandler};
     * specifically, it must have a no-argument constructor and require no
     * parsing logic outside of an invocation of
     * {@link Packet#parse(java.nio.ByteBuffer)}.
     *
     * @param type the type of the packet to register
     * @param clazz the class of the packet to register
     */
    public final void register(int type, Class<? extends Packet> clazz) {
        handlers.put(type, new GenericHandler<>(clazz));
    }

    /**
     * Registers a generic packet type. All requirements of
     * {@link GenericHandler} must met; specifically, classes must have a
     * no-args constructor and require no additional parsing logic.
     * Additionally, a public static integer {@code TYPE} field must be defined.
     *
     * @param <T> the packet type to register
     * @param clazz the packet class to register
     */
    public final <T extends Packet> void register(Class<T> clazz) {
        GenericHandler<T> handler = new GenericHandler<>(clazz);

        if (!handler.isTypeFound()) {
            throw new IllegalArgumentException("Unable to register generic packet with no TYPE field.");
        }

        handlers.put(handler.getType(), handler);
    }

    /**
     * Gets a registered handler for the given packet type, if any exists. If
     * no matching handler can be found, {@code null} is returned.
     *
     * @param packetType the packet type of the handler to retrieve
     * @return a packet handler, or null
     */
    public @Nullable PacketHandler<?> getHandler(int packetType) {
        return handlers.get(packetType);
    }

    public static @Nullable PacketHandler<?> createHandler(int packetType) {
        return getInstance().getHandler(packetType);
    }
}
