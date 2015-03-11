function getThingTypeUID(thingUID) {
	var segments = thingUID.split(':');
	return segments[0] + ':' + segments[1];
};
    
angular.module('SmartHomeManagerApp.controllers.configuration', 
[]).controller('ConfigurationPageController', function($scope, $location) {
    $scope.navigateTo = function(path) {
        $location.path('configuration/' + path);
    }
}).controller('BindingController', function($scope, $mdDialog, bindingRepository) {
	$scope.setSubtitle(['Bindings']);
	$scope.setHeaderText('Shows all installed bindings.');
	$scope.refresh = function() {
		bindingRepository.getAll();	
	};
	$scope.openBindingInfoDialog = function(bindingId, event) {
		$mdDialog.show({
			controller : 'BindingInfoDialogController',
			templateUrl : 'partials/dialog.bindinginfo.html',
			targetEvent : event,
			hasBackdrop: true,
			locals: {bindingId: bindingId}
		});
	}
	bindingRepository.getAll();
}).controller('BindingInfoDialogController', function($scope, $mdDialog, bindingRepository, bindingId) {
	$scope.binding = undefined;
	bindingRepository.getOne(function(binding) {
		return binding.id === bindingId;
	}, function(binding) {
		$scope.binding = binding;
	});
	$scope.close = function() {
		$mdDialog.hide();
	}
}).controller('GroupController', function($scope, $mdDialog, toastService, homeGroupRepository, groupSetupService) {
	$scope.setSubtitle(['Home Groups']);
	$scope.setHeaderText('Shows all configured Home Groups.');
	$scope.getAll = function() {
		homeGroupRepository.getAll();	
	}
	$scope.add = function() {
		$mdDialog.show({
			controller : 'AddGroupDialogController',
			templateUrl : 'partials/dialog.addgroup.html',
			targetEvent : event
		}).then(function(label) {
			groupSetupService.add({
	            name : 'home_group_' + $scope.generateUUID(),
	            label: label
	        }, function() {
	            $scope.getAll();
	            toastService.showDefaultToast('Group added.');
	        });
		});
	};
	$scope.remove = function(homeGroup, event) {
    	var confirm = $mdDialog.confirm()
	      .title('Remove ' + homeGroup.label)
	      .content('Would you like to remove the group?')
	      .ariaLabel('Remove Group')
	      .ok('Remove')
	      .cancel('Cancel')
	      .targetEvent(event);
	    $mdDialog.show(confirm).then(function() {
	    	groupSetupService.remove({
	            itemName : homeGroup.name
	        }, function() {
	            $scope.getAll();
	            toastService.showSuccessToast('Group removed');
	        });
	    });
    };
	$scope.getAll();
}).controller('AddGroupDialogController', function($scope, $mdDialog) {
	$scope.binding = undefined;
	
	$scope.close = function() {
		$mdDialog.cancel();
	}
	$scope.add  = function(label) {		
		$mdDialog.hide(label);
	}
}).controller('ThingController', function($scope, $timeout, $mdDialog, thingTypeRepository, thingRepository, thingSetupService, toastService) {
	$scope.setSubtitle(['Things']);
	$scope.setHeaderText('Shows all configured Things.');
	
	thingTypeRepository.getAll();
	thingRepository.getAll();
	
	$scope.refresh = function() {
		thingRepository.getAll();	
	};
	$scope.remove = function(thing, event) {
		var label = thing.item ? thing.item.label : thing.UID;
		var confirm = $mdDialog.confirm()
	      .title('Remove ' + label)
	      .content('Would you like to remove the thing from the system?')
	      .ariaLabel('Remove Thing')
	      .ok('Remove')
	      .cancel('Cancel')
	      .targetEvent(event);
	    $mdDialog.show(confirm).then(function() {
	    	thingSetupService.remove({thingUID: thing.UID});
	    	toastService.showDefaultToast('Thing removed');
	    	$scope.refresh();
	    });
	    event.stopImmediatePropagation();
	};
	
}).controller('ViewThingController', function($scope, $mdDialog, toastService, thingTypeRepository, 
		thingRepository, thingSetupService) {
	
	var thingUID = $scope.path[4];
	var thingTypeUID = getThingTypeUID(thingUID);
	
	$scope.thing;
	$scope.thingType;
	$scope.edit = function(thing, event) {
		$mdDialog.show({
			controller : 'EditThingDialogController',
			templateUrl : 'partials/dialog.editthing.html',
			targetEvent : event,
			hasBackdrop: true,
			locals: {thing: thing}
		});
	};
	$scope.remove = function(thing, event) {
		var confirm = $mdDialog.confirm()
	      .title('Remove ' + thing.item.label)
	      .content('Would you like to remove the thing from the system?')
	      .ariaLabel('Remove Thing')
	      .ok('Remove')
	      .cancel('Cancel')
	      .targetEvent(event);
	    $mdDialog.show(confirm).then(function() {
	    	thingSetupService.remove({thingUID: thing.UID});
	    	toastService.showDefaultToast('Thing removed');
	    	$scope.navigateTo('things');
	    });
	};
	
	$scope.enableChannel = function(thingUID, channelID) {
		thingSetupService.enableChannel({channelUID: thingUID + ':' + channelID}, function() {
			$scope.getThing(true);
			toastService.showDefaultToast('Channel enabled');
		});
	};
	
	$scope.disableChannel = function(thingUID, channelID) {
		thingSetupService.disableChannel({channelUID: thingUID + ':' + channelID}, function() {
			$scope.getThing(true);
			toastService.showDefaultToast('Channel disabled');
		});
	};
	
    $scope.getChannelById = function(channelId) {
        if (!$scope.thingType) {
            return;
        }
        return $.grep($scope.thingType.channels, function(channel, i) {
            return channelId == channel.id;
        })[0];
    };
    
    $scope.getChannels = function(advanced) {
        if (!$scope.thingType || !$scope.thing) {
            return;
        }
        return $.grep($scope.thing.channels, function(channel, i) {
           var channelType = $scope.getChannelById(channel.id);
           return channelType ? advanced == channelType.advanced : false;
        });
    };
	
    $scope.getThing = function(refresh) {
    	thingRepository.getOne(function(thing) {
    		return thing.UID === thingUID;
    	}, function(thing) {
    		$scope.thing = thing;
    		$scope.setTitle(thing.item ? thing.item.label : thing.UID);
    	}, refresh);	
	}
	$scope.getThing(false);
	
	thingTypeRepository.getOne(function(thingType) {
		return thingType.UID === thingTypeUID;
	}, function(thingType) {
		$scope.thingType = thingType;
		$scope.setHeaderText(thingType.description);
	});
}).controller('EditThingController', function($scope, $mdDialog, toastService, 
		thingTypeRepository, thingRepository, thingSetupService, homeGroupRepository) {
	
	$scope.setHeaderText('Click the \'Save\' button to apply the changes.');
	
	var thingUID = $scope.path[4];
	var thingTypeUID = getThingTypeUID(thingUID);
	
	$scope.thing;
	$scope.groups = [];
	$scope.thingType;
	
	$scope.openGroupSelectionDialog = function(groupNames, event) {
		$mdDialog.show({
			controller : 'SelectGroupsDialogController',
			templateUrl : 'partials/dialog.groupselection.html',
			targetEvent : event,
			locals: {
				groupNames: groupNames
			}
		}).then(function(selectedGroupNames) {
			$scope.thing.item.groupNames = selectedGroupNames;
			$scope.setGroupLabels();
		});
	};
	
	$scope.update = function(thing) {
		for (var i = 0; i < $scope.thingType.configParameters.length; i++) {
			var parameter = $scope.thingType.configParameters[i];
			if(thing.configuration[parameter.name]) {
				if(parameter.type === 'TEXT') {
					// no conversation
				} else if(parameter.type === 'BOOLEAN') {
					thing.configuration[parameter.name] = new Boolean(thing.configuration[parameter.name]);
				} else if(parameter.type === 'INTEGER' || parameter.type === 'DECIMAL') {
					thing.configuration[parameter.name] = parseInt(thing.configuration[parameter.name]);
				} else {
					// no conversation
				}
			}
		}
		thingSetupService.update(thing, function() {
			toastService.showDefaultToast('Thing updated');
			$scope.navigateTo('things/view/' + thing.UID);
		});
	};
	$scope.setGroupLabels = function() {
		homeGroupRepository.getAll(function(homeGroups) {
			var groupLabels = [];
			var item = $scope.thing.item;
			if (item && item.groupNames) {
				for (var i = 0; i < homeGroups.length; i++) {
					var homeGroup = homeGroups[i];
					if(item.groupNames.indexOf(homeGroup.name) >= 0) {
						groupLabels.push(homeGroup.label);
					}
				}
				$scope.groups = groupLabels.join(', ');
			} else {
				$scope.groups = '';
			}
		});
	};
	$scope.getThing = function(refresh) {
	    	thingRepository.getOne(function(thing) {
	    		return thing.UID === thingUID;
	    	}, function(thing) {
					var label = thing.item ? thing.item.label : thing.UID;
					$scope.thing = thing;
	    		$scope.setGroupLabels();
	    		$scope.setTitle('Edit ' + label);
	    	}, refresh);	
		};
	$scope.getThing(false);
	
	thingTypeRepository.getOne(function(thingType) {
		return thingType.UID === thingTypeUID;
	}, function(thingType) {
		$scope.thingType = thingType;
	});
});