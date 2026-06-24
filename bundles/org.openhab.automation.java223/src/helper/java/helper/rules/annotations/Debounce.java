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
package helper.rules.annotations;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation ads a debouncing behavior to the code run by a rule action.
 * When using it on a method or a 'Runnable' field, the bundle will always run code in another thread.
 * If FIRST_ONLY type is specified (default), then all executions after the first one occurring within the delay
 * will be discarded.
 * If LAST_ONLY type is specified, then only the last execution occurring within the delay period will be kept.
 * If STABLE type is specified, then all executions will be kept until a full stable delay period (with no new call) ends.
 *
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@NonNullByDefault
public @interface Debounce {

    /**
     * Debounce type
     */
    Type type() default Type.FIRST_ONLY;

    /**
     * Delay in milliseconds
     */
    long value() default 1000;

    enum Type {
        /**
         * All executions after the first one occurring within the delay
         *  * will be discarded.
         */
        FIRST_ONLY,

        /**
         * Only the last execution occurring within the delay period will be kept.
         */
        LAST_ONLY,

        /**
         * All executions will be kept until a full stable delay period (with no new call) occurs.
         */
        STABLE;
    }
}
