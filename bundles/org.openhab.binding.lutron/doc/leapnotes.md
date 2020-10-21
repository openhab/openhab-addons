# Configuring LEAP Authentication

Unlike LIP, which was designed to use a simple serial or telnet connection and authenticates using a username/password, LEAP uses a SSL connection and authenticates using certificates.
This necessarily makes configuration more complicated.
There are several open source utilities available for generating the certificate files necessary to access your Caseta or RA2 Select hub.
One good choice is the get_lutron_cert.py script included with the popular pylutron library which is available on Github at https://github.com/gurumitts/pylutron-caseta .
On a unix-like system, you can easily retrieve it using curl with a command like:

```
curl https://raw.githubusercontent.com/gurumitts/pylutron-caseta/dev/get_lutron_cert.py >get_lutron_cert.py
```

Remember that the get_lutron_cert.py script must be run using python3, not 2!
Also, the script will prompt you to press the button on your smart hub to authorize key generation, so you should be somewhere near the hub when you run it.
Running it will not affect your existing hub configuration or Lutron app installations.
When it has completed, it will have generated three files: caseta.crt, caseta.key, and caseta-bridge.crt.

Once the key and certificate files have been generated, you will need to load them into a java keystore.


You can load a keystore from the key and certificate files on a linux system with the following commands.
You’ll need access to both the java keytool and openssl.

```
openssl pkcs12 -export -in caseta.crt -inkey caseta.key -out caseta.p12 -name caseta

keytool -importkeystore -destkeystore lutron.keystore -srckeystore caseta.p12 -srcstoretype PKCS12 -srcstorepass secret -alias caseta

keytool -importcert -file caseta-bridge.crt -keystore lutron.keystore -alias caseta-bridge
```

Respond to the password prompt(s) with a password, and then use that password in the -srcstorepass parameter of the keytool command and in the keystorePassword parameter for leapbridge.
In the example above, the pkcs12 store password was set to “secret”, but hopefully you can think of a better one!
The lutron.keystore file that you end up with is the one you’ll need to give the binding access to.
The caseta.p12 file is just an intermediate file that you can delete later.

Finally you’ll then need to set the ipAddress, keystore, and keystorePassword parameters of the leapbridge thing.
The ipAddress will be set for you if you used discovery to detect a Caseta Smart Bridge.
This should also work with DHCP, although setting a static IP address for your bridge is still recommended.
