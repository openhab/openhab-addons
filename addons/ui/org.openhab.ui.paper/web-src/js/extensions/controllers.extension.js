angular.module('PaperUI.controllers.extension', [ 'PaperUI.constants' ]).controller('ExtensionPageController', function($scope, $routeParams, extensionService, bindingRepository, thingTypeRepository, eventService, toastService, $filter, $window, $timeout, $location, templateRepository) {
    $scope.navigateTo = function(path) {
        $location.path('extensions/' + path);
    };

    $scope.selectedIndex;
    var selectedTabName = $routeParams.tab;
    $scope.extensionTypes = [];

    var view = window.localStorage.getItem('paperui.extension.view')
    $scope.showCards = view ? view.toUpperCase() == 'LIST' ? false : true : false;
    $scope.searchText = [];
    $scope.refresh = function() {
        extensionService.getAllTypes(function(extensionTypes) {
            $scope.extensionTypes = [];
            $scope.searchText = new Array(extensionTypes.length);
            registerWatchers();
            angular.forEach(extensionTypes, function(extensionType) {
                $scope.extensionTypes.push({
                    typeId : extensionType.id,
                    label : extensionType.label,
                    extensions : [],
                    inProgress : false
                });
            });
            extensionService.getAll(function(extensions) {
                angular.forEach(extensions, function(extension) {
                    var extensionType = $scope.getType(extension.type);
                    if (extensionType !== undefined) {
                        extensionType.extensions.push(extension);
                    }
                });
                angular.forEach($scope.extensionTypes, function(extensionType) {
                    extensionType.extensions = $filter('orderBy')(extensionType.extensions, "label")
                });
            });

            if (selectedTabName) {
                var selectedTab = $scope.extensionTypes.find(function(tab) {
                    return tab.typeId.toUpperCase() === selectedTabName.toUpperCase();
                });
                $scope.selectedIndex = selectedTab ? $scope.extensionTypes.indexOf(selectedTab) : 0;
            }
        });
    }

    $scope.onTabSelected = function(extensionType) {
        $scope.masonry($scope.showCards);
        $location.path('/extensions').search('tab', extensionType.typeId);
    }

    $scope.changeView = function(showCards) {
        if (showCards) {
            window.localStorage.setItem('paperui.extension.view', 'card');
        } else {
            window.localStorage.setItem('paperui.extension.view', 'list');
        }
        $scope.showCards = showCards;
    }

    $scope.getType = function(extensionTypeId) {
        var result;
        angular.forEach($scope.extensionTypes, function(extensionType) {
            if (extensionType.typeId === extensionTypeId) {
                result = extensionType;
            }
        });
        return result;
    };
    $scope.getExtension = function(extensionId) {
        var result;
        angular.forEach($scope.extensionTypes, function(extensionType) {
            angular.forEach(extensionType.extensions, function(extension) {
                if (extension.id === extensionId) {
                    result = extension;
                }
            });
        });
        return result;
    };
    $scope.refresh();
    $scope.install = function(extensionId) {
        var extension = $scope.getExtension(extensionId);
        extension.inProgress = true;
        extensionService.install({
            id : extensionId
        });
        bindingRepository.setDirty(true);
        thingTypeRepository.setDirty(true);
    };
    $scope.installExtensionFromURL = function(url) {
        return extensionService.installFromURL({
            url : url
        });
    };
    $scope.uninstall = function(extensionId) {
        var extension = $scope.getExtension(extensionId);
        extension.inProgress = true;
        extensionService.uninstall({
            id : extensionId
        });
        bindingRepository.setDirty(true);
        thingTypeRepository.setDirty(true);
    };
    $scope.openExternalLink = function(link) {
        if (link) {
            $window.open(link, '_blank');
        }
    }

    $scope.filterItems = function(lookupFields) {
        return function(item) {
            var searchText = $scope.searchText[$scope.selectedIndex];
            if (searchText && searchText.length > 0) {
                for (var i = 0; i < lookupFields.length; i++) {
                    if (item[lookupFields[i]] && item[lookupFields[i]].toUpperCase().indexOf(searchText.toUpperCase()) != -1) {
                        return true;
                    }
                }
                return false
            }
            return true;
        }
    }
    $scope.masonry = function(showCards) {
        if (showCards) {
            $timeout(function() {
                var itemContainer = '#extensions-' + ($scope.selectedIndex ? $scope.selectedIndex : 0);
                new Masonry(itemContainer, {});
            }, 1, true);
        }
    }

    function registerWatchers() {
        for (var i = 0; i < $scope.searchText.length; i++) {
            function intern(local) {
                var index = local;
                $scope.$watch(function() {
                    return $scope.searchText[index];
                }, function(newValue, oldValue) {
                    if ($scope.showCards && (newValue === undefined || newValue !== oldValue)) {
                        $scope.masonry(true);
                    }
                });
            }
            intern(i);
        }
    }

    eventService.onEvent('smarthome/extensions/*', function(topic, extensionObject) {
        var id = extensionObject;
        if (extensionObject && Array.isArray(extensionObject)) {
            id = extensionObject[0]
        }
        var extension = $scope.getExtension(id);
        if (extension) {
            extension.inProgress = false;
            if (topic.indexOf("uninstalled") > -1) {
                extension.installed = false;
                toastService.showDefaultToast('Extension ' + extension.label + ' uninstalled.');
            } else if (topic.indexOf("installed") > -1) {
                extension.installed = true;
                toastService.showDefaultToast('Extension ' + extension.label + ' installed.');
                if (extension.type == "ruletemplate") {
                    $scope.$broadcast("RuleExtensionInstalled", extension.id);
                }
            } else {
                var msg = Array.isArray(extensionObject) ? extensionObject[1] : 'Install or uninstall of extension ' + extension.label + ' failed.';
                toastService.showDefaultToast(msg);
                $scope.$broadcast("RuleExtensionFailed");
            }
        }
    });
}).directive('droppable', function(toastService) {
    return {
        restrict : 'A',
        link : function(scope, element, attrs) {

            scope.ondrag = false;
            var counter = 0;
            element[0].addEventListener('dragover', function(event) {
                event.preventDefault();
            });
            element[0].addEventListener('dragenter', function(event) {
                event.preventDefault();
                if (counter == 0) {
                    scope.$apply(function() {
                        scope.ondrag = true;
                    });
                }
                counter++;
            });
            element[0].addEventListener('dragleave', function(event) {
                event.preventDefault();
                counter--;
                if (counter == 0) {
                    scope.$apply(function() {
                        scope.ondrag = false;
                    });
                }

            });
            element[0].addEventListener('drop', function(event) {
                event.preventDefault();
                var data = event.dataTransfer.getData("Text");
                var response = scope.installExtensionFromURL(data);
                response.$promise.then(function() {
                    toastService.showDefaultToast('Extension installed from URL');
                    scope.ondrag = false;
                }, function() {
                    toastService.showDefaultToast('Extension installation from URL failed');
                    scope.ondrag = false;
                });

            });

        }
    };
});
