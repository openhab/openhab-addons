package org.openhab.binding.supla.internal.server.http;

import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SuplaHttpExecutor implements HttpExecutor {
    private final Logger logger = LoggerFactory.getLogger(SuplaHttpExecutor.class);
    private final SuplaCloudServer server;

    public SuplaHttpExecutor(SuplaCloudServer server) {
        this.server = checkNotNull(server);
    }

    @Override
    public Response get(Request request) {
        final HttpURLConnection connection = buildConnection(request);
        setGetRequestMethod(connection);
        setHeaders(request, connection);
        return execute(connection);
    }

    @Override
    public Response post(Request request, Body body) {
        return runNotGetMethod(request, body, SuplaHttpExecutor::setPostRequestMethod);
    }

    @Override
    public Response patch(Request request, Body body) {
        return runNotGetMethod(request, body, SuplaHttpExecutor::setPatchRequestMethod);
    }

    private Response runNotGetMethod(Request request, Body body, Consumer<HttpURLConnection> setMethod) {
        final HttpURLConnection connection = buildConnection(request);
        setMethod.accept(connection);
        setHeaders(request, connection);

        // only for POST
        connection.setDoOutput(true);
        final byte[] json = body.buildBytesToSend();
        connection.setFixedLengthStreamingMode(json.length);
        connect(connection);
        writeRequest(json, connection);

        return execute(connection);
    }


    private static void connect(HttpURLConnection connection) {
        try {
            connection.connect();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] buildBytesToSend(Body body, JsonMapper jsonMapper) {
        try {
            return jsonMapper.map(body.getBody()).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Response execute(HttpURLConnection connection) {
        final int responseCode = getResponseCode(connection);
        final BufferedReader in = buildBufferedReader(connection);
        final String response = readStream(in);
        return new Response(responseCode, response);
    }

    private void writeRequest(byte[] body, HttpURLConnection connection) {
        DataOutputStream wr;
        try {
            wr = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            wr.write(body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            try {
                wr.close();
            } catch (IOException e) {
                logger.error("Can't close writing stream!", e);
            }
        }
    }

    private static void setHeaders(Request request, HttpURLConnection connection) {
        for (Header header : request.getHeaders()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
    }

    private static void setRequestMethod(String method, HttpURLConnection connection) {
        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setGetRequestMethod(HttpURLConnection connection) {
        setRequestMethod("GET", connection);
    }

    private static void setPostRequestMethod(HttpURLConnection connection) {
        setRequestMethod("POST", connection);
    }

    private static void setPatchRequestMethod(HttpURLConnection connection) {
        setRequestMethod("PATCH", connection);
    }

    private String readStream(BufferedReader in) {
        String inputLine;
        final StringBuilder response = new StringBuilder();

        try {
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("Can't close 'in' stream!", e);
            }
        }
        return response.toString();
    }

    private static BufferedReader buildBufferedReader(HttpURLConnection connection) {
        try {
            return new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int getResponseCode(HttpURLConnection connection) {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpURLConnection buildConnection(Request request) {
        try {
            return (HttpURLConnection) buildUrl(request).openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Can't build connection for request " + request, e);
        }
    }

    private URL buildUrl(Request request) {
        final String url = server.getServer() + request.getPath();
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Can't create URL from this string '" + url + "'", e);
        }
    }
}
