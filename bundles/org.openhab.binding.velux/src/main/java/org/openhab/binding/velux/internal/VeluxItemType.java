/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration of Types of a Velux item.
 * <br>
 * Provides information about:
 * <ul>
 * <li>associated thing identified by String</li>
 * <li>defined channel identified by String</li>
 * <li>{@link VeluxItemType#getItemClass getItemClass} item class</li>
 * <li>{@link VeluxItemType#isReadable isReadable} about a read possibility</li>
 * <li>{@link VeluxItemType#isWritable isWritable} about a write possibility</li>
 * <li>{@link VeluxItemType#isExecutable isExecutable} about an execute possibility</li>
 * <li>{@link VeluxItemType#isToBeRefreshed isToBeRefreshed} about necessarily to be refreshed.</li>
 * <li>{@link VeluxItemType#isToBeRefreshedNow isToBeRefreshedNow} about necessarily to be refreshed at this time.</li>
 * </ul>
 *
 * In addition there are helper methods providing information about:
 *
 * <ul>
 * <li>{@link VeluxItemType#getByString getByString} the enum by identifier string,</li>
 * <li>{@link VeluxItemType#getByThingAndChannel getByThingAndChannel} to retrieve an enum instance selected by Thing
 * and Channel identifier,</li>
 * <li>{@link VeluxItemType#getThingIdentifiers getThingIdentifiers} to retrieve any Thing identifiers as array of
 * String,</li>
 * <li>{@link VeluxItemType#getChannelIdentifiers getChannelIdentifiers} to retrieve any Channel identifiers as array of
 * String.</li>
 * </ul>
 * <p>
 * Within this enumeration, the expected behaviour of the OpenHAB item (resp. Channel)
 * is set. For each kind of Channel (i.e. bridge or device) parameter a
 * set of information is defined:
 * <ul>
 * <li>
 * Unique identification by:
 * <ul>
 * <li>Thing name as string,</li>
 * <li>Channel name as string,</li>
 * </ul>
 * </li>
 * <li>Channel type as OpenHAB type,</li>
 * <li>ability flag whether this item is to be read,</li>
 * <li>ability flag whether this item is able to be modified,</li>
 * <li>ability flag whether this item is to be used as execution trigger.</li>
 * </ul>
 *
 * @author Guenther Schreiner - Initial contribution
 *
 */
@NonNullByDefault
public enum VeluxItemType {
    // @formatter:off
    UNKNOWN(VeluxBindingConstants.THING_TYPE_BRIDGE,                            VeluxBindingConstants.UNKNOWN,                      TypeFlavor.UNUSABLE),
    //
    BINDING_INFORMATION(VeluxBindingConstants.THING_TYPE_BINDING,               VeluxBindingConstants.CHANNEL_BINDING_INFORMATION,  TypeFlavor.READONLY_VOLATILE_STRING),
    //
    BRIDGE_STATUS(VeluxBindingConstants.THING_TYPE_BRIDGE,                      VeluxBindingConstants.CHANNEL_BRIDGE_STATUS,        TypeFlavor.READONLY_VOLATILE_STRING),
    BRIDGE_DOWNTIME(VeluxBindingConstants.THING_TYPE_BRIDGE,                    VeluxBindingConstants.CHANNEL_BRIDGE_DOWNTIME,      TypeFlavor.READONLY_VOLATILE_NUMBER),
    BRIDGE_RELOAD(VeluxBindingConstants.THING_TYPE_BRIDGE,                      VeluxBindingConstants.CHANNEL_BRIDGE_RELOAD,        TypeFlavor.INITIATOR),
    BRIDGE_DO_DETECTION(VeluxBindingConstants.THING_TYPE_BRIDGE,                VeluxBindingConstants.CHANNEL_BRIDGE_DO_DETECTION,  TypeFlavor.INITIATOR),
    BRIDGE_FIRMWARE(VeluxBindingConstants.THING_TYPE_BRIDGE,                    VeluxBindingConstants.CHANNEL_BRIDGE_FIRMWARE,      TypeFlavor.READONLY_STATIC_STRING),
    BRIDGE_IPADDRESS(VeluxBindingConstants.THING_TYPE_BRIDGE,                   VeluxBindingConstants.CHANNEL_BRIDGE_IPADDRESS,     TypeFlavor.READONLY_STATIC_STRING),
    BRIDGE_SUBNETMASK(VeluxBindingConstants.THING_TYPE_BRIDGE,                  VeluxBindingConstants.CHANNEL_BRIDGE_SUBNETMASK,    TypeFlavor.READONLY_STATIC_STRING),
    BRIDGE_DEFAULTGW(VeluxBindingConstants.THING_TYPE_BRIDGE,                   VeluxBindingConstants.CHANNEL_BRIDGE_DEFAULTGW,     TypeFlavor.READONLY_STATIC_STRING),
    BRIDGE_DHCP(VeluxBindingConstants.THING_TYPE_BRIDGE,                        VeluxBindingConstants.CHANNEL_BRIDGE_DHCP,          TypeFlavor.READONLY_STATIC_SWITCH),
    BRIDGE_WLANSSID(VeluxBindingConstants.THING_TYPE_BRIDGE,                    VeluxBindingConstants.CHANNEL_BRIDGE_WLANSSID,      TypeFlavor.READONLY_STATIC_STRING),
    BRIDGE_WLANPASSWORD(VeluxBindingConstants.THING_TYPE_BRIDGE,                VeluxBindingConstants.CHANNEL_BRIDGE_WLANPASSWORD,  TypeFlavor.READONLY_STATIC_STRING),
    BRIDGE_PRODUCTS(VeluxBindingConstants.THING_TYPE_BRIDGE,                    VeluxBindingConstants.CHANNEL_BRIDGE_PRODUCTS,      TypeFlavor.READONLY_STATIC_STRING),
    BRIDGE_SCENES(VeluxBindingConstants.THING_TYPE_BRIDGE,                      VeluxBindingConstants.CHANNEL_BRIDGE_SCENES,        TypeFlavor.READONLY_STATIC_STRING),
    BRIDGE_CHECK(VeluxBindingConstants.THING_TYPE_BRIDGE,                       VeluxBindingConstants.CHANNEL_BRIDGE_CHECK,         TypeFlavor.READONLY_STATIC_STRING),
    //
    ACTUATOR_POSITION(VeluxBindingConstants.THING_TYPE_VELUX_ACTUATOR,          VeluxBindingConstants.CHANNEL_ACTUATOR_POSITION,    TypeFlavor.MANIPULATOR_SHUTTER),
    ACTUATOR_STATE(VeluxBindingConstants.THING_TYPE_VELUX_ACTUATOR,             VeluxBindingConstants.CHANNEL_ACTUATOR_STATE,       TypeFlavor.MANIPULATOR_SWITCH),
    ACTUATOR_LIMIT_MINIMUM(VeluxBindingConstants.THING_TYPE_VELUX_ACTUATOR,     VeluxBindingConstants.CHANNEL_ACTUATOR_LIMIT_MINIMUM,TypeFlavor.MANIPULATOR_SHUTTER),
    ACTUATOR_LIMIT_MAXIMUM(VeluxBindingConstants.THING_TYPE_VELUX_ACTUATOR,     VeluxBindingConstants.CHANNEL_ACTUATOR_LIMIT_MAXIMUM,TypeFlavor.MANIPULATOR_SHUTTER),
    //
    ROLLERSHUTTER_POSITION(VeluxBindingConstants.THING_TYPE_VELUX_ROLLERSHUTTER,VeluxBindingConstants.CHANNEL_ACTUATOR_POSITION,    TypeFlavor.MANIPULATOR_SHUTTER),
    ROLLERSHUTTER_LIMIT_MINIMUM(VeluxBindingConstants.THING_TYPE_VELUX_ROLLERSHUTTER,VeluxBindingConstants.CHANNEL_ACTUATOR_LIMIT_MINIMUM,TypeFlavor.MANIPULATOR_SHUTTER),
    ROLLERSHUTTER_LIMIT_MAXIMUM(VeluxBindingConstants.THING_TYPE_VELUX_ROLLERSHUTTER,VeluxBindingConstants.CHANNEL_ACTUATOR_LIMIT_MAXIMUM,TypeFlavor.MANIPULATOR_SHUTTER),
    //
    WINDOW_POSITION(VeluxBindingConstants.THING_TYPE_VELUX_WINDOW,              VeluxBindingConstants.CHANNEL_ACTUATOR_POSITION,    TypeFlavor.MANIPULATOR_SHUTTER),
    WINDOW_LIMIT_MINIMUM(VeluxBindingConstants.THING_TYPE_VELUX_WINDOW,         VeluxBindingConstants.CHANNEL_ACTUATOR_LIMIT_MINIMUM,TypeFlavor.MANIPULATOR_SHUTTER),
    WINDOW_LIMIT_MAXIMUM(VeluxBindingConstants.THING_TYPE_VELUX_WINDOW,         VeluxBindingConstants.CHANNEL_ACTUATOR_LIMIT_MAXIMUM,TypeFlavor.MANIPULATOR_SHUTTER),
    //
    SCENE_ACTION(VeluxBindingConstants.THING_TYPE_VELUX_SCENE,                  VeluxBindingConstants.CHANNEL_SCENE_ACTION,         TypeFlavor.INITIATOR),
    SCENE_SILENTMODE(VeluxBindingConstants.THING_TYPE_VELUX_SCENE,              VeluxBindingConstants.CHANNEL_SCENE_SILENTMODE,     TypeFlavor.WRITEONLY_VOLATILE_SWITCH),
    //
    VSHUTTER_POSITION(VeluxBindingConstants.THING_TYPE_VELUX_VSHUTTER,          VeluxBindingConstants.CHANNEL_VSHUTTER_POSITION,    TypeFlavor.MANIPULATOR_SHUTTER),
    ;
    // @formatter:on

    private enum TypeFlavor {
        /**
         * Used to present read-only non-volatile configuration parameters as StringItem.
         */
        READONLY_STATIC_STRING,
        /**
         * Used to present read-only non-volatile configuration parameters as SwitchItem.
         */
        READONLY_STATIC_SWITCH,
        /**
         * Used to present volatile configuration parameters as StringItem.
         */
        READONLY_VOLATILE_STRING,
        /**
         * Used to present volatile configuration parameters as NumberItem.
         */
        READONLY_VOLATILE_NUMBER,
        /**
         * Used to present volatile configuration parameters as NumberItem.
         */
        WRITEONLY_VOLATILE_SWITCH,
        /**
         * Used to present volatile configuration parameters as SwitchItem.
         */
        READWRITE_VOLATILE_SWITCH,
        /**
         * Used to initiate an action.
         */
        INITIATOR,
        /**
         * Used to manipulate an actuator.
         */
        MANIPULATOR_SHUTTER,
        /**
         * Used to manipulate an actuator.
         */
        MANIPULATOR_SWITCH,
        /**
         * Used to define an UNUSABLE entry.
         */
        UNUSABLE,
    }

    private ThingTypeUID thingIdentifier;
    private String channelIdentifier;
    private Class<? extends GenericItem> itemClass;
    private boolean itemIsReadable;
    private boolean itemIsWritable;
    private boolean itemIsExecutable;
    private boolean itemIsToBeRefreshed;
    private int itemsRefreshDivider;

    private static final Logger LOGGER = LoggerFactory.getLogger(VeluxItemType.class);
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int REFRESH_CYCLE_FIRST_TIME = 0;
    private static final int REFRESH_ONCE_A_DAY = 8640;
    private static final int REFRESH_EACH_MINUTE = 6;
    private static final int REFRESH_EVERY_CYCLE = 1;

    VeluxItemType(ThingTypeUID thingIdentifier, String channelIdentifier, TypeFlavor typeFlavor) {
        this.thingIdentifier = thingIdentifier;
        this.channelIdentifier = channelIdentifier;
        switch (typeFlavor) {
            case READONLY_STATIC_STRING:
                this.itemClass = StringItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_ONCE_A_DAY;
                break;
            case READONLY_STATIC_SWITCH:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_ONCE_A_DAY;
                break;

            case READONLY_VOLATILE_STRING:
                this.itemClass = StringItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EACH_MINUTE;
                break;
            case READONLY_VOLATILE_NUMBER:
                this.itemClass = NumberItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EVERY_CYCLE;
                break;
            case WRITEONLY_VOLATILE_SWITCH:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = false;
                this.itemIsWritable = true;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = false;
                this.itemsRefreshDivider = REFRESH_EACH_MINUTE;
                break;
            case READWRITE_VOLATILE_SWITCH:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = true;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EVERY_CYCLE;
                break;

            case INITIATOR:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = false;
                this.itemIsWritable = false;
                this.itemIsExecutable = true;
                this.itemIsToBeRefreshed = false;
                this.itemsRefreshDivider = 1;
                break;

            case MANIPULATOR_SHUTTER:
                this.itemClass = RollershutterItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = true;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EACH_MINUTE;
                break;

            case MANIPULATOR_SWITCH:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = true;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EACH_MINUTE;
                break;

            case UNUSABLE:
            default:
                this.itemClass = StringItem.class;
                this.itemIsReadable = false;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = false;
                this.itemsRefreshDivider = REFRESH_ONCE_A_DAY;
        }
    }

    private VeluxItemType(ThingTypeUID thingIdentifier, String channelIdentifier,
            Class<? extends GenericItem> itemClass, boolean isReadable, boolean isWritable, boolean isExecutable,
            boolean isToBeRefreshed, int refreshDivider) {
        this.thingIdentifier = thingIdentifier;
        this.channelIdentifier = channelIdentifier;
        this.itemClass = itemClass;
        this.itemIsReadable = isReadable;
        this.itemIsWritable = isWritable;
        this.itemIsExecutable = isExecutable;
        this.itemIsToBeRefreshed = isToBeRefreshed;
        this.itemsRefreshDivider = refreshDivider;
    }

    @Override
    public String toString() {
        return this.thingIdentifier + "/" + this.channelIdentifier;
    }

    /**
     * {@link VeluxItemType} access method to query Identifier on this type of item.
     *
     * @return <b>thingIdentifier</b> of type String describing the value of the enum {@link VeluxItemType}
     *         return
     */
    public ThingTypeUID getIdentifier() {
        return this.thingIdentifier;
    }

    /**
     * {@link VeluxItemType} access method to query the appropriate type of item.
     *
     * @return <b>itemClass</b> of type Item describing the possible type of this item.
     */
    public Class<? extends GenericItem> getItemClass() {
        return this.itemClass;
    }

    /**
     * {@link VeluxItemType} access method to query Read possibility on this type of item.
     *
     * @return <b>itemIsReadable</b> of type boolean describing the ability to perform a write operation.
     */
    public boolean isReadable() {
        logger.trace("isReadable() returns {}.", this.itemIsReadable);
        return this.itemIsReadable;
    }

    /**
     * {@link VeluxItemType} access method to query Write possibility on this type of item.
     *
     * @return <b>itemIsWritable</b> of type boolean describing the ability to perform a write operation.
     */
    public boolean isWritable() {
        logger.trace("isWritable() returns {}.", this.itemIsWritable);
        return this.itemIsWritable;
    }

    /**
     * {@link VeluxItemType} access method to query Execute possibility on this type of item.
     *
     * @return <b>isExecute</b> of type boolean describing the ability to perform a write operation.
     */
    public boolean isExecutable() {
        logger.trace("isExecutable() returns {}.", this.itemIsExecutable);
        return this.itemIsExecutable;
    }

    /**
     * {@link VeluxItemType} access method to query the need of refresh on this type of item.
     *
     * @return <b>isExecute</b> of type boolean describing the ability to perform a write operation.
     */
    public boolean isToBeRefreshed() {
        logger.trace("isToBeRefreshed() returns {}.", this.itemIsToBeRefreshed);
        return this.itemIsToBeRefreshed;
    }

    /**
     * {@link VeluxItemType} access method to query the refreshMSecs interval on this type of item.
     *
     * @return <b>refreshDivider</b> of type int describing the factor.
     */
    public int getRefreshDivider() {
        logger.trace("getRefreshDivider() returns {}.", this.itemsRefreshDivider);
        return this.itemsRefreshDivider;
    }

    /**
     * {@link VeluxItemType} access method to find an enum by itemTypeName.
     *
     * @param itemTypeName as name of requested Thing of type String.
     *
     * @return <b>veluxItemType</b> of type VeluxItemType describing the appropriate enum.
     */
    public VeluxItemType getByString(String itemTypeName) {
        try {
            return VeluxItemType.valueOf(itemTypeName);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /**
     * {@link VeluxItemType} access method to find an enum by name.
     *
     * @param thingIdentifier as name of requested Thing of type String.
     * @param channelIdentifier as name of requested Channel of type String.
     *
     * @return <b>veluxItemType</b> of type VeluxItemType describing the appropriate enum.
     */
    public static VeluxItemType getByThingAndChannel(ThingTypeUID thingIdentifier, String channelIdentifier) {
        LOGGER.trace("getByThingAndChannel({},{}) called.", thingIdentifier, channelIdentifier);
        for (VeluxItemType v : VeluxItemType.values()) {
            if (((thingIdentifier.equals(v.thingIdentifier)) || (thingIdentifier.equals(v.thingIdentifier)))
                    && (channelIdentifier.equals(v.channelIdentifier))) {
                LOGGER.trace("getByThingAndChannel() returns enum {}.", v);
                return v;
            }
        }
        LOGGER.trace("getByThingAndChannel() returns UNKNOWN.");
        return UNKNOWN;
    }

    /**
     * {@link VeluxItemType} access method to find an enum by name.
     *
     * @return <b>veluxItemType</b> of type VeluxItemType describing the appropriate enum.
     */
    public static String[] getThingIdentifiers() {
        LOGGER.trace("getThingIdentifiers() called.");
        Set<List<ThingTypeUID>> uniqueSet = new HashSet<List<ThingTypeUID>>();
        for (VeluxItemType v : VeluxItemType.values()) {
            LOGGER.trace("getThingIdentifiers() adding {}.", v.thingIdentifier);
            uniqueSet.add(Arrays.asList(v.thingIdentifier));
        }
        return uniqueSet.toArray(new String[uniqueSet.size()]);
    }

    /**
     * {@link VeluxItemType} access method to find an enum by name.
     *
     * @param thingIdentifier as name of requested Thing of type String.
     *
     * @return <b>veluxItemType</b> of type VeluxItemType describing the appropriate enum.
     */
    public static String[] getChannelIdentifiers(ThingTypeUID thingIdentifier) {
        LOGGER.trace("getChannelIdentifiers() called.");
        Set<List<String>> uniqueSet = new HashSet<List<String>>();
        for (VeluxItemType v : VeluxItemType.values()) {
            if (thingIdentifier.equals(v.thingIdentifier)) {
                uniqueSet.add(Arrays.asList(v.channelIdentifier));
            }
        }
        return uniqueSet.toArray(new String[uniqueSet.size()]);
    }

    /**
     * Helper function: Calculate modulo.
     *
     * @param a as dividend.
     * @param b as divisor.
     *
     * @return <b>true</b> if zero is remainder after division.
     */
    private static boolean isModulo(int a, int b) {
        return (a % b) == 0;
    }

    /**
     * {@link VeluxItemType} access method to determine the necessity of being refreshed
     * within the current refresh cycle.
     *
     * @param refreshCycleCounter as identification of the refresh round.
     * @param thingIdentifier as name of requested Thing.
     * @param channelIdentifier as name of requested Channel.
     *
     * @return <b>boolean</b> value which expresses the need.
     */
    public static boolean isToBeRefreshedNow(int refreshCycleCounter, ThingTypeUID thingIdentifier,
            String channelIdentifier) {
        VeluxItemType itemType = getByThingAndChannel(thingIdentifier, channelIdentifier);

        if (itemType == VeluxItemType.UNKNOWN) {
            LOGGER.warn("isToBeRefreshedNow({},{},{}): returning false, as item is not found.", refreshCycleCounter,
                    thingIdentifier, channelIdentifier);
            return false;
        }

        if (((refreshCycleCounter == REFRESH_CYCLE_FIRST_TIME) && (itemType.isReadable()))
                || (itemType.isToBeRefreshed())) {
            if ((refreshCycleCounter == REFRESH_CYCLE_FIRST_TIME)
                    || (isModulo(refreshCycleCounter, itemType.getRefreshDivider()))) {
                LOGGER.trace("isToBeRefreshedNow(): returning true, as item is to be refreshed, now.");
                return true;
            } else {
                LOGGER.trace("isToBeRefreshedNow(): returning false, as refresh cycle has not yet come for this item.");
            }
        } else {
            LOGGER.trace("isToBeRefreshedNow(): returning false, as item is not refreshable.");
        }
        return false;
    }

}
