/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.xml.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.openhab.binding.denonmarantz.internal.xml.entities.types.OnOffType;

/**
 * Holds information about the Main zone of the receiver
 *
 * @author Jeroen Idserda - Initial contribution
 */
@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
public class Main {

    private OnOffType power;

    public OnOffType getPower() {
        return power;
    }

    public void setPower(OnOffType power) {
        this.power = power;
    }

}
