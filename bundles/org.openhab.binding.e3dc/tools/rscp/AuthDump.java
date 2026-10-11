import java.util.List;

import org.openhab.binding.e3dc.internal.rscp.RscpCodec;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/** Dumps the encoded authentication request so its structure can be checked by hand. */
public final class AuthDump {
    public static void main(String[] args) throws Exception {
        String user = "bernd@example.com";
        String password = "secret";
        RscpValue request = RscpValue.container(RscpTag.RSCP_REQ_AUTHENTICATION,
                List.of(RscpValue.cstring(RscpTag.RSCP_AUTHENTICATION_USER, user),
                        RscpValue.cstring(RscpTag.RSCP_AUTHENTICATION_PASSWORD, password)));
        byte[] frame = RscpCodec.encodeFrame(request);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < frame.length; i++) {
            sb.append(String.format("%02x", frame[i]));
            if ((i + 1) % 4 == 0) {
                sb.append(' ');
            }
        }
        System.out.println("frame length = " + frame.length);
        System.out.println(sb);
        int payloadLength = ((frame[16] & 0xFF)) | ((frame[17] & 0xFF) << 8);
        System.out.println("payload length field = " + payloadLength);
        System.out.println("user length = " + user.length() + ", password length = " + password.length());
        System.out.println("expected inner = (7+" + (user.length() + 1) + ")+(7+" + (password.length() + 1) + ") = "
                + (7 + user.length() + 1 + 7 + password.length() + 1));
    }

    private AuthDump() {
    }
}
