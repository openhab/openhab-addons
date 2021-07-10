package org.openhab.binding.threema.internal;

import ch.threema.apitool.PublicKeyStore;

final class ThreemaKeyStore extends PublicKeyStore {
    @Override
    protected byte[] fetchPublicKey(String threemaId) {
        // TODO: implement public key fetch
        // (e.g. fetch from a locally saved file)
        return null;
    }

    @Override
    protected void save(String threemaId, byte[] publicKey) {
        // TODO: implement public key saving
        // (e.g. save to a locally saved file)
    }
}
