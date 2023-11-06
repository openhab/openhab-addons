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
/* This file is based on:
 *
 * ResponseListener
 * Connect SDK
 *
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 19 Jan 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhab.binding.lgwebos.internal.handler.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Generic asynchronous operation response success handler block. If there is any response data to be processed, it will
 * be provided via the responseObject parameter.
 *
 * @author Hyun Kook Khang - Connect SDK initial contribution
 * @author Sebastian Prehn - Adoption for openHAB
 */
@NonNullByDefault
public interface ResponseListener<T> {

    /**
     * Returns the success of the call of type T.
     * Contains the output data as a generic object reference.
     * This value may be any of a number of types as defined by T in subclasses of ResponseListener.
     *
     * @param responseObject Response object, can be any number of object types, depending on the
     *            protocol/capability/etc
     */
    void onSuccess(T responseObject);

    /**
     * Method to return the error message that was generated.
     *
     */
    void onError(String message);
}
