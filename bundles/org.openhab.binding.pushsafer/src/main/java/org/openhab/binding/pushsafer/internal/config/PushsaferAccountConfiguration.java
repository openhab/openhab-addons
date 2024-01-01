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
package org.openhab.binding.pushsafer.internal.config;

import static org.openhab.binding.pushsafer.internal.PushsaferBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PushsaferAccountConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
 */
@NonNullByDefault
public class PushsaferAccountConfiguration {
    public @Nullable String apikey;
    public @Nullable String user;
    public String device = ALL_DEVICES;
    public String title = DEFAULT_TITLE;
    public String format = "none";
    public String sound = DEFAULT_SOUND;
    public String icon = DEFAULT_ICON;
    public String color = DEFAULT_COLOR;
    public String url = DEFAULT_URL;
    public String urlTitle = DEFAULT_URLTITLE;
    public boolean answer = DEFAULT_ANSWER;
    public int confirm = DEFAULT_CONFIRM;
    public int time2live = DEFAULT_TIME2LIVE;
    public String vibration = DEFAULT_VIBRATION;
    public int retry = 300;
    public int expire = 3600;
}
