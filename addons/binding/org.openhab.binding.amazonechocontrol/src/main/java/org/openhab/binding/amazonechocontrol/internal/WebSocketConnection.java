package org.openhab.binding.amazonechocontrol.internal;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.extensions.ExtensionConfig;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.common.extensions.compress.PerMessageDeflateExtension;

@NonNullByDefault
public class WebSocketConnection {

    WebSocketClient webSocketClient;
    @Nullable
    Session session;
    @Nullable
    Timer timer;
    Listener listener;

    WebSocketConnection(String amazonSite, List<HttpCookie> sessionCookies) throws Exception {
        listener = new Listener();
        listener.verify();
        SslContextFactory sslContextFactory = new SslContextFactory();
        webSocketClient = new WebSocketClient(sslContextFactory);

        try {
            String host;
            if (StringUtils.equalsIgnoreCase(amazonSite, "amazon.com")) {
                host = "dp-gw-na-js." + amazonSite;
            } else {
                host = "dp-gw-na." + amazonSite;
            }

            String deviceSerial = "";
            List<HttpCookie> cookiesForWs = new ArrayList<HttpCookie>();
            for (HttpCookie cookie : sessionCookies) {
                if (cookie.getName().equals("ubid-acbde")) {
                    deviceSerial = cookie.getValue();
                }
                // Clone the cookie without the security attribute, because the web socket implementation ignore secure
                // cookies
                HttpCookie cookieForWs = new HttpCookie(cookie.getName(), cookie.getValue());
                cookiesForWs.add(cookieForWs);
            }
            deviceSerial += "-" + new Date().getTime();
            URI uri;

            uri = new URI("wss://" + host + "/?x-amz-device-type=ALEGCNGL9K0HM&x-amz-device-serial=" + deviceSerial);

            webSocketClient.getExtensionFactory().register("permessage-deflate", PerMessageDeflateExtension.class);

            webSocketClient.start();

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setHeader("host", host);
            request.setHeader("Cache-Control", "no-cache");
            request.setHeader("Pragma", "no-cache");
            request.setHeader("Origin", "alexa." + amazonSite);

            request.addExtensions(new ExtensionConfig("permessage-deflate"));

            request.setCookies(cookiesForWs);

            webSocketClient.connect(listener, uri, request);

        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setSession(Session session) {
        this.session = session;
        Timer timer = new Timer();
        this.timer = timer;
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                listener.sendPing();
            }
        }, 180000, 180000);
    }

    public void close() {
        Timer timer = this.timer;
        if (timer != null) {
            timer.cancel();
        }
        Session session = this.session;
        this.session = null;
        if (session != null) {
            session.close();
        }
        webSocketClient.destroy();
    }

    class Listener implements WebSocketListener {

        int msgCounter = -1;
        int messageId;

        Listener() {
            this.messageId = ThreadLocalRandom.current().nextInt(0, Short.MAX_VALUE);
        }

        void sendMessage(String message) {
            sendMessage(message.getBytes(StandardCharsets.UTF_8));
        }

        void sendMessageHex(String message) {
            sendMessage(hexStringToByteArray(message));
        }

        void sendMessage(byte[] buffer) {
            try {
                Session session = WebSocketConnection.this.session;
                if (session != null) {
                    session.getRemote().sendBytes(ByteBuffer.wrap(buffer));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        byte[] hexStringToByteArray(String str) {
            byte[] bytes = new byte[str.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
            }
            return bytes;
        }

        long readHex(byte[] data, int index, int length) {
            String str = readString(data, index, length);
            if (str.startsWith("0x")) {
                str = str.substring(2);
            }
            return Long.parseLong(str, 16);
        }

        String readString(byte[] data, int index, int length) {
            return new String(data, index, length, StandardCharsets.UTF_8);
        }

        class Message {
            String service = "";
            Content content = new Content();
            String contentTune = "";
            String messageType = "";
            long channel;
            long checksum;
            long messageId;
            String moreFlag = "";
            long seq;
        }

        class Content {
            String messageType = "";
            String protocolVersion = "";
            String connectionUUID = "";
            long established;
            long timestampINI;
            long timestampACK;
            String subMessageType = "";
            long channel;
            String destinationIdentityUrn = "";
            String deviceIdentityUrn = "";
            @Nullable
            String payload;
            byte[] payloadData = new byte[0];
        }

        Message parseIncomingMessage(byte[] data) {
            int idx = 0;
            Message message = new Message();
            message.service = readString(data, data.length - 4, 4);

            if (message.service.equals("TUNE")) {
                message.checksum = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                int contentLength = (int) readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                message.contentTune = readString(data, idx, contentLength - 4 - idx);
            } else if (message.service.equals("FABE")) {
                message.messageType = readString(data, idx, 3);
                idx += 4;
                message.channel = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                message.messageId = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                message.moreFlag = readString(data, idx, 1);
                idx += 2; // 1 + delimiter;
                message.seq = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;
                message.checksum = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;

                long contentLength = readHex(data, idx, 10);
                idx += 11; // 10 + delimiter;

                message.content.messageType = readString(data, idx, 3);
                idx += 4;

                if (message.channel == 0x361) { // GW_HANDSHAKE_CHANNEL
                    if (message.content.messageType.equals("ACK")) {
                        int length = (int) readHex(data, idx, 10);
                        idx += 11; // 10 + delimiter;
                        message.content.protocolVersion = readString(data, idx, length);
                        idx += length + 1;
                        length = (int) readHex(data, idx, 10);
                        idx += 11; // 10 + delimiter;
                        message.content.connectionUUID = readString(data, idx, length);
                        idx += length + 1;
                        message.content.established = readHex(data, idx, 10);
                        idx += 11; // 10 + delimiter;
                        message.content.timestampINI = readHex(data, idx, 18);
                        idx += 19; // 18 + delimiter;
                        message.content.timestampACK = readHex(data, idx, 18);
                        idx += 19; // 18 + delimiter;
                    }
                } else if (message.channel == 0x362) { // GW_CHANNEL
                    if (message.content.messageType.equals("GWM")) {
                        message.content.subMessageType = readString(data, idx, 3);
                        idx += 4;
                        message.content.channel = readHex(data, idx, 10);
                        idx += 11; // 10 + delimiter;

                        if (message.content.channel == 0xb479) { // DEE_WEBSITE_MESSAGING
                            int length = (int) readHex(data, idx, 10);
                            idx += 11; // 10 + delimiter;
                            message.content.destinationIdentityUrn = readString(data, idx, length);
                            idx += length + 1;

                            length = (int) readHex(data, idx, 10);
                            idx += 11; // 10 + delimiter;
                            String idData = readString(data, idx, length);
                            idx += length + 1;

                            String[] idDataElements = idData.split(" ", 2);
                            message.content.deviceIdentityUrn = idDataElements[0];
                            if (idDataElements.length == 2) {
                                String payload = idDataElements[1];
                                if (!StringUtils.isEmpty(message.content.payload)) {
                                    payload = readString(data, idx, data.length - 4 - idx);
                                }
                                message.content.payload = payload;
                            }
                        }
                    }
                } else if (message.channel == 0x65) { // CHANNEL_FOR_HEARTBEAT
                    idx -= 1; // no delimiter!
                    message.content.payloadData = Arrays.copyOfRange(data, idx, data.length - 4);
                }
            }
            // console.log(JSON.stringify(message, null, 4));
            return message;
        }

        @Override
        public void onWebSocketConnect(@Nullable Session session) {
            if (session != null) {
                this.msgCounter = -1;
                setSession(session);
                sendMessage("0x99d4f71a 0x0000001d A:HTUNE");
            }
        }

        @Override
        public void onWebSocketBinary(byte @Nullable [] data, int offset, int len) {
            this.msgCounter++;
            if (this.msgCounter == 0) {
                sendMessage(
                        "0xa6f6a951 0x0000009c {\"protocolName\":\"A:H\",\"parameters\":{\"AlphaProtocolHandler.receiveWindowSize\":\"16\",\"AlphaProtocolHandler.maxFragmentSize\":\"16000\"}}TUNE");

                sendMessage(encodeGWHandshake());

            } else if (this.msgCounter == 1) {

                sendMessage(encodeGWRegister());

                sendPing();
            } else {
                byte[] buffer = data;
                if (offset > 0 || len != buffer.length) {
                    buffer = Arrays.copyOfRange(data, offset, offset + len);
                }
                try {
                    Message message = parseIncomingMessage(buffer);
                    if (message.service.equals("FABE") && message.content.messageType.equals("PON")
                            && message.content.payloadData.length > 0) {
                        // 'Alexa-Remote WS-MQTT: Received Pong');
                        /*
                         * if (initTimeout) {
                         * clearTimeout(initTimeout);
                         * initTimeout = null;
                         * this._options.logger && this._options.logger('Alexa-Remote WS-MQTT: Initialization
                         * completed');
                         * this.emit('connect');
                         * }
                         * if (this.pongTimeout) {
                         * clearTimeout(this.pongTimeout);
                         * this.pongTimeout = null;
                         * }
                         * this.connectionActive = true;
                         */
                        return;
                    } else {
                        if (!StringUtils.isEmpty(message.content.payload)) {

                        }
                        // let command = message.content.payload.command;
                        // let payload = message.content.payload.payload;

                        // this._options.logger && this._options.logger('Alexa-Remote WS-MQTT: Command ' + command + ':
                        // ' +
                        // JSON.stringify(payload, null, 4));
                        // this.emit('command', command, payload);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        public void sendPing() {
            try {
                if (false) {
                    sendMessageHex(
                            "4D53472030783030303030303635203078306534313465343720662030783030303030303031203078626332666262356620307830303030303036322050494E00000000D1098D8CD1098D8C000000070052006500670075006C0061007246414245");
                } else {
                    sendMessage(encodePing());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onWebSocketText(@Nullable String message) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onWebSocketClose(int code, @Nullable String reason) {
            WebSocketConnection.this.close();
        }

        @Override
        public void onWebSocketError(@Nullable Throwable error) {

            WebSocketConnection.this.close();

        }

        String encodeNumber(long val) {
            return encodeNumber(val, 8);
        }

        String encodeNumber(long val, int len) {

            String str = Long.toHexString(val);
            if (str.length() > len) {
                str = str.substring(str.length() - len);
            }
            while (str.length() < len) {
                str = '0' + str;
            }
            return "0x" + str;
        }

        long b(long a, long len) {

            long lenCounter = len;
            long value;
            for (value = c(a); 0 != lenCounter && 0 != value;) {
                value = (long) Math.floor(value / 2);
                lenCounter--;
            }
            return value;
        }

        long c(long a) {

            long result = a;
            if (0 > a) {
                result = 4294967295L + a + 1;
            }
            return result;
        }

        int computeChecksum(byte[] data, int exclusionStart, int exclusionEnd) {
            if (exclusionEnd < exclusionStart) {
                return 0;
            }

            long h;
            long l;
            int index;
            for (h = 0, l = 0, index = 0; index < data.length; index++) {
                if (index != exclusionStart) {
                    l += c(data[index] << ((index & 3 ^ 3) << 3));
                    h += b(l, 32);
                    l = c((int) l & (int) 4294967295L);
                } else {
                    index = exclusionEnd - 1;
                }
            }

            while (h != 0) {
                l += h;
                h = b(l, 32);
                l = (int) l & (int) 4294967295L;

            }
            long value = c(l);
            return (int) value;
        }

        void verify() {

            verifyChecksum(false,
                    "MSG 0x00000361 0x0e414e45 f 0x00000001 0xd7c62f29 0x0000009b INI 0x00000003 1.0 0x00000024 ff1c4525-c036-4942-bf6c-a098755ac82f 0x00000164d106ce6b END FABE");
            verifyChecksum(false,
                    "MSG 0x00000362 0x0e414e46 f 0x00000001 0xf904b9f5 0x00000109 GWM MSG 0x0000b479 0x0000003b urn:tcomm-endpoint:device:deviceType:0:deviceSerialNumber:0 0x00000041 urn:tcomm-endpoint:service:serviceName:DeeWebsiteMessagingService {\"command\":\"REGISTER_CONNECTION\"}FABE");

            verifyChecksum(true,
                    "4D53472030783030303030303635203078306534313465343720662030783030303030303031203078626332666262356620307830303030303036322050494E00000000D1098D8CD1098D8C000000070052006500670075006C0061007246414245");
        }

        void verifyChecksum(boolean hex, String message) {
            byte[] binary = hex ? hexStringToByteArray(message) : message.getBytes(StandardCharsets.US_ASCII);
            int checksum = computeChecksum(binary, 39, 50);

            String checksumHex = encodeNumber(checksum);

            String original = readString(binary, 39, 10);

            if (!checksumHex.equals(original)) {
                original += "";
            }
        }

        byte[] encodeGWHandshake() {
            // pubrelBuf = new Buffer('MSG 0x00000361 0x0e414e45 f 0x00000001 0xd7c62f29 0x0000009b INI 0x00000003 1.0
            // 0x00000024 ff1c4525-c036-4942-bf6c-a098755ac82f 0x00000164d106ce6b END FABE');
            this.messageId++;
            String msg = "MSG 0x00000361 "; // Message-type and Channel = GW_HANDSHAKE_CHANNEL;
            msg += this.encodeNumber(this.messageId) + " f 0x00000001 ";
            int checkSumStart = msg.length();
            msg += "0x00000000 "; // Checksum!
            int checkSumEnd = msg.length();
            msg += "0x0000009b "; // length content
            msg += "INI 0x00000003 1.0 0x00000024 "; // content part 1
            msg += UUID.randomUUID().toString();
            msg += ' ';
            msg += this.encodeNumber(new Date().getTime(), 16);
            msg += " END FABE";
            // msg = "MSG 0x00000361 0x0e414e45 f 0x00000001 0xd7c62f29 0x0000009b INI 0x00000003 1.0 0x00000024
            // ff1c4525-c036-4942-bf6c-a098755ac82f 0x00000164d106ce6b END FABE";
            byte[] completeBuffer = msg.getBytes(StandardCharsets.US_ASCII);

            int checksum = this.computeChecksum(completeBuffer, checkSumStart, checkSumEnd);
            String checksumHex = encodeNumber(checksum);
            byte[] checksumBuf = checksumHex.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(checksumBuf, 0, completeBuffer, checkSumStart, checksumBuf.length);

            return completeBuffer;
        }

        byte[] encodeGWRegister() {
            // pubrelBuf = new Buffer('MSG 0x00000362 0x0e414e46 f 0x00000001 0xf904b9f5 0x00000109 GWM MSG 0x0000b479
            // 0x0000003b urn:tcomm-endpoint:device:deviceType:0:deviceSerialNumber:0 0x00000041
            // urn:tcomm-endpoint:service:serviceName:DeeWebsiteMessagingService
            // {"command":"REGISTER_CONNECTION"}FABE');
            this.messageId++;
            String msg = "MSG 0x00000362 "; // Message-type and Channel = GW_CHANNEL;
            msg += this.encodeNumber(this.messageId) + " f 0x00000001 ";
            int checkSumStart = msg.length();
            msg += "0x00000000 "; // Checksum!
            int checkSumEnd = msg.length();
            msg += "0x00000109 "; // length content
            msg += "GWM MSG 0x0000b479 0x0000003b urn:tcomm-endpoint:device:deviceType:0:deviceSerialNumber:0 0x00000041 urn:tcomm-endpoint:service:serviceName:DeeWebsiteMessagingService {\"command\":\"REGISTER_CONNECTION\"}FABE";

            // msg = "MSG 0x00000362 0x0e414e46 f 0x00000001 0xf904b9f5 0x00000109 GWM MSG 0x0000b479 0x0000003b
            // urn:tcomm-endpoint:device:deviceType:0:deviceSerialNumber:0 0x00000041
            // urn:tcomm-endpoint:service:serviceName:DeeWebsiteMessagingService
            // {\"command\":\"REGISTER_CONNECTION\"}FABE";

            byte[] completeBuffer = msg.getBytes(StandardCharsets.US_ASCII);

            int checksum = this.computeChecksum(completeBuffer, checkSumStart, checkSumEnd);

            String checksumHex = encodeNumber(checksum);
            byte[] checksumBuf = checksumHex.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(checksumBuf, 0, completeBuffer, checkSumStart, checksumBuf.length);

            String test = readString(completeBuffer, 0, completeBuffer.length);
            test.toString();
            return completeBuffer;
        }

        void encode(byte[] data, long b, int offset, int len) {
            /*
             * byte[] array;
             * if (len == 4) {
             * ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
             * buffer.putInt((int) b);
             * array = buffer.array();
             * } else {
             * ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
             * buffer.putLong(b);
             * array = buffer.array();
             * }
             * for (int index = 0; index < len; index++) {
             * data[index + offset] = array[index];
             * }
             */
            for (int index = 0; index < len; index++) {
                data[index + offset] = (byte) (b >> 8 * (len - 1 - index) & 255);
            }

        }

        byte[] encodePing() {

            // MSG 0x00000065 0x0e414e47 f 0x00000001 0xbc2fbb5f 0x00000062 PIN    � ��� ��    R e g u l a rFABE
            this.messageId++;
            String msg = "MSG 0x00000065 "; // Message-type and Channel = CHANNEL_FOR_HEARTBEAT;
            msg += this.encodeNumber(this.messageId) + " f 0x00000001 ";
            int checkSumStart = msg.length();
            msg += "0x00000000 "; // Checksum!
            int checkSumEnd = msg.length();
            msg += "0x00000062 "; // length content

            byte[] completeBuffer = new byte[0x62];
            byte[] startBuffer = msg.getBytes(StandardCharsets.US_ASCII);

            System.arraycopy(startBuffer, 0, completeBuffer, 0, startBuffer.length);

            byte[] header = "PIN".getBytes(StandardCharsets.US_ASCII);
            byte[] payload = "Regular".getBytes(StandardCharsets.US_ASCII); // g = h.length
            byte[] bufferPing = new byte[header.length + 4 + 8 + 4 + 2 * payload.length];
            int idx = 0;
            for (int q = 0; q < header.length; q++) {
                bufferPing[q] = header[q];
            }
            idx += header.length;
            encode(bufferPing, 0, idx, 4);
            idx += 4;
            encode(bufferPing, new Date().getTime(), idx, 8);
            idx += 8;
            encode(bufferPing, payload.length, idx, 4);
            idx += 4;

            for (int q = 0; q < payload.length; q++) {
                bufferPing[idx + q * 2] = (byte) 0;
                bufferPing[idx + q * 2 + 1] = payload[q];
            }
            System.arraycopy(bufferPing, 0, completeBuffer, startBuffer.length, bufferPing.length);

            byte[] buf2End = "FABE".getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(buf2End, 0, completeBuffer, startBuffer.length + bufferPing.length, buf2End.length);

            int checksum = this.computeChecksum(completeBuffer, checkSumStart, checkSumEnd);
            String checksumHex = encodeNumber(checksum);
            byte[] checksumBuf = checksumHex.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(checksumBuf, 0, completeBuffer, checkSumStart, checksumBuf.length);

            byte[] fixedPing = hexStringToByteArray(
                    "4D53472030783030303030303635203078306534313465343720662030783030303030303031203078626332666262356620307830303030303036322050494E00000000D1098D8CD1098D8C000000070052006500670075006C0061007246414245");
            String test1 = readString(fixedPing, 0, fixedPing.length);
            test1.toString();

            String checksumHex2 = encodeNumber(this.computeChecksum(fixedPing, checkSumStart, checkSumEnd));

            String test = readString(completeBuffer, 0, completeBuffer.length);
            test.toString();
            return completeBuffer;
        }
    }

}
