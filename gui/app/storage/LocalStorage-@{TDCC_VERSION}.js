angular.module('main')

.factory('LocalStorage',['store',function(store) {
        return store.getNamespacedStore('tdcc');
    }
])
.factory('LocalCacheMgr',['LocalStorage', function(localStorage) {
    var LocalCacheMgr={};

    LocalCacheMgr.get = function(name) {
        return localStorage.get(name);
    };

    LocalCacheMgr.put = function(name, dataObj) {
        localStorage.set(name, dataObj);
    };

    LocalCacheMgr.remove = function(name) {
        localStorage.remove(name);
    };

    return LocalCacheMgr;
}]);


