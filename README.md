# [![Build Status](https://travis-ci.org/moodysalem/java-phantomjs-wrapper.svg?branch=master)](https://travis-ci.org/moodysalem/java-phantomjs-wrapper) phantomjs-wrapper
A Java wrapper around the PhantomJS binaries

# Public Interface
PhantomJS#exec(InputStream script, PhantomJSOptions options, String... arguments)

Execute a phantomjs script stored in the InputStream

PhantomJS#render(InputStream html, float width, PageSizeUnit widthUnit, float height, PageSizeUnit heightUnit,int viewportWidth, int viewportHeight, Format format)

Use an included script to render the html in the InputStream using PhantomJS
