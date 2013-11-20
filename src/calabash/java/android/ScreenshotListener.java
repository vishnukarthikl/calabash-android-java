package calabash.java.android;

/**
 *
 * Provides a callback when screenshot is taken
 *
 */
public interface ScreenshotListener {

    /**
     * This method will be invoked when calabash takes a screenshot
     *
     * @param path
     *            Full path to the file
     * @param imageType
     *            image type. Usually this is image/png
     * @param fileName
     *            File name
     */
    void screenshotTaken(String path, String imageType, String fileName);

}