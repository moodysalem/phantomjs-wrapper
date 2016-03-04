"use strict";
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
  sourcePath = system.args[ 9 ],
  targetPath = system.args[ 10 ],
  log = system.stdout.writeLine,
  err = system.stderr.writeLine;

page.viewportSize = { width: viewportWidth, height: viewportHeight };
page.paperSize = {
  width: width,
  height: height,
  margin: {
    top: marginTop,
    right: marginRight,
    bottom: marginBottom,
    left: marginLeft
  }
};

log('reading source file: ' + sourcePath);
page.content = fs.read(sourcePath);

log('setting zoom on html');
page.evaluate(function () {
  document.body.style.zoom = 0.75;
});

log('rendering to target file');
page.render(targetPath);

phantom.exit(0);