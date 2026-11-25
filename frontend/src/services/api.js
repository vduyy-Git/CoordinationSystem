/**
 * api.js - Universal Centralized API Service for Coordination System
 * Compatible with both file:/// protocol and HTTP/HTTPS servers.
 * Connects Frontend directly to Spring Boot REST API (http://localhost:8080/api)
 */

(function (global) {
  'use strict';

  var API_CONFIG = {
    BASE_URL: (typeof window !== 'undefined' && window.__API_BASE__) ||
              (typeof localStorage !== 'undefined' && localStorage.getItem('api_base')) ||
              'http://localhost:8088/api',
    STORAGE_TOKEN_KEY: 'coords_token',
    STORAGE_USER_KEY: 'coords_user'
  };

  // ==========================================
  // TOKEN & SESSION HELPERS
  // ==========================================
  function getToken() {
    try {
      return localStorage.getItem(API_CONFIG.STORAGE_TOKEN_KEY);
    } catch (e) {
      return null;
    }
  }

  function setToken(token) {
    try {
      if (token) localStorage.setItem(API_CONFIG.STORAGE_TOKEN_KEY, token);
      else localStorage.removeItem(API_CONFIG.STORAGE_TOKEN_KEY);
    } catch (e) {}
  }

  function getCurrentUser() {
    try {
      return JSON.parse(localStorage.getItem(API_CONFIG.STORAGE_USER_KEY) || 'null');
    } catch (e) {
      return null;
    }
  }

  function setCurrentUser(user) {
    try {
      if (user) localStorage.setItem(API_CONFIG.STORAGE_USER_KEY, JSON.stringify(user));
      else localStorage.removeItem(API_CONFIG.STORAGE_USER_KEY);
    } catch (e) {}
  }

  function logout() {
    setToken(null);
    setCurrentUser(null);
    var path = (typeof window !== 'undefined' && window.location.pathname) || '';
    var isSubdir = path.indexOf('/admin/') !== -1 || path.indexOf('/citizen/') !== -1;
    window.location.href = isSubdir ? '../index.html' : 'index.html';
  }

  function isAuthenticated() {
    return !!getToken();
  }

  function hasRole(role) {
    var user = getCurrentUser();
    if (!user || !user.roles) return false;
    for (var i = 0; i < user.roles.length; i++) {
      var r = user.roles[i];
      if (r === role || r === 'ROLE_' + role || (r.roleName && r.roleName.indexOf(role) !== -1)) {
        return true;
      }
    }
    return false;
  }

  // ==========================================
  // CORE REQUEST WRAPPER
  // ==========================================
  async function request(endpoint, options) {
    options = options || {};
    var url = API_CONFIG.BASE_URL + (endpoint.charAt(0) === '/' ? endpoint : '/' + endpoint);
    var token = getToken();

    var headers = Object.assign({
      'Accept': 'application/json'
    }, options.headers || {});

    if (!(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json; charset=utf-8';
    }

    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    var config = Object.assign({}, options, { headers: headers });

    if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
      config.body = JSON.stringify(options.body);
    }

    try {
      var response = await fetch(url, config);

      var data = null;
      var contentType = response.headers.get('content-type') || '';
      if (contentType.indexOf('application/json') !== -1) {
        data = await response.json();
      } else {
        var text = await response.text();
        data = { message: text };
      }

      if (!response.ok) {
        if (response.status === 401 && endpoint.indexOf('/auth/login') === -1) {
          setToken(null);
          setCurrentUser(null);
        }
        return {
          success: false,
          status: response.status,
          message: data?.message || data?.error || ('Lỗi hệ thống (' + response.status + ')'),
          data: data
        };
      }

      return {
        success: true,
        status: response.status,
        data: data?.data !== undefined ? data.data : data,
        message: data?.message || 'Thành công'
      };
    } catch (err) {
      console.warn('[API Connection] Failed request to ' + url + ':', err);
      return {
        success: false,
        status: 0,
        message: 'Chưa thể kết nối tới Backend Spring Boot (http://localhost:8080). Đang hoạt động ở chế độ ngoại tuyến (Offline Demo).',
        error: err
      };
    }
  }

  // ==========================================
  // AUTH API
  // ==========================================
  var authApi = {
    login: async function (email, password) {
      var res = await request('/auth/login', {
        method: 'POST',
        body: { email: email, password: password }
      });
      if (res.success && res.data && res.data.token) {
        setToken(res.data.token);
        setCurrentUser(res.data);
      }
      return res;
    },

    register: async function (userData) {
      return await request('/auth/register', {
        method: 'POST',
        body: userData
      });
    },

    getMe: async function () {
      var res = await request('/auth/me');
      if (res.success && res.data) {
        setCurrentUser(res.data);
      }
      return res;
    }
  };

  // ==========================================
  // CATALOG & PUBLIC ITEMS API
  // ==========================================
  var catalogApi = {
    getItems: function () { return request('/items'); },
    getCategories: function () { return request('/categories'); },
    getUnits: function () { return request('/units'); }
  };

  // ==========================================
  // ASSISTANCE REQUESTS API
  // ==========================================
  var requestApi = {
    create: function (data) { return request('/assistance-requests', { method: 'POST', body: data }); },
    addItem: function (data) { return request('/request-items', { method: 'POST', body: data }); },
    getMine: function () { return request('/assistance-requests/mine'); },
    getAll: function () { return request('/assistance-requests'); },
    approve: function (id) { return request('/assistance-requests/' + id + '/approve', { method: 'POST' }); },
    reject: function (id) { return request('/assistance-requests/' + id + '/reject', { method: 'POST' }); },
    cancel: function (id) { return request('/assistance-requests/' + id + '/cancel', { method: 'POST' }); }
  };

  // ==========================================
  // DONATIONS API
  // ==========================================
  var donationApi = {
    create: function (data) { return request('/donations', { method: 'POST', body: data }); },
    addItem: function (data) { return request('/donation-items', { method: 'POST', body: data }); },
    getMine: function () { return request('/donations/mine'); },
    getAll: function () { return request('/donations'); },
    approve: function (id) { return request('/donations/' + id + '/approve', { method: 'POST' }); },
    reject: function (id) { return request('/donations/' + id + '/reject', { method: 'POST' }); },
    cancel: function (id) { return request('/donations/' + id + '/cancel', { method: 'POST' }); }
  };

  // ==========================================
  // ALLOCATIONS & SHIPMENTS API
  // ==========================================
  var allocationApi = {
    create: function (data) { return request('/allocations', { method: 'POST', body: data }); },
    getAll: function () { return request('/allocations'); },
    cancel: function (id) { return request('/allocations/' + id + '/cancel', { method: 'POST' }); }
  };

  var shipmentApi = {
    getAll: function () { return request('/shipments'); },
    addEvent: function (shipmentId, eventData) { return request('/shipments/' + shipmentId + '/events', { method: 'POST', body: eventData }); }
  };

  // Export API object to Global/Window scope
  var CoordSystemApi = {
    API_CONFIG: API_CONFIG,
    request: request,
    getToken: getToken,
    setToken: setToken,
    getCurrentUser: getCurrentUser,
    setCurrentUser: setCurrentUser,
    logout: logout,
    isAuthenticated: isAuthenticated,
    hasRole: hasRole,
    authApi: authApi,
    catalogApi: catalogApi,
    requestApi: requestApi,
    donationApi: donationApi,
    allocationApi: allocationApi,
    shipmentApi: shipmentApi
  };

  global.CoordSystemApi = CoordSystemApi;
  global.authApi = authApi;
  global.catalogApi = catalogApi;
  global.requestApi = requestApi;
  global.donationApi = donationApi;
  global.allocationApi = allocationApi;
  global.shipmentApi = shipmentApi;

})(typeof window !== 'undefined' ? window : this);
