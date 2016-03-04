# [![Build Status](https://travis-ci.org/moodysalem/java-phantomjs-wrapper.svg?branch=master)](https://travis-ci.org/moodysalem/java-phantomjs-wrapper) phantomjs-wrapper
A Java wrapper around the PhantomJS binaries with additional support for rendering HTML from an InputStream

## Public Interface
##### PhantomJS#exec(InputStream script, PhantomJSOptions options, String... arguments)

Execute the PhantomJS script stored in the InputStream

##### PhantomJS#render(InputStream html, PaperSize paperSize, ViewportDimensions dimensions, Margin margin, RenderFormat renderFormat)

Use an included script to render the html in the InputStream using PhantomJS

## TODO

* Represent headers and footers in Java in such a way that can be serialized to a PhantomJS script
