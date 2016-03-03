"use strict";
var page = require('webpage').create(),
  system = require('system'),
  output = system.args[ 1 ],
  log = system.stdout.writeLine,
  err = system.stderr.writeLine;

log('setting paper and viewport settings');
page.viewportSize = { width: 816, height: 1056 };
page.paperSize = {
  width: '8.5in',
  height: '11in',
  margin: '0px'
};

page.open('https://google.com', function (status) {
  if (status !== 'success') {
    err('Unable to load the address!');
    phantom.exit(1);
  } else {
    log('loaded page');
    page.evaluate(function () {
      document.body.style.zoom = 0.75;
    });

    log('rendering page to: ' + output);
    page.render(output);

    log('completed rendering');
    phantom.exit(0);
  }
});