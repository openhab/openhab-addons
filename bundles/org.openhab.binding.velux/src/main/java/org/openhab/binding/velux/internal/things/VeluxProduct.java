/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.bridge.slip.FunctionalParameters;
import org.openhab.binding.velux.internal.things.VeluxProductType.ActuatorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Velux</B> product representation.
 * <P>
 * Combined set of information describing a single Velux product.
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public class VeluxProduct {
    private final Logger logger = LoggerFactory.getLogger(VeluxProduct.class);

    // Public definition

    public static final VeluxProduct UNKNOWN = new VeluxProduct();

    // Type definitions

    public static class ProductBridgeIndex {

        // Public definition
        public static final ProductBridgeIndex UNKNOWN = new ProductBridgeIndex(0);

        // Class internal
        private int id;

        // Constructor
        public ProductBridgeIndex(int id) {
            this.id = id;
        }

        // Class access methods
        public int toInt() {
            return id;
        }

        @Override
        public String toString() {
            return Integer.toString(id);
        }
    }

    // State (of movement) of an actuator
    public static enum State {
        NON_EXECUTING(0),
        ERROR(1),
        NOT_USED(2),
        WAITING_FOR_POWER(3),
        EXECUTING(4),
        DONE(5),
        MANUAL_OVERRIDE(0x80),
        UNKNOWN(0xFF);

        public final int value;

        private State(int value) {
            this.value = value;
        }
    }

    // Class internal

    private VeluxProductName name;
    private VeluxProductType typeId;
    private ActuatorType actuatorType;
    private ProductBridgeIndex bridgeProductIndex;

    private boolean v2 = false;
    private int order = 0;
    private int placement = 0;
    private int velocity = 0;
    private int variation = 0;
    private int powerMode = 0;
    private String serialNumber = VeluxProductSerialNo.UNKNOWN;
    private int state = State.UNKNOWN.value;
    private int currentPosition = 0;
    private int targetPosition = 0;
    private @Nullable FunctionalParameters functionalParameters = null;
    private int remainingTime = 0;
    private int timeStamp = 0;

    // Constructor

    /**
     * Constructor
     *
     * just for the dummy VeluxProduct.
     */
    public VeluxProduct() {
        logger.trace("VeluxProduct() created.");
        this.name = VeluxProductName.UNKNOWN;
        this.typeId = VeluxProductType.UNDEFTYPE;
        this.bridgeProductIndex = ProductBridgeIndex.UNKNOWN;
        this.actuatorType = ActuatorType.UNDEFTYPE;
    }

    /**
     * Constructor
     *
     * @param name This field Name holds the name of the actuator, ex. “Window 1”. This field is 64 bytes
     *            long, formatted as UTF-8 characters.
     * @param typeId This field indicates the node type, ex. Window, Roller shutter, Light etc.
     * @param bridgeProductIndex NodeID is an Actuator index in the system table, to get information from. It must be a
     *            value from 0 to 199.
     */
    public VeluxProduct(VeluxProductName name, VeluxProductType typeId, ProductBridgeIndex bridgeProductIndex) {
        logger.trace("VeluxProduct(v1,name={}) created.", name.toString());
        this.name = name;
        this.typeId = typeId;
        this.bridgeProductIndex = bridgeProductIndex;
        this.actuatorType = ActuatorType.WINDOW_4_0;
    }

    /**
     * Constructor
     *
     * @param name This field Name holds the name of the actuator, ex. “Window 1”. This field is 64 bytes
     *            long, formatted as UTF-8 characters.
     * @param typeId This field indicates the node type, ex. Window, Roller shutter, Light etc.
     * @param bridgeProductIndex NodeID is an Actuator index in the system table, to get information from. It must be a
     *            value from 0 to 199.
     * @param order Order can be used to store a sort order. The sort order is used in client end, when
     *            presenting a list of nodes for the user.
     * @param placement Placement can be used to store a room group index or house group index number.
     * @param velocity This field indicates what velocity the node is operation with.
     * @param variation More detail information like top hung, kip, flat roof or sky light window.
     * @param powerMode This field indicates the power mode of the node (ALWAYS_ALIVE/LOW_POWER_MODE).
     * @param serialNumber This field tells the serial number of the node. This field is 8 bytes.
     * @param state This field indicates the operating state of the node.
     * @param currentPosition This field indicates the current position of the node.
     * @param target This field indicates the target position of the current operation.
     * @param functionalParameters the target Functional Parameters.
     * @param remainingTime This field indicates the remaining time for a node activation in seconds.
     * @param timeStamp UTC time stamp for last known position.
     */
    public VeluxProduct(VeluxProductName name, VeluxProductType typeId, ActuatorType actuatorType,
            ProductBridgeIndex bridgeProductIndex, int order, int placement, int velocity, int variation, int powerMode,
            String serialNumber, int state, int currentPosition, int target,
            @Nullable FunctionalParameters functionalParameters, int remainingTime, int timeStamp) {
        logger.trace("VeluxProduct(v2,name={}) created.", name.toString());
        this.name = name;
        this.typeId = typeId;
        this.actuatorType = actuatorType;
        this.bridgeProductIndex = bridgeProductIndex;
        this.v2 = true;
        this.order = order;
        this.placement = placement;
        this.velocity = velocity;
        this.variation = variation;
        this.powerMode = powerMode;
        this.serialNumber = serialNumber;
        this.state = state;
        this.currentPosition = currentPosition;
        this.targetPosition = target;
        this.functionalParameters = functionalParameters != null ? functionalParameters.clone() : null;
        this.remainingTime = remainingTime;
        this.timeStamp = timeStamp;
    }

    // Utility methods

    @Override
    public VeluxProduct clone() {
        if (this.v2) {
            return new VeluxProduct(this.name, this.typeId, this.actuatorType, this.bridgeProductIndex, this.order,
                    this.placement, this.velocity, this.variation, this.powerMode, this.serialNumber, this.state,
                    this.currentPosition, this.targetPosition, this.functionalParameters, this.remainingTime,
                    this.timeStamp);
        } else {
            return new VeluxProduct(this.name, this.typeId, this.bridgeProductIndex);
        }
    }

    // Class access methods

    /**
     * Returns the name of the current product (aka actuator) for convenience as type-specific class.
     *
     * @return nameOfThisProduct as type {@link VeluxProductName}.
     */
    public VeluxProductName getProductName() {
        return this.name;
    }

    /**
     * Returns the type of the current product (aka actuator) for convenience as type-specific class.
     *
     * @return typeOfThisProduct as type {@link VeluxProductType}.
     */
    public VeluxProductType getProductType() {
        return this.typeId;
    }

    public ProductBridgeIndex getBridgeProductIndex() {
        return this.bridgeProductIndex;
    }

    @Override
    public String toString() {
        if (this.v2) {
            return String.format("Product \"%s\" / %s (bridgeIndex=%d,serial=%s,position=%04X)", this.name, this.typeId,
                    this.bridgeProductIndex.toInt(), this.serialNumber, this.currentPosition);
        } else {
            return String.format("Product \"%s\" / %s (bridgeIndex %d)", this.name, this.typeId,
                    this.bridgeProductIndex.toInt());
        }
    }

    // Class helper methods

    /**
     * Return the product unique index.
     * Either the serial number (for normal Velux devices), or its name (for e.g. Somfy devices).
     *
     * @return ..
     */
    public String getProductUniqueIndex() {
        if (!v2 || serialNumber.startsWith(VeluxProductSerialNo.UNKNOWN)) {
            return name.toString();
        }
        return VeluxProductSerialNo.cleaned(serialNumber);
    }

    // Getter and Setter methods

    /**
     * @return <b>v2</b> as type boolean signals the availability of firmware version two (product) details.
     */
    public boolean isV2() {
        return v2;
    }

    /**
     * @return <b>order</b> as type int describes the user-oriented sort-order.
     */
    public int getOrder() {
        return order;
    }

    /**
     * @return <B>placement</B> as type int is used to describe a group index or house group index number.
     */
    public int getPlacement() {
        return placement;
    }

    /**
     * @return <B>velocity</B> as type int describes what velocity the node is operation with
     */
    public int getVelocity() {
        return velocity;
    }

    /**
     * @return <B>variation</B> as type int describes detail information like top hung, kip, flat roof or sky light
     *         window.
     */
    public int getVariation() {
        return variation;
    }

    /**
     * @return <B>powerMode</B> as type int is used to show the power mode of the node (ALWAYS_ALIVE/LOW_POWER_MODE).
     */
    public int getPowerMode() {
        return powerMode;
    }

    /**
     * @return <B>serialNumber</B> as type String is the serial number of 8 bytes length of the node.
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @return <B>state</B> as type int is used to operating state of the node.
     */
    public int getState() {
        return state;
    }

    /**
     * @param newState Update the operating state of the node.
     * @return <B>modified</B> as type boolean to signal a real modification.
     */
    public boolean setState(int newState) {
        if (this.state == newState) {
            return false;
        } else {
            logger.trace("setState(name={},index={}) state {} replaced by {}.", name.toString(),
                    bridgeProductIndex.toInt(), this.state, newState);
            this.state = newState;
            return true;
        }
    }

    /**
     * @return <B>currentPosition</B> as type int signals the current position of the node.
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * @param newCurrentPosition Update the current position of the node.
     * @return <B>modified</B> as boolean to signal a real modification.
     */
    public boolean setCurrentPosition(int newCurrentPosition) {
        if (this.currentPosition == newCurrentPosition) {
            return false;
        } else {
            logger.trace("setCurrentPosition(name={},index={}) currentPosition {} replaced by {}.", name.toString(),
                    bridgeProductIndex.toInt(), this.currentPosition, newCurrentPosition);
            this.currentPosition = newCurrentPosition;
            return true;
        }
    }

    /**
     * @return <b>target</b> as type int shows the target position of the current operation.
     */
    public int getTarget() {
        return targetPosition;
    }

    /**
     * @param newTarget Update the target position of the current operation.
     * @return <b>modified</b> as boolean to signal a real modification.
     */
    public boolean setTarget(int newTarget) {
        if (this.targetPosition == newTarget) {
            return false;
        } else {
            logger.trace("setCurrentPosition(name={},index={}) target {} replaced by {}.", name.toString(),
                    bridgeProductIndex.toInt(), this.targetPosition, newTarget);
            this.targetPosition = newTarget;
            return true;
        }
    }

    /**
     * @return <b>remainingTime</b> as type int describes the intended remaining time of current operation.
     */
    public int getRemainingTime() {
        return remainingTime;
    }

    /**
     * @return <b>timeStamp</b> as type int describes the current time.
     */
    public int getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns the display position of the actuator.
     * <li>As a general rule it returns <b>currentPosition</b>, except as follows..
     * <li>If the actuator is in a motion state it returns <b>targetPosition</b>
     * <li>If the motion state is 'done' but the currentPosition is invalid it returns <b>targetPosition</b>
     * <li>If the manual override flag is set it returns the <b>unknown</b> position value
     *
     * @return The display position of the actuator
     */
    public int getDisplayPosition() {
        // manual override flag set: position is 'unknown'
        if ((state & State.MANUAL_OVERRIDE.value) != 0) {
            return VeluxProductPosition.VPP_VELUX_UNKNOWN;
        }
        // only check other conditions if targetPosition is valid and differs from currentPosition
        if ((targetPosition != currentPosition) && (targetPosition <= VeluxProductPosition.VPP_VELUX_MAX)
                && (targetPosition >= VeluxProductPosition.VPP_VELUX_MIN)) {
            int state = this.state & 0xf;
            // actuator is in motion: for quicker UI update, return targetPosition
            if ((state > State.ERROR.value) && (state < State.DONE.value)) {
                return targetPosition;
            }
            // motion complete but currentPosition is not valid: return targetPosition
            if ((state == State.DONE.value) && ((currentPosition > VeluxProductPosition.VPP_VELUX_MAX)
                    || (currentPosition < VeluxProductPosition.VPP_VELUX_MIN))) {
                return targetPosition;
            }
        }
        return currentPosition;
    }

    /**
     * Get the Functional Parameters.
     *
     * @return the Functional Parameters.
     */
    public @Nullable FunctionalParameters getFunctionalParameters() {
        FunctionalParameters functionalParameters = this.functionalParameters;
        return functionalParameters != null ? functionalParameters.clone() : null;
    }

    /**
     * Set the Functional Parameters. Calls setProductAllowedValues() so that any values that are not allowed by normal
     * products will be replaced by the 'undefined' (i.e. 0xF7FF) value.
     *
     * @param newFunctionalParameters the new values of the Functional Parameters.
     * @return <b>modified</b> if any of the Functional Parameters have been changed.
     */
    public boolean setFunctionalParameters(@Nullable FunctionalParameters newFunctionalParameters) {
        if (newFunctionalParameters == null) {
            return false;
        }
        FunctionalParameters functionalParameters = this.functionalParameters;
        if (functionalParameters == null) {
            functionalParameters = this.functionalParameters = new FunctionalParameters();
        }
        return functionalParameters.setProductAllowedFPValues(newFunctionalParameters);
    }

    /**
     * Determines which of the Functional Parameters contains the vane position.
     * As defined in the Velux KLF 200 API Technical Specification Appendix 2 Table 276.
     *
     * @return the index of the vane position Functional Parameter, or -1 if not supported.
     */
    private int getVanePositionIndex() {
        switch (actuatorType) {
            case BLIND_1_0:
                return 0;
            case ROLLERSHUTTER_2_1:
            case BLIND_17:
            case BLIND_18:
                return 2;
            default:
        }
        return -1;
    }

    /**
     * Return the vane position. Reads the vane position from the Functional Parameters, or returns 'UNKNOWN' if vane
     * position is not supported.
     *
     * @return the vane position.
     */
    public int getVanePosition() {
        FunctionalParameters functionalParameters = this.functionalParameters;
        int index = getVanePositionIndex();
        if ((index >= 0) && (functionalParameters != null)) {
            return functionalParameters.getValues()[index];
        }
        return VeluxProductPosition.VPP_VELUX_UNKNOWN;
    }

    /**
     * Set the vane position into the appropriate Functional Parameter. If the actuator does not support vane positions
     * then a message is logged.
     *
     * @param vanePosition the new vane position.
     */
    public void setVanePosition(int vanePosition) {
        int index = getVanePositionIndex();
        if ((index >= 0) && FunctionalParameters.isNormalPosition(vanePosition)) {
            FunctionalParameters functionalParameters = this.functionalParameters = new FunctionalParameters();
            functionalParameters.setValue(index, vanePosition);
        } else {
            functionalParameters = null;
            logger.info("setVanePosition(): actuator type '{}' ({}) does not support vane position {}.",
                    actuatorType.getNodeType(), actuatorType.getDescription(), vanePosition);
        }
    }

    /**
     * Get the actuator type.
     *
     * @return the actuator type.
     */
    public ActuatorType getActuatorType() {
        return this.actuatorType;
    }

    /**
     * Set the actuator type.
     *
     * @param actuatorType the new value for the actuator type.
     */
    public void setActuatorType(ActuatorType actuatorType) {
        this.actuatorType = actuatorType;
        this.typeId = actuatorType.getTypeClass();
    }
}
