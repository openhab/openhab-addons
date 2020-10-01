/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link RequestAnnotation} provides informations needed at
 * runtime in order to handle properly API calls
 *
 * @author Gaël L'hopital - Initial contribution
 *
 *         responseClass : the answer class used to interpret the request answer
 *         relativeUrl : url used to execute the request
 *         retryAuth : true if the request must be resent after potential authentication error
 *         method : get, put or post
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestAnnotation {
    Class<?> responseClass() default EmptyResponse.class;

    String relativeUrl();

    int maxRetries() default 3;

    String method() default "GET";

    boolean endsWithSlash() default true;
}
