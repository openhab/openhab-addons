/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.io.rest.docs.swagger.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/* Data Objects for JSON, as defined in the spec: https://github.com/wordnik/swagger-core/wiki */
public class SwaggerParameter {
    public final String paramType;
    public final String name;
    public final String description;
    public final String type;
    public final String format;
    @SerializedName("enum")
    public final List<String> enumValues;
    public final Boolean required;
    public final String defaultValue;

    public SwaggerParameter(String paramType, String name, String description, SwaggerDataType type, String defaultValue) {
        this.paramType = paramType;
        this.name = name;
        this.description = description;
        this.type = type.dataType;
        this.format = type.dataFormat;
        this.defaultValue = defaultValue;
        this.enumValues = null;
        this.required = false;
    }

    public SwaggerParameter(String paramType, String name, String description, SwaggerDataType type, String defaultValue, List<String> enumValues) {
        this.paramType = paramType;
        this.name = name;
        this.description = description;
        this.type = type.dataType;
        this.format = type.dataFormat;
        this.enumValues = enumValues;
        this.defaultValue = defaultValue;
        this.required = false;
   }
}