angular.module('main').directive('characterMobile', function(){
    return{
        restrict:'E',
        templateUrl:'character/mobile/characterMobile-@{TDCC_VERSION}.html'
    };
});