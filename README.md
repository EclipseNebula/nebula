# Nebula and Opal: Rich SWT Widgets
Welcome to the [Eclipse Nebula project](https://projects.eclipse.org/projects/technology.nebula).

This repository contains a large set of UI elements that can be used in applications based on Java and SWT.
See [Nebula Widgets](https://github.com/EclipseNebula/nebula/blob/master/docs/Widgets.md) for information about the available widgets.


## Download
Downloads are available at the following location:
* [https://download.eclipse.org/nebula/updates/](https://download.eclipse.org/nebula/updates/)
* [Link to the archives](https://archive.eclipse.org/nebula/)

These sites provide both mature, stable widgets as well as incubating widgets; the latter are clearly marked as *incubating* and may break API from release to release.

Refer to the [general document](https://eclipse.dev/justj/?page=tools#p2-anatomy) for details about the update site structure.

## Contact
* [Issues](https://github.com/EclipseNebula/nebula/issues)
* [Questions](https://github.com/EclipseNebula/nebula/discussions) 
* [Mailing List](https://dev.eclipse.org/mailman/listinfo/nebula-dev)

## Contribute
We can accept your patches if you have [properly set up an account](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#setting-up-your-eclipse-and-github-account).

For every patch we require a [corresponding GitHub issue](https://github.com/EclipseNebula/nebula/issues).
See the [recommended workflow](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md#setting-up-your-eclipse-and-github-account) for guidelines.

## Setup a Development enviroment
Please use the following automated setup to configure a development environment pull requests:

[![Create Eclipse Development Environment for Eclipse Nebula](https://download.eclipse.org/oomph/www/setups/svg/Nebula.svg)](https://www.eclipse.org/setups/installer/?url=https://raw.githubusercontent.com/EclipseNebula/nebula/master/configuration.setup&show=true "Click to open Eclipse-Installer Auto Launch or drag into your running installer")

## Building on our build server
After admin approval, pull requests get built by this job:
 * [Pull Request Builds](https://ci.eclipse.org/nebula/job/nebula-build/view/change-requests/)

## Building locally
    git clone https://github.com/EclipseNebula/nebula.git
    cd nebula
    mvn verify

More info can be found on Nebula's [Build Server](https://ci.eclipse.org/nebula/).

## New Committers
Before you decide to contribute your code to Eclipse, keep in mind that your code must follow the EPL, Eclipse's Public License.  
This means that committers and contributors need to follow some rules.

A good overview of what this entails and how this affects you [can be read here](http://www.eclipse.org/legal/#Committers)

There are a number of ways you can contribute. 

* A new project inside Nebula.
* New widgets in the Nebula project.
* Maintain existing widgets.

If you choose any of these options, just open an issue.

## New Widgets
If you want to contribute a widget, just open an issue and state your intention. We will then guide you through the process.

