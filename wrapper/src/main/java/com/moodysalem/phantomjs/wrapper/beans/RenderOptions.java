package com.moodysalem.phantomjs.wrapper.beans;

import com.moodysalem.phantomjs.wrapper.enums.RenderFormat;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;

public class RenderOptions {

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @NotNull(message = "PhantomJsOptions may not be null.")
    private PhantomJsOptions phantomJsOptions;

    @NotNull(message = "PaperSize may not be null.")
    private PaperSize paperSize;

    @NotNull(message = "PaperSize may not be null.")
    private ViewportDimensions viewportDimensions;

    @NotNull(message = "Margin may not be null.")
    private Margin margin;

    @NotNull(message = "HeaderInfo may not be null.")
    private BannerInfo headerInfo;

    @NotNull(message = "FooterInfo may not be null.")
    private BannerInfo footerInfo;

    @NotNull(message = "RenderFormat may not be null.")
    private RenderFormat renderFormat;

    @NotNull(message = "jsExecutionTimeout may not be null.")
    @Min(value = 0, message = "jsExecutionTimeout must be greater than or equal to 0")
    private Long jsExecutionTimeout;

    @NotNull(message = "jsInterval may not be null")
    @Min(value = 0, message = "jsInterval must be greater than or equal to 0")
    private Long jsInterval;


    public RenderOptions() {
        margin = Margin.ZERO;
        viewportDimensions = ViewportDimensions.VIEW_1280_1024;
        paperSize = PaperSize.Letter;
        renderFormat = RenderFormat.PDF;
        footerInfo = BannerInfo.EMPTY;
        headerInfo = BannerInfo.EMPTY;
        jsExecutionTimeout = 1000L;
        jsInterval = 100L;
        phantomJsOptions = PhantomJsOptions.DEFAULT.withDiskCache(true);
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

    public void setPhantomJsOptions(PhantomJsOptions phantomJsOptions) {
        this.phantomJsOptions = phantomJsOptions;
    }

    /**
     * @param paperSize Size of the paper (for printed output formats).
     */
    public void setPaperSize(PaperSize paperSize) {
        this.paperSize = paperSize;
    }

    public void setViewportDimensions(ViewportDimensions viewportDimensions) {
        this.viewportDimensions = viewportDimensions;
    }

    /**
     * @param margin Margin of the paper.
     */
    public void setMargin(Margin margin) {
        this.margin = margin;
    }

    /**
     * @param headerInfo Information on how to generate the header.
     */
    public void setHeaderInfo(BannerInfo headerInfo) {
        this.headerInfo = headerInfo;
    }

    /**
     * @param footerInfo Information on how to generate the footer.
     */
    public void setFooterInfo(BannerInfo footerInfo) {
        this.footerInfo = footerInfo;
    }

    public void setRenderFormat(RenderFormat renderFormat) {
        this.renderFormat = renderFormat;
    }

    /**
     * @param jsExecutionTimeout The maximum amount of time to wait for JS to finish execution in milliseconds.
     */
    public void setJsExecutionTimeout(Long jsExecutionTimeout) {
        this.jsExecutionTimeout = jsExecutionTimeout;
    }

    public void setJsInterval(Long jsInterval) {
        this.jsInterval = jsInterval;
    }

    public RenderOptions phantomJSOptions(PhantomJsOptions options) {
        this.phantomJsOptions = options;
        return this;
    }

    /**
     * @param paperSize Size of the paper (for printed output formats).
     * @return
     */
    public RenderOptions paperSize(PaperSize paperSize) {
        this.paperSize = paperSize;
        return this;
    }

    public RenderOptions viewportDimensions(ViewportDimensions viewportDimensions) {
        this.viewportDimensions = viewportDimensions;
        return this;
    }

    /**
     * @param margin Margin of the paper.
     * @return
     */
    public RenderOptions margin(Margin margin) {
        this.margin = margin;
        return this;
    }

    /**
     * @param headerInfo Information on how to generate the header.
     * @return
     */
    public RenderOptions headerInfo(BannerInfo headerInfo) {
        this.headerInfo = headerInfo;
        return this;
    }

    /**
     * @param footerInfo Information on how to generate the footer.
     * @return
     */
    public RenderOptions footerInfo(BannerInfo footerInfo) {
        this.footerInfo = footerInfo;
        return this;
    }

    public RenderOptions renderFormat(RenderFormat renderFormat) {
        this.renderFormat = renderFormat;
        return this;
    }

    /**
     * @param jsWait The maximum amount of time to wait for JS to finish execution in milliseconds.
     * @return
     */
    public RenderOptions jsWait(Long jsWait) {
        this.jsExecutionTimeout = jsWait;
        return this;
    }

    public RenderOptions jsInterval(Long jsInterval) {
        this.jsInterval = jsInterval;
        return this;
    }

    //TODO: evaluate if this can be done with BeanValidation as well
    private boolean isWaitAndIntervalConstraintInvalid() {
        return (jsExecutionTimeout > 0 && jsInterval > jsExecutionTimeout) || (jsInterval == 0 && jsExecutionTimeout
            > 0);
    }

    public boolean isValid() {
        if(isWaitAndIntervalConstraintInvalid()) {
            return  false;
        }

        return validator.validate(this).isEmpty();
    }

    public Set<ConstraintViolation<RenderOptions>> getConstraintViolations() {
        Set<ConstraintViolation<RenderOptions>> constraintViolations = validator.validate(this);

        if(isWaitAndIntervalConstraintInvalid()) {
            constraintViolations.add(ConstraintViolationImpl.forBeanValidation(null, null,null,"If jsExecutionTimeout is greater than 0, the jsInterval needs to be 0 < jsInterval < jsExecutionTimeout",null,null,null,null,null,null,null,null));
        }

        return constraintViolations;
    }
}