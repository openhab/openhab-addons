/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.speedporthybrid.internal.model;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents the JSON model from the Speedport Hybrid router.
 *
 * @author Henning Treu - initial contribution
 *
 */
public class JsonModelList {

    public List<JsonModel> jsonModels;

    public @Nullable JsonModel getModel(String varId) {
        Optional<JsonModel> model = jsonModels.stream().filter(lm -> lm.isId(varId)).findFirst();
        if (model.isPresent()) {
            return model.get();
        }

        return null;
    }

}
