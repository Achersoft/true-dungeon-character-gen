angular.module('main').directive('defendMobile', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/defendMobile-@{TDCC_VERSION}.html'
    };
});