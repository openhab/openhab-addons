/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.local.handlers;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * The {@link DatagramToByteBufDecoder} is a Netty Decoder for UDP messages
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DatagramToByteBufDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(@Nullable ChannelHandlerContext ctx, DatagramPacket msg,
            @NonNullByDefault({}) List<Object> out) throws Exception {
        out.add(msg.content().copy());
    }
}
