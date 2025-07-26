# Configuring LEAP Authentication

Unlike LIP, which was designed to use a simple serial or telnet connection and authenticates using a username/password, LEAP uses a SSL connection and authenticates using certificates.
This necessarily makes configuration more complicated.
There are several open source utilities available for generating the certificate files necessary to access your Caseta or RA2 Select hub.
One good choice is included with the popular pylutron library which is available on GitHub at [pylutron-caseta](https://github.com/gurumitts/pylutron-caseta).
On a Unix-like system, you can easily install it via `pip`:

```bash
$ pip install pylutron_caseta[cli]
Defaulting to user installation because normal site-packages is not writeable
Collecting pylutron_caseta[cli]
...
Installing collected packages: pylutron_caseta
Successfully installed pylutron_caseta-0.20.0
```

First, you need to locate your bridge using `leap-scan`:

```bash
$ leap-scan
Lutron-05701a1c.local. 192.168.1.10 fe80::5a2b:aee:ed11:c6d5
<Ctrl-C>
```

Then, you can pair with the found device using the address found above:

```bash
$ lap-pair Lutron-05701a1c.local. --cert caseta.crt --key caseta.key --cacert caseta-bridge.crt
Press the small black button on the back of the bridge to complete pairing.
Successfully paired with 1.119
```

Running this will not affect your existing hub configuration or Lutron app installations.
When it has completed, it will have generated three files: caseta.crt, caseta.key, and caseta-bridge.crt.

Once the key and certificate files have been generated, you will need to load them into a java keystore.

You can load a keystore from the key and certificate files on a Linux system with the following commands.
You’ll need access to both the java keytool and openssl.

```bash
$ openssl pkcs12 -export -in caseta.crt -inkey caseta.key -out caseta.p12 -name caseta
Enter Export Password:
Verifying - Enter Export Password:
$ keytool -importkeystore -destkeystore lutron.keystore -srckeystore caseta.p12 -srcstoretype PKCS12 -alias caseta
Importing keystore caseta.p12 to lutron.keystore...
Enter destination keystore password:  
Re-enter new password: 
Enter source keystore password: 
$ keytool -importcert -file caseta-bridge.crt -keystore lutron.keystore -alias caseta-bridge
Enter keystore password:  
Owner: CN=SmartBridge572B0A11C6D5, O="Lutron Electronics Co., Inc.", L=Coopersburg, ST=Pennsylvania, C=US
Issuer: CN=SmartBridge572B0A11C6D5, O="Lutron Electronics Co., Inc.", L=Coopersburg, ST=Pennsylvania, C=US
Serial number: 1
Valid from: Fri Oct 30 18:00:00 MDT 2015 until: Thu Oct 25 18:00:00 MDT 2035
Certificate fingerprints:
    SHA1: 20:BE:07:23:0D:61:E7:EE:C4:17:C5:A2:6D:AB:85:0D:64:CF:2A:51
    SHA256: C3:51:D0:C0:8C:15:7D:21:34:6F:B6:91:5E:0F:03:85:AB:06:DB:74:63:2D:7B:22:F2:1C:CB:12:7E:3C:29:E2
Signature algorithm name: SHA256withECDSA
Subject Public Key Algorithm: 256-bit EC (secp256r1) key
Version: 3

Extensions: 

#1: ObjectId: 2.5.29.19 Criticality=true
BasicConstraints:[
  CA:true
  PathLen: no limit
]

#2: ObjectId: 2.5.29.15 Criticality=true
KeyUsage [
  DigitalSignature
  Key_Encipherment
  Data_Encipherment
  Key_Agreement
  Key_CertSign
  Crl_Sign
]

Trust this certificate? [no]:  yes
Certificate was added to keystore
```

Respond to the password prompt(s) with a password.
The lutron.keystore file that you end up with is the one you’ll need to give the binding access to.
The caseta.p12 file is just an intermediate file that you can delete later.

Finally you’ll then need to set the ipAddress, keystore, and keystorePassword parameters of the leapbridge thing.
The ipAddress will be set for you if you used discovery to detect a Caseta Smart Bridge.
This should also work with DHCP, although setting a static IP address for your bridge is still recommended.
