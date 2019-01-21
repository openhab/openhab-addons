angular.module('PaperUI.things') //
.controller('EditThingController', function($scope, $mdDialog, $q, $location, toastService, thingRepository, configService, configDescriptionService, thingService) {
    $scope.setSubtitle([ 'Things' ]);
    $scope.setHeaderText('Click the \'Save\' button to apply the changes.');

    var thingUID = $scope.path[4];

    $scope.thing;
    $scope.groups = [];
    $scope.isEditing = true;
    var originalThing = {};

    // used for the thing config parameters
    $scope.parameters = undefined
    $scope.configuration = {}

    $scope.update = update;

    this.$onInit = activate;

    $scope.navigateTo = function(path) {
        if (path.startsWith("/")) {
            $location.path(path);
        } else {
            $location.path('configuration/things/' + path);
        }
    }

    function activate() {
        $q(function() {
            $scope.$watch('configuration', function() {
                if ($scope.configuration && $scope.thing) {
                    $scope.thing.configuration = $scope.configuration;
                }
            });

            thingRepository.getOne(function(thing) {
                return thing.UID === thingUID;
            }, function(thing) {
                $scope.thing = thing;
                angular.copy(thing, originalThing);

                $scope.setSubtitle([ 'Things', 'Edit', thing.label ]);

                // Now get the configuration information for this thing
                configDescriptionService.getByUri({
                    uri : "thing:" + thing.UID
                }, function(configDescription) {
                    if (configDescription) {
                        $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
                        $scope.configuration = configService.setConfigDefaults($scope.thing.configuration, $scope.parameters)
                    }
                });

            }, true);
        });
    }

    function update(thing) {
        thing.configuration = configService.setConfigDefaults(thing.configuration, $scope.parameters, true);
        if (JSON.stringify(originalThing.configuration) !== JSON.stringify(thing.configuration)) {
            thing.configuration = configService.replaceEmptyValues(thing.configuration);
            thingService.updateConfig({
                thingUID : thing.UID
            }, thing.configuration);
        }
        originalThing.configuration = thing.configuration;
        originalThing.channels = thing.channels;
        if (JSON.stringify(originalThing) !== JSON.stringify(thing)) {
            thingService.update({
                thingUID : thing.UID
            }, thing);
        }
        toastService.showDefaultToast('Thing updated');
        $scope.navigateTo('view/' + thing.UID);
    }
})
