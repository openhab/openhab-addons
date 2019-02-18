angular.module('PaperUI.items')//
.controller('ItemSetupController', function($scope, $timeout, $location, $mdDialog, $filter, itemRepository, toastService, sharedProperties) {
    $scope.navigateTo = function(path) {
        $location.path('configuration/' + path);
    }

    $scope.setSubtitle([ 'Items' ]);
    $scope.setHeaderText('Shows all configured Items.');
    $scope.items = [], $scope.groups = [], $scope.types = [];

    $scope.refresh = function() {
        itemRepository.getAll(function(items) {
            $scope.items = items;
            var groups = [], types = [];
            for (var i = 0; i < items.length; i++) {
                if (items[i].type && items[i].type == "Group") {
                    groups.push(items[i]);
                }
                if (items[i].type && types.indexOf(items[i].type) == -1) {
                    types.push(items[i].type);
                }
            }
            $scope.groups = groups;
            $scope.types = types;
        }, true);

    };
    $scope.remove = function(item, event) {
        event.stopImmediatePropagation();
        $mdDialog.show({
            controller : 'ItemRemoveController',
            templateUrl : 'partials/dialog.remove.html',
            targetEvent : event,
            hasBackdrop : true,
            locals : {
                item : item
            }
        }).then(function() {
            $scope.refresh();
        });
    }
    $scope.getSrcURL = function(category, type) {
        return category ? '../icon/' + category.toLowerCase() : type ? '../icon/' + type.toLowerCase().replace('item', '') : '';
    }
    $scope.clearAll = function() {
        $scope.searchText = "";
        $scope.$broadcast("ClearFilters");
    }
    $scope.createItem = function(selectedType, selectedGroup) {
        sharedProperties.updateParams({
            selectedType : selectedType,
            selectedGroup : selectedGroup ? selectedGroup.name : ''
        });
        $scope.navigateTo('item/create')
    }

    $scope.refresh();
}).controller('ItemConfigController', function($scope, $mdDialog, $filter, $location, $q, toastService, itemService, itemConfig, itemRepository, sharedProperties) {
    $scope.items = [];
    $scope.oldCategory;
    $scope.types = itemConfig.types;
    $scope.groupTypes = itemConfig.groupTypes;
    $scope.dimensions = itemConfig.dimensions;
    $scope.functions = [];
    $scope.selectedMember = null;
    $scope.selectedParent = null;
    $scope.searchText = null;
    $scope.childItems = [];
    $scope.linking = false;

    var ctrl = this;
    this.itemName;
    this.originalItem = {};

    this.$onInit = activate;

    function activate() {
        return $q(function(resolve, reject) {
            itemRepository.getAll().then(function(items) {
                $scope.items = items;
            });

            if (sharedProperties.getParams().length > 0 && sharedProperties.getParams()[0].linking) {
                $scope.linking = true;
                $scope.types = sharedProperties.getParams()[0].acceptedItemType;
            } else if ($scope.path && $scope.path.length > 4) {
                ctrl.itemName = $scope.path[4];
            }

            if (ctrl.itemName) {
                itemService.getByName({
                    itemName : ctrl.itemName,
                    metadata : '.*'
                }, function(item) {
                    $scope.item = item;
                    setFunctionToItem();
                    angular.copy($scope.item, ctrl.originalItem);
                    if (!$scope.item['function']) {
                        $scope.item['function'] = {
                            name : ''
                        };
                    }
                    if (!$scope.item.groupType) {
                        $scope.item.groupType = "None";
                    }

                    if ($scope.item.type.indexOf("Number:") === 0) {
                        $scope.item.dimension = $scope.item.type.substring(7, $scope.item.type.length);
                        $scope.item.type = "Number";
                    }
                    $scope.configMode = "edit";
                    $scope.srcURL = $scope.getSrcURL($scope.item.category, $scope.item.type);
                    $scope.oldCategory = $scope.item.category;
                    $scope.setTitle('Edit ' + $scope.item.name);
                    $scope.setSubtitle([]);

                    resolve();
                })
            } else {
                $scope.item = {
                    editable : true
                };
                $scope.item.groupNames = [];
                if ($scope.setTitle) {
                    $scope.setTitle('Configuration');
                }
                if ($scope.setSubtitle) {
                    $scope.setSubtitle([ 'New Item' ]);
                }
                if ($scope.types.length > 0) {
                    $scope.item.type = $scope.types[0];
                }
                if (sharedProperties.getParams().length > 0) {
                    if (sharedProperties.getParams()[0].linking) {
                        $scope.item.name = sharedProperties.getParams()[0].suggestedName;
                        $scope.item.label = sharedProperties.getParams()[0].suggestedLabel;
                        $scope.item.category = sharedProperties.getParams()[0].suggestedCategory;
                    } else {
                        if (sharedProperties.getParams()[0].selectedType) {
                            $scope.item.type = sharedProperties.getParams()[0].selectedType;
                        }
                        if (sharedProperties.getParams()[0].selectedGroup) {
                            $scope.item.groupNames = $scope.item.groupNames ? $scope.item.groupNames : [];
                            $scope.item.groupNames.push(sharedProperties.getParams()[0].selectedGroup);
                        }
                    }
                }
                $scope.configMode = "create";
                resolve();
            }
        });
    }

    $scope.update = function() {
        var toastText = "Item updated.";
        if (!$scope.item.editable) {
            updateMetadata($scope.item.name, $scope.item.metadata, ctrl.originalItem, toastText);
            $location.path('configuration/items');
        } else {
            putItem(toastText);
        }
    }
    $scope.create = function() {
        putItem("Item created.");
    }

    function putItem(text) {
        if ($scope.item.type !== "Group") {
            delete $scope.item['function'];
            delete $scope.item.groupType;
        } else {
            setItemToFunction();
        }
        if ($scope.item.dimension && $scope.item.dimension !== '') {
            $scope.item.type = $scope.item.type + ":" + $scope.item.dimension;
            delete $scope.item.dimension;
        }

        if ($scope.item.editable && JSON.stringify($scope.item) !== JSON.stringify(ctrl.originalItem)) {
            if ($scope.item.category == "") {
                $scope.item.category = null;
            }
            itemService.create({
                itemName : $scope.item.name
            }, $scope.item).$promise.then(function() {
                return updateMetadata($scope.item.name, $scope.item.metadata, ctrl.originalItem);
            }).then(function() {
                toastService.showDefaultToast(text);
                itemRepository.setDirty(true);
                if ($scope.linking) {
                    $scope.$emit("ItemCreated", {
                        status : true,
                        itemName : $scope.item.name,
                        label : $scope.item.label
                    });
                } else {
                    $location.path('configuration/items');
                }
                sharedProperties.resetParams();
            }, function(failed) {
                if ($scope.linking) {
                    $scope.$emit("ItemCreated", {
                        status : false
                    });
                } else {
                    $location.path('configuration/items');
                }
                sharedProperties.resetParams();
            });
        }
    }

    function setItemToFunction() {
        if ($scope.item.groupType.indexOf("None") == -1) {
            var splitValue = $scope.item['function'].name.split('_');
            $scope.item['function'].name = splitValue[0];
            if (splitValue.length > 1) {
                $scope.item['function'].params = [ splitValue[1], splitValue[2] ];
            }
        } else {
            delete $scope.item.groupType;
        }
        if ($scope.item['function'] && !$scope.item['function'].name) {
            $scope.item['function'] = null;
        }
    }

    function setFunctionToItem() {
        if ($scope.item['function'] && $scope.item['function'].name && $scope.item['function'].params) {
            $scope.item['function'].name += "_" + $scope.item['function'].params[0] + "_" + $scope.item['function'].params[1];
        }
    }

    function updateMetadata(itemName, metadata, originalItem, toastText) {
        return $q(function(resolve, reject) {
            var resolveLater = false;
            var namespaceUpdatePromisses = [];
            if (!originalItem.metadata || JSON.stringify(metadata) !== JSON.stringify(originalItem.metadata)) {
                for ( var namespace in metadata) {
                    if (!metadata.hasOwnProperty(namespace) || metadata[namespace].value === undefined || metadata[namespace].value.length === 0) {
                        continue;
                    }

                    if (!originalItem.metadata || JSON.stringify(metadata[namespace]) !== JSON.stringify(originalItem.metadata[namespace])) {
                        resolveLater = true;
                        var namespaceUpdatePromise = itemService.updateMetadata({
                            itemName : itemName,
                            namespace : namespace
                        }, metadata[namespace]).$promise.then(function() {
                            if (toastText) {
                                toastService.showDefaultToast(toastText);
                            }
                        });

                        namespaceUpdatePromisses.push(namespaceUpdatePromise);
                    }
                }
            }
            if (!resolveLater) {
                resolve();
            } else {
                $q.all(namespaceUpdatePromisses).then(resolve());
            }
        });
    }

    $scope.renderIcon = function() {
        $scope.oldCategory = $scope.item.category;
        $scope.srcURL = $scope.getSrcURL($scope.item.category, $scope.item.type);
    }

    $scope.searchItem = function(searchText) {
        var criterion = {
            name : searchText,
            type : "Group"
        };

        var items = $filter('filter')($scope.items, criterion);
        items = $filter('orderBy')(items, 'name');
        if (items.indexOf($scope.item.name) != -1) {
            items.splice(items.indexOf($scope.item.name), 1);
        }
        return items.map(function(item) {
            return item.name;
        });
    }

    $scope.openItem = function() {
        $location.path('configuration/item/edit/' + $scope.selectedItem);
    }

    $scope.setParentItem = function($chip) {
        if ($chip) {
            $scope.selectedParent = $chip;
        } else {
            $scope.selectedParent = null;
        }
    }
    $scope.setMemberItem = function($chip) {
        if ($chip) {
            $scope.selectedMember = $chip;
        } else {
            $scope.selectedMember = null;
        }
    }

    $scope.boxClicked = function() {
        $scope.selectedItem = null;
    }
    $scope.$on('ItemLinkedClicked', function(event, args) {
        event.preventDefault();
        $scope.create();
    });

    $scope.$watch('item.groupType', function() {
        if (!$scope.item) {
            return;
        }
        var groupType = $scope.item.groupType;
        switch (groupType) {
            case 'Number':
            case 'Dimmer':
            case 'Rollershutter':
                $scope.functions = itemConfig.arithmeticFunctions;
                break;
            case 'Contact':
                $scope.functions = itemConfig.logicalOpenClosedFunctions;
                break;
            case 'DateTime':
                $scope.functions = itemConfig.dateTimeFunctions;
                break;
            default:
                $scope.functions = itemConfig.logicalOnOffFunctions;
        }
    });

    $scope.getSrcURL = function(category, type) {
        return category ? '../icon/' + category.toLowerCase() : type ? '../icon/' + type.toLowerCase().replace('item', '') : '';
    }

}).controller('ItemRemoveController', function($scope, $mdDialog, $filter, $location, toastService, itemService, itemRepository, item) {
    $scope.item = item;
    $scope.remove = function(itemName) {
        itemService.remove({
            itemName : itemName
        }, function() {
            itemRepository.setDirty(true);
            toastService.showDefaultToast('Item removed.');
        });
        $mdDialog.hide();
    }

    $scope.close = function() {
        $mdDialog.cancel();
    }
}).directive('itemname', function() {
    return {
        restrict : 'A',
        require : 'ngModel',
        link : function(scope, element, attr, ctrl) {
            function customValidator(ngModelValue) {

                var items = getItems();
                if (!searchItemNameExists(ngModelValue, items)) {
                    ctrl.$setValidity('nameValidator', true);
                    if (ngModelValue != null && ngModelValue.length != 0) {
                        element.parent().removeClass('md-input-invalid');
                    }
                } else {
                    ctrl.$setValidity('nameValidator', false);
                    element.parent().addClass('md-input-invalid');
                }
                return ngModelValue;
            }
            function searchItemNameExists(value, arr) {
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i].name == value) {
                        return true;
                    }
                }
                return false;
            }
            function getItems() {
                return scope.items;
            }
            ctrl.$parsers.push(customValidator);
            setTimeout(function() {
                if (ctrl.$viewValue) {
                    customValidator(ctrl.$viewValue);
                }
            });
        }
    };
}).directive('mdChips', function() {
    return {
        restrict : 'E',
        require : 'mdChips',
        link : function(scope, element, attributes, ctrl) {
            setTimeout(deferListeners, 500);
            function deferListeners() {
                var chipContents = element[0].getElementsByClassName('md-chip-content');
                for (var i = 0; i < chipContents.length; i++) {
                    chipContents[i].addEventListener("blur", function() {
                        ctrl.$scope.$apply();
                    });
                }
            }
            scope.createChip = function(chip) {
                setTimeout(deferListeners, 500);
                function deferListeners() {
                    var chipContents = document.getElementsByClassName('md-chip-content');
                    for (var i = 0; i < chipContents.length; i++) {
                        chipContents[i].addEventListener("blur", addChipBlurEvent);
                    }
                }
            }
            function addChipBlurEvent() {
                scope.$apply();
                setTimeout(function() {
                    // scope.selectedItem = null;
                    scope.$apply();
                }, 300);
            }
            scope.removeChip = function(chipIndex) {
                var chipContents = document.getElementsByClassName('md-chip-content');
                if (chipContents.length > chipIndex) {
                    chipContents[chipIndex].removeEventListener("blur", addChipBlurEvent);
                }
            }
        }
    }
});