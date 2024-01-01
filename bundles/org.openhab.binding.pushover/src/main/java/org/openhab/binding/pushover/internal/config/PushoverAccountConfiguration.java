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
package org.openhab.binding.pushover.internal.config;

import static org.openhab.binding.pushover.internal.PushoverBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pushover.internal.dto.Sound;

/**
 * The {@link PushoverAccountConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PushoverAccountConfiguration {
    public static final Sound SOUND_DEFAULT = new Sound("default", "Default");
    public static final List<Sound> DEFAULT_SOUNDS = List.of(new Sound("alien", "Alien Alarm (long)"),
            new Sound("bike", "Bike"), new Sound("bugle", "Bugle"), new Sound("cashregister", "Cash Register"),
            new Sound("classical", "Classical"), new Sound("climb", "Climb (long)"), new Sound("cosmic", "Cosmic"),
            new Sound("falling", "Falling"), new Sound("gamelan", "Gamelan"), new Sound("incoming", "Incoming"),
            new Sound("intermission", "Intermission"), new Sound("magic", "Magic"),
            new Sound("mechanical", "Mechanical"), new Sound("none", "None (silent)"),
            new Sound("persistent", "Persistent (long)"), new Sound("pianobar", "Piano Bar"),
            new Sound("pushover", "Pushover (default)"), new Sound("echo", "Pushover Echo (long)"),
            new Sound("siren", "Siren"), new Sound("spacealarm", "Space Alarm"), new Sound("tugboat", "Tug Boat"),
            new Sound("updown", "Up Down (long)"), new Sound("vibrate", "Vibrate Only"), SOUND_DEFAULT);

    public @Nullable String apikey;
    public @Nullable String user;
    public String title = DEFAULT_TITLE;
    public String format = "none";
    public String sound = DEFAULT_SOUND;
    public int retry = 300;
    public int expire = 3600;
    public int timeout = 10;
}
