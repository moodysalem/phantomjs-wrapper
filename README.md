# [![Build Status](https://travis-ci.org/moodysalem/java-phantomjs-wrapper.svg?branch=master)](https://travis-ci.org/moodysalem/java-phantomjs-wrapper) [![Maven Central](https://img.shields.io/maven-central/v/com.moodysalem/phantomjs-wrapper.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22phantomjs-wrapper%22%20g%3A%22com.moodysalem%22) phantomjs-wrapper
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
     * Another way to call PhantomJS#render using the RenderOptions to specify all the common phantomJsOptions
     *
     * @param html    to render
     * @param phantomJsOptions for rendering
     * @return a stream of the rendered output
     * @throws IOException
     * @throws RenderException
     */
    public static InputStream render(InputStream html, RenderOptions phantomJsOptions) throws IOException, RenderException;

    /**
     * Execute a script with phantomJsOptions and a list of arguments
     *
     * @param script    path of script to execute
     * @param phantomJsOptions   phantomJsOptions to execute
     * @param arguments list of arguments
     * @return the exit code of the script
     * @throws IOException if cmd execution fails
     */
    public static PhantomJSExecutionResponse exec(InputStream script, PhantomJSOptions phantomJsOptions, CommandLineArgument... arguments) throws IOException;