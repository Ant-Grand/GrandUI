Description
-----------

Grand is a tool to create visual representation of ant target dependencies.
Grand-Ui is a GUI based on Grand based on SWT, draw2d (http://www.eclipse.org)
and Jzgraph (http://jzgraph.sf.net).

This is an alpha release so be prepare to face bugs.


Usage
-----

Get the tar.gz/zip file, extract it in the right place, and either run
grand-ui (linux) or grand-ui.bat.


Building from source
--------------------

You need Ant 1.6.1 to compile Grand-Ui. Get a source distribution, run "ant",
and voilà, the build file should be intelligent enough to download the
external dependencies. You can skip this part by defining the "noget" property.
You can override any property in the build.properties file by creating a
build-local.properties file.

The build process should work on windows but hasn't been tested so far.


More information
----------------

 * The grand web site (http://www.ggtools.net/grand),
 * The Eclipse site (http://www.eclipse.org/) will provide info on SWT and
   Draw2d.

You can contact me at grand@ggtools.net.


Licensing
---------
 
This software is licensed under the terms you may find in the file
named "LICENSE" in this directory.
