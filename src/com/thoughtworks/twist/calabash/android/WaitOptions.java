/**
 *
 */
package com.thoughtworks.twist.calabash.android;

/**
 *
 *
 */
public final class WaitOptions {

    private final int timeoutInSec;
    private final int retryFreqInSec;
    private final int postTimeoutInSec;
    private final String timeoutMessage;
    private final boolean screenshotOnError;

    public WaitOptions(int timeoutInSec, int retryFreqInSec,
                       int postTimeoutInSec, String timeoutMessage,
                       boolean screenshotOnError) {
        this.timeoutInSec = timeoutInSec;
        this.retryFreqInSec = retryFreqInSec;
        this.postTimeoutInSec = postTimeoutInSec;
        this.timeoutMessage = timeoutMessage;
        this.screenshotOnError = screenshotOnError;
    }

    public WaitOptions(int timeout) {
        this.timeoutInSec = timeout;
        this.retryFreqInSec = 0;
        this.postTimeoutInSec = 1;
        this.timeoutMessage = "Timed out";
        this.screenshotOnError = true;
    }

    public int getTimeoutInSec() {
        return timeoutInSec;
    }

    public int getRetryFreqInSec() {
        return retryFreqInSec;
    }

    public int getPostTimeoutInSec() {
        return postTimeoutInSec;
    }

    public String getTimeoutMessage() {
        return timeoutMessage;
    }

    public boolean shouldScreenshotOnError() {
        return screenshotOnError;
    }

}
