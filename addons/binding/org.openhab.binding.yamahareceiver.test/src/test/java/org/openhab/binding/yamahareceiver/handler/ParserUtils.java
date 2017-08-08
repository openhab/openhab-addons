/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * To minimise dependencies, we parse the HTTP headers ourself.
 *
 * @author David Graeff - Initial contribution
 */
public class ParserUtils {

    public static String parseBodyXML(InputStream inputStream, int len) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (--len >= 0) {
            int charRead = inputStream.read();
            if (charRead == -1) {
                break;
            }

            if ((char) charRead == '\r') { // if we've got a '\r'
                inputStream.read(); // ignore '\n'
                break;
            }
            sb.append((char) charRead);
        }
        return sb.toString();
    }

    public static String parseRequestURL(InputStream inputStream) throws IOException {
        StringBuffer sb = new StringBuffer();
        while (true) {
            int charRead = inputStream.read();
            if (charRead == -1) {
                break;
            }

            if ((char) charRead == '\r') { // if we've got a '\r'
                inputStream.read(); // ignore '\n'
                break;
            }
            sb.append((char) charRead);
        }

        String data = sb.toString();
        if (data.length() < 12) {
            return "";
        }
        return data.substring(data.indexOf(' ') + 1, data.length() - 9);

    }

    public static Map<String, String> parseHTTPHeaders(InputStream inputStream) throws IOException {
        int charRead;
        StringBuffer sb = new StringBuffer();
        while (true) {
            sb.append((char) (charRead = inputStream.read()));
            if ((char) charRead == '\r') { // if we've got a '\r'
                sb.append((char) inputStream.read()); // then write '\n'
                charRead = inputStream.read(); // read the next char;
                if (charRead == '\r') { // if it's another '\r'
                    sb.append((char) inputStream.read());// write the '\n'
                    break;
                } else {
                    sb.append((char) charRead);
                }
            }
        }

        String data = sb.toString();
        String[] headersArray = data.split("\r\n");
        // GET /YamahaRemoteControl/ctrl HTTP/1.1\r\n
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 0; i < headersArray.length; i++) {
            String[] parts = headersArray[i].split(": ");
            if (parts.length != 2) {
                continue;
            }
            headers.put(parts[0], parts[1]);
        }

        return headers;
    }

}
