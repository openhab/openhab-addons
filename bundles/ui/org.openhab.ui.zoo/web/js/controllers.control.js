angular.module('SmartHomeManagerApp.controllers.control', []).controller('ControlPageController', function($scope, $routeParams, $location, $timeout, itemRepository) {
    $scope.items = [];
    $scope.selectedTabIndex = 0;
    $scope.tabs = [];

    $scope.next = function() {
    	var newIndex = $scope.selectedTabIndex + 1;
    	if(newIndex > ($scope.tabs.length - 1)) {
    		newIndex = 0;
    	}
    	$scope.selectedTabIndex = newIndex;
	}
    $scope.prev = function() {
    	var newIndex = $scope.selectedTabIndex - 1;
    	if(newIndex < 0) {
    		newIndex = $scope.tabs.length - 1;
    	}
    	$scope.selectedTabIndex = newIndex;
	}

    itemRepository.getAll(function(items) {
        $scope.items['All'] = items;
        for (var int = 0; int < items.length; int++) {
            var item = items[int];
            if (item.type === 'GroupItem') {
                if(item.tags.indexOf("home-group") > -1) {
                    $scope.tabs.push({name:item.name, label: item.label});
                }
            }
        }
        
        $scope.masonry = function() {
            if ($scope.data.items) {
                $timeout(function() {
                    new Masonry('.items', {});
                }, 100, false);
            }
		}
	    $scope.$watch('data.items', function(value) {
	    	$scope.masonry();
	    });
        $scope.$watch('selectedTabIndex', function() {
        	$scope.masonry();
		});
        $scope.masonry();
        $scope.getItem = function(itemName) {
        	for (var int = 0; int < $scope.data.items.length; int++) {
                var item = $scope.data.items[int];
                if (item.name === itemName) {
                    return item;
                }
            }
        	return null;
		}
    });

}).controller('ControlController', function($scope, $timeout, itemService) {
	$scope.getItemName = function(itemName) {
        return itemName.replace(/_/g, ' ');
    }
	
	$scope.getStateText = function(item) {
		if(item.state === 'Uninitialized') {
			return item.state;
		}
		var state = item.type === 'NumberItem' ? parseInt(item.state) : item.state;
		
		if(!item.stateDescription || !item.stateDescription.pattern) {
			return state;
		} else {
			return sprintf(item.stateDescription.pattern, state);
		}
    }
	
	$scope.getMinText = function(item) {
		if(!item.stateDescription || !item.stateDescription.minimum) {
			return '';
		} else if (!item.stateDescription.pattern) {
			return item.stateDescription.minimum;
		} else {
			return sprintf(item.stateDescription.pattern, item.stateDescription.minimum);
		}
    }
	
	$scope.getMaxText = function(item) {
		if(!item.stateDescription || !item.stateDescription.maximum) {
			return '';
		} else if (!item.stateDescription.pattern) {
			return item.stateDescription.maximum;
		} else {
			return sprintf(item.stateDescription.pattern, item.stateDescription.maximum);
		}
    }

    var categories = {
		'Alarm' : {},
		'Battery' : {},
		'Blinds' : {},
		'ColorLight' : {
			label: 'Color',
			icon: 'md-icon-wb-incandescent'
		},
		'Contact' : {},
		'DimmableLight' : {
			label: 'Brightness',
			icon: 'md-icon-wb-incandescent'
		},
		'CarbonDioxide' : {
			label: 'CO2'
		},
		'Door' : {},
		'Energy' : {},
		'Fan' : {},
		'Fire' : {},
		'Flow' : {},
		'GarageDoor' : {},
		'Gas' : {},
		'Humidity' : {},
		'Light' : {},
		'Motion' : {},
		'MoveControl' : {},
		'Player' : {},
		'PowerOutlet' : {},
		'Pressure' : {
			// icon: 'home-icon-measure_pressure_bar'
		},
		'Rain' : {},
		'Recorder' : {},
		'Smoke' : {},
		'SoundVolume' : {
			label: 'Volume',
			icon: 'md-icon-volume-up',
			hideSwitch: true
		},
		'Switch' : {},
		'Temperature' : {},
		'Water' : {},
		'Wind' : {},
		'Window' : {},
		'Zoom' : {},
    }
    
    $scope.getLabel = function (itemCategory, label, defaultLabel) {
    	if(label) {
    		return label;
    	}
    	
    	if(itemCategory) {
    		var category = categories[itemCategory];
    		if(category) {
    			return category.label ? category.label : itemCategory;
    		} else {
    			return defaultLabel;
    		}
    	} else {
			return defaultLabel;
		}
    }
    $scope.getIcon = function (itemCategory, fallbackIcon) {
    	var defaultIcon = fallbackIcon ? fallbackIcon : 'md-icon-radio-button-off';
    	if(itemCategory) {
    		var category = categories[itemCategory];
    		if(category) {
    			return category.icon ? category.icon : defaultIcon;
    		} else {
    			return defaultIcon;
			}
    	} else {
			return defaultIcon;
		}
    }
    $scope.isHideSwitch = function(itemCategory) {
    	if(itemCategory) {
    		var category = categories[itemCategory];
    		if(category) {
    			return category.hideSwitch;
    		}
    	}
    	return false;
	}
    $scope.isReadOnly = function (item) {
    	return item.stateDescription ? item.stateDescription.readOnly : false;
    }
    
    $scope.sendCommand = function(state) {
        itemService.sendCommand({
            itemName : $scope.item.name
        }, state);
    }
}).controller('ItemController', function($scope, itemService) {
    $scope.sendCommand = function(command) {
        itemService.sendCommand({
            itemName : $scope.item.name
        }, command);
    };
    $scope.sendState = function(state) {
        $scope.sendCommand(state);
        $scope.item.state = state;
    }
}).controller('DefaultItemController', function($scope, itemService) {

}).controller('SwitchItemController', function($scope, $timeout, itemService) {
    $scope.setOn = function(state) {
        itemService.sendCommand({
            itemName : $scope.item.name
        }, state);
    }
}).controller('DimmerItemController', function($scope, $timeout, itemService) {

	$scope.on = parseInt($scope.item.state) > 0 ? 'ON' : 'OFF';
    
	$scope.setOn = function(on) {
		$scope.on = on === 'ON' ? 'ON' : 'OFF';
		
        itemService.sendCommand({
            itemName : $scope.item.name
        }, on);
        
        var brightness = parseInt($scope.item.state);
        if(on === 'ON' && brightness === 0) {
        	$scope.item.state = 100;
        }
        if(on === 'OFF' && brightness > 0) {
        	$scope.item.state = 0;
        }
    }
	$scope.setBrightness = function(brightness) {
        var brightnessValue = brightness === 0 ? '0' : brightness;
        itemService.sendCommand({
            itemName : $scope.item.name
        }, brightnessValue);
    }
	$scope.$watch('item.state', function() {
		var brightness = parseInt($scope.item.state);
		if(brightness > 0 && $scope.on === 'OFF') {
        	$scope.on = 'ON';
        }
        if(brightness === 0 && $scope.on === 'ON') {
        	$scope.on = 'OFF';
        }
	});
}).controller('ColorItemController', function($scope, $timeout, $element, itemService) {

	function getStateAsObject(state) {
		var stateParts = state.split(",");
		if(stateParts.length == 3) {
			return {
				h: parseInt(stateParts[0]),
				s: parseInt(stateParts[1]),
				b: parseInt(stateParts[2])
			}
		} else {
			return {
				h: 0,
				s: 0,
				b: 0
			}
		}
	}
	
	function toState(stateObject) {
		return Math.ceil(stateObject.h) + ',' + Math.ceil(stateObject.s) + ',' + Math.ceil(stateObject.b);
	}
	
	$scope.setOn = function(on) {
		
        itemService.sendCommand({
            itemName : $scope.item.name
        }, on);
        
        if(on === 'ON' && $scope.brightness === 0) {
        	// set state to ON
        	var stateObject = getStateAsObject($scope.item.state);
        	stateObject.b = 100;
        	$scope.item.state = toState(stateObject);
        }
        if(on === 'OFF' && $scope.brightness > 0) {
        	// set state to OFF
        	var stateObject = getStateAsObject($scope.item.state);
        	stateObject.b = 0;
        	$scope.item.state = toState(stateObject);
        }
    }
	
    $scope.setBrightness = function(brightness) {
        var brightnessValue = brightness === 0 ? '0' : brightness;
        
        itemService.sendCommand({
            itemName : $scope.item.name
        }, brightnessValue);
        
        var stateObject = getStateAsObject($scope.item.state);
    	stateObject.b = brightnessValue;
    	$scope.item.state = toState(stateObject);
    }
    
    $scope.setHue = function(hue) {
        var hueValue = hue === 0 ? '0' : hue;
        
        var stateObject = getStateAsObject($scope.item.state);
    	stateObject.h = hueValue;
    	
        if(!stateObject.s) {
        	stateObject.s = 100;
        }
        // if light is OFF, change it to ON
        if(!stateObject.b) {
        	stateObject.b = 100;
        }
         
        itemService.sendCommand({
            itemName : $scope.item.name
        }, $scope.item.state);
        
        $scope.item.state = toState(stateObject);
    }

    $scope.getHexColor = function() {
    	var stateObject = getStateAsObject($scope.item.state);
        var hsv = tinycolor({
            h : stateObject.h,
            s : 1,
            v : 1
        }).toHsv();
        return tinycolor(hsv).toHexString();
    }
    
    var setStates = function() {
    	var stateObject = getStateAsObject($scope.item.state);
    	var hue = stateObject.h;
        var brightness = stateObject.b;
        
        $scope.hue = hue ? hue : 0;
        $scope.brightness = brightness ? brightness : 0;
        $scope.on = $scope.brightness > 0 ? 'ON' : 'OFF';
        
        var hexColor = $scope.getHexColor();
        $($element).find('.hue .md-thumb').css('background-color', hexColor);
	}
    
    setStates();
     
    $scope.$watch('item.state', function() {
    	setStates(); 
	});
    
    var hexColor =  $scope.getHexColor();
    $($element).find('.hue .md-thumb').css('background-color', hexColor);
}).controller('NumberItemController', function($scope) {
	$scope.shouldRenderSlider = function(item) {
		if(item.stateDescription) {
			var stateDescription = item.stateDescription;
			if(stateDescription.readOnly) {
				return false;
			} else {
				if(stateDescription.minimum && stateDescription.maximum) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
}).controller('RollershutterItemController', function($scope) {
	
}).controller('PlayerItemController', function($scope) {

});
