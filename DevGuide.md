Calabash-Android-Java developer guide
=====================================

Building package after clone
----------------------------
```shell
ant -Dgems.zip.path=scripts/gems_android.zip distro 
```
this will generate the all the jar files into `build` directory. You can now add those jar to your project as dependency

```
├── calabash-android-java-<version>-javadoc.jar
├── calabash-android-java-<version>.jar
├── commons-io-2.4.jar
├── jackson-core-asl-1.9.13.jar
├── jackson-mapper-asl-1.9.13.jar
├── jruby-1.7.5.jar
├── log4j-1.2.17.jar
└── zip4j_1.3.1.jar
```

Making a new package with latest calabash
-----------------------------------------

If you want to package latest calabash gem or some custom gems or libraries you can follow these steps. Calabash-android-java uses `JRuby` to invoke the Ruby client maintained by calabash-android developers. calabash-android gem and all other dependent gems are zipped and put it to the distributable JAR file of `calabash-android-java`.  While executing, `calabash-android-java` will extract the gems from the JAR and sets up the gem path for the JRuby runtime.

To create a distributable package, follow the below instructions.

Create a temporary directory to get all the gems

```shell
rvm gemset create calabash-android-latest-gems
rvm use ruby-1.9.3-p194@calabash-android-latest-gems
```

Install the calabash-android gem. This will install the gem and it's dependencies to the temporary gemset created before.

```shell
gem install calabash-android --no-ri --no-rdoc
```

Download latest JRuby from `http://jruby.org.s3.amazonaws.com/downloads/<version>/jruby-bin-<version>.tar.gz`. Unzip and move this directory to the gemset directory as `jruby.home`. 

```shell
cd ~/.rvm/gems/ruby-1.9.3-p194@calabash-cucumber-latest-gems
wget http://jruby.org.s3.amazonaws.com/downloads/1.7.5/jruby-bin-1.7.5.tar.gz
tar -zxvf jruby-bin-1.7.5.tar.gz
mv jruby-1.7.5  jruby.home
rm jruby-bin-1.7.5.tar.gz
```

Calabash-android-java depends on java version of 'gherkin'. To get that, you have to download the [gherking-java-gem](http://rubygems.org/downloads/gherkin-2.12.2-java.gem) and install it locally
```shell
gem install --local <path>/gherkin-2.12.2-java.gem
```

Create a zip of all the contents.

```shell
zip -r gems_android.zip .
```

Move the `gems_android.zip` file to `calabash-android-java/scripts` directory and make the distro.

```shell
ant -Dgems.zip.path=scripts/gems_android.zip distro 
```

This will make the distributable files inside the `build` directory. Grab the JAR from the distribution, test it and release!
