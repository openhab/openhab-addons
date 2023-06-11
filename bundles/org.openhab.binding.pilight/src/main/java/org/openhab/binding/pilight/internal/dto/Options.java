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
package org.openhab.binding.pilight.internal.dto;

import org.openhab.binding.pilight.internal.serializers.BooleanToIntegerSerializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Options that can be set as a pilight client.
 *
 * @author Jeroen Idserda - Initial contribution
 * @author Stefan Röllin - Port to openHAB 2 pilight binding
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
public class Options {

    public static final String MEDIA_ALL = "all";

    public static final String MEDIA_WEB = "web";

    public static final String MEDIA_MOBILE = "mobile";

    public static final String MEDIA_DESKTOP = "desktop";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BooleanToIntegerSerializer.class)
    private Boolean core;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BooleanToIntegerSerializer.class)
    private Boolean receiver;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BooleanToIntegerSerializer.class)
    private Boolean config;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BooleanToIntegerSerializer.class)
    private Boolean forward;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = BooleanToIntegerSerializer.class)
    private Boolean stats;

    private String uuid;

    private String media;

    public Boolean getCore() {
        return core;
    }

    public void setCore(Boolean core) {
        this.core = core;
    }

    public Boolean getReceiver() {
        return receiver;
    }

    public void setReceiver(Boolean receiver) {
        this.receiver = receiver;
    }

    public Boolean getConfig() {
        return config;
    }

    public void setConfig(Boolean config) {
        this.config = config;
    }

    public Boolean getForward() {
        return forward;
    }

    public void setForward(Boolean forward) {
        this.forward = forward;
    }

    public Boolean getStats() {
        return stats;
    }

    public void setStats(Boolean stats) {
        this.stats = stats;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }
}
