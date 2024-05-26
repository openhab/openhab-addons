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
package org.openhab.binding.emotiva.internal.dto;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;

import javax.xml.bind.JAXBException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.emotiva.internal.AbstractDTOTestBase;
import org.openhab.binding.emotiva.internal.EmotivaBindingConstants;
import org.openhab.binding.emotiva.internal.protocol.EmotivaSubscriptionTags;

/**
 * Unit tests for EmotivaUpdate requests.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
class EmotivaUpdateRequestTest extends AbstractDTOTestBase {

    public EmotivaUpdateRequestTest() throws JAXBException {
    }

    @Test
    void marshallWithNoProperty() {
        EmotivaUpdateRequest dto = new EmotivaUpdateRequest(Collections.emptyList());
        String xmlAsString = xmlUtils.marshallJAXBElementObjects(dto);
        assertThat(xmlAsString, containsString("<emotivaUpdate/>"));
        assertThat(xmlAsString, not(containsString("<property")));
        assertThat(xmlAsString, not(containsString("</emotivaUpdate>")));
    }

    @Test
    void marshalFromChannelUID() {
        EmotivaSubscriptionTags subscriptionChannel = EmotivaSubscriptionTags
                .fromChannelUID(EmotivaBindingConstants.CHANNEL_TUNER_RDS);
        EmotivaUpdateRequest emotivaUpdateRequest = new EmotivaUpdateRequest(subscriptionChannel);
        String xmlString = xmlUtils.marshallJAXBElementObjects(emotivaUpdateRequest);
        assertThat(xmlString, containsString("<emotivaUpdate>"));
        assertThat(xmlString, containsString("<tuner_RDS />"));
        assertThat(xmlString, containsString("</emotivaUpdate>"));
    }
}
