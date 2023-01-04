package io.flic.fliclib.javaclient.enums;

/**
 * Created by Emil on 2016-05-03.
 */
public enum RemovedReason {
    RemovedByThisClient,
    ForceDisconnectedByThisClient,
    ForceDisconnectedByOtherClient,

    ButtonIsPrivate,
    VerifyTimeout,
    InternetBackendError,
    InvalidData,
    
    CouldntLoadDevice,
    
    DeletedByThisClient,
    DeletedByOtherClient,
    ButtonBelongsToOtherPartner,
    DeletedFromButton
}
