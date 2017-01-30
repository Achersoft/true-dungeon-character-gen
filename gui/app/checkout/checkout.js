'use strict';

angular.module('main.checkout', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/checkout', {
        templateUrl: 'checkout/checkout.html',
        controller: 'CheckoutCtrl'
    });
}])

.controller('CheckoutCtrl', ['$scope', '$location', '$route', 'CheckoutSvc', 'ngCart', function ($scope, $location, $route, checkoutSvc, ngCart) {
    $scope.cartSize = ngCart.$cart.items.length;
    $scope.customerName = null;
        
    $scope.checkout = function(){
        var index;
        var order = [];
        for (index = 0; index < ngCart.$cart.items.length; ++index) {
            order.push({'id':ngCart.$cart.items[index]._data.id,'condition':ngCart.$cart.items[index]._data.condition,'qty':ngCart.$cart.items[index]._quantity});
        }
        
        checkoutSvc.createOrder({"customerName":$scope.customerName,"items":order}).success(function(data) {
            ngCart.empty();
            $location.path("/setSelection/English");
            $route.reload();
        });
    };
}])

.factory('CheckoutSvc',['$http', 'RESOURCES', function($http, RESOURCES){    
    var checkoutSvc={};
    
    checkoutSvc.createOrder = function(order){
        return $http.put(RESOURCES.REST_BASE_URL + '/orders/create/', order);
    };

    return checkoutSvc;
}]);