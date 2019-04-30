The cloud protocol is not QUIC. It is simply UDP. Nothing smart, no encryption, just UDP.

* The gateway looks for srmp01lb02.ch.pub.arrayent.com
* This is a CNAME for srm-emea-p01-lb02.arrayent.com (note the emea there)

* It sends a UDP request to that server
  002c000001960000000000010000000202030320000200000000000000000000080a000000010000000100000007

* The server replies
  003000000000000000000000000000020200032100020000000000000000000000000000000000000002d49f4ced0000c005

* This initial server appears to be a redirect service because this reply causes the gateway to start talking to another server within Amazon AWS compute. Note that the address of this server is contained in the reply from the initial server above.

* The next request (and all future requests) is to 35.156.129.67 (hex: )
  0060000001960000000000010000000302230064000300000000000000000000080aa168436d17f0fd79114ae611c6de2195991c15ae99a02d40df6c364db3ab94c12d1575e907639571928ce09a6c831e5b695d007edf1e311c2a4ce1ff9e6f53ba

* The first part of the request response is obviously unencrypted - they clearly start with a 2 byte packet length and there is an obvious sequence number. We can only hope that the following payload has some form of protection.

* So, your occupancy and habits are being exposed to OSRAM, Arrayent and Amazon at least. Insurance companies take note!
