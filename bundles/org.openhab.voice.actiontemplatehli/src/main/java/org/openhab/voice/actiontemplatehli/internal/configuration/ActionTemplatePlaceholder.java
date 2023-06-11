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
package org.openhab.voice.actiontemplatehli.internal.configuration;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link ActionTemplatePlaceholder} class configures placeholders for the action template
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplatePlaceholder {
    @JsonProperty(value = "label", required = true)
    public String label = "";
    @JsonProperty("ner")
    public @Nullable String nerFile = null;
    @JsonProperty("nerValues")
    public String @Nullable [] nerStaticValues = null;
    @JsonProperty("pos")
    public @Nullable String posFile = null;
    @JsonProperty("posValues")
    public @Nullable Map<String, String> posStaticValues = null;

    public static ActionTemplatePlaceholder withLabel(String label) {
        var placeholder = new ActionTemplatePlaceholder();
        placeholder.label = label;
        return placeholder;
    }
}
