import java.util.List;

import org.openhab.binding.e3dc.internal.rscp.RscpCodec;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/** Reproduces the scalar round trip outside the test framework. */
public final class RoundTripProbe {
    public static void main(String[] args) throws Exception {
        Object[][] cases = { { RscpType.BOOL, Boolean.TRUE }, { RscpType.UCHAR8, 200 }, { RscpType.INT16, -3000 },
                { RscpType.UINT16, 60000 }, { RscpType.INT32, -70000 }, { RscpType.FLOAT32, 1.5f },
                { RscpType.DOUBLE64, -2.25d } };
        for (Object[] c : cases) {
            RscpType type = (RscpType) c[0];
            RscpValue encoded = RscpValue.of(1000, type, c[1]);
            byte[] frame = RscpCodec.encodeFrame(encoded);
            List<RscpValue> decoded = RscpCodec.decodeFrame(frame);
            System.out.printf("%-9s encoded=%d bytes -> items=%d", type, frame.length, decoded.size());
            if (!decoded.isEmpty()) {
                RscpValue root = decoded.get(0);
                RscpValue found = root.find(1000);
                System.out.printf(" rootTag=%d rootType=%s found=%s value=%s%n", root.getTag(), root.getType(),
                        found != null ? "yes" : "NO", found != null ? found.getValue() : "-");
            } else {
                System.out.println(" NO ITEMS");
            }
        }
    }

    private RoundTripProbe() {
    }
}
