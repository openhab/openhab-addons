angular.module('SmartHomeManagerApp.controllers', []).controller('BodyController', function($rootScope, $scope, eventService, 
        toastService, discoveryResultRepository) {
	$scope.getSchemeClass = function() {
        var theme = localStorage.getItem('theme');
        if (theme) {
            return 'theme-' + theme;
        } else {
            return 'theme-openhab';
        }
    }
	$scope.scrollTop = 0;
	$(window).scroll(function() {
		$scope.$apply(function (scope) {
			$scope.scrollTop = $('body').scrollTop();
		});
	});
	$scope.isBigTitle = function() {
		return $scope.scrollTop < 80 && !$rootScope.simpleHeader;
    }
    $scope.isEshTheme = function() {
        return $scope.getSchemeClass() === 'theme-white';
    }
    $scope.setTitle = function(title) {
    	$rootScope.title = title;
	}
    $scope.subtitles = [];
    $scope.setSubtitle = function(args) {
    	$scope.subtitles = [];
    	$.each(args, function(i, subtitle) {
			$scope.subtitles.push(subtitle);
		})
	}
    $scope.setHeaderText = function(headerText) {
    	$scope.headerText = headerText;
	}
    $rootScope.$on('$routeChangeStart', function(){
    	$scope.subtitles = [];
    	$scope.headerText = null;
    });
    $scope.generateUUID = function() {
	    var d = new Date().getTime();
	    var uuid = 'xxxxxxxx'.replace(/[x]/g, function(c) {
	        var r = (d + Math.random()*16)%16 | 0;
	        d = Math.floor(d/16);
	        return (c=='x' ? r : (r&0x3|0x8)).toString(16);
	    });
	    return uuid;
	};

    var numberOfInboxEntries = -1;
    eventService.onEvent('smarthome/inbox/added/*', function(topic, discoveryResult) {
    	toastService.showDefaultToast('New Inbox Entry: ' + discoveryResult.label, 'Show Inbox', 'setup');
	});
    eventService.onEvent('smarthome/update/*', function(topic, state) {
    	var itemName = topic.split('/')[2];
    	
    	var changeStateRecursively = function(item) {
    		if(item.name === itemName) {
    			$scope.$apply(function (scope) {
    				item.state = state;
    			});
				console.log('Changed state of ' + itemName + ' to ' +state)
    		}
			if(item.members) {
				$.each(item.members, function(i, memberItem) {
					changeStateRecursively(memberItem);
				});
			}
    	}
    	
    	if($rootScope.data.items) {
	    	$.each($rootScope.data.items, function(i, item) {
				changeStateRecursively(item);
			});
    	}
	});
    
    $scope.getNumberOfNewDiscoveryResults = function() {
		var numberOfNewDiscoveryResults = 0;
		if(!$scope.data.discoveryResults) {
			return numberOfNewDiscoveryResults;
		}
    	for (var i = 0; i < $scope.data.discoveryResults.length; i++) {
			var discoveryResult = $scope.data.discoveryResults[i];
			if(discoveryResult.flag === 'NEW') {
				numberOfNewDiscoveryResults++;
			}
		}
    	return numberOfNewDiscoveryResults;
	}
    
    discoveryResultRepository.getAll();
}).controller('PreferencesPageController', function($scope) {
	$scope.setHeaderText('Edit user preferences.');
	
	var localStorage = window.localStorage;
    var language = localStorage.getItem('language');
    var theme = localStorage.getItem('theme');

    $scope.language = language ? language : 'english';
    $scope.theme = theme ? theme : 'openhab';

    $scope.save = function(language, theme) {
        localStorage.setItem('language', language);
        localStorage.setItem('theme', theme);
        $scope.showSuccessToast('Preferences saved successfully. Please reload the page.');
    }

    $scope.getSelected = function(property) {
        return $('select#' + property + ' option:selected').val();
    }
}).controller('NavController', function($scope, $location) {
    $scope.opened = null;
    $scope.open = function(viewLocation) {
    	$scope.opened = viewLocation;
    }
    $scope.isActive = function(viewLocation) {
        var active = (viewLocation === $location.path().split('/')[1]);
        return active || $scope.opened === viewLocation;
    }
    $scope.isSubActive = function(viewLocation) {
        var active = (viewLocation === $location.path().split('/')[2]);
        return active;
    }
    $scope.$on('$routeChangeSuccess', function() {
        $('body').removeClass('sml-open');
        $('.mask').remove();
        $scope.opened = null;
    });
}).controller('SelectGroupsDialogController', function($scope, $mdDialog, groupNames, homeGroupRepository) {
	$scope.homeGroups = [];
	$scope.groupNames = [];
	homeGroupRepository.getAll(function(homeGroups) {
		$.each(homeGroups, function(i, homeGroup) {
			if(groupNames.indexOf(homeGroup.name) >= 0) {
				$scope.groupNames[homeGroup.name] = true;
			} else {
				$scope.groupNames[homeGroup.name] = false;
			}
		});
		$scope.homeGroups = homeGroups;
	});
	$scope.close = function() {
		$mdDialog.cancel();
	}
	$scope.ok = function(groupNames) {
		var selectedGroupNames = [];
		for (var gropuName in groupNames) {
			if(groupNames[gropuName]) {
				selectedGroupNames.push(gropuName);
			}
		}
		$mdDialog.hide(selectedGroupNames);
	}
});