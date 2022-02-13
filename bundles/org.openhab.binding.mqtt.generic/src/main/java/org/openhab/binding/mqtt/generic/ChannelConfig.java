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
package org.openhab.binding.mqtt.generic;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;

/**
 * A user can add custom channels to an MQTT Thing.
 * <p>
 * This class contains the channel configuration.
 * <p>
 * You may want to extend this for channel configurations of MQTT extensions.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelConfig {
    /** This is either a state topic or a trigger topic, depending on {@link #trigger}. */
    public String stateTopic = "";
    public String commandTopic = "";

    /**
     * If true, the channel state is not updated on a new message.
     * Instead a postCommand() call is performed.
     */
    public boolean postCommand = false;
    public @Nullable Integer qos;
    public boolean retained = false;
    /** If true, the state topic will not update a state, but trigger a channel instead. */
    public boolean trigger = false;
    public String unit = "";

    public String transformationPattern = "";
    public String transformationPatternOut = "";
    public String formatBeforePublish = "%s";
    public String allowedStates = "";

    public @Nullable BigDecimal min;
    public @Nullable BigDecimal max;
    public @Nullable BigDecimal step;
    public @Nullable String on;
    public @Nullable String off;
    public @Nullable String stop;

    public int onBrightness = 10;
    public String colorMode = ColorMode.HSB.toString();
}
