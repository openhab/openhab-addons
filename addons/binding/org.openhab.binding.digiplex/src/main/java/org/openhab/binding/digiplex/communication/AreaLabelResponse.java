/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

/**
 * Response for {@link AreaLabelRequest}
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class AreaLabelResponse extends AbstractResponse {

    private int areaNo;
    private String areaName;

    private AreaLabelResponse(int areaNo, String areaName) {
        super(true);
        this.areaNo = areaNo;
        this.areaName = areaName;
    }

    private AreaLabelResponse(int areaNo) {
        super(false);
    }

    public static AreaLabelResponse failure(int areaNo) {
        return new AreaLabelResponse(areaNo);
    }

    public static AreaLabelResponse success(int areaNo, String areaName) {
        return new AreaLabelResponse(areaNo, areaName);
    }

    public int getAreaNo() {
        return areaNo;
    }

    public String getAreaName() {
        return areaName;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleAreaLabelResponse(this);
    }
}
