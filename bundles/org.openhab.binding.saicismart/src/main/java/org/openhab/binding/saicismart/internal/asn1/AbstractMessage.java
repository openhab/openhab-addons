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
package org.openhab.binding.saicismart.internal.asn1;

import org.bn.coders.IASN1PreparedElement;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Markus Heberling - Initial contribution
 */
public class AbstractMessage<H extends IASN1PreparedElement, B extends IASN1PreparedElement, E extends IASN1PreparedElement> {
    protected H header;
    protected B body;
    @Nullable
    protected E applicationData;

    public AbstractMessage(H header, B body, @Nullable E applicationData) {
        this.header = header;
        this.body = body;
        this.applicationData = applicationData;
    }

    public H getHeader() {
        return header;
    }

    public B getBody() {
        return body;
    }

    @Nullable
    public E getApplicationData() {
        return applicationData;
    }
}
