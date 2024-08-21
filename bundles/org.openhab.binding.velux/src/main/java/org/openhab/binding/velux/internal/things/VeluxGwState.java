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
package org.openhab.binding.velux.internal.things;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Velux</B> product characteristics: GatewayState Description.
 * <P>
 * See <a href=
 * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=19">KLF200
 * GatewayState value Description</a>
 * <P>
 * Methods in handle this type of information:
 * *
 * <UL>
 * <LI>{@link #VeluxGwState(byte, byte)} to convert a value into the complete characteristic.</LI>
 * <LI>{@link #toString()} to retrieve the human-readable description of the complete characteristic.</LI>
 * <LI>{@link #toDescription()} to retrieve the verbose human-readable description of the complete characteristic..</LI>
 * <LI>{@link #getSubState()} to retrieve a Velux value as part of the complete characteristic.</LI>
 * </UL>
 * In addition, the subtype are accessible via:
 * <UL>
 * <LI>{@link VeluxGatewayState#getStateValue()} to retrieve the Velux value of the characteristic.</LI>
 * <LI>{@link VeluxGatewayState#getStateDescription()} to retrieve the human-readable description of the
 * characteristic.</LI>
 * <LI>{@link VeluxGatewayState#get(int)} to convert a value into the characteristic.</LI>
 * <LI>{@link VeluxGatewaySubState#getStateValue()} to retrieve the Velux value of the characteristic.</LI>
 * <LI>{@link VeluxGatewaySubState#getStateDescription()} to retrieve the human-readable description of the
 * characteristic.</LI>
 * <LI>{@link VeluxGatewaySubState#get(int)} to convert a value into the characteristic.</LI>
 * </UL>
 *
 * @see VeluxKLFAPI
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public class VeluxGwState {
    private final Logger logger = LoggerFactory.getLogger(VeluxGwState.class);

    // Type definition

    public enum VeluxGatewayState {
        UNDEFTYPE(-1, "Unkwown state."),
        GW_S_TEST(0, "Test mode."),
        GW_S_GWM_EMPTY(1, "Gateway mode, no actuator nodes in the system table."),
        GW_S_GWM(2, "Gateway mode, with one or more actuator nodes in the system table."),
        GW_S_BM_UNCONFIG(3, "Beacon mode, not configured by a remote controller."),
        GW_S_BM(4, "Beacon mode, has been configured by a remote controller."),
        GW_STATE_RESERVED(255, "Reserved");

        // Class internal

        private int stateValue;
        private String stateDescription;

        // Reverse-lookup map for getting a VeluxProductVelocity from a value.
        private static final Map<Integer, VeluxGatewayState> LOOKUPTYPEID2ENUM = Stream.of(VeluxGatewayState.values())
                .collect(Collectors.toMap(VeluxGatewayState::getStateValue, Function.identity()));

        // Constructor

        private VeluxGatewayState(int stateValue, String stateDescription) {
            this.stateValue = stateValue;
            this.stateDescription = stateDescription;
        }

        // Class access methods

        public int getStateValue() {
            return stateValue;
        }

        public String getStateDescription() {
            return stateDescription;
        }

        public static VeluxGatewayState get(int stateValue) {
            return LOOKUPTYPEID2ENUM.getOrDefault(stateValue, VeluxGatewayState.UNDEFTYPE);
        }
    }

    public enum VeluxGatewaySubState {
        UNDEFTYPE(-1, "Unknown state."),
        GW_SS_IDLE(0, "Idle state."),
        GW_SS_P1(1, "Performing task in Configuration Service handler."),
        GW_SS_P2(2, "Performing Scene Configuration."),
        GW_SS_P3(3, "Performing Information Service Configuration."),
        GW_SS_P4(4, "Performing Contact input Configuration."),
        GW_SS_PFF(88, "Reserved");

        // Class internal

        private int stateValue;
        private String stateDescription;

        // Reverse-lookup map for getting a VeluxGatewayState from a TypeId
        private static final Map<Integer, VeluxGatewaySubState> LOOKUPTYPEID2ENUM = Stream
                .of(VeluxGatewaySubState.values())
                .collect(Collectors.toMap(VeluxGatewaySubState::getStateValue, Function.identity()));

        // Constructor

        private VeluxGatewaySubState(int stateValue, String stateDescription) {
            this.stateValue = stateValue;
            this.stateDescription = stateDescription;
        }

        // Class access methods

        public int getStateValue() {
            return stateValue;
        }

        public String getStateDescription() {
            return stateDescription;
        }

        public static VeluxGatewaySubState get(int stateValue) {
            return LOOKUPTYPEID2ENUM.getOrDefault(stateValue, VeluxGatewaySubState.UNDEFTYPE);
        }
    }

    // Class internal

    private VeluxGatewayState gwState;
    private VeluxGatewaySubState gwSubState;

    // Constructor

    public VeluxGwState(byte stateValue, byte subStateValue) {
        logger.trace("VeluxGwState() created.");

        this.gwState = VeluxGatewayState.get(stateValue);
        this.gwSubState = VeluxGatewaySubState.get(subStateValue);
    }

    // Class access methods

    @Override
    public String toString() {
        return this.gwState.name().concat("/").concat(this.gwSubState.name());
    }

    public String toDescription() {
        return this.gwState.getStateDescription().concat(", ").concat(this.gwSubState.getStateDescription());
    }

    public byte getSubState() {
        return (byte) this.gwSubState.getStateValue();
    }
}
