angular.module('main').directive('itemsMobile', function(){
    return{
        restrict:'E',
        templateUrl:'character/mobile/itemsMobile-@{TDCC_VERSION}.html'
    };
});