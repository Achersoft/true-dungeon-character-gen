angular.module('main').directive('resultDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/resultDesktop-@{TDCC_VERSION}.html',
        link: function(scope) {
            scope.activeIndex = 0;

            scope.setActiveIndex = function(index) {
                scope.activeIndex = index;
            };
        }
    };
});