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

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate an attribute class field to mark it as required. If a required topic value cannot be received
 * within a given timeframe, the entire attribute classes
 * {@link AbstractMqttAttributeClass#subscribeAndReceive(org.openhab.core.io.transport.mqtt.MqttBrokerConnection, java.util.concurrent.ScheduledExecutorService, String, org.openhab.binding.mqtt.generic.internal.mapping.AbstractMqttAttributeClass.AttributeChanged, int)}
 * call will fail.
 *
 * <p>
 * Example: The MQTT topic is "my-example/name". The corresponding attribute class looks like this:
 * </p>
 *
 * <pre>

 * class MyExample extends AbstractMqttAttributeClass {
 * * &#64;MandatoryField
 *     String name;
 * }
 * </pre>
 *
 * @author David Graeff - Initial contribution
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD })
public @interface MandatoryField {
    boolean value() default true;
}
