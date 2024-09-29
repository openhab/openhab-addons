/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.mapping;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate an attribute class or class fields if the MQTT topic differs compared to the field name by a prefix.
 * The default prefix if annotated without an argument is "$".
 *
 * <p>
 * Example: The MQTT topic is "my-example/$testname". The corresponding attribute class looks like this:
 * </p>
 *
 * <pre>
 * &#64;TopicPrefix
 * class MyExample extends AbstractMqttAttributeClass {
 *     String testname;
 * }
 * </pre>
 *
 * @author David Graeff - Initial contribution
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, FIELD })
public @interface TopicPrefix {
    String value() default "$";
}
