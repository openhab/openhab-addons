angular.module('PaperUI.controllers.configuration').controller('SystemController', function($scope, $mdDialog, serviceConfigService, toastService) {
    $scope.setSubtitle([ 'System' ]);
    $scope.setHeaderText('Shows all system configurations.');
    $scope.tabs = [];
    $scope.refresh = function() {
        serviceConfigService.getAll(function(services) {
            var arrOfIndex = [];
            var index = 0;
            $scope.services = $.grep(services, function(service) {
                return service.category && service.category.toUpperCase() === 'SYSTEM';
            });
            angular.forEach($scope.services, function(service) {
                service.found = true;
            });
        });
    };
    $scope.$on('noConfigDesc', function(event, serviceId) {
        var services = $.grep($scope.services, function(service) {
            return service.id == serviceId;
        });
        services[0].found = false;
        event.stopPropagation();
        event.preventDefault();
    });
    $scope.refresh();
}).controller('ConfigureSystemServiceController', function($rootScope, $scope, $mdDialog, configService, serviceConfigService, configDescriptionService, toastService) {

    $scope.editing = false;
    $scope.service = null;
    $scope.parameters = [];
    $scope.configuration = {};
    $scope.config = {};
    $scope.serviceId;
    $scope.configDescriptionURI;
    var originalServiceConf = {};
    $scope.getConfigDescription = function() {
        var serviceId = $scope.serviceId;
        var configDescriptionURI = $scope.configDescriptionURI;
        if (configDescriptionURI) {
            configDescriptionService.getByUri({
                uri : configDescriptionURI
            }, function(configDescription) {
                if (configDescription) {
                    $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
                    $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
                    $scope.configuration = replaceUndefWithEmpty($scope.configuration);
                    angular.copy($scope.configuration, originalServiceConf);
                }
            }, function() {
                $scope.$emit('noConfigDesc', serviceId);
            });
        }
        if (serviceId) {
            serviceConfigService.getById({
                id : serviceId
            }, function(service) {
                $scope.service = service;
            });
            serviceConfigService.getConfigById({
                id : serviceId
            }).$promise.then(function(config) {
                if (config) {
                    $scope.configuration = configService.convertValues(config);
                    if ($scope.parameters && $scope.parameters.length > 0) {
                        $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
                        angular.copy($scope.configuration, originalServiceConf)
                    }
                }
            });
        }
    }

    $scope.save = function() {
        if (JSON.stringify($scope.configuration) !== JSON.stringify(originalServiceConf)) {
            var configuration = {};
            var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
            serviceConfigService.updateConfig({
                id : $scope.serviceId
            }, configuration, function() {
                angular.copy($scope.configuration, originalServiceConf);
                if ($scope.serviceId === "org.eclipse.smarthome.links") {
                    $rootScope.advancedMode = !jQuery.isEmptyObject($scope.configuration) ? !$scope.configuration.autoLinks : $rootScope.advancedMode;
                }
                toastService.showDefaultToast('System config updated.');
            });
        }
        $scope.editing = false;
    }
    function replaceUndefWithEmpty(obj) {
        for ( var key in obj) {
            if (obj.hasOwnProperty(key)) {
                if (obj[key] === undefined || obj[key] === null) {
                    obj[key] = "";
                }
            }
        }
        return obj;
    }
    $scope.$watch('configuration', function() {
        if (!$scope.configuration) {
            return;
        }
        if (JSON.stringify($scope.configuration) !== JSON.stringify(originalServiceConf)) {
            $scope.editing = true;
        } else {
            $scope.editing = false;
        }
    }, true);
});