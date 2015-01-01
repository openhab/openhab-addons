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

import static org.openhab.io.rest.docs.swagger.Constants.DEFAULT_API_VERSION;
import static org.openhab.io.rest.docs.swagger.Constants.SWAGGER_API_VERSION;

import java.util.List;

/* Data Objects for JSON, as defined in the spec: https://github.com/wordnik/swagger-core/wiki */
public class SwaggerAPI {
    public final String apiVersion;
    public final String swaggerVersion;
    public final String basePath;
    public final String resourcePath;
    public final List<SwaggerAPIPath> apis;
    public final SwaggerModel models;

    public SwaggerAPI(String basePath, String resourcePath, List<SwaggerAPIPath> apis, SwaggerModel models) {
        this(DEFAULT_API_VERSION, basePath, resourcePath, apis, models);
    }

    public SwaggerAPI(String apiVersion, String basePath, String resourcePath, List<SwaggerAPIPath> apis,
        SwaggerModel models) {
        this.apiVersion = apiVersion;
        this.swaggerVersion = SWAGGER_API_VERSION;

        this.basePath = basePath;
        this.resourcePath = resourcePath;
        this.apis = apis;
        this.models = models;
    }
}