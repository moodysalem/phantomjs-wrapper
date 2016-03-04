# [![Build Status](https://travis-ci.org/moodysalem/java-phantomjs-wrapper.svg?branch=master)](https://travis-ci.org/moodysalem/java-phantomjs-wrapper) [![Maven Central](https://img.shields.io/maven-central/v/com.moodysalem/phantomjs-wrapper.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22phantomjs-wrapper%22%20g%3A%22com.moodysalem%22) phantomjs-wrapper
A Java wrapper around the PhantomJS binaries with additional support for rendering HTML from an InputStream

## Public Interface
##### PhantomJS#exec(InputStream script, PhantomJSOptions options, String... arguments)

Execute the PhantomJS script stored in the InputStream

##### PhantomJS#render(InputStream html, PaperSize paperSize, ViewportDimensions dimensions, Margin margin, BannerInfo headerInfo, BannerInfo footerInfo, RenderFormat renderFormat)

Use an included script to render the html in the InputStream using PhantomJS

## TODO

* Represent headers and footers in Java in such a way that can be serialized to a PhantomJS script (slated for v1.2)
