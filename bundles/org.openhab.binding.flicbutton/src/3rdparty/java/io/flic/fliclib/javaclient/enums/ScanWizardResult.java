package io.flic.fliclib.javaclient.enums;

public enum ScanWizardResult {
    WizardSuccess,
    WizardCancelledByUser,
    WizardFailedTimeout,
    WizardButtonIsPrivate,
    WizardBluetoothUnavailable,
    WizardInternetBackendError,
    WizardInvalidData,
    WizardButtonBelongsToOtherPartner,
    WizardButtonAlreadyConnectedToOtherDevice
}
