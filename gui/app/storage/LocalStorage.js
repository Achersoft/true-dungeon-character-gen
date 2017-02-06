angular.module('main')

.factory('LocalStorage',['store',function(store) {
        return store.getNamespacedStore('mtginv');
    }
])
.factory('LocalCacheMgr',['LocalStorage', 'RESOURCES', function(localStorage, RESOURCES) {
    var LocalCacheMgr={};

    LocalCacheMgr.get = function(name) {
        var entry = localStorage.get(name);
        var dataObj = null;
        if ( entry !== null ) {
            if ( entry.expiryTime >= Date.now() ) {
                dataObj =  entry.data;
            } else {
                localStorage.remove(name);
            }
        }
        return dataObj;
    };

    LocalCacheMgr.put = function(name, dataObj, maxAgeInMillis) {
        localStorage.set(name, {'data' : dataObj,
                                'expiryTime' : Date.now() + ((typeof maxAgeInMillis === 'undefined') ?
                                                                RESOURCES.LCL_CACHE_DFLT_MAX_TIME_TO_LIVE :
                                                                maxAgeInMillis)});
    };

    LocalCacheMgr.remove = function(name) {
        localStorage.remove(name);
    };

    return LocalCacheMgr;
}]);


