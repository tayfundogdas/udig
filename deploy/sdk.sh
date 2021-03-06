#!/bin/bash
echo "Release"

# ignore mac resources when using tar,zip,etc...
#
export COPYFILE_DISABLE=true
source ./versions.sh

# Release win32 if available
if [ -f ${TARGET}/udig-${VERSION}-sdk.zip ] 
then
    echo "Releasing SDK"
    
    if [ ! -d ${BUILD}/sdk ] 
    then
       echo "Creating ${BUILD}/sdk"
       mkdir -p ${BUILD}/sdk
    fi
    
    if [ ! -f ${BUILD}/udig-${VERSION}-sdk.zip ]
    then
        echo "Building  ${BUILD}/udig-${VERSION}-sdk.zip ..."
        
        echo "Extracting ${TARGET}/udig-${VERSION}-sdk.zip"
        unzip -q -d ${BUILD}/sdk ${TARGET}/udig-${VERSION}-sdk.zip
        
        echo "Prepairing ${BUILD}/sdk"
        rm -rf ${BUILD}/sdk/udig_sdk/*.app
        rm ${BUILD}/sdk/udig_sdk/*.exe
        rm ${BUILD}/sdk/udig_sdk/*.ini
        rm ${BUILD}/sdk/udig_sdk/*.sh
        
        rm -rf ${BUILD}/sdk/udig_sdk/configuration
        
        rm ${BUILD}/sdk/udig_sdk/plugins/*swt*macosx*
        rm ${BUILD}/sdk/udig_sdk/plugins/*swt*win32*
        rm ${BUILD}/sdk/udig_sdk/plugins/*swt*linux*
        
        echo "Assemble ${BUILD}/udig-${VERSION}-sdk.zip "
        cd ${BUILD}/sdk
        zip -9 -r -q ../udig-${VERSION}-sdk.zip udig_sdk 
     else 
       echo "Already Exists ${BUILD}/udig-${VERSION}-sdk.zip"
     fi
fi
