# Gephi Plugins

This repository is a clone of the official Gephi plugins, hosted [here] (https://github.com/gephi/gephi-plugins). 
Gephi plugins are implemented in Java and can extend [Gephi](https://gephi.org) in many different ways, adding or improving features. 
Getting started is easy with this repository but also checkout the [Bootcamp](https://github.com/gephi/gephi-plugins-bootcamp) for examples of plugins you can create. 

## Get started

### Requirements

- [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later 
- [Maven](http://maven.apache.org/)
- [Netbeans IDE](https://netbeans.org/) or any other IDE/Editor

### Create a plugin

The creation of a new plugin is simple thanks to our custom [Gephi Maven Plugin](https://github.com/gephi/gephi-maven-plugin). 
The `generate` goal asks a few questions and then configures everything for you.

- Fork and checkout the latest version of this repository:

        git clone https://www.github.com/getuliopereira/gephi-plugins.git

- Run the following command and answer the questions:

        mvn org.gephi:gephi-maven-plugin:generate

This is an example of what this process will ask:

        Name of organization (e.g. my.company): org.foo
        Name of artifact (e.g my-plugin): my-first-plugin
        Version (e.g. 1.0.0): 1.0.0
        Directory name (e.g MyPlugin): MyFirstPlugin
        Branding name (e.g My Plugin): My First Plugin
        Category (e.g Layout, Filter, etc.): Layout
        Author: My Name
        Author email (optional): 
        Author URL (optional): 
        License (e.g Apache 2.0): Apache 2.0
        Short description (i.e. one sentence): Plugin catch-phrase
        Long description (i.e multiple sentences): Plugin features are great
        Would you like to add a README.md file (yes|no): yes

The plugin configuration is created. Now you can (in any order):

- Add some Java code in the `src/main/java` folder of your plugin
- Add some resources (e.g. Bundle.properties, images) into the `src/main/resources/` folder of your plugin
- Change the version, author or license information into the `pom.xml` file, which is in your plugin folder
- Edit the description or category details into the `src/main/nbm/manifest.mf` file in your plugin folder 

### Build a plugin

Run the following command to compile and build your plugin:

       mvn clean package

In addition of compiling and building the JAR and NBM, this command uses the `Gephi Maven Plugin` to verify the plugin's configuration. 
In care something is wrong it will fail and indicte the reason.

### Run Gephi with plugin

Run the following command to run Gephi with your plugin pre-installed. Make sure to run `mvn package` beforehand to rebuild.

       mvn org.gephi:gephi-maven-plugin:run

In Gephi, when you navigate to `Tools` > `Plugins` you should see your plugin listed in `Installed`.

## Submit a plugin

Submitting a Gephi plugin for approval is a simple process based on GitHub's [pull request](https://help.github.com/articles/using-pull-requests/) mechanism.

- First, make sure you're working on a fork of [gephi-plugins](https://github.com/gephi/gephi-plugins). You can check that by running `git remote -v` and look at the url, it should contain your GitHub username, for example `git@github.com:username/gephi-plugins.git`.

- Add and commit your work. It's recommended to keep your fork synced with the upstream repository, as explained [here](https://help.github.com/articles/syncing-a-fork/), so you can run `git merge upstream/master` beforehand.

- Push your commits to your fork with `git push origin master`.

- Navigate to your fork's URL and create a pull request. Select `master-forge` instead of `master` as base branch.

- Submit your pull request.

## Update a plugin

Updating a Gephi plugin has the same process as submiting it for the first time. Don't forget to merge from upstream's master branch.

## IDE Support

### Netbeans IDE

- Start Netbeans and go to `File` and then `Open Project`. Navigate to your fork repository, Netbeans automatically recognizes it as Maven project. 
- Each plugin module can be found in the `Modules` folder.

To run Gephi with your plugin pre-installed, right click on the `gephi-plugins` project and select `Run`.

To debug Gephi with your plugin, right click on the `gephi-plugins` project and select `Debug`.

# References

* <https://help.github.com/categories/writing-on-github>
* <https://daringfireball.net/projects/markdown/syntax>
* <https://github.com/gephi/gephi/wiki/Build-a-plugin-without-Gephi-source-code>
* <https://github.com/gephi/gephi/wiki/Plugin-Quick-Start>
* <https://github.com/gephi/gephi/wiki/Import>
* <https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet>
* <https://git-scm.com/book/pt-br/v1/Primeiros-passos>