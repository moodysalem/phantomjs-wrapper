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
  log = system.stdout.writeLine,
  err = system.stderr.writeLine;

page.viewportSize = { width: viewportWidth, height: viewportHeight };


var getFn = function (str) {
  return eval('var fn = ' + str + '; fn');
};

try {
  var headerFunction = getFn(fs.read(headerFunctionFile));
} catch (error) {
  err('failed to read header function: ' + error);
  phantom.exit(4);
}

try {
  var footerFunction = getFn(fs.read(footerFunctionFile));
} catch (error) {
  err('failed to read footer function: ' + error);
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

page.onResourceRequested = function (requestData, request) {
  log('loading url: ', requestData[ 'url' ]);
};

page.onLoadFinished = function () {
  if (operatingSystem !== "WINDOWS") {
    try {
      log('setting zoom on html to 0.75');
      page.evaluate(function () {
        document.body.style.zoom = 0.75;
      });
    } catch (error) {
      err('failed to set zoom on html file: ' + error);
      phantom.exit(2);
    }
  }

  try {
    log('rendering to target path: ' + targetPath);
    page.render(targetPath);
  } catch (error) {
    err('failed to render pdf: ' + error);
    phantom.exit(3);
  }

  phantom.exit(0);
};

try {
  log('reading source file: ' + sourcePath);
  page.content = fs.read(sourcePath, { encoding: 'utf-8' });
} catch (error) {
  err('failed to read source file: ' + error);
  phantom.exit(1);
}