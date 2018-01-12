/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.openhab.ui.cometvisu.internal.backend.beans.StateBean;

/**
 * {@link StateBeanMessageBodyWriter} is used to serialize state update messages
 * for the CometVisu client
 *
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class StateBeanMessageBodyWriter implements MessageBodyWriter<Object> {

    @Override
    public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return 0;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] arg2, MediaType arg3) {
        return (type == StateBean.class || genericType == StateBean.class);
    }

    @Override
    public void writeTo(Object stateBean, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                    throws IOException, WebApplicationException {
        StringBuilder sb = new StringBuilder();
        sb.append(serialize(stateBean));
        try (DataOutputStream dos = new DataOutputStream(entityStream)) {
            dos.writeUTF(sb.toString());
        }
    }

    /**
     *
     * @param bean
     *            - StateBean or List<StateBean>
     * @return String
     *         - CV-Protocol state update json format {d:{item:state,...}}
     */
    public String serialize(Object bean) {
        String msg = "{\"d\":{";
        if (bean instanceof StateBean) {
            StateBean stateBean = (StateBean) bean;
            msg += "\"" + stateBean.name + "\":\"" + stateBean.state + "\"";
        } else if (bean instanceof List<?>) {
            List<String> states = new ArrayList<String>();
            for (Object bo : (List<?>) bean) {
                if (bo instanceof StateBean) {
                    StateBean stateBean = (StateBean) bo;
                    states.add("\"" + stateBean.name + "\":\"" + stateBean.state + "\"");
                }
            }
            if (states.size() > 0) {
                msg += StringUtils.join(states, ",");
            }
        }
        msg += "}}";
        return msg;
    }
}
