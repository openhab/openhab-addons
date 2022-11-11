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
package org.openhab.voice.actiontemplatehli.internal.configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link ActionTemplateConfiguration} class represents each configured action
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplateConfiguration {
    @JsonProperty("type")
    public String type = "tokens";
    @JsonProperty("read")
    public boolean read = false;
    @JsonProperty(value = "template", required = true)
    public String template = "";
    @JsonProperty("value")
    public @Nullable Object value = null;
    @JsonProperty("emptyValue")
    public String emptyValue = "";
    @JsonProperty("placeholders")
    public List<ActionTemplatePlaceholder> placeholders = List.of();
    @JsonProperty("requiredTags")
    public String[] requiredItemTags = new String[] {};
    @JsonProperty("silent")
    public boolean silent = false;
    @JsonProperty("memberTargets")
    public @Nullable ActionTemplateGroupTargets memberTargets = null;

    public static ActionTemplateConfiguration[] fromMetadata(Metadata metadata) throws JsonProcessingException {
        var configuration = metadata.getConfiguration();
        var multipleValues = configuration.get("multiple");
        ObjectMapper mapper = new ObjectMapper();
        if (multipleValues != null) {
            return mapper.readValue(mapper.writeValueAsString(multipleValues), ActionTemplateConfiguration[].class);
        } else {
            var actionConfig = mapper.readValue(mapper.writeValueAsString(configuration),
                    ActionTemplateConfiguration.class);
            return new ActionTemplateConfiguration[] { actionConfig };
        }
    }

    public static ActionTemplateConfiguration[] fromJSON(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonFile, ActionTemplateConfiguration[].class);
    }
}
