function KBeacon() {}

// Android

KBeacon.prototype.checkPermissions = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'checkPermissions', []);
}

KBeacon.prototype.startScan = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'startScan', []);
}

KBeacon.prototype.getDiscoveredDevices = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'getDiscoveredDevices', []);
}

KBeacon.prototype.connectToDevice = function (mac, password, timeout, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "cordovaPluginKBeacon", "connectToDevice", []);
};

KBeacon.prototype.ringDevice = function(ringType, ringTime, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "cordovaPluginKBeacon", "ringDevice", []);
};

KBeacon.prototype.disconnect = function () {
    cordova.exec(null, null, "cordovaPluginKBeacon", "disconnect", []);
};

KBeacon.prototype.stopScanning = function() {
    cordova.exec(null, null, 'cordovaPluginKBeacon', 'stopScanning', []);
};

// iOS

KBeacon.prototype.startScaniOS = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'startScaniOS', []);
}

KBeacon.prototype.getDiscoveredDevicesiOS = function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'cordovaPluginKBeacon', 'getDiscoveredDevicesiOS', []);
}

// Installation constructor that binds ToastyPlugin to the window
KBeacon.install = function() {
    if (!window.plugins) {
        window.plugins = {};
    }
    window.plugins.kbeacon = new KBeacon();
    return window.plugins.kbeacon;
};
cordova.addConstructor(KBeacon.install);
