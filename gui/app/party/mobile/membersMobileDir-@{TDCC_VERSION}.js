angular.module('main').directive('membersMobile', function(){
    return{
        restrict:'E',
        templateUrl:'party/mobile/membersMobile-@{TDCC_VERSION}.html'
    };
});