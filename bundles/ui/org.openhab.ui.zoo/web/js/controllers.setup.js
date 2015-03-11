angular.module('SmartHomeManagerApp.controllers.setup', 
[]).controller('SetupPageController', function($scope, $location) {
    $scope.navigateTo = function(path) {
        $location.path('setup/' + path);
    }
}).controller('InboxController', function($scope, $timeout, $mdDialog, inboxService, discoveryResultRepository, toastService) {
	$scope.setSubtitle(['Search']);
    $scope.setHeaderText('Shows a list of found things in your home.')
	$scope.approve = function(thingUID, event) {
    	$mdDialog.show({
			controller : 'ApproveInboxEntryDialogController',
			templateUrl : 'partials/dialog.approveinboxentry.html',
			targetEvent : event,
			locals: {discoveryResult: discoveryResultRepository.find(function(discoveryResult) {
				return discoveryResult.thingUID === thingUID;
			})}
		}).then(function(label) {
			inboxService.approve({
	            'thingUID' : thingUID
	        }, label, function() {
	            $scope.getAll();
	            toastService.showDefaultToast('Thing added.', 'Show Thing', 'configuration/things/view/' + thingUID);
	        });
		});
    };
    $scope.ignore = function(thingUID) {
        inboxService.ignore({
            'thingUID' : thingUID
        }, function() {
            $scope.getAll();
        });
    };
    $scope.remove = function(thingUID, event) {
    	var discoveryResult = discoveryResultRepository.find(function(discoveryResult) {
			return discoveryResult.thingUID === thingUID;
		});
    	var confirm = $mdDialog.confirm()
	      .title('Remove ' + discoveryResult.label)
	      .content('Would you like to remove the discovery result from the inbox?')
	      .ariaLabel('Remove Discovery Result')
	      .ok('Remove')
	      .cancel('Cancel')
	      .targetEvent(event);
	    $mdDialog.show(confirm).then(function() {
	    	inboxService.remove({
	            'thingUID' : thingUID
	        }, function() {
	            $scope.getAll();
	            toastService.showSuccessToast('Inbox entry removed');
	        });
	    });
    };
    
    $scope.showScanDialog = function(event) {
		$mdDialog.show({
			controller : 'ScanDialogController',
			templateUrl : 'partials/dialog.scan.html',
			targetEvent : event,
		});
    }
    
    $scope.getAll = function() {
    	discoveryResultRepository.getAll();
    };
    $scope.getAll();
}).controller('ScanDialogController', function($scope, $rootScope, $timeout, $mdDialog, discoveryService, bindingRepository) {
    $scope.supportedBindings = [];
    $scope.activeScans = [];
    
    $scope.scan = function(bindingId) {
        $scope.activeScans.push(bindingId);
    	discoveryService.scan({
            'bindingId' : bindingId
        }, function() {

        });
    	setTimeout(function() {
    		$scope.activeScans.splice($scope.activeScans.indexOf(bindingId), 1)
        }, 3000);
    };
    
    bindingRepository.getAll();
    
    $scope.getBindingById = function(bindingId) {
    	for (var i = 0; i < $rootScope.data.bindings.length; i++) {
            var binding = $rootScope.data.bindings[i];
            if(binding.id === bindingId) {
            	return binding;
            }
    	}
    	return {};
    }
    
    discoveryService.getAll(function(response) {
        $scope.supportedBindings = response;
    });
    
    $scope.close = function() {
		$mdDialog.hide();
	}
}).controller('ApproveInboxEntryDialogController', function($scope, $mdDialog, discoveryResult) {
	$scope.discoveryResult = discoveryResult;
	$scope.label = discoveryResult.label;
	$scope.close = function() {
		$mdDialog.cancel();
	}
	$scope.approve = function(label) {
		$mdDialog.hide(label);
	}
}).controller('ManualSetupChooseController', function($scope, bindingRepository, thingTypeRepository, thingSetupService) {
	$scope.setSubtitle(['Manual Setup']);
	$scope.setHeaderText('Choose a thing, which should be aded manually to your Smart Home.')
	
	$scope.currentBindingId = null;
	$scope.setCurrentBindingId = function(bindingId) {
		$scope.currentBindingId = bindingId;
	};
	
    bindingRepository.getAll(function(data) {
	});
   
}).controller('ManualSetupConfigureController', function($scope, $routeParams, $mdDialog, toastService, 
		bindingRepository, thingTypeRepository, thingSetupService, homeGroupRepository) {
	
	var thingTypeUID = $routeParams.thingTypeUID;
	
	function generateUUID() {
	    var d = new Date().getTime();
	    var uuid = 'xxxxxxxx'.replace(/[x]/g, function(c) {
	        var r = (d + Math.random()*16)%16 | 0;
	        d = Math.floor(d/16);
	        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
	    });
	    return uuid;
	};
	
	$scope.thingType = null;
	$scope.thing = {
		UID: null,
		configuration : {},
		item: {
			label: null,
			groupNames: []
		}
	}
	
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
	$scope.setGroupLabels = function() {
		homeGroupRepository.getAll(function(homeGroups) {
			var groupLabels = [];
			for (var i = 0; i < homeGroups.length; i++) {
				var homeGroup = homeGroups[i];
				if($scope.thing.item.groupNames.indexOf(homeGroup.name) >= 0) {
					groupLabels.push(homeGroup.label);
				}
			}
			$scope.groups = groupLabels.join(', ');
		});
	}
	$scope.setGroupLabels();
	
	$scope.addThing = function(thing) {
		thingSetupService.add(thing, function() {
			toastService.showDefaultToast('Thing added');
			$scope.navigateFromRoot('configuration/things/view/' + thing.UID);
		});
	};
	
	thingTypeRepository.getOne(function(thingType) {
    	return thingType.UID === thingTypeUID;
    },function(thingType) {
    	$scope.setTitle('Configure ' + thingType.label);
    	$scope.setHeaderText(thingType.description);
		$scope.thingType = thingType;
		$scope.thing.UID = thingType.UID + ':' + generateUUID();
		$.each($scope.thingType.configParameters, function(i, parameter) {
			if(parameter.defaultValue !== 'null') {
				if(parameter.type === 'TEXT') {
					$scope.thing.configuration[parameter.name] = parameter.defaultValue
				} else if(parameter.type === 'BOOLEAN') {
					$scope.thing.configuration[parameter.name] = new Boolean(parameter.defaultValue);
				} else if(parameter.type === 'INTEGER' || parameter.type === 'DECIMAL') {
					$scope.thing.configuration[parameter.name] = parseInt(parameter.defaultValue);
				} else {
					$scope.thing.configuration[parameter.name] = parameter.defaultValue;
				}
			} else {
				$scope.thing.configuration[parameter.name] = '';
			}
		});
    });
});