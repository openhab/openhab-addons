/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HomieBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieBindingConstants {

    /**
     * Separator for MQTT Topic segments
     */
    public final static String MQTT_TOPIC_SEPARATOR = "/";

    /**
     * ID of this binding
     */
    public static final String BINDING_ID = "homie";

    /**
     * UID for Homie Device
     */
    public final static ThingTypeUID HOMIE_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "homieDeviceV2");

    /**
     * Timeout for Homie discovery procedure
     */
    public final static int DEVICE_DISCOVERY_TIMEOUT_SECONDS = 30;

    /**
     * MQTT ClientID Prefix
     */
    public final static String MQTT_CLIENTID = "homieOpenhab2Binding";

    /**
     * Homie Device property key for homie specification version
     */
    public final static String THING_PROP_SPEC_VERSION = "homie-specification-version";

    /**
     * Homie Device property key for implementation version
     */
    public final static String THING_PROP_IMPL_VERSION = "implementation-version";

    /**
     * Channel property key for readonly marker
     */
    public static final String CHANNELPROPERTY_READONLY = "readonly";

    /**
     * Channel property key for topic suffix
     */
    public static final String CHANNELPROPERTY_TOPICSUFFIX = "topic_suffix";

    /**
     * Channel property key for thingstate marker
     */
    public static final String CHANNELPROPERTY_THINGSTATEINDICATOR = "thing_state_indicator";

}
