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
package org.openhab.binding.mqtt.generic.mapping;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If the MQTT topic value needs to be transformed first before assigned to a field,
 * annotate that field with this annotation.
 *
 * <p>
 * Example: Two MQTT topics are "my-example/testname" with value "abc" and "my-example/values" with value "abc,def". The
 * corresponding attribute class looks like this:
 * </p>
 *
 * <pre>
 * class MyExample extends AbstractMqttAttributeClass {
 *     enum Testnames {
 *         abc_
 *     };
 *
 *     &#64;MapToField(suffix = "_")
 *     Testnames testname;
 *
 *     &#64;MapToField(splitCharacter = ",")
 *     String[] values;
 * }
 * </pre>
 *
 * @author David Graeff - Initial contribution
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface MQTTvalueTransform {
    String suffix() default "";

    String prefix() default "";

    String splitCharacter() default "";
}
