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
package org.openhab.binding.mielecloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MieleCloudBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Added locale config parameter, added i18n key collection
 * @author Benjamin Bolte - Add pre-heat finished and plate step channels, door state and door alarm channels, info
 *         state channel and map signal flags from API
 * @author Björn Lange - Add elapsed time channel, dish warmer thing, removed e-mail validation
 */
@NonNullByDefault
public final class MieleCloudBindingConstants {

    private MieleCloudBindingConstants() {
    }

    /**
     * ID of the binding.
     */
    public static final String BINDING_ID = "mielecloud";

    /**
     * Thing type ID of Miele cloud bridges / accounts.
     */
    public static final String BRIDGE_TYPE_ID = "account";

    /**
     * The {@link ThingTypeUID} of Miele cloud bridges / accounts.
     */
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_ID);

    /**
     * The {@link ThingTypeUID} of Miele washing machines.
     */
    public static final ThingTypeUID THING_TYPE_WASHING_MACHINE = new ThingTypeUID(BINDING_ID, "washing_machine");

    /**
     * The {@link ThingTypeUID} of Miele washer-dryers.
     */
    public static final ThingTypeUID THING_TYPE_WASHER_DRYER = new ThingTypeUID(BINDING_ID, "washer_dryer");

    /**
     * The {@link ThingTypeUID} of Miele coffee machines.
     */
    public static final ThingTypeUID THING_TYPE_COFFEE_SYSTEM = new ThingTypeUID(BINDING_ID, "coffee_system");

    /**
     * The {@link ThingTypeUID} of Miele fridge-freezers.
     */
    public static final ThingTypeUID THING_TYPE_FRIDGE_FREEZER = new ThingTypeUID(BINDING_ID, "fridge_freezer");

    /**
     * The {@link ThingTypeUID} of Miele fridges.
     */
    public static final ThingTypeUID THING_TYPE_FRIDGE = new ThingTypeUID(BINDING_ID, "fridge");

    /**
     * The {@link ThingTypeUID} of Miele freezers.
     */
    public static final ThingTypeUID THING_TYPE_FREEZER = new ThingTypeUID(BINDING_ID, "freezer");

    /**
     * The {@link ThingTypeUID} of Miele ovens.
     */
    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, "oven");

    /**
     * The {@link ThingTypeUID} of Miele hobs.
     */
    public static final ThingTypeUID THING_TYPE_HOB = new ThingTypeUID(BINDING_ID, "hob");

    /**
     * The {@link ThingTypeUID} of Miele wine storages.
     */
    public static final ThingTypeUID THING_TYPE_WINE_STORAGE = new ThingTypeUID(BINDING_ID, "wine_storage");

    /**
     * The {@link ThingTypeUID} of Miele dishwashers.
     */
    public static final ThingTypeUID THING_TYPE_DISHWASHER = new ThingTypeUID(BINDING_ID, "dishwasher");

    /**
     * The {@link ThingTypeUID} of Miele dryers.
     */
    public static final ThingTypeUID THING_TYPE_DRYER = new ThingTypeUID(BINDING_ID, "dryer");

    /**
     * The {@link ThingTypeUID} of Miele hoods.
     */
    public static final ThingTypeUID THING_TYPE_HOOD = new ThingTypeUID(BINDING_ID, "hood");

    /**
     * The {@link ThingTypeUID} of Miele dish warmers.
     */
    public static final ThingTypeUID THING_TYPE_DISH_WARMER = new ThingTypeUID(BINDING_ID, "dish_warmer");

    /**
     * The {@link ThingTypeUID} of Miele robotic vacuum cleaners.
     */
    public static final ThingTypeUID THING_TYPE_ROBOTIC_VACUUM_CLEANER = new ThingTypeUID(BINDING_ID,
            "robotic_vacuum_cleaner");

    /**
     * Name of the property storing the OAuth2 access token.
     */
    public static final String PROPERTY_ACCESS_TOKEN = "accessToken";

    /**
     * Name of the configuration parameter for the e-mail address.
     */
    public static final String CONFIG_PARAM_EMAIL = "email";

    /**
     * Name of the configuration parameter for the device identifier uniquely identifying a Miele device.
     */
    public static final String CONFIG_PARAM_DEVICE_IDENTIFIER = "deviceIdentifier";

    /**
     * Name of the configuration parameter for the locale. The locale is stored as a 2-letter language code.
     */
    public static final String CONFIG_PARAM_LOCALE = "locale";

    /**
     * Name of the property storing the number of plates for hobs.
     */
    public static final String PROPERTY_PLATE_COUNT = "plateCount";

    /**
     * Constants for all channels.
     */
    public static final class Channels {
        private Channels() {
        }

        public static final String REMOTE_CONTROL_CAN_BE_STARTED = "remote_control_can_be_started";
        public static final String REMOTE_CONTROL_CAN_BE_STOPPED = "remote_control_can_be_stopped";
        public static final String REMOTE_CONTROL_CAN_BE_PAUSED = "remote_control_can_be_paused";
        public static final String REMOTE_CONTROL_CAN_BE_SWITCHED_ON = "remote_control_can_be_switched_on";
        public static final String REMOTE_CONTROL_CAN_BE_SWITCHED_OFF = "remote_control_can_be_switched_off";
        public static final String REMOTE_CONTROL_CAN_SET_PROGRAM_ACTIVE = "remote_control_can_set_program_active";
        public static final String SPINNING_SPEED = "spinning_speed";
        public static final String SPINNING_SPEED_RAW = "spinning_speed_raw";
        public static final String PROGRAM_ACTIVE = "program_active";
        public static final String PROGRAM_ACTIVE_RAW = "program_active_raw";
        public static final String DISH_WARMER_PROGRAM_ACTIVE = "dish_warmer_program_active";
        public static final String VACUUM_CLEANER_PROGRAM_ACTIVE = "vacuum_cleaner_program_active";
        public static final String PROGRAM_PHASE = "program_phase";
        public static final String PROGRAM_PHASE_RAW = "program_phase_raw";
        public static final String OPERATION_STATE = "operation_state";
        public static final String OPERATION_STATE_RAW = "operation_state_raw";
        public static final String PROGRAM_START_STOP = "program_start_stop";
        public static final String PROGRAM_START_STOP_PAUSE = "program_start_stop_pause";
        public static final String POWER_ON_OFF = "power_state_on_off";
        public static final String FINISH_STATE = "finish_state";
        public static final String DELAYED_START_TIME = "delayed_start_time";
        public static final String PROGRAM_REMAINING_TIME = "program_remaining_time";
        public static final String PROGRAM_ELAPSED_TIME = "program_elapsed_time";
        public static final String PROGRAM_PROGRESS = "program_progress";
        public static final String DRYING_TARGET = "drying_target";
        public static final String DRYING_TARGET_RAW = "drying_target_raw";
        public static final String PRE_HEAT_FINISHED = "pre_heat_finished";
        public static final String TEMPERATURE_TARGET = "temperature_target";
        public static final String TEMPERATURE_CURRENT = "temperature_current";
        public static final String TEMPERATURE_CORE_TARGET = "temperature_core_target";
        public static final String TEMPERATURE_CORE_CURRENT = "temperature_core_current";
        public static final String VENTILATION_POWER = "ventilation_power";
        public static final String VENTILATION_POWER_RAW = "ventilation_power_raw";
        public static final String ERROR_STATE = "error_state";
        public static final String INFO_STATE = "info_state";
        public static final String FRIDGE_SUPER_COOL = "fridge_super_cool";
        public static final String FREEZER_SUPER_FREEZE = "freezer_super_freeze";
        public static final String SUPER_COOL_CAN_BE_CONTROLLED = "super_cool_can_be_controlled";
        public static final String SUPER_FREEZE_CAN_BE_CONTROLLED = "super_freeze_can_be_controlled";
        public static final String FRIDGE_TEMPERATURE_TARGET = "fridge_temperature_target";
        public static final String FRIDGE_TEMPERATURE_CURRENT = "fridge_temperature_current";
        public static final String FREEZER_TEMPERATURE_TARGET = "freezer_temperature_target";
        public static final String FREEZER_TEMPERATURE_CURRENT = "freezer_temperature_current";
        public static final String TOP_TEMPERATURE_TARGET = "top_temperature_target";
        public static final String TOP_TEMPERATURE_CURRENT = "top_temperature_current";
        public static final String MIDDLE_TEMPERATURE_TARGET = "middle_temperature_target";
        public static final String MIDDLE_TEMPERATURE_CURRENT = "middle_temperature_current";
        public static final String BOTTOM_TEMPERATURE_TARGET = "bottom_temperature_target";
        public static final String BOTTOM_TEMPERATURE_CURRENT = "bottom_temperature_current";
        public static final String LIGHT_SWITCH = "light_switch";
        public static final String LIGHT_CAN_BE_CONTROLLED = "light_can_be_controlled";
        public static final String PLATE_1_POWER_STEP = "plate_1_power_step";
        public static final String PLATE_1_POWER_STEP_RAW = "plate_1_power_step_raw";
        public static final String PLATE_2_POWER_STEP = "plate_2_power_step";
        public static final String PLATE_2_POWER_STEP_RAW = "plate_2_power_step_raw";
        public static final String PLATE_3_POWER_STEP = "plate_3_power_step";
        public static final String PLATE_3_POWER_STEP_RAW = "plate_3_power_step_raw";
        public static final String PLATE_4_POWER_STEP = "plate_4_power_step";
        public static final String PLATE_4_POWER_STEP_RAW = "plate_4_power_step_raw";
        public static final String PLATE_5_POWER_STEP = "plate_5_power_step";
        public static final String PLATE_5_POWER_STEP_RAW = "plate_5_power_step_raw";
        public static final String PLATE_6_POWER_STEP = "plate_6_power_step";
        public static final String PLATE_6_POWER_STEP_RAW = "plate_6_power_step_raw";
        public static final String DOOR_STATE = "door_state";
        public static final String DOOR_ALARM = "door_alarm";
        public static final String BATTERY_LEVEL = "battery_level";
    }

    /**
     * Constants for i18n keys.
     */
    public static final class I18NKeys {
        private I18NKeys() {
        }

        public static final String BRIDGE_STATUS_DESCRIPTION_ACCESS_TOKEN_NOT_CONFIGURED = "@text/mielecloud.bridge.status.access.token.not.configured";
        public static final String BRIDGE_STATUS_DESCRIPTION_ACCOUNT_NOT_AUTHORIZED = "@text/mielecloud.bridge.status.account.not.authorized";
        public static final String BRIDGE_STATUS_DESCRIPTION_ACCESS_TOKEN_REFRESH_FAILED = "@text/mielecloud.bridge.status.access.token.refresh.failed";
        public static final String BRIDGE_STATUS_DESCRIPTION_TRANSIENT_HTTP_ERROR = "@text/mielecloud.bridge.status.transient.http.error";

        public static final String THING_STATUS_DESCRIPTION_WEBSERVICE_MISSING = "@text/mielecloud.thing.status.webservice.missing";
        public static final String THING_STATUS_DESCRIPTION_REMOVED = "@text/mielecloud.thing.status.removed";
        public static final String THING_STATUS_DESCRIPTION_RATELIMIT = "@text/mielecloud.thing.status.ratelimit";
        public static final String THING_STATUS_DESCRIPTION_DISCONNECTED = "@text/mielecloud.thing.status.disconnected";
    }
}
