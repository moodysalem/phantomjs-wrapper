var page = require('webpage').create(),
  system = require('system'),
  fs = require('fs'),
  width = system.args[ 1 ],
  height = system.args[ 2 ],
  viewportWidth = system.args[ 3 ],
  viewportHeight = system.args[ 4 ],
  marginTop = system.args[ 5 ],
  marginRight = system.args[ 6 ],
  marginBottom = system.args[ 7 ],
  marginLeft = system.args[ 8 ],
  headerHeight = system.args[ 9 ],
  headerFunctionFile = system.args[ 10 ],
  footerHeight = system.args[ 11 ],
  footerFunctionFile = system.args[ 12 ],
  operatingSystem = system.args[ 13 ],
  sourcePath = system.args[ 14 ],
  targetPath = system.args[ 15 ],
  jsWait = +system.args[ 16 ],
  jsInterval = +system.args[ 17 ];


// this function writes the arguments to stdout
var log = function () {
  Array.prototype.slice.call(arguments).map(system.stdout.writeLine);
};

// this function writes the arguments to the stderr
var err = function () {
  Array.prototype.slice.call(arguments).map(system.stderr.writeLine);
};

// this function checks to see if javascript is finished executing on the page
var done = function () {
  return page.evaluate(function () {
    var PageRendered = window.PageRendered;
    return (typeof PageRendered === 'undefined') ||
      (typeof PageRendered === 'boolean' && PageRendered === true) ||
      (typeof PageRendered === 'function' && PageRendered());
  });
};

// this sets a zoom on the page because of the dpi differences between windows and unix
var setZoom = function () {
  if (operatingSystem !== "WINDOWS") {
    try {
      log('Setting zoom on HTML to 0.75');
      page.evaluate(function () {
        document.body.style.zoom = 0.75;
      });
    } catch (error) {
      err('Failed to set zoom on HTML file: ', error);
      phantom.exit(2);
    }
  }
};

// this function renders the page
var renderPage = function () {
  setZoom();

  // render the page
  try {
    log('Rendering PDF to target path: ' + targetPath);
    page.render(targetPath);
  } catch (error) {
    err('Failed to render PDF: ' + error);
    phantom.exit(3);
  }

  phantom.exit(0);
};


page.viewportSize = { width: viewportWidth, height: viewportHeight };

var getFn = function (str) {
  return eval('var fn = ' + str + '; fn');
};

try {
  var headerFunction = getFn(fs.read(headerFunctionFile));
} catch (error) {
  err('Failed to read header function: ' + error);
  phantom.exit(4);
}

try {
  var footerFunction = getFn(fs.read(footerFunctionFile));
} catch (error) {
  err('Failed to read footer function: ' + error);
  phantom.exit(5);
}

var paperSize = {
  width: width,
  height: height,
  margin: {
    top: marginTop,
    right: marginRight,
    bottom: marginBottom,
    left: marginLeft
  },

  header: {
    height: headerHeight,
    contents: phantom.callback(headerFunction)
  },

  footer: {
    height: footerHeight,
    contents: phantom.callback(footerFunction)
  }
};

page.paperSize = paperSize;

// log all the resource requests
page.onResourceRequested = function (requestData, request) {
  log('Loading URL: ' + requestData[ 'url' ]);
};

var waited = 0;
var renderIfDone = function renderIfDone() {
  if (done()) {
    renderPage();
  } else {
    if (waited > jsWait) {
      err('Timed out on JavaScript execution');
      phantom.exit(6);
    }
    log('Waiting an additional ' + jsInterval + 'ms');
    waited += jsInterval;
    setTimeout(renderIfDone, jsInterval);
  }
};

// when finishing loading the resources
// start the timer for js execution to complete
page.onLoadFinished = renderIfDone;

try {
  log('Reading source file: ' + sourcePath);
  page.content = fs.read(sourcePath, { encoding: 'utf-8' });
} catch (error) {
  err('Failed to read source file: ' + error);
  phantom.exit(1);
}