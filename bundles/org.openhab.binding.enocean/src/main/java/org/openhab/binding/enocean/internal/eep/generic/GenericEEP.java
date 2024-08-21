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
package org.openhab.binding.enocean.internal.eep.generic;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;
import static org.openhab.binding.enocean.internal.messages.ESP3Packet.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.config.EnOceanChannelTransformationConfig;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.transform.actions.Transformation;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class GenericEEP extends EEP {

    final List<Class<? extends State>> supportedStates = Collections
            .unmodifiableList(List.of(DateTimeType.class, DecimalType.class, HSBType.class, OnOffType.class,
                    OpenClosedType.class, PercentType.class, PlayPauseType.class, PointType.class,
                    RewindFastforwardType.class, StringListType.class, StringType.class, UpDownType.class));

    public GenericEEP() {
        super();
    }

    public GenericEEP(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command command,
            Function<String, State> getCurrentStateFunc, @Nullable Configuration config) {
        if (config == null) {
            logger.error("Cannot handle command {}, when transformation configuration is null", command.toString());
            return;
        }
        EnOceanChannelTransformationConfig transformationInfo = config.as(EnOceanChannelTransformationConfig.class);

        String input = channelId + "|" + command.toString();
        String output = Transformation.transform(transformationInfo.transformationType,
                transformationInfo.transformationFunction, input);

        if (output != null && !output.isEmpty() && !input.equals(output)) {
            try {
                setData(HexUtils.hexToBytes(output));
            } catch (IllegalArgumentException e) {
                logger.debug("Command {} could not transformed", command.toString());
                throw e;
            }
        }
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, @Nullable State> getCurrentStateFunc, Configuration config) {
        EnOceanChannelTransformationConfig transformationInfo = config.as(EnOceanChannelTransformationConfig.class);

        String payload = HexUtils.bytesToHex(bytes);
        String input = channelId + "|" + payload;
        String output = Transformation.transform(transformationInfo.transformationType,
                transformationInfo.transformationFunction, input);

        if (output != null && !output.isEmpty() && !input.equals(output)) {
            String[] parts = output.split("\\|");

            if (parts.length == 2) {
                @Nullable
                Class<? extends State> state = supportedStates.stream().filter(s -> s.getName().contains(parts[0]))
                        .findFirst().orElse(null);

                if (state != null) {
                    if (state.isEnum()) {
                        State[] states;
                        if ((states = state.getEnumConstants()) != null) {
                            for (State s : states) {
                                if (s.toString().equalsIgnoreCase(parts[1])) {
                                    return s;
                                }
                            }
                        }
                        logger.debug("Could not find value '{}' for state '{}'", parts[1], parts[0]);
                    } else {
                        try {
                            return state.getConstructor(String.class).newInstance(parts[1]);
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                            logger.debug("Could not create state '{}' with value '{}'", parts[0], parts[1]);
                        }
                    }
                } else {
                    logger.debug("State '{}' not found", parts[0]);
                }
            } else {
                logger.debug("Transformation result malformed: {}", output);
            }
        }

        return UnDefType.UNDEF;
    }

    @Override
    protected int getDataLength() {
        ERP1Message localPacket = packet;
        if (localPacket != null) {
            return localPacket.getPayload().length - ESP3_SENDERID_LENGTH - ESP3_RORG_LENGTH - ESP3_STATUS_LENGTH;
        } else {
            return bytes.length;
        }
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return true;
    }

    @Override
    public void addConfigPropertiesTo(DiscoveryResultBuilder discoveredThingResultBuilder) {
        discoveredThingResultBuilder.withProperty(PARAMETER_SENDINGEEPID, getEEPType().getId())
                .withProperty(PARAMETER_RECEIVINGEEPID, getEEPType().getId());
    }
}
