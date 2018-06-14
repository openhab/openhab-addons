/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xmltv;

import org.openhab.binding.xmltv.internal.jaxb.Tv;

/**
 * The {@link XmlTVDataListener} is notified by the bridge thing handler
 * when updated data are available from the XMLTV file.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public interface XmlTVDataListener {

    /**
     * This method is called just after the bridge thing handler fetched new data
     * from the XML TV file.
     *
     * @param tv
     */
    public void onDataFetched(Tv tv);
}
