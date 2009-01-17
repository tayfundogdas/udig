;uDig Null Soft Installer creation file.
;Written by Chris Holmes

;This is an example of a windows installer for uDig.  It was adapted from
;the geoserver nsi file.  There are a few things which I don't know enough
;about uDig to do right, so I will add some comments of suggestions and hints
;through out this file.  I spent a solid day getting everything going right
;for geoserver, and doing this took me less than an hour, and should hopefully
;be sufficient so you don't have to replicate my work.

;Building
;--------
;First thing you'll need is NSIS, the null soft installer.  I think the site
;is http://nsis.sourceforge.net.  You'll need a windows box to run the 
;installer.  It's easy to use, just install, and then run the 'compiler'.
;It compiles the fun little scripting language contained in this file
;to create the .exe file.  I used the modern ui stuff, as it seems to look
;nice and, well, modern.  
;After installing the nsis software what I did was unzip the eclipse rcp,
;unzip uDig on top of that, and copy this file (uDig.nsis) to the directory
;where I unzip eclipse, right next to the eclipse/ directory, where the 
;eclipse dir contains everything.  Then with the NSIS compiler open this 
;file and it will compile the .exe in the same directory.  Hit Test Installer
;to run it.  

;You could make an ant task to do this fairly easily, I just never got around
;to it.  Tomcat does it by having the nsis variable, their source trees have
;nsi files for reference (but I could never get them to work, as their build
;involves setting all these random variables
;For more info on this file read the Users Manual, and especially the
;Modern UI Readme.  And check out the other examples, though I imagine this
;one will be the best...

;--------------------------------
;Include Modern UI

  !include "MUI.nsh"

;--------------------------------
;General

  ;Name and file
  ;:TODO: Change this with each release of uDIG!
  Name "uDig 1.0.RC1"
  OutFile "udig1.0.RC1.exe"
  ;:TODO: End of changes required when upgrading installer to new version of uDIG.


  ;Default installation folder
  InstallDir "$PROGRAMFILES\uDig"
  
  ;Get installation folder from registry if available - This will check the registry to see if an install directory
  ;is present, and if so, replace the value in InstallDir with it.  If there is no value, the installer will fall
  ;back on InstallDir as the default install directory.
  InstallDirRegKey HKCU "Software\uDig" ""

;--------------------------------
;Variables

  Var MUI_TEMP
  Var STARTMENU_FOLDER

;--------------------------------
;User-defined macros to allow us to put a link to the uDig help website.
!macro CreateInternetShortcut FILENAME URL ICONFILE ICONINDEX
    WriteINIStr "${FILENAME}.url" "InternetShortcut" "URL" "${URL}"
    WriteINIStr "${FILENAME}.url" "InternetShortcut" "IconFile" "${ICONFILE}"
    WriteINIStr "${FILENAME}.url" "InternetShortcut" "IconIndex" "${ICONINDEX}"
!macroend

;--------------------------------
;Interface Settings


  ;Used your udig.ico.  All paths are relative to the location of _this_ file,
  ;which is why it needs to be right next to the eclipse folder. -ch
  
  !define MUI_ICON "eclipse\icons\32-uDigIcon.ico"
  ;I tried to use the same windows uninstaller I did, but NSIS doesn't seem
  ;to like icons of different sizes -ch

;  !define MUI_UNICON "eclipse\plugins\net.refractions.udig.ui_0.3.0\icons\udig.ico"

  ;!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\win-uninstall.ico"
  !define MUI_UNICON "eclipse\icons\32-uninstallIcon.ico"
  
  !define MUI_ABORTWARNING
  
  ;You can obviously change any of this text junk. -ch
  !define MUI_WELCOMEPAGE_TEXT "This wizard will guide you through the \
      installation of uDig \r\n \r\nNote that this is the first \
      attempt by the uDig project to create \
      a Windows executable installer.  \
      Please report any problems or suggestions for improvement to \
      udig-devel@lists.refractions.net. \r\n \r\n \
      Click Next to continue."
;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME
  ;custom page I made to detect java.  I have it repeat the java version that
  ;it found.  I like the functionality, unfortunately it looks ghetto, as I 
  ;just did a pop up box.  See my comments on the echoJava function -ch
  ;
  ;Chris included this, but we do not require a JDK of any sort, so it is commented
  ;out for now.
  ;Page custom echoJava
  
  ; ---------------------------------------------------------------------------
  ; At this point, we should also determine the location of the JRE, and what
  ; version we are dealing with.  We should also make sure that JAI and ImageIO
  ; are installed.
  ; ---------------------------------------------------------------------------

  ;A text file for the license here would be better.  And it probably should
  ;be your license text, as aren't you doing lgpl instead of the eclipse one?
  ;You should explain something here, have the license as users install it. -ch
  !insertmacro MUI_PAGE_LICENSE "LGPL.txt"
  !insertmacro MUI_PAGE_DIRECTORY

  ;Not sure about this stuff, some registery storing of preferences as to where
  ;you like the uDig start menu
  ;Start Menu Folder Page Configuration
  !define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU" 
  !define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\uDig" 
  !define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"
  
  !insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER
  
  !insertmacro MUI_PAGE_INSTFILES

  !insertmacro MUI_PAGE_FINISH
  
  !insertmacro MUI_UNPAGE_WELCOME
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  !insertmacro MUI_UNPAGE_FINISH

;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "uDig Section" SecuDig

  SetOutPath "$INSTDIR"
  
  ;This is where the files to add are.  You could change this to run in
  ;the eclipse folder, and name all the files and directories individually,
  ;which will then install them all directly in the uDig folder.  As it is
  ;uDig is installed in Program Files, with eclipse as a sub folder, and then
  ;it seems to build a bin/ directory in the uDig folder as well. -ch
  ;ADD YOUR OWN FILES HERE...
  File /r eclipse
    
  ;Store installation folderh
  WriteRegStr HKCU "Software\uDig" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    
    ;Create shortcuts
    
    ;You guys don't need this, as you just run the .exe.  I call geoserver with
    ;java directly, so the findJavaPath gets which one I should use.  Though
    ;actually I prefer this method because you can specify JAVA_HOME instead
    ;of changing your registry around. -ch
    ;Call findJavaPath
    ;Pop $2


    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    SetOutPath "$INSTDIR"

    ;Link for documentation
    !insertmacro CreateInternetShortcut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\uDig Documentation" \
        "http://udig.refractions.net/confluence/display/UDIGGuide/Home" \
        "$INSTDIR\eclipse\icons\32-uDigIcon.ico" 0

    ;Set specific out page for uDig
    ;SetOutPath "$INSTDIR\UDIG\eclipse"
    ;SetOutPath "$PROFILE\UDIG\workspace"

    ;Start-up, using the udig.exe file
    ;For some reason, uDig will NOT start if it doesn't have a parameter following -data.
    ;-noop does nothing and seems to be okay.
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\uDig.lnk" \
                   "$INSTDIR\eclipse\udig.exe" "-data $\"$APPDATA\uDig\$\" -noop" \
                   "$INSTDIR\eclipse\icons\32-uDigIcon.ico" 0 SW_SHOWNORMAL

    ;Set path back to normal
    SetOutPath "$INSTDIR"
    ;Commented out the stop, but it shows how you call with java.
    ;CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Stop uDig.lnk" \
    ;               "$2\bin\java.exe" '-jar stop.jar'\
    ;               "$INSTDIR\server\uDig\images\gs.ico" 0 SW_SHOWMINIMIZED
    ;link to unintall, you guys should come up with a better icon...

    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" \
                   "$INSTDIR\Uninstall.exe" "" \
                   "$INSTDIR\eclipse\icons\32-uninstallIcon.ico" 0 SW_SHOWNORMAL

  
  !insertmacro MUI_STARTMENU_WRITE_END

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_uDig ${LANG_ENGLISH} "uDig section"

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecuDig} $(DESC_uDig)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END


