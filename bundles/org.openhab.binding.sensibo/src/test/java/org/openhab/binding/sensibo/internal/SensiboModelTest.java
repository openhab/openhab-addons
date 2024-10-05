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
package org.openhab.binding.sensibo.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetailsDTO;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;

/**
 * @author Arne Seime - Initial contribution
 * 
 */
public class SensiboModelTest {

    private final WireHelper wireHelper = new WireHelper();

    @Test
    public void testCaseInsensitiveMacAddress() throws IOException {
        final PodDetailsDTO rsp = wireHelper.deSerializeResponse("/get_pod_details_response.json", PodDetailsDTO.class);
        SensiboSky sky = new SensiboSky(rsp);

        SensiboModel model = new SensiboModel(0);
        model.addPod(sky);

        assertFalse(model.findSensiboSkyByMacAddress("MA:C:AD:DR:ES:XX").isPresent());
        assertTrue(model.findSensiboSkyByMacAddress("MA:C:AD:DR:ES:S0").isPresent());
        assertTrue(model.findSensiboSkyByMacAddress("MA:C:AD:DR:ES:S0".toLowerCase()).isPresent());
    }
}
