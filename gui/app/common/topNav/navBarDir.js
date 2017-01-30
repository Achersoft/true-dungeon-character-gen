angular.module('main').directive('navBar',[ 'SearchSvc', 'SearchState', '$location', '$route', function(searchSvc, searchState, $location, $route){
    return{
        restrict:'E',
        templateUrl:'common/topNav/navBar.html',
        bindToController: true,
        controllerAs: 'topNavCtrl',
        controller: function () {
            this.selectedCard = '';
            
            this.onSelect = function($item){
                this.selectedCard = '';
                searchState.setCardName($item.name);
                $location.path("/search/results");
                $route.reload();
            };
            
            this.searchForCard = function(viewValue) {
                searchState.setLikeName(viewValue);
                return searchSvc.search().then(function(response) {
                    return response.data.cards;
                });
            };
            
            this.oneDollarBinder = function(){
                var state = searchState.reset();
                state.priceMin = 0.99;
                state.priceMax = 1.99;   
                state.inStock = true;
                $location.path("/search/results");
                $route.reload();
            }
            
            this.twoDollarBinder = function(){
                var state = searchState.reset();
                state.priceMin = 1.99;
                state.priceMax = 2.99; 
                state.inStock = true;
                $location.path("/search/results");
                $route.reload();
            };
        }
    };
}]);