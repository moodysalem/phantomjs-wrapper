"use strict";
var page = require('webpage').create(),
  system = require('system'),
  fs = require('fs'),
  width = system.args[ 1 ],
  height = system.args[ 2 ],
  viewportWidth = system.args[ 3 ],
  viewportHeight = system.args[ 4 ],
  sourcePath = system.args[ 5 ],
  targetPath = system.args[ 6 ],
  log = system.stdout.writeLine,
  err = system.stderr.writeLine;

page.viewportSize = { width: viewportWidth, height: viewportHeight };
page.paperSize = {
  width: width,
  height: height,
  margin: '0px'
};

log('reading source file');
page.html = fs.read(sourcePath);

log('setting zoom');
page.evaluate(function () {
  document.body.style.zoom = 0.75;
});

log('rendering to target file');
page.render(targetPath);

phantom.exit(0);