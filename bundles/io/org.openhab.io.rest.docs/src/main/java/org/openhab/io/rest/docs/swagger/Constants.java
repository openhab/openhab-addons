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
package org.openhab.io.rest.docs.swagger;

public interface Constants {

    /**
     * Version string used for "unversioned" APIs.
     */
    String DEFAULT_API_VERSION = "0.8.0";

    /**
     * The version of the Swagger API used.
     */
    String SWAGGER_API_VERSION = "1.2";
    
    String PATH_PARAM = "path";
    String QUERY_PARAM = "query";
    String HEADER_PARAM = "header";
    String BODY_PARAM = "body";
    String FORM_PARAM = "form";
    
    String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
}
