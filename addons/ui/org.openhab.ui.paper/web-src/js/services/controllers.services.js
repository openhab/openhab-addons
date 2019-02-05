angular.module('PaperUI.controllers.configuration', [ 'PaperUI.constants', 'PaperUI.controllers.firmware', 'PaperUI.controllers.configurableServiceDialog' ]) //
.controller('ServicesController', function($scope, $routeParams, $mdDialog, $location, serviceConfigService, toastService) {
    $scope.setSubtitle([ 'Services' ]);
    $scope.setHeaderText('Shows all configurable services.');
    $scope.tabs = [];
    $scope.selectedTabIndex;

    var selectedTabName = $routeParams.tab;

    $scope.navigateTo = function(path) {
        $location.path('/configuration/services/' + path);
    }

    $scope.refresh = function() {
        var tempTabs = {};
        serviceConfigService.getAll(function(services) {
            angular.forEach(services, function(service) {
                if (service.category === 'system') {
                    return true;
                }

                if (tempTabs[service.category] === undefined) {
                    tempTabs[service.category] = {
                        services : [],
                        category : service.category
                    }
                }
                tempTabs[service.category].services.push(service);
            });

            var renderedTabs = [];
            angular.forEach(tempTabs, function(tab) {
                renderedTabs.push(tab);
            });

            renderedTabs = renderedTabs.sort(function(a, b) {
                return a.category < b.category ? -1 : a.category > b.category ? 1 : 0
            })

            $scope.tabs = renderedTabs;

            if (selectedTabName) {
                var selectedTab = $scope.tabs.find(function(tab) {
                    return tab.category.toUpperCase() === selectedTabName.toUpperCase();
                });
                $scope.selectedTabIndex = selectedTab ? $scope.tabs.indexOf(selectedTab) : 0;
            }
        });
    };

    $scope.configure = function(serviceId, configDescriptionURI, event) {
        $mdDialog.show({
            controller : 'ConfigurableServiceDialogController',
            templateUrl : 'partials/services/dialog.configureservice.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                serviceId : serviceId,
                configDescriptionURI : configDescriptionURI,
                multiple : false
            }
        });
    }

    $scope.onSelectedTab = function(tab) {
        $location.path('/configuration/services').search('tab', tab.category);
    }

    $scope.refresh();
}).controller('MultiServicesController', function($scope, $mdDialog, $location, $routeParams, serviceConfigService, toastService) {
    $scope.setSubtitle([ 'Services' ]);
    $scope.setHeaderText('Shows all multiple configurable services.');
    $scope.servicePID = $routeParams.servicePID;
    $scope.serviceContexts = [];

    $scope.navigateTo = function(path) {
        $location.path('/configuration/services/' + path);
    }

    $scope.configure = function(serviceId, event) {
        $mdDialog.show({
            controller : 'ConfigurableServiceDialogController',
            templateUrl : 'partials/services/dialog.configureservice.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                serviceId : serviceId,
                configDescriptionURI : $scope.serviceConfigDescriptionURI,
                multiple : true
            }
        }).then(function() {
            $scope.refresh();
        });
    };

    $scope.deleteConfig = function(serviceContext, event) {
        $mdDialog.show({
            controller : 'ServiceConfigRemoveController',
            templateUrl : 'partials/dialog.remove.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                serviceContext : serviceContext
            }
        }).then(function() {
            $scope.refresh();
        });
    }

    serviceConfigService.getById({
        id : $scope.servicePID
    }, function(service) {
        $scope.serviceLabel = service.label;
        $scope.setSubtitle([ 'Services', service.label ]);
        $scope.serviceConfigDescriptionURI = service.configDescriptionURI;
    });

    $scope.refresh = function() {
        serviceConfigService.getContexts({
            id : $scope.servicePID
        }, function(serviceContexts) {
            $scope.serviceContexts = serviceContexts;
        });
    }

    $scope.refresh();
}).controller('ServiceConfigRemoveController', function($scope, $mdDialog, $filter, $location, toastService, serviceConfigService, serviceContext) {
    $scope.serviceContext = serviceContext;
    $scope.remove = function() {
        serviceConfigService.deleteConfig({
            id : serviceContext.id
        }, function() {
            toastService.showDefaultToast('Service config removed.');
        });
        $mdDialog.hide();
    }

    $scope.close = function() {
        $mdDialog.cancel();
    }
});
