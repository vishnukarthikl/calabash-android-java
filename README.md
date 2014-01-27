calabash-android-java
=====================

This project implements a calabash android client in Java.

Download
=========

[calabash-android-java](https://github.com/vishnukarthikl/calabash-android-java/releases/)

Getting started
===============
* Download [Android Sdk](https://developer.android.com/sdk/index.html) and set up ANDROID_HOME and JAVA_HOME in your environment.
* Calabash-android-java depends on Jruby,log4j and Zip4j. You need to add those jars also into the classpath. Download the JAR file and add all the JAR files to the class path.
* If you have an existing apk, point to it in the AndroidRunner, see example below on how to write tests.

Note: AndroidRunner starts the emulator if the device name is specified in the configuration, but it is not very stable. So it is advised that the emulator is manually started and serial(e.g emulator-5554) is set in the configuration.

```java
import com.thoughtworks.twist.calabash.android.AndroidConfiguration;
import com.thoughtworks.twist.calabash.android.AndroidRunner;
import com.thoughtworks.twist.calabash.android.CalabashException;

public class Main {
    public static void main(String[] args) throws CalabashException {
        AndroidConfiguration androidConfiguration = new AndroidConfiguration();
        androidConfiguration.setSerial("emulator-5554");
        AndroidRunner androidRunner = new AndroidRunner("res/AndroidTestApplication.apk", androidConfiguration);
        androidRunner.setup();
        androidRunner.start();
    }
}
```

Writing tests
==============

Finding an element and touching it. In the below example, we query for a button and then touch the first one.

```java
import calabash.java.IOSApplication;
import calabash.java.CalabashException;
import calabash.java.CalabashRunner;
import calabash.java.UIElement;

public class Main {
    public static void main(String[] args) throws CalabashException {
        AndroidConfiguration androidConfiguration = new AndroidConfiguration();
        androidConfiguration.setSerial("emulator-5554");
        AndroidRunner androidRunner = new AndroidRunner("res/AndroidTestApplication.apk", androidConfiguration);
        AndroidApplication application = androidRunner.start();

        UIElements button = application.query("button");
        button.touch();
    }
}
```

For the query syntax, please take a look at [Calabash Wiki](http://blog.lesspainful.com/2012/12/18/Android-Query/). You can use Junit asserts to perform assertions. For more information, visit [Calabash](https://github.com/calabash/calabash-android) page.

Inspecting elements
===================

If you need to know how elements are structured in your application, you can use `inspect()` method on `Application` or `UIElement` instances. It will iterate over each element and it's child elements from which you can build a tree view.

```java
public class Main {
    public static void main(String[] args) throws CalabashException {
        AndroidConfiguration androidConfiguration = new AndroidConfiguration();
        androidConfiguration.setSerial("emulator-5554");
        AndroidRunner androidRunner = new AndroidRunner("res/AndroidTestApplication.apk", androidConfiguration);
        androidRunner.setup();
        AndroidApplication application = androidRunner.start();

           application.inspect(new InspectCallback() {
   			public void onEachElement(UIElement element, int nestingLevel) {
   				for (int i = 0; i < nestingLevel; i++) {
   					System.out.print("-");
   				}
   				System.out.print(element.getElementClass() + "\n");
   			}
   		});

    }
}
```

Screenshots
==============

`takeScreenshot()` function can be used to take the screenshot. You can also listen to screenshot events which will be called whenever a screenshot is taken. Calabash ruby client takes screenshots when there is a failure. Hooking on to this event handler will let you know when screenshots are taken.

```java
public class Main {
    public static void main(String[] args) throws CalabashException, OperationTimedoutException {
            AndroidConfiguration androidConfiguration = new AndroidConfiguration();
            androidConfiguration.setSerial("emulator-5554");
            AndroidRunner androidRunner = new AndroidRunner("res/AndroidTestApplication.apk", androidConfiguration);
            androidRunner.setup();
            AndroidApplication application = androidRunner.start();

            application.waitFor(new ICondition() {
                @Override
                public boolean test() throws CalabashException {
                    return false;
                }
            }, 1);
        }
}
```

Licence
==========

The MIT License (MIT)

Copyright (c) 2013 Thoughtworks Studios

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
