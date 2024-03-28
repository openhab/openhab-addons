/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.internal.errors.LGThinqException;
import org.openhab.binding.lgthinq.lgservices.FeatureDefinition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link AbstractJsonCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractCapabilityFactory<T extends CapabilityDefinition> {
    protected ObjectMapper mapper = new ObjectMapper();

    public T create(JsonNode rootNode) throws LGThinqException {
        T cap = getCapabilityInstance();
        cap.setDeviceType(ModelUtils.getDeviceType(rootNode));
        cap.setDeviceVersion(ModelUtils.discoveryAPIVersion(rootNode));
        return cap;
    }

    protected abstract List<DeviceTypes> getSupportedDeviceTypes();

    protected abstract List<LGAPIVerion> getSupportedAPIVersions();

    protected abstract FeatureDefinition getFeatureDefinition(String featureName, JsonNode featuresNode);

    protected abstract T getCapabilityInstance();

    protected void validateMandatoryNote(JsonNode node) throws LGThinqException {
        if (node.isMissingNode()) {
            throw new LGThinqApiException(
                    String.format("Error extracting mandatory %s node for this device cap file", node));
        }
    }
}