Function .onInit

   ClearErrors

FunctionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  ;ADD YOUR OWN FILES HERE...
  
  Delete "$INSTDIR\Uninstall.exe"
  ;RMDIR /r "$INSTDIR\bin"
  RMDIR /r "$INSTDIR\eclipse"

  RMDir "$INSTDIR"
  RMDir "$APPDATA\uDig"
  
  IfFileExists "$INSTDIR" 0 Removed
     MessageBox MB_YESNO|MB_ICONQUESTION \
          "Remove all files in your uDig directory, including workspace? (If you have anything you created that you want to keep, click No)" IDNO Removed
     Delete "$INSTDIR\*.*" ;
     RMDIR /r "$INSTDIR"
     Sleep 500
     IfFileExists "$INSTDIR" 0 Removed
        MessageBox MB_OK|MB_ICONEXCLAMATION \
            "Note: $INSTDIR could not be removed."


  Removed:

  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP
    
  Delete "$SMPROGRAMS\$MUI_TEMP\uDig.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\uDig Documentation.url"
  
  ;Delete empty start menu parent diretories
  StrCpy $MUI_TEMP "$SMPROGRAMS\$MUI_TEMP"
 
  startMenuDeleteLoop:
    RMDir $MUI_TEMP
    GetFullPathName $MUI_TEMP "$MUI_TEMP\.."
    
    IfErrors startMenuDeleteLoopDone
  
    StrCmp $MUI_TEMP $SMPROGRAMS startMenuDeleteLoopDone startMenuDeleteLoop
  startMenuDeleteLoopDone:

  DeleteRegKey /ifempty HKCU "Software\uDig"

SectionEnd