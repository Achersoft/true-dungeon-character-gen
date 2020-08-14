angular.module('main').directive('attackMobile', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/attackMobile-@{TDCC_VERSION}.html'
    };
});