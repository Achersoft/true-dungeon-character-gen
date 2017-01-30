'use strict';

angular.module('main.orders', ['ngRoute'])

.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    .when('/orders', {
        templateUrl: 'orders/orders.html',
        controller: 'OrderCtrl'
    })
    .when('/completedorders', {
        templateUrl: 'orders/completedOrders.html',
        controller: 'OrderCompleteCtrl'
    })
    .when('/orders/:id', {
        templateUrl: 'orders/orderDetails.html',
        controller: 'OrderDetailCtrl'
    })
    .when('/completedOrders/:id', {
        templateUrl: 'orders/completedOrderDetails.html',
        controller: 'OrderDetailCtrl'
    });
}])

.controller('OrderCtrl', ['$scope', '$routeParams', 'OrderSvc', 'RESOURCES', 'NgTableParams', function ($scope, $routeParams, orderSvc, RESOURCES, NgTableParams) {
    $scope.tableParams = new NgTableParams({
        page: 1,         
        count: 20     
    },
    {   total: 0, 
        counts: [], 
        getData: function ($defer, params) {
            orderSvc.getOrders(params.page(), params.count()).success(function (result) {
                params.total(result.count);
                $defer.resolve(result.orders);
            });
        }
    });
}])

.controller('OrderCompleteCtrl', ['$scope', '$routeParams', 'OrderSvc', 'RESOURCES', 'NgTableParams', function ($scope, $routeParams, orderSvc, RESOURCES, NgTableParams) {
    $scope.tableParams = new NgTableParams({
        page: 1,         
        count: 20     
    },
    {   total: 0, 
        counts: [], 
        getData: function ($defer, params) {
            orderSvc.getCompletedOrders(params.page(), params.count()).success(function (result) {
                params.total(result.count);
                $defer.resolve(result.orders);
            });
        }
    });
}])

.controller('OrderDetailCtrl', ['$scope', '$routeParams', 'OrderSvc', '$location', '$route', 'NgTableParams', function ($scope, $routeParams, orderSvc, $location, $route, NgTableParams) {
    $scope.order = {"items":[]};
    
    $scope.tableParams = new NgTableParams({
        page: 1,         
        count: 20     
    },
    {   total: 0, 
        counts: [], 
        getData: function ($defer, params) {
            orderSvc.getOrder($routeParams.id).success(function (result) {
                $scope.order = result;
                params.total(result.items.length);
                $defer.resolve(result.items);
            });
        }
    });
    
    $scope.removeItem = function(index){
        $scope.order.items.splice(index, 1);
        console.log(index);
    };
    
    $scope.changeQty = function(card, decrease){
        if(card.orgQty === undefined)
            card.orgQty = card.qty;
        if(decrease && card.qty > 1)
            card.qty = card.qty - 1;
        else if(!decrease && card.qty < card.orgQty)
            card.qty = card.qty + 1;
    };
    
    $scope.getTotal = function(){
        var total = 0;
        for(var i = 0; i < $scope.order.items.length; i++){
            var product = $scope.order.items[i];
            total += (product.price * product.qty);
        }
        return (($scope.order.discount)?total - (($scope.order.discount/100)*total):total).toFixed(2);
    };
    
    $scope.cancel = function(){
        orderSvc.cancelOrder($scope.order).success(function (result) {
            $location.path("/orders");
            $route.reload();
        }); 
    };
    
    $scope.fulfill = function(){
        orderSvc.fulfillOrder($scope.order).success(function (result) {
            $location.path("/orders");
            $route.reload();
        }); 
    };
}])

.factory('OrderSvc',['$http', 'RESOURCES', function($http, RESOURCES){    
    var orderSvc={};

    orderSvc.getOrders = function(page, size){
        return $http.get(RESOURCES.REST_BASE_URL + '/orders?page=' + page + '&size=' + size);
    };
    
    orderSvc.getCompletedOrders = function(page, size){
        return $http.get(RESOURCES.REST_BASE_URL + '/orders/completed?page=' + page + '&size=' + size);
    };
    
    orderSvc.getOrder = function(id){
        return $http.get(RESOURCES.REST_BASE_URL + '/orders/' + id);
    };
    
    orderSvc.cancelOrder = function(order){
        return $http.post(RESOURCES.REST_BASE_URL + '/orders/cancel', order);
    };
    
    orderSvc.fulfillOrder = function(order){
        return $http.post(RESOURCES.REST_BASE_URL + '/orders/fulfill', order);
    };

    return orderSvc;
}])

.filter('yesNo', function() {
    return function(input) {
        return input ? 'Yes' : 'No';
    }
});