angular.module('main').directive('partyMobile', function(){
    return{
        restrict:'E',
        templateUrl:'party/mobile/partyMobile-@{TDCC_VERSION}.html'
    };
});