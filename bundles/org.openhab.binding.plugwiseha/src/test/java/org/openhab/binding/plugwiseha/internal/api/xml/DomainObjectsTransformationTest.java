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
package org.openhab.binding.plugwiseha.internal.api.xml;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.plugwiseha.internal.api.model.dto.ActuatorFunctionalities;
import org.openhab.binding.plugwiseha.internal.api.model.dto.ActuatorFunctionality;
import org.openhab.binding.plugwiseha.internal.api.model.dto.DomainObjects;

/**
 * Tests actuator functionality recognition through XML transformation and deserialization.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class DomainObjectsTransformationTest {

    @ParameterizedTest
    @ValueSource(strings = { "appliance", "location" })
    void thermostatWithoutTypeIsRecognized(String scope) throws Exception {
        ActuatorFunctionalities functionalities = functionalities(scope, """
                <thermostat_functionality id="thermostat-id"><setpoint>21</setpoint></thermostat_functionality>
                """);

        ActuatorFunctionality thermostat = functionalities.getFunctionalityThermostat().orElseThrow();
        assertEquals("thermostat-id", thermostat.getId());
        assertEquals("21", thermostat.getSetpoint());
        assertEquals(1, functionalities.size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "thermostat", "custom" })
    void explicitTypeIsPreserved(String type) throws Exception {
        ActuatorFunctionalities functionalities = functionalities("appliance",
                """
                        <thermostat_functionality id="thermostat-id"><type>%s</type><setpoint>21</setpoint></thermostat_functionality>
                        """
                        .formatted(type));

        ActuatorFunctionality functionality = requireNonNull(functionalities.get(type));
        assertEquals("thermostat-id", functionality.getId());
        assertEquals("21", functionality.getSetpoint());
        assertEquals(1, functionalities.size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "appliance", "location" })
    void relayWithoutTypeRemainsRecognized(String scope) throws Exception {
        ActuatorFunctionalities functionalities = functionalities(scope, """
                <relay_functionality id="relay-id"><lock>true</lock></relay_functionality>
                """);

        assertTrue(functionalities.getRelayLockState().orElseThrow());
        assertEquals("relay-id", functionalities.getFunctionalityRelay().orElseThrow().getId());
        assertEquals(1, functionalities.size());
    }

    private ActuatorFunctionalities functionalities(String scope, String functionality) throws Exception {
        String xml = """
                <domain_objects>
                    <%1$s id="entity"><actuator_functionalities>%2$s</actuator_functionalities></%1$s>
                </domain_objects>
                """.formatted(scope, functionality);
        try (InputStream stylesheet = requireNonNull(getClass().getResourceAsStream("/domain_objects.xslt"));
                StringReader input = new StringReader(xml);
                StringWriter output = new StringWriter()) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(stylesheet));
            transformer.transform(new StreamSource(input), new StreamResult(output));
            DomainObjects objects = (DomainObjects) new PlugwiseHAXStream().fromXML(output.toString());
            return "appliance".equals(scope)
                    ? requireNonNull(objects.getAppliances().get("entity")).getActuatorFunctionalities()
                    : requireNonNull(objects.getLocations().get("entity")).getActuatorFunctionalities();
        }
    }
}
