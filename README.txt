Description
-----------

Grand is a tool to create visual representation of Ant target dependencies.
Grand-Ui is a GUI for Grand based on SWT (https://www.eclipse.org/swt),
Draw2d (https://www.eclipse.org/gef/draw2d) and JzGraph (http://jzgraph.sf.net).

This is an alpha release so be prepared to face bugs.


Usage
-----

Get the zip file, extract it in the right place, and either run
open GrandUi.app (macOS), grand-ui (Linux) or grand-ui.bat (Windows).


Building from source
--------------------

You need Ant 1.8+ to compile Grand-Ui. Get a source distribution, run "ant",
and voilà, the build file should be intelligent enough to download the
external dependencies. You can skip this part by defining the "noget" property.
You can override any property in the build.properties file by creating a
build-local.properties file.


More information
----------------

 * The Grand web site (https://ant-grand.github.io/Grand/grand.html),
 * The Eclipse web site (https://www.eclipse.org/) will provide info on SWT and
   Draw2d.

You can contact me at grand@ggtools.net.


Licensing
---------
 
This software is licensed under the terms you may find in the file
named "LICENSE" in this directory.
