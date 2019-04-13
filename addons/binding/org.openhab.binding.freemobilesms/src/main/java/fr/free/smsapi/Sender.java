/**
 * Copyright (c) 2014,2019 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package fr.free.smsapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class Sender {

    static public String DEFAULT_URL = "https://smsapi.free-mobile.fr/sendmsg";
    static String charset = "UTF-8";

    protected final String url;

    public Sender(String url) {
        this.url = url;
    }
    public Sender() {
        this.url = DEFAULT_URL;
    }

    public String getQuerry(Account account, String message) throws UnsupportedEncodingException {
        String query = String.format("%s?user=%s&pass=%s&msg=%s",
                this.url,
                URLEncoder.encode(account.getUser(), charset),
                URLEncoder.encode(account.getPassword(), charset),
                URLEncoder.encode(message, charset));
        return query;
    }

    public void send(Account account, String message) throws IOException {
        String urlString = getQuerry(account, message);
        URL url = new URL(urlString);
        send(url);
    }

    public void send(URL url) throws IOException {
        InputStream stream = null;
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                // FIXME read stream
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void main(String args[]) {
        if (args.length != 3) {
            System.err.print("Incorrect number of arguments: given "+args.length);
            System.exit(1);
        }

        Account tempAccount = new RawAccount(args[0], args[1]);
        try {
            new Sender().send(tempAccount, args[2]);
        } catch (IOException e) {
            System.err.println("Failed to send message");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
