/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.automation.java223.common;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Mark a class candidate for a singleton instantiation.
 * The java223 module will try to use an already created instance
 * instead of creating one on each execution.
 * The instance will be searched in the cache.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
@NonNullByDefault
public @interface ReuseScriptInstance {
    boolean value() default true;
}
