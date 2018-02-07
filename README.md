# phantomjs-wrapper
[![Build Status](https://travis-ci.org/moodysalem/java-phantomjs-wrapper.svg?branch=master)](https://travis-ci.org/moodysalem/java-phantomjs-wrapper) [![Maven Central](https://img.shields.io/maven-central/v/com.moodysalem/phantomjs-wrapper.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22phantomjs-wrapper%22%20g%3A%22com.moodysalem%22) [![Dependency Status](https://www.versioneye.com/java/com.moodysalem:phantomjs-wrapper/badge.svg?style=flat-square)](https://www.versioneye.com/java/com.moodysalem:phantomjs-wrapper) 

A Java wrapper around the PhantomJS binaries with additional support for rendering HTML from an InputStream

## Install

Use maven to install this module and its dependencies.

    <dependency>
      <groupId>com.moodysalem</groupId>
      <artifactId>phantomjs-wrapper</artifactId>
      <version>see pom.xml</version>
    </dependency>

## 3.0 Optional binary dependencies

In 3.0, all the phantom JS binaries are included in separate modules declared as optional dependencies. If you are building a cross platform app, you will need to include all 3. Otherwise, you can include only the optional dependency for the target Operating System.

      <dependency>
        <groupId>com.moodysalem</groupId>
        <artifactId>phantomjs-wrapper-windows-binary</artifactId>
        <version>see pom.xml</version>
      </dependency>

      <dependency>
        <groupId>com.moodysalem</groupId>
        <artifactId>phantomjs-wrapper-linux-binary</artifactId>
        <version>see pom.xml</version>
      </dependency>

      <dependency>
        <groupId>com.moodysalem</groupId>
        <artifactId>phantomjs-wrapper-macosx-binary</artifactId>
        <version>see pom.xml</version>
      </dependency>

## JS Execution

The phantomJS script bundled with this wrapper (render.js) to support the public render method described below includes logic for waiting for JS to complete execution before attempting to render the page. A page is considered rendered under any of these conditions:

* `PageRendered` is undefined
* `PageRendered` is a boolean and equal to true
* `PageRendered` is a function and its return value is truthy


## Public Interface

    /**
     * Another way to call PhantomJS#render using the RenderOptions to specify all the common options
     *
     * @param html    to render
     * @param options for rendering
     * @return same as PhantomJS#render
     * @throws IOException
     * @throws RenderException
     */
    public static InputStream render(InputStream html, RenderOptions options) throws IOException, RenderException;

    /**
     * Render the html in the input stream with the following properties using a script included with the wrapper
     *
     * @param html         to render
     * @param paperSize    size of the paper (for printed output formats)
     * @param dimensions   dimensions of the viewport
     * @param margin       of the paper
     * @param headerInfo   how the header is generated
     * @param footerInfo   how the footer is generated
     * @param renderFormat the format to render
     * @param jsWait       the maximum amount of time to wait for JS to finish execution in milliseconds
     * @param jsInterval   the interval
     * @return a stream of the rendered output
     * @throws IOException     if any file operations fail
     * @throws RenderException if the render script fails for any reason
     */
    public static InputStream render(InputStream html, PaperSize paperSize, ViewportDimensions dimensions,
                                     Margin margin, BannerInfo headerInfo, BannerInfo footerInfo,
                                     RenderFormat renderFormat, Long jsWait, Long jsInterval) throws IOException, RenderException;


    /**
     * Execute a script with options and a list of arguments
     *
     * @param script    path of script to execute
     * @param options   options to execute
     * @param arguments list of arguments
     * @return the exit code of the script
     * @throws IOException if cmd execution fails
     */
    public static PhantomJSExecutionResponse exec(InputStream script, PhantomJSOptions options, CommandLineArgument... arguments) throws IOException;
