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

import static org.openhab.automation.java223.common.Java223Constants.ANNOTATION_DEFAULT;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This annotation tags fields with an injection intent and related details.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
@NonNullByDefault
public @interface InjectBinding {

    /**
     * Prevent injection. Useful if you want to do it yourself.
     *
     * @return If the binding should not be injected in the annotated element
     */
    boolean disable() default false;

    /**
     * If set, use this name. Else try to get the name from the code.
     *
     * @return The name to search in the openHAB bindings
     */
    String named() default ANNOTATION_DEFAULT;

    /**
     * If set, instructs the framework to get value inside a preset
     *
     * @return The name of the preset
     */
    String preset() default ANNOTATION_DEFAULT;

    /**
     * If true and no value to inject are found, the binding process will fail with an exception.
     * When using this annotation, "true" is the default.
     * If you don't use the annotation, the parameter is NOT mandatory)
     * If false, a null value is allowed.
     *
     * @return True if the value searched MUST be present in the openHAB binding
     */
    boolean mandatory() default true;

    /**
     * if true, the injected value will also be inspected to be injected recursively.
     */
    boolean recursive() default true;
}
