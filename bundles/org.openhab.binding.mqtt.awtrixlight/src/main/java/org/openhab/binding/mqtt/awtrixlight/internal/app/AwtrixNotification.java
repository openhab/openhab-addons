/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.awtrixlight.internal.app;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mqtt.awtrixlight.internal.Helper;

/**
 * The {@link AwtrixNotification} is the representation of...
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AwtrixNotification extends AwtrixApp {

    public static final boolean DEFAULT_HOLD = false;
    public static final boolean DEFAULT_WAKEUP = false;
    public static final boolean DEFAULT_STACK = true;
    public static final String DEFAULT_RTTTL = "";
    public static final String DEFAULT_SOUND = "";
    public static final boolean DEFAULT_SOUND_LOOP = false;

    private boolean hold = DEFAULT_HOLD;
    private boolean wakeUp = DEFAULT_WAKEUP;
    private boolean stack = DEFAULT_STACK;
    private String rtttl = DEFAULT_RTTTL;
    private String sound = DEFAULT_SOUND;
    private boolean loopSound = DEFAULT_SOUND_LOOP;

    private Map<String, Object> getNotificationParams() {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("hold", this.hold);
        fields.put("wakeUp", this.wakeUp);
        fields.put("stack", this.stack);
        fields.put("rtttl", this.rtttl);
        fields.put("sound", this.sound);
        fields.put("loopSound", this.loopSound);
        return fields;
    }

    public String getNotificationConfig() {
        Map<String, Object> params = super.getAppParams();
        params.putAll(getNotificationParams());
        return Helper.encodeJson(params);
    }

    public boolean isHold() {
        return hold;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public boolean isWakeUp() {
        return wakeUp;
    }

    public void setWakeUp(boolean wakeUp) {
        this.wakeUp = wakeUp;
    }

    public boolean isStack() {
        return stack;
    }

    public void setStack(boolean stack) {
        this.stack = stack;
    }

    public String getRtttl() {
        return rtttl;
    }

    public void setRtttl(String rtttl) {
        this.rtttl = rtttl;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public boolean isLoopSound() {
        return loopSound;
    }

    public void setLoopSound(boolean loopSound) {
        this.loopSound = loopSound;
    }
}
