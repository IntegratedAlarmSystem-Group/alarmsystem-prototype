#!/usr/bin/python
'''
Created on Sep 21, 2016

@author: acaproni
'''
import argparse
import os

# Possible types of files recognized by this tool
#
# iasFileType matches with the folder names of a module
# to ensure a valid value is given here
iasFileType = ("lib", "bin","config","src")

def recursivelyLookForFile(folder,name):
    """
    Search recursively for a file in a folder
    
    @param folder: The folder to search for the file
    @param name: the name of the file
    @return: the full name of the file if it exists,
             None otherwise
    """
    for root, subFolders, files in os.walk(folder):
        for filePath in files:
            if (filePath == name):
                return os.path.join(folder,filePath)
    return None

def iasFindFile(fileName,fileType=None):
    """
    Looks for a file named fileName in the hierarchy of IAS folders:
    * current folder (only if it ends with src for example)
    * module folders (lib for example)
    * parent module folders (../lib for example)
    * $IAS_ROOT folders
    
    The fileType tells where the file must be looked for and improves
    the response time of the script.
    
    
    @param fileName: The name of the file to look for
    @param fileType: the type (iasFileType) of file to look for (example BINARY,LIB...
    @return: if the file is found The full path name of the file
             otherwise throws a IOError exception
    """
    iasRoot=os.environ['IAS_ROOT']
    if fileType:
        fileType = fileType.lower()
        if not fileType in iasFileType:
            raise ValueError("Unrecognized fileType '"+fileType+"' not in "+str(iasFileType))
        folders = (fileType,)
    else:
        # No folder passed: search in all the folders
        folders = iasFileType
    
    for folder in folders:
        # Search in the current folder if its terminates with folder
        # i.e. search in lib if the name of the current folder terminates with lib
        currentFolder = os.getcwd()
        if currentFolder.endswith(folder):
            filePath = recursivelyLookForFile(currentFolder,fileName)
            if filePath is not None:
                return filePath
                
        # Assume to be in the root of a folder 
        ## i.e. search in ./lib
        folderToSearch=os.path.join(currentFolder,folder)
        if os.path.exists(folderToSearch) and os.path.isdir(folderToSearch):
            filePath = recursivelyLookForFile(folderToSearch,fileName)
            if filePath is not None:
                return filePath
        # Assume to be in a  module folder and look for the file in the passed folder
        # for example current folder is ../src, search in ../lib
        folderToSearch=os.path.join(currentFolder,"..",folder)
        if os.path.exists(folderToSearch) and os.path.isdir(folderToSearch):
            filePath = recursivelyLookForFile(folderToSearch,fileName)
            if filePath is not None:
                return filePath
        # Finally search in IAS_ROOT
        folderToSearch=os.path.join(iasRoot,folder)
        if os.path.exists(folderToSearch) and os.path.isdir(folderToSearch):
            filePath = recursivelyLookForFile(folderToSearch,fileName)
            if filePath is not None:
                return filePath
    # Bad luck!
    raise IOError(fileName+" not found")

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Search for a file in the hierarchy of IAS folders.')
    parser.add_argument(
        '-t',
        '--type',
        help='The type of files to search for: '+str(iasFileType),
        dest="fileType",
        action='store',
        default=None)
    parser.add_argument(
        dest='fileName', 
        help='The name of the file to search for')
    args = parser.parse_args()
    
    try:
        if not args.fileType is None:
            filePath=iasFindFile(args.fileName, args.fileType)
        else:
            filePath=iasFindFile(args.fileName)
        print filePath
    except IOError as e:
        print "File not found"
