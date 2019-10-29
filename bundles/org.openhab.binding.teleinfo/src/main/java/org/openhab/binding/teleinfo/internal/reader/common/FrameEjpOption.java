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
package org.openhab.binding.teleinfo.internal.reader.common;

/**
 * The {@link FrameEjpOption} interface defines common attributes for EJP option.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public interface FrameEjpOption {

    int getEjphpm();

    void setEjphpm(int ejphpm);

    int getEjphn();

    void setEjphn(int ejphn);

    Integer getPejp();

    void setPejp(Integer pejp);

}
