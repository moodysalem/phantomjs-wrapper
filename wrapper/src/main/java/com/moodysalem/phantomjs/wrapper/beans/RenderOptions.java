package com.moodysalem.phantomjs.wrapper.beans;

import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;

public class RenderOptions {

  private PhantomJsOptions phantomJsOptions = PhantomJsOptions.DEFAULT.withDiskCache(true);
  private PaperSize paperSize = PaperSize.Letter;
  private ViewportDimensions viewportDimensions = ViewportDimensions.VIEW_1280_1024;
  private Margin margin = Margin.ZERO;
  private BannerInfo headerInfo = BannerInfo.EMPTY;
  private BannerInfo footerInfo = BannerInfo.EMPTY;
  private RenderFormat renderFormat = RenderFormat.PDF;
  private Long jsExecutionTimeout = 1000L;
  private Long jsInterval = 100L;


  /**
   * Default constructor applies default options. Use the fluent API to customize your options.
   */
  public RenderOptions() {
  }

  public RenderOptions(final Margin margin, final ViewportDimensions viewportDimensions,
      final PaperSize paperSize, final RenderFormat renderFormat, final BannerInfo footerInfo,
      final BannerInfo headerInfo, final Long jsExecutionTimeout, final Long jsInterval,
      final PhantomJsOptions phantomJsOptions) {
    this.margin = margin;
    this.viewportDimensions = viewportDimensions;
    this.paperSize = paperSize;
    this.renderFormat = renderFormat;
    this.footerInfo = footerInfo;
    this.headerInfo = headerInfo;
    this.jsExecutionTimeout = jsExecutionTimeout;
    this.jsInterval = jsInterval;
    this.phantomJsOptions = phantomJsOptions;
  }

  public PhantomJsOptions getPhantomJsOptions() {
    return phantomJsOptions;
  }

  /**
   * @return Size of the paper (for printed output formats).
   */
  public PaperSize getPaperSize() {
    return paperSize;
  }

  public ViewportDimensions getViewportDimensions() {
    return viewportDimensions;
  }

  /**
   * @return Margin of the paper.
   */
  public Margin getMargin() {
    return margin;
  }

  /**
   * @return Information on how to generate the header.
   */
  public BannerInfo getHeaderInfo() {
    return headerInfo;
  }

  /**
   * @return Information on how to generate the footer.
   */
  public BannerInfo getFooterInfo() {
    return footerInfo;
  }

  public RenderFormat getRenderFormat() {
    return renderFormat;
  }

  /**
   * @return The maximum amount of time to wait for JS to finish execution in milliseconds.
   */
  public Long getJsExecutionTimeout() {
    return jsExecutionTimeout;
  }

  public Long getJsInterval() {
    return jsInterval;
  }


  /**
   * @param paperSize Size of the paper (for printed output formats).
   */
  public RenderOptions paperSize(final PaperSize paperSize) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, jsExecutionTimeout, jsInterval, phantomJsOptions);
  }

  public RenderOptions viewportDimensions(final ViewportDimensions viewportDimensions) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, jsExecutionTimeout, jsInterval, phantomJsOptions);
  }

  /**
   * @param margin Margin of the paper.
   */
  public RenderOptions margin(final Margin margin) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, jsExecutionTimeout, jsInterval, phantomJsOptions);
  }

  /**
   * @param headerInfo Information on how to generate the header.
   */
  public RenderOptions headerInfo(final BannerInfo headerInfo) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, jsExecutionTimeout, jsInterval, phantomJsOptions);
  }

  /**
   * @param footerInfo Information on how to generate the footer.
   */
  public RenderOptions footerInfo(final BannerInfo footerInfo) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, jsExecutionTimeout, jsInterval, phantomJsOptions);
  }

  public RenderOptions renderFormat(final RenderFormat renderFormat) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, jsExecutionTimeout, jsInterval, phantomJsOptions);
  }

  /**
   * @param jsExecutionTimeout The maximum amount of time to wait for JS to finish execution in
   * milliseconds.
   */
  public RenderOptions jsExecutionTimeout(final Long jsExecutionTimeout) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, this.jsExecutionTimeout, jsInterval, phantomJsOptions);
  }

  public RenderOptions jsInterval(final Long jsInterval) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, jsExecutionTimeout, jsInterval, phantomJsOptions);
  }

  public RenderOptions phantomJsOptions(final PhantomJsOptions phantomJsOptions) {
    return new RenderOptions(margin, viewportDimensions, paperSize, renderFormat, footerInfo,
        headerInfo, jsExecutionTimeout, jsInterval, phantomJsOptions);
  }
}