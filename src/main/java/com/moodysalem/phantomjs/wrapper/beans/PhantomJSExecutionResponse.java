package com.moodysalem.phantomjs.wrapper.beans;

public class PhantomJSExecutionResponse {

	private int exitCode;
	private String stdOut;
	private String stdErr;
	
	public PhantomJSExecutionResponse(int exitCode, String stdOut, String stdErr) {
		super();
		this.exitCode = exitCode;
		this.stdOut = stdOut;
		this.stdErr = stdErr;
	}
	
	public int getExitCode() {
		return exitCode;
	}
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	public String getStdOut() {
		return stdOut;
	}
	public void setStdOut(String stdOut) {
		this.stdOut = stdOut;
	}
	public String getStdErr() {
		return stdErr;
	}
	public void setStdErr(String stdErr) {
		this.stdErr = stdErr;
	}
}
