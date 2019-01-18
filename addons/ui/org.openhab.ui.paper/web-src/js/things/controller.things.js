angular.module('PaperUI.things') //
.controller('RemoveThingDialogController', function($scope, $mdDialog, toastService, thingService, thing) {
    $scope.thing = thing;
    if (thing.statusInfo) {
        $scope.isRemoving = thing.statusInfo.status === 'REMOVING';
    }
    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.remove = function(thingUID) {
        var forceRemove = $scope.isRemoving ? true : false;
        thingService.remove({
            thingUID : thing.UID,
            force : forceRemove
        }, function() {
            if (forceRemove) {
                toastService.showDefaultToast('Thing removed (forced).');
            } else {
                toastService.showDefaultToast('Thing removal initiated.');
            }
            $mdDialog.hide();
        }, function() {
            $mdDialog.hide();
        });
    }
}).controller('LinkChannelDialogController', function($rootScope, $scope, $mdDialog, $filter, toastService, itemRepository, itemService, profileTypeRepository, sharedProperties, params) {
    $scope.itemName;
    $scope.linkedItems = params.linkedItems;
    $scope.advancedMode = $rootScope.advancedMode;
    $scope.category = params.category;
    $scope.itemFormVisible = false;
    $scope.itemsList = [];
    $scope.channel = params.channel;
    $scope.linkModel = params.link;

    var createAcceptedItemTypes = function(paramItemTypes) {
        var acceptedItemTypes = [];
        var addToAcceptedItemTypes = function(itemType) {
            if (acceptedItemTypes.indexOf(itemType) < 0) {
                acceptedItemTypes.push(itemType);
            }
        }

        angular.forEach(paramItemTypes, function(itemType) {
            addToAcceptedItemTypes(itemType);
            if (itemType == 'Color') {
                addToAcceptedItemTypes('Switch');
                addToAcceptedItemTypes('Dimmer');
            } else if (itemType == 'Dimmer') {
                addToAcceptedItemTypes('Switch');
            } else if (itemType.indexOf('Number:') === 0) {
                addToAcceptedItemTypes('Number');
            }

        });

        return acceptedItemTypes;
    }

    activate();

    $scope.checkCreateOption = function() {
        if ($scope.itemName == "_createNew") {
            $scope.itemFormVisible = true;
            sharedProperties.resetParams();
            sharedProperties.updateParams({
                linking : true,
                acceptedItemType : $scope.acceptedItemTypes,
                suggestedName : params.suggestedName,
                suggestedLabel : params.suggestedLabel,
                suggestedCategory : params.suggestedCategory
            });
        } else {
            $scope.itemFormVisible = false;
        }
    }
    $scope.createAndLink = function() {
        $scope.$broadcast("ItemLinkedClicked");
    }
    $scope.close = function() {
        $mdDialog.cancel();
        sharedProperties.resetParams();
    }
    $scope.link = function(itemName, label) {
        $mdDialog.hide({
            itemName : itemName,
            label : label
        });
    }
    $scope.$on('ItemCreated', function(event, args) {
        event.preventDefault();
        if (args.status) {
            $scope.link(args.itemName, args.label);
        } else {
            toastService.showDefaultToast('Some error occurred');
            $scope.close();
        }
    });
    $scope.$watch(function watchFunction() {
        return $scope.linkModel.configuration['profile'];
    }, function(newValue) {
        activate();
    });

    if (params.preSelectCreate) {
        $scope.itemName = "_createNew";
        $scope.checkCreateOption();
    }

    function activate() {
        profileTypeRepository.getAll().then(function() {
            var profileTypeUid = $scope.linkModel.configuration['profile'];
            if (profileTypeUid === undefined || profileTypeUid === "system:default") {
                $scope.acceptedItemTypes = createAcceptedItemTypes(params.acceptedItemTypes);
            } else {
                profile = profileTypeRepository.find(function(element) {
                    return element.uid == profileTypeUid;
                });
                $scope.acceptedItemTypes = profile.supportedItemTypes;
            }

            itemRepository.getAll(function(items) {
                $scope.items = items;
                if ($scope.acceptedItemTypes.length > 0) {
                    $scope.itemsList = $.grep($scope.items, function(item) {
                        return $scope.acceptedItemTypes.indexOf(item.type) != -1;
                    });
                } else {
                    $scope.itemsList = $scope.items;
                }
                $scope.itemsList = $.grep($scope.itemsList, function(item) {
                    return $scope.linkedItems.indexOf(item.name) == -1;
                });
                if (params.allowNewItemCreation) {
                    $scope.itemsList.push({
                        name : "_createNew",
                        type : $scope.acceptedItemType
                    });
                }
                $scope.itemsList = $filter('orderBy')($scope.itemsList, "name");
            });
        });
    }
}).controller('UnlinkChannelDialogController', function($scope, $mdDialog, toastService, linkService, itemName) {
    $scope.itemName = itemName;
    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.unlink = function() {
        $mdDialog.hide();
    }
}).controller('ChannelConfigController', function($scope, $mdDialog, toastService, thingService, configService, channelType, channel, thing) {
    $scope.parameters = configService.getRenderingModel(channelType.parameters, channelType.parameterGroups);
    $scope.configuration = {}
    angular.copy(channel.configuration, $scope.configuration)

    $scope.close = function() {
        $mdDialog.cancel();
    }
    $scope.save = function() {
        channel.configuration = $scope.configuration
        thingService.update({
            thingUID : thing.UID
        }, thing, function() {
            $mdDialog.hide();
            toastService.showDefaultToast('Channel updated');
        });
    }
})