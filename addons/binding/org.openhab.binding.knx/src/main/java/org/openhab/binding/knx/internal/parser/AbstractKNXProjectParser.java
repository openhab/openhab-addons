/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.knx.internal.parser;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractKNXProjectParser} is an abstract implementation of a KNXProjectParser. It provides some common
 * helper methods to convert and process data
 *
 * @author Karel Goderis - Initial contribution
 *
 */
public abstract class AbstractKNXProjectParser implements KNXProjectParser {

    private final Logger logger = LoggerFactory.getLogger(AbstractKNXProjectParser.class);

    public static DecimalFormat df = new DecimalFormat("000");

    @Override
    public void addXML(HashMap<String, String> xmlRepository) {

        for (String anXML : xmlRepository.keySet()) {
            addXML(anXML, xmlRepository.get(anXML));
        }
    }

    protected String convertGroupAddress(int intAddr) {
        return (intAddr >> 11) + "/" + ((intAddr >> 8) & 7) + "/" + (intAddr & 0xFF);
    }

    protected String convertDPT(String dpt) {
        if (dpt != null && !dpt.isEmpty()) {
            if (dpt.contains("DPST") || dpt.contains("DPT")) {
                dpt = dpt.split(" ")[0];
                String[] split = dpt.split("-");
                switch (split[0]) {
                    case "DPST": {
                        return Integer.parseInt(split[1]) + "." + df.format(Integer.parseInt(split[2]));
                    }
                    case "DPT": {
                        long sub = 1;
                        switch (Integer.parseInt(split[1])) {
                            case 5: {
                                sub = 10;
                                break;
                            }
                            default: {
                                sub = 1;
                                break;
                            }
                        }
                        return Integer.parseInt(split[1]) + "." + df.format(sub);
                    }
                }
            } else {
                logger.error("Can not convert {}", dpt);
            }
        }
        return null;
    }
}
