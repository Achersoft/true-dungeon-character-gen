angular.module('main')
        
.config(['$httpProvider',
    function($httpProvider) {
        $httpProvider.interceptors.push(['$q', '$injector', 'AuthorizationState', '$rootScope',
                                         function($q, $injector, AuthorizationState, $rootScope) {
            AuthorizationState.addAuthorization($httpProvider.defaults.headers.common);
            return {
                // Intercept all requests and add saved Token Auth header
                request: function(config) {
                    // Set the `Authorization` header for every outgoing REST HTTP request
                    // (don't set on static UI requests)
                    if($rootScope.loginRequired === true) {
                        if ( (config.url.substring( config.url.length - '.html'.length, config.url.length ) !== '.html') &&
                             (config.url.substring( config.url.length - '.js'.length, config.url.length ) !== '.js') &&
                             (config.url.substring( config.url.length - '.css'.length, config.url.length ) !== '.css') ) {
                            // We add Auth. hdr to global $http config, but after a refresh, it may be missing
                            // until after 1st http response (after refresh) is intercepted and latest token is
                            // added back into global config
                            if ( config.headers.Authorization === undefined ) {
                                 AuthorizationState.addAuthorization(config.headers);
                             }
                        }
                    }
                    return config;
                },

                response: function(response) {
                    AuthorizationState.updateAuthorization(response.headers, $httpProvider.defaults.headers.common);
                    return response;
                }  //,

                // This is the responseError interceptor
//                responseError: function(rejection) {
                    // (Note we need to use $injector here to avoid circular dependency on $state - uugghh!)
//                    var $state = $injector.get('$state');
                    
                    // Expired/absent/etc. auth token - requires user to log back in...
//                    if (rejection.status === 401 && !$state.is('login')) {
//                        $state.go('login');
//                    }
                    /* If not a 401, do nothing (here) with this error.
                     * This is necessary to make a `responseError` interceptor a no-op. */
//                    return($q.reject(rejection));
//                }
            };
        }]);
    }
])
.factory('AuthorizationState',['LocalCacheMgr', 'jwtHelper', 
    function(localCacheMgr, jwtHelper) {
        var ID_AUTH_INFO     = 'authInfo';
        var ID_AUTH_HEADER   = 'Authorization';
        var ID_AUTH_TOKEN_ID = 'Token';
        var ID_LOGIN_REQ_ID  = 'LoginRequired';

        var AuthorizationState={};
        
        var extractToken = function(headers) {
            var token   = null;
            var authHdr = headers(ID_AUTH_HEADER);
            if ( authHdr !== null ) {
                var res = authHdr.split(' ');
                if ( res.length === 2  && res[0] === ID_AUTH_TOKEN_ID) {
                    token = res[1];
                }
            }
            return token;
        };
        
        var putAuthInfo = function(token, claims, expiryTime) {
            localCacheMgr.put(ID_AUTH_INFO, {'token' : token, 'claims' : claims}, expiryTime);
        };
        
        var getAuthInfo = function() {
            return localCacheMgr.get(ID_AUTH_INFO);
        };
 
        AuthorizationState.getLoggedInUser = function() {
            var entry = getAuthInfo();
            return(entry ? entry.claims.name : '');
        };
        
        AuthorizationState.getLoggedInUserId = function() {
            var entry = getAuthInfo();
            return(entry ? entry.claims.sub : '');
        };

        AuthorizationState.getAuthToken = function() {
            var entry = getAuthInfo();
            return(entry ? entry.token : null);
        };
        
        AuthorizationState.getUserPrivileges = function() {
            var entry = getAuthInfo();
            return(entry ? entry.claims.privileges : null);
        };
        
        AuthorizationState.addAuthorization = function(headers) {
            var token = AuthorizationState.getAuthToken();
            if ( token !== null ) {
                headers.Authorization = ID_AUTH_TOKEN_ID + ' ' + token;
            }
        };

        AuthorizationState.updateAuthorization = function(responseHeaders, httpGlobalHeaders) {
            // Updated (if necessary) token is passed back on each response - e.g., srvr will continue update expiryTime
            var token = extractToken(responseHeaders);
            if ( token !== null ) {
                var claims = jwtHelper.decodeToken(token);
                //console.log("Token contents = " + JSON.stringify(claims) +
                //            " expiry time = " + jwtHelper.getTokenExpirationDate(token));
               
                //Add/update token in cache - make it expire from cache slightly after (1 min) token expiry time...
                // Note per JWT spec, expiryTime claim ('exp') is in Seconds (not millis) since Epoch Time.
                // Should always be present, but we'll failsafe to 30min if not...
                putAuthInfo(token, claims, (claims.exp ?  ((claims.exp * 1000) - Date.now()) + 60000 : 1800000));
                
                // Add to default HTTP headers - so Auth token will be added to all requests...
                this.addAuthorization(httpGlobalHeaders);
            }
        };
        
        AuthorizationState.removeAuthorization = function() {
            localCacheMgr.remove(ID_AUTH_INFO);
        };
        
        AuthorizationState.isAuthorized = function() {
            return(AuthorizationState.getAuthToken() !== null);
        };

        return AuthorizationState;
    }
])
.factory('AuthorizationSvc',['$http', '$q', 'authService', 'RESOURCES', 'AuthorizationState',
    function($http, $q, httpAuthService, RESOURCES, AuthorizationState){
        var AuthorizationSvc={};
        
        AuthorizationSvc.authenticate = function(username, password) {
            // Set ignoreAuthModule = true so if we get a 401 status, we don't get in endless loop...
            return $http.post(RESOURCES.LOGIN_URL, {'userName' : username, 'password' : password}, {'ignoreAuthModule' : true})
                        .then(function(response) {
                                // Extract auth token from response, save off and also add to default http req headers
                                AuthorizationState.updateAuthorization(response.headers, $http.defaults.headers.common);
                                // Let AuthService know that we have successfully logged in...
                                httpAuthService.loginConfirmed(username, function(config) {
                                    // Add new/valid token to requests we will replay (now that we are authorized).
                                    // These are request(s) that generated 401 responses and "forced" us to log in
                                    AuthorizationState.addAuthorization(config.headers);
                                    return(config);
                                });
                                return response;
                            },
                            function(response) {
                                // Erase the token if the user fails to log in
                                //console.log("Login failed - " + response.statusText);
                                AuthorizationState.removeAuthorization();
                                // Reject - so we can percolate this error up to caller
                                return $q.reject(response);
                            });
        };
        
        AuthorizationSvc.release = function () {
            return $http.post(RESOURCES.LOGOUT_URL)
                    .then(  function(response) {
                                // Erase the token from local now that user is logged out
                                AuthorizationState.removeAuthorization();
                                return response;
                            });
            
        };
        
        AuthorizationSvc.isUserAuthorized = function(privilege) {
            this.isUserAuthorized(privilege, false);
        };
        
        AuthorizationSvc.isUserAuthorized = function(privilege, startsWith) {
            var privs = AuthorizationState.getUserPrivileges();
            if (startsWith) {
                for (var i = 0; i < (privs ? privs.length : 0); i ++) {
                    if (privs[i].startsWith(privilege)) {
                        return true;
                    }
                }
                return false;
            } else {
                return(privs ? privs.indexOf(privilege) > -1 : false);
            }
        };
        
        AuthorizationSvc.isLoggedInUser = function(userSubjectId) {
            return(AuthorizationState.getLoggedInUserId() === userSubjectId);
        };

        return AuthorizationSvc;
    }
])
.controller('PrivilegeCtrl',['$rootScope', 'AuthorizationSvc', 'AuthorizationState',
    function($rootScope, authorizationSvc, authorizationState){

        this.isUserAuthorized = function(privilege) {
            return authorizationSvc.isUserAuthorized(privilege);
        };
        this.isUserAuthorized = function(privilege, startsWith) {
            return authorizationSvc.isUserAuthorized(privilege, startsWith);
        };

        this.isLoggedIn = function() {
            return (authorizationState.getLoggedInUserId() !== '');
        };

        // loginRequired flag is used to "communicate" when a 401 error response occurs and 
        // requires authentication to proceed. Flag is managed by event listeners (defined in
        // app.js) for 'event:auth-loginRequired' and 'event:auth-loginConfirmed'
        this.isLoginRequired = function() {
            return ($rootScope.loginRequired === undefined ? false : $rootScope.loginRequired);
        };
    }
]);




