/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.ws.datatypes;

/**
 * <p>
 * Java class for WSSceneRelayValue complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="WSSceneRelayValue">
 *   &lt;complexContent>
 *     &lt;extension base="{utcs.values}WSResourceValue">
 *       &lt;sequence>
 *         &lt;element name="delayTime" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="relayValue" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
/**
 * IHC WSSceneRelayValue data value.
 *
 * @author Pauli Anttila
 * @since 1.5.0
 */
public class WSSceneRelayValue extends WSResourceValue {

    protected int delayTime;
    protected boolean relayValue;

    /**
     * Gets the value of the delayTime property.
     *
     */
    public int getDelayTime() {
        return delayTime;
    }

    /**
     * Sets the value of the delayTime property.
     *
     */
    public void setDelayTime(int value) {
        this.delayTime = value;
    }

    /**
     * Gets the value of the relayValue property.
     *
     */
    public boolean isRelayValue() {
        return relayValue;
    }

    /**
     * Sets the value of the relayValue property.
     *
     */
    public void setRelayValue(boolean value) {
        this.relayValue = value;
    }

}
