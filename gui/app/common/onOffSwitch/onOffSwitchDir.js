angular.module('main').directive('onOffSwitch',function(){
    return{
        restrict:'E',
        scope:{
            switchId:'@',
            switchName:'@',
            switchInline: '=',
            switchDisabled: '@',
            ngModel: '='
        },
        templateUrl:'common/onOffSwitch/onOffSwitchTemplate.html'
    };
});