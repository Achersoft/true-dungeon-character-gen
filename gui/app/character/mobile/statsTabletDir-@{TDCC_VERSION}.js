angular.module('main').directive('statsTablet', function(){
    return{
        restrict:'E',
        templateUrl:'character/mobile/statsTablet-@{TDCC_VERSION}.html'
    };
});