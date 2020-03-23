/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.fox.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.fox.internal.core.Fox;
import org.openhab.binding.fox.internal.core.FoxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FoxResultHandler} is responsible for handling system results.
 *
 * @author Kamil Subzda - Initial contribution
 */
@NonNullByDefault
public class FoxResultHandler {

    private final Logger logger = LoggerFactory.getLogger(FoxResultHandler.class);
    private List<StateOption> stateOptions;

    public FoxResultHandler() {
        stateOptions = new ArrayList<StateOption>();
    }

    public void setStates(Map<String, String> options) {
        stateOptions.clear();
        for (String key : options.keySet()) {
            stateOptions.add(new StateOption(key, options.get(key)));
        }
    }

    public List<StateOption> listStates() {
        logger.debug("Results options: {}", stateOptions.toString());
        return new ArrayList<StateOption>(stateOptions);
    }

    private boolean hasStateValue(String stateValue) {
        for (StateOption stateOption : stateOptions) {
            if (stateValue.equals(stateOption.getValue())) {
                return true;
            }
        }
        return false;
    }

    private String findStateLabel(String stateValue) {
        for (StateOption stateOption : stateOptions) {
            if (stateValue.equals(stateOption.getValue())) {
                String label = stateOption.getLabel();
                return label != null ? label : "";
            }
        }
        return "";
    }

    private String tryNoticeResult(Fox fox) throws FoxException {
        return fox.noticeResult();
    }

    public String acquire(Fox fox) throws FoxException {
        return tryNoticeResult(fox);
    }

    public String findResult(String result) {
        return hasStateValue(result) ? result : "";
    }

    public String findResultLabel(String result) {
        return findStateLabel(result);
    }
}
