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

/* Data Objects for JSON, as defined in the spec: https://github.com/wordnik/swagger-core/wiki */
public class SwaggerOperation {
    public final String method;
    public final String nickname;
    public final String type;
    public final String format;
    public final List<SwaggerParameter> parameters;
    public final List<SwaggerResponseMessage> responseMessages;
    public final List<String> consumes;
    public final List<String> produces;
    public final String summary;
    public final String notes;
    public final String deprecated;

    public SwaggerOperation(String httpMethod, String nickname, SwaggerDataType responseType,
        List<SwaggerParameter> parameters, List<SwaggerResponseMessage> responseMessages, List<String> produces, List<String> consumes, String summary, String notes, Boolean deprecated) {
        this.method = httpMethod;
        this.nickname = nickname;
        this.type = responseType.dataType;
        this.format = responseType.dataFormat;
        this.parameters = parameters;
        this.responseMessages = responseMessages;
        this.consumes = consumes;
        this.produces = produces;
        this.summary = summary;
        this.notes = notes;
        this.deprecated = Boolean.toString(deprecated);
    }
}