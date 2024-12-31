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
package org.openhab.binding.chatgpt.internal.dto;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatFunction {

    private String name;
    private String description;
    private Parameters parameters;

    @JsonIgnore
    private Function<Object, Object> executor;

    @JsonIgnore
    private Class<?> parametersClass;

    public ChatFunction() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public Function<Object, Object> getExecutor() {
        return executor;
    }

    public Class<?> getParametersClass() {
        return parametersClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExecutor(Function<Object, Object> executor) {
        this.executor = executor;
    }

    public void setParameters(Parameters requestClass) {
        this.parameters = requestClass;
    }

    public void setParametersClass(Class<?> parametersClass) {
        this.parametersClass = parametersClass;
    }
}
