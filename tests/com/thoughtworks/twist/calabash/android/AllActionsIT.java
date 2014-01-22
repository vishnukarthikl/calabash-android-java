package com.thoughtworks.twist.calabash.android;


import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AllActionsIT {


    public static final String EMULATOR = "emulator-5554";
    public static final String MAIN_ACTIVITY = "MyActivity";
    private static String packageName;
    private static File tempDir;
    private static File apkPath;
    private static AndroidApplication application;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void installApp() throws Exception {
        packageName = "com.example.AndroidTestApplication";
        tempDir = TestUtils.createTempDir("TestAndroidApps");
        apkPath = TestUtils.createTempDirWithProj("AndroidTestApplication.apk", tempDir);
        application = TestUtils.installAppOnEmulator(EMULATOR, packageName, apkPath);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        FileUtils.deleteDirectory(tempDir);
    }

    @After
    public void goToMainActivity() throws CalabashException, OperationTimedoutException {
        if (!application.getCurrentActivity().equals(MAIN_ACTIVITY)) {
            application.goBack();
            try {
                application.waitForActivity(MAIN_ACTIVITY, 6);
            } catch (CalabashException e) {
                application.goBack();
                application.waitForActivity(MAIN_ACTIVITY, 6);
            }
        }
    }

    @Test
    public void shouldQueryForElements() throws CalabashException, OperationTimedoutException {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SIMPLE_ELEMENTS);

        UIElements elements = application.query("textview marked:'Hello world!'");

        assertEquals(1, elements.size());
        assertEquals("Hello world!", elements.first().getText());
    }

    @Test
    public void shouldInspectApplicationElements() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_NESTED_VIEWS);
        String expectedElementCollection = "Element : com.android.internal.policy.impl.PhoneWindow$DecorView , Nesting : 0\n" +
                "Element : com.android.internal.widget.ActionBarOverlayLayout , Nesting : 1\n" +
                "Element : android.widget.FrameLayout , Nesting : 2\n" +
                "Element : android.widget.LinearLayout , Nesting : 3\n" +
                "Element : android.widget.TableLayout , Nesting : 4\n" +
                "Element : android.widget.TableRow , Nesting : 5\n" +
                "Element : android.widget.Button , Nesting : 6\n" +
                "Element : android.widget.Button , Nesting : 6\n" +
                "Element : android.widget.TableRow , Nesting : 5\n" +
                "Element : android.widget.TableRow , Nesting : 6\n" +
                "Element : android.widget.Button , Nesting : 7\n" +
                "Element : android.widget.RadioButton , Nesting : 7\n" +
                "Element : android.widget.LinearLayout , Nesting : 6\n" +
                "Element : android.widget.ToggleButton , Nesting : 7\n" +
                "Element : android.widget.CheckBox , Nesting : 7\n" +
                "Element : android.widget.TableRow , Nesting : 5\n" +
                "Element : android.widget.FrameLayout , Nesting : 6\n" +
                "Element : android.widget.ProgressBar , Nesting : 7\n" +
                "Element : android.widget.RelativeLayout , Nesting : 7\n" +
                "Element : android.widget.RelativeLayout , Nesting : 6\n" +
                "Element : android.widget.TextView , Nesting : 7\n" +
                "Element : android.widget.TextView , Nesting : 7\n" +
                "Element : com.android.internal.widget.ActionBarContainer , Nesting : 2\n" +
                "Element : com.android.internal.widget.ActionBarView , Nesting : 3\n" +
                "Element : android.widget.LinearLayout , Nesting : 4\n" +
                "Element : com.android.internal.widget.ActionBarView$HomeView , Nesting : 5\n" +
                "Element : android.widget.ImageView , Nesting : 6\n" +
                "Element : android.widget.LinearLayout , Nesting : 5\n" +
                "Element : android.widget.TextView , Nesting : 6\n";
        final StringBuilder actualElementCollection = new StringBuilder();

        application.inspect(new InspectCallback() {
            public void onEachElement(UIElement element, int nestingLevel) {
                actualElementCollection.append(String.format("Element : %s , Nesting : %d\n", element.getElementClass(), nestingLevel));
            }
        });

        assertEquals(expectedElementCollection, actualElementCollection.toString());
    }

    @Test
    public void shouldInspectAllElementsBelowElement() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_NESTED_VIEWS);
        UIElement uiElement = application.query("tableRow index:1").first();

        String expectedElementsCollection = "Element : android.widget.TableRow , Nesting : 0\n" +
                "Element : android.widget.TableRow , Nesting : 1\n" +
                "Element : android.widget.Button , Nesting : 2\n" +
                "Element : android.widget.RadioButton , Nesting : 2\n" +
                "Element : android.widget.LinearLayout , Nesting : 1\n" +
                "Element : android.widget.ToggleButton , Nesting : 2\n" +
                "Element : android.widget.CheckBox , Nesting : 2\n";

        final StringBuilder actualElementsCollection = new StringBuilder();
        uiElement.inspect(new InspectCallback() {
            public void onEachElement(UIElement element, int nestingLevel) {
                actualElementsCollection.append(String.format("Element : %s , Nesting : %d\n", element.getElementClass(), nestingLevel));
            }
        });

        assertEquals(expectedElementsCollection, actualElementsCollection.toString());

    }

    @Test
    public void shouldTouchElements() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SIMPLE_ELEMENTS);
        UIElement button = application.query("button marked:'Normal Button'").first();
        UIElement radioButton = application.query("radioButton").first();
        UIElement imageButton = application.query("imageButton").first();

        button.touch();
        UIElement textView = application.query("textView id:'textView'").first();
        assertEquals("normal button was clicked", textView.getText());

        radioButton.touch();
        textView = application.query("textView id:'textView'").first();
        assertEquals("radio button was clicked", textView.getText());

        imageButton.touch();
        textView = application.query("textView id:'textView'").first();
        assertEquals("image button was clicked", textView.getText());
    }

    @Test
    public void shouldSetText() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SIMPLE_ELEMENTS);
        UIElement editText = application.query("editText").first();

        editText.setText("foo bar");

        UIElement textView = application.query("textView id:'textView'").first();
        assertEquals("foo bar was entered", textView.getText());
    }

    @Test
    public void shouldPerformCheckboxActions() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_NESTED_VIEWS);

        UIElement checkBox = application.query("checkBox").first();
        String isChecked = checkBox.getProperty("checked").toString();
        assertEquals(false, Boolean.parseBoolean(isChecked));

        checkBox.setChecked(true);
        isChecked = checkBox.getProperty("checked").toString();
        assertEquals(true, Boolean.parseBoolean(isChecked));

        checkBox.setChecked(false);
        isChecked = checkBox.getProperty("checked").toString();
        assertEquals(false, Boolean.parseBoolean(isChecked));
    }

    @Test
    public void shouldPerformScrollActions() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SCROLL_LIST);

        String queryForSecondPageElement = "textView marked:'No Highway'";
        assertEquals(0, application.query(queryForSecondPageElement).size());

        application.scrollDown();
        assertEquals(1, application.query(queryForSecondPageElement).size());

        String queryForFirstPageElement = "textView marked:'A Time to Kill'";
        assertEquals(0, application.query(queryForFirstPageElement).size());

        application.scrollUp();
        assertEquals(1, application.query(queryForFirstPageElement).size());
    }

    @Test
    public void shouldTakeScreenshot() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_NESTED_VIEWS);
        File screenshotsDir = new File(tempDir, "screenshots");
        screenshotsDir.mkdirs();

        application.takeScreenshot(screenshotsDir, "first");
        File screenshot = new File(screenshotsDir, "first_0.png");

        assertTrue(screenshot.exists());
    }

    @Test
    public void shouldSelectMenuOptions() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SIMPLE_ELEMENTS);

        application.selectMenuItem("Third");
        UIElement textView = application.query("textView id:'textView'").first();
        assertEquals("Third menu item was selected", textView.getText());

        application.selectMenuItem("Fourth");
        textView = application.query("textView id:'textView'").first();
        assertEquals("Fourth menu item was selected", textView.getText());

    }

    @Test
    public void shouldPerformSwipeActions() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SWIPE_PAGE);

        application.swipe(Direction.RIGHT);
        int index = Integer.parseInt((String) application.query("* id:'pager'").first().getProperty("currentItem"));
        assertEquals(1, index);

        application.swipe(Direction.LEFT);
        index = Integer.parseInt((String) application.query("* id:'pager'").first().getProperty("currentItem"));
        assertEquals(0, index);
    }

    @Test
    public void shouldPerformLongPress() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SIMPLE_ELEMENTS);

        application.query("textView marked:'Long press text'").first().longPress();
        UIElement resultTextView = application.query("textView id:'textView'").first();
        assertEquals("long press text was long pressed", resultTextView.getText());

        application.query("imageView id:'longPressImage'").first().longPress();
        UIElement resultTextViewAfter = application.query("textView id:'textView'").first();
        assertEquals("long press image was long pressed", resultTextViewAfter.getText());
    }

    @Test
    public void shouldSetGPSCoordinates() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_CURRENT_LOCATION);

        application.setGPSCoordinates(12.928909, 77.628906);
        UIElement latitudeText = application.query("textView id:'latitude'").first();
        UIElement longitudeText = application.query("textView id:'longitude'").first();
        assertEquals(Double.parseDouble(latitudeText.getText()), 12.928909, .001);
        assertEquals(Double.parseDouble(longitudeText.getText()), 77.628906, .001);

        application.setGPSLocation("Thoughtworks inc, San Francisco");
        latitudeText = application.query("textView id:'latitude'").first();
        longitudeText = application.query("textView id:'longitude'").first();

        assertEquals(Double.parseDouble(latitudeText.getText()), 37.792626, .05);
        assertEquals(Double.parseDouble(longitudeText.getText()), -122.402698, .05);
    }

    @Test
    public void shouldSetDateOnDatePicker() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_DATE_TIME_ELEMENTS);

        UIElement datePicker = application.query("datePicker").first();
        datePicker.setDate(new DateTime(2011, 1, 25, 0, 0));

        datePicker = application.query("datePicker").first();

        DateTime actualDate = datePicker.getDate();

        assertEquals(2011, actualDate.getYear());
        assertEquals(1, actualDate.getMonthOfYear());
        assertEquals(25, actualDate.getDayOfMonth());

    }

    @Test
    public void shouldGetSharedPreferences() throws Exception {
        Map<String, String> preferences = application.getSharedPreferences("my_preferences");

        assertEquals("true", preferences.get("a boolean"));
        assertEquals("my string", preferences.get("a string"));
        assertEquals("1.5", preferences.get("a float"));
        assertEquals("123", preferences.get("an int"));
    }

    @Test
    public void shouldTestGoBack() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_NESTED_VIEWS);
        application.goBack();

        application.waitForActivity(MAIN_ACTIVITY, 2);
        assertEquals(MAIN_ACTIVITY, application.getCurrentActivity());
    }

    @Test
    public void shouldTestPerformCalabashAction() throws Exception {
        final String enteredText = "text";
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SIMPLE_ELEMENTS);

        application.performCalabashAction("enter_text_into_numbered_field", enteredText, "1");

        final String actualText = application.query("editText index:0").get(0).getText();
        assertEquals(actualText, enteredText);
    }

    @Test
    public void shouldTakeScreenshotOnFailure() throws CalabashException {
        final StringBuffer screenshotPath = new StringBuffer();
        AndroidConfiguration androidConfiguration = new AndroidConfiguration();
        androidConfiguration.setSerial(EMULATOR);
        androidConfiguration.setScreenshotListener(new ScreenshotListener() {
            public void screenshotTaken(String path, String imageType, String fileName) {
                screenshotPath.append(path);
            }
        });
        try {
            application.waitFor(new ICondition() {
                @Override
                public boolean test() throws CalabashException {
                    return false;
                }
            }, 1);
        } catch (OperationTimedoutException e) {
        }

        assertTrue(new File(tempDir, screenshotPath.toString()).exists());
    }

    @Test
    public void shouldWaitForAnElementWithId() throws Exception {
        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SIMPLE_ELEMENTS);

        application.waitForElementWithId("button", 5);
    }

    @Test
    public void shouldFailForAnElementWithIdNotFound() throws Exception {
        expectedException.expect(OperationTimedoutException.class);
        expectedException.expectMessage("Timed out");

        TestUtils.goToActivity(application, TestUtils.ACTIVITY_SIMPLE_ELEMENTS);

        application.waitForElementWithId("foobarid", 5);
    }

}
