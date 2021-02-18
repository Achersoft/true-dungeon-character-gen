angular.module('main').directive('playItemsMobile', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/itemsMobile-@{TDCC_VERSION}.html'
    };
});