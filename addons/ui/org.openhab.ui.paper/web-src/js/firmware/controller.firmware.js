var firmwareControllers = angular.module('PaperUI.controllers.firmware', [ 'PaperUI.filters.firmware' ]);

firmwareControllers.controller('FirmwareController', function($scope, $mdDialog, toastService, eventService, thingService, FIRMWARE_STATUS) {
    $scope.updatingFirmware = false;
    $scope.percentComplete = 0;
    $scope.updateStep;
    $scope.firmwareStatus;
    $scope.firmwares = [];
    $scope.fvdetails = false;
    var thingUID = $scope.path[4];

    var refreshFirmwareStatus = function(thingUID) {
        $scope.firmwareStatus = null;
        if (thingUID) {
            thingService.getFirmwareStatus({
                thingUID : thingUID
            }, function(firmwareStatus) {
                $scope.firmwareStatus = firmwareStatus;
            });
        }

        var thingUID = $scope.$parent.thing.thingUID;
        if (thingUID) {
            thingService.getFirmwares({
                thingUID : thingUID
            }, function(firmwares) {
                $scope.firmwares = firmwares;
            });
        }
    };

    refreshFirmwareStatus(thingUID);

    $scope.installFirmware = function(version) {
        if (!$scope.updatingFirmware) {
            thingService.installFirmware({
                thingUID : thingUID,
                firmwareVersion : version
            }, function() {
                $scope.updatingFirmware = true;
                $scope.percentComplete = 0;
            }, function(resp) {
                toastService.showDefaultToast('Error: ' + resp.data.error.message);
            });
        }
    };

    $scope.showChangelog = function(firmwareVersion) {
        var changelog = $scope.firmwares.filter(function(firmware) {
            return firmware.version === firmwareVersion;
        })[0].changelog;

        $mdDialog.show({
            controller : 'ChangelogDialogController',
            templateUrl : 'partials/firmware/dialog.changelog.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                changelog : changelog,
                firmwareVersion : firmwareVersion
            }
        });
    };

    $scope.isFirmwareUpdateable = function(firmwareStatus) {
        return firmwareStatus && firmwareStatus.status == FIRMWARE_STATUS.UPDATE_EXECUTABLE && firmwareStatus.updatableVersion;
    }

    $scope.hasChangelog = function(firmwareStatus) {
        if (firmwareStatus && firmwareStatus.updatableVersion) {
            var changelog = $scope.firmwares.filter(function(firmware) {
                return firmware.version === firmwareStatus.updatableVersion;
            })[0].changelog;

            return changelog !== undefined && changelog !== null && changelog !== '';
        }

        return false;
    }

    eventService.onEvent('smarthome/things/*/firmware/update/progress', function(topic, updateStatus) {
        if (updateStatus && updateStatus.progress) {
            if (updateStatus.progress == 100) {
                $scope.updatingFirmware = false;
            }
            $scope.$apply(function() {
                $scope.percentComplete = updateStatus.progress;
                $scope.updateStep = updateStatus.progressStep;
            });
        }
    });

    eventService.onEvent('smarthome/things/*/firmware/update/result', function(topic, status) {
        if (status && status.result == "SUCCESS") {
            $scope.$parent.getThing(function() {
                refreshFirmwareStatus(thingUID);
            });

            toastService.showDefaultToast('Firmware updated successfully.');
        } else {
            $scope.$parent.getThing(function() {
                refreshFirmwareStatus(thingUID);
            });
            toastService.showDefaultToast('Firmware update failed.');
        }
    });

    eventService.onEvent('smarthome/things/*/firmware/status', function(topic, object) {
        if (object) {
            $scope.firmwareStatus.status = object.firmwareStatus;
            if (object.updatableFirmwareUID && object.updatableFirmwareUID.firmwareVersion) {
                $scope.firmwareStatus.updateableVersion = object.updatableFirmwareUID.firmwareVersion
            } else {
                $scope.firmwareStatus.updateableVersion = null;
            }
        }
    });

}).controller('ChangelogDialogController', function($scope, $mdDialog, firmwareVersion, changelog) {
    $scope.changelog = changelog;
    $scope.firmwareVersion = firmwareVersion;
    $scope.close = function() {
        $mdDialog.hide();
    }
});
