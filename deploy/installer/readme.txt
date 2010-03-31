There is a wiki page which may have more recent instructions:
- http://udig.refractions.net/confluence/display/ADMIN/Create+an+installer

In order to compile the installer script, you'll need to download and install NSIS installer.

This program can be downloaded from here: http://nsis.sourceforge.net/

It shouldn't matter where you install this program.

The install script can be edited with any text editor, and is the file titled uDigInstallScript.nsi

This file and the other files contained in the Installer Tools.zip archive should all lie in the root of the uDIG install 
directory - that is to say, in the same folder that contains the "udig" directory.

To recompile the installer for a new version of uDig, you'll need to do the following:
1. create a fresh archive release of uDig; copy in the jre and gdal_data as a a subfolder to the udig directory
2. unzip your archive into the udig folder in this directory
    uDigInstallScript.nsi
    ...
    udig/ <-- extracted from your udig release
    udig/.eclipseproduct
    udig/configuration/
    udig/features/
    udig/gdal_data/
    udig/icons/
    udig/imageio-ext-imagereadmt-BSD-LICENSE.txt
    udig/imageio-ext-tiff-BSD-LICENSE.txt
    udig/ImageIO-License.txt
    udig/jre/
    udig/LICENSE.txt
    udig/plugins/
    udig/README.txt
    udig/sun-copyright.txt
    udig/udig.bat
    udig/udig_internal.exe
    udig/udig_internal.ini

3. Next, open up uDigInstallScript.nsi, and edit the parts that state the version of uDig. This includes the following lines: 44,45,50,55,125,163,218,270,303
   (Replace VersionXXXX with whatever version you are working with for example 1.2-M4)

4. use compiler to open the uDigInstallerScript.nsi file and it will compile the .exe in the same directory
7. Hit Test Installer to runt it

