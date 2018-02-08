/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.parser.response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.listener.ResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public class ProductInformation extends Response {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(ProductInformation.class);

    @Override
    public boolean check(String message) {
        return message.matches("\\*#13\\*[0-9]+#9\\*66\\*[0-9]+\\*[0-9]+##");
    }

    @Override
    public void process(String message, ResponseListener e) {
        int where;
        int index;
        int value;
        try {
            where = Integer.parseInt(message.split("[\\*#]")[3]);
        } catch (NumberFormatException e2) {
            // open network with negative value
            logger.warn("Product information for source that cannot be parsed ({})", message);
            return;
        }
        try {
            index = Integer.parseInt(message.split("[\\*#]")[6]);
        } catch (NumberFormatException e2) {
            // open network with negative value
            logger.warn("Product information for an index that cannot be parsed ({})", message);
            return;
        }
        try {
            value = Integer.parseInt(message.split("[\\*#]")[7]);
        } catch (NumberFormatException e2) {
            // open network with negative value
            logger.warn("Product information type cannot be parsed ({})", message);
            return;
        }
        e.onProductInformation(where, index, value);
    }
}
