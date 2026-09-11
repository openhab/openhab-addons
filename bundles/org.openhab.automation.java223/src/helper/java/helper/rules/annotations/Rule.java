/**
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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.openhab.automation.java223.common.Java223Constants.ANNOTATION_DEFAULT;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Gwendal Roulleau - Initial contribution
 */

@Retention(RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
@NonNullByDefault
public @interface Rule {

    String name() default ANNOTATION_DEFAULT;

    String description() default ANNOTATION_DEFAULT;

    String[] tags() default {};

    boolean disabled() default false;

    String uid() default ANNOTATION_DEFAULT;
}
