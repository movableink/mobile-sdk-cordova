var exec = require('cordova/exec');

function MovableInk() {}

MovableInk.prototype.start = function (callback) {
  exec(callback, function (err) {}, "MovableInkClient", "start", []);
};

MovableInk.prototype.lastResolvedURL = function (callback) {
  exec(
    callback,
    function (err) { callback("") },
    "MovableInkClient",
    "retrieveStoredDeeplink",
    []
  );
};

MovableInk.prototype.resolveURL = function (url, callback) {
  exec(
    callback,
    function (err) { callback("") },
    "MovableInkClient",
    "resolveUrl",
    [url]
  );
}

MovableInk.prototype.setMIU = function (value) {
  exec(function() {}, function (err) { console.log(err); }, "MovableInkClient", "setMIU", [value]);
};

MovableInk.prototype.identifyUser = function () {
  exec(function () {}, function (err) {}, "MovableInkClient", "identifyUser", []);
};

MovableInk.prototype.logEvent = function (name, properties) {
  exec(
    function () {},
    function (err) { console.log(err); },
    "MovableInkClient",
    "logEvent",
    [name, properties]
  );
};

MovableInk.prototype.categoryViewed = function (properties) {
  exec(
    function () {},
    function (err) {
      console.log(err);
    },
    "MovableInkClient",
    "categoryViewed",
    [properties]
  );
};

MovableInk.prototype.orderCompleted = function (properties) {
  exec(
    function () {},
    function (err) {
      console.log(err);
    },
    "MovableInkClient",
    "orderCompleted",
    [properties]
  );
};

MovableInk.prototype.productAdded = function (properties) {
  exec(
    function () {},
    function (err) {
      console.log(err);
    },
    "MovableInkClient",
    "productAdded",
    [properties]
  );
};

MovableInk.prototype.productRemoved = function (properties) {
  exec(
    function () {},
    function (err) {
      console.log(err);
    },
    "MovableInkClient",
    "productRemoved",
    [properties]
  );
};

MovableInk.prototype.productViewed = function (properties) {
  exec(
    function () {},
    function (err) {
      console.log(err);
    },
    "MovableInkClient",
    "productViewed",
    [properties]
  );
};

MovableInk.prototype.productSearched = function (properties) {
  exec(
    function () {},
    function (err) {
      console.log(err);
    },
    "MovableInkClient",
    "productSearched",
    [properties]
  );
};

MovableInk.prototype.checkPasteboardOnInstall = function (callback) {
  exec(
    callback,
    function (err) {
      console.log(err);
    },
    "MovableInkClient",
    "checkPasteboardOnInstall",
    []
  );
}

MovableInk.prototype.showInAppMessage = function (url, callback) {
  exec(
    callback,
    function (err) {
      console.log(err);
    },
    "MovableInkClient",
    "showInAppMessage",
    [url]
  );
};

MovableInk.prototype.setValidPasteboardValues = function (values) {
  exec(
    function () {},
    function (err) {
      console.log(err)
    },
    'MovableInkClient',
    'setValidPasteboardValues',
    [values]
  )
}

MovableInk.prototype.setAppInstallEventEnabled = function (enabled) {
  exec(
    function () {},
    function (err) {
      console.log(err)
    },
    'MovableInkClient',
    'setAppInstallEventEnabled',
    [enabled]
  )
}

module.exports = new MovableInk();