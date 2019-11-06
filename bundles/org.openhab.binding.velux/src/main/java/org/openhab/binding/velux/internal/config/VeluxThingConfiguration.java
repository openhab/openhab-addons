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
package org.openhab.binding.velux.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.openhab.binding.velux.VeluxBindingConstants;
import org.openhab.binding.velux.things.VeluxProductSerialNo;

/**
 * The {@link VeluxThingConfiguration} is a wrapper for
 * configuration settings needed to access the <B>Velux</B> device.
 * <p>
 * It contains the factory default values as well.
 * <p>
 * There are three parts. Information for:
 * <UL>
 * <LI>{@link #sceneName Channel of type scene},</LI>
 * <LI>{@link #serial Channels of type actuator, rollershutter and window},</LI>
 * <LI>{@link #sceneLevels Channel of type virtualshutter}.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 * @author Andrew Fiddian-Green - adapted.
 */
@NonNullByDefault
public class VeluxThingConfiguration extends Configuration {

    /**
     * {@link #sceneName} of type {@link String}, identifying a Velux scene by human-readable name.
     * <P>
     * <B>Configuration for the channel scene:</B>
     * </P>
     * <UL>
     * <LI>{@link #sceneName} for identification of a set of settings, so called scene.</LI>
     * </UL>
     */
    @SuppressWarnings("unused")
    private String sceneName;

    /**
     * {@link #serial} of type {@link String}, identifying a io-homecontrol device by its serial number (i.e.
     * 43:12:14:5A:12:1C:05:5F).
     * <P>
     * <B>Configuration for the channels actuator, rollershutter and window:</B>
     * </P>
     * <UL>
     * <LI>{@link #serial} for identification of a io-homecontrol device,</LI>
     * <LI>{@link #name} for alternate identification of a io-homecontrol device,</LI>
     * <LI>{@link #inverted} for modified value behavior.</LI>
     * <LI>{@link #velocity} for modified action speed.</LI>
     * </UL>
     */
    @SuppressWarnings("unused")
    private String serial;
    /**
     * {@link #name} of type {@link String}, identifying a io-homecontrol device by its registration name especially
     * for <B>somfy</B> as they do not provide a valid serial number.
     * <P>
     * Part of the {@link #serial Configuration for the channels actuator, rollershutter and window}.
     * </P>
     */
    @SuppressWarnings("unused")
    private String name;
    /**
     * {@link #inverted} of type {@link Boolean}, inverts each Channel value. This means 0% will be handled as 100%,
     * and vice versa, 100% will be handled as 0%.
     * <P>
     * Part of the {@link #serial Configuration for the channels actuator, rollershutter and window}.
     * </P>
     */
    @SuppressWarnings("unused")
    private Boolean inverted;
    /**
     * {@link #velocity} of type {@link String}, describes the intended speed of action.
     * Possible values are defined within VeluxProductVelocity.
     * <P>
     * Part of the {@link #serial Configuration for the channels actuator, rollershutter and window}.
     * </P>
     */
    @SuppressWarnings("unused")
    private String velocity;

    /**
     * {@link #sceneLevels} of type {@link String}, identifying a number of Velux scenes which act together as a virtual
     * shutter. Each scene is defined to a corresponding shutter level.
     * <P>
     * <B>Configuration for the channel virtualshutter:</B>
     * </P>
     * <UL>
     * <LI>{@link #sceneLevels} for identification of a set of settings, so called scene.</LI>
     * </UL>
     * <P>
     * Additionally it contains an internal variable for keeping the actual virtual shutter level.
     * </P>
     * <UL>
     * <LI>{@link #currentLevel} for identification of a set of settings, so called scene.</LI>
     * </UL>
     */
    @SuppressWarnings("unused")
    private String sceneLevels;
    /**
     * {@link #currentLevel} of type {@link int}, which represents the current shutter level.
     * <P>
     * Private part of the {@link #sceneLevels Configuration for the channel virtualshutter}.
     * </P>
     */
    @SuppressWarnings("unused")
    private int currentLevel;

    /**
     * Default values - should not be modified
     */
    public VeluxThingConfiguration() {
        //
        sceneName = VeluxBindingConstants.UNKNOWN;
        //
        serial = VeluxProductSerialNo.UNKNOWN;
        name = VeluxBindingConstants.UNKNOWN;
        inverted = false;
        velocity = VeluxBindingConstants.UNKNOWN;
        //
        sceneLevels = VeluxBindingConstants.UNKNOWN;
        currentLevel = 0;
    }

}
/*
 * end-of-internal/config/VeluxThingConfiguration.java
 */
