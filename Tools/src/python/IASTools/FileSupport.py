'''
Created on Sep 23, 2016

@author: acaproni
'''
from os import environ,getcwd, walk, path

class FileSupport(object):
    '''
    Support method for IAS files
    '''
    # Possible types of files recognized by this tool
    #
    # iasFileType matches with the folder names of a module
    # to ensure a valid value is given here
    iasFileType = ("lib", "bin","config","src")  
    

    def __init__(self, fileName,fileType=None):
        '''
        Constructor
        
        @param filename: The mae of the file
        @param fileType: The type of the file (one in iasFileType) or None
        '''
        self.fileName=fileName
        self.fileType=fileType
    
    def recursivelyLookForFile(self,folder):
        """
        Search recursively for a file in the given folder
        
        @param folder: The folder to search for the file
        @param name: the name of the file
        @return: the full name of the file if it exists,
                 None otherwise
        """
        for root, subFolders, files in walk(folder):
            for filePath in files:
                if (filePath == self.fileName):
                    return path.join(folder,filePath)
        return None
    
    def findFile(self):
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
        iasRoot=environ['IAS_ROOT']
        if self.fileType:
            fileType = self.fileType.lower()
            if not fileType in FileSupport.iasFileType:
                raise ValueError("Unrecognized fileType '"+fileType+"' not in "+str(FileSupport.iasFileType))
            folders = (fileType,)
        else:
            # No folder passed: search in all the folders
            folders = FileSupport.iasFileType
        
        for folder in folders:
            # Search in the current folder if its terminates with folder
            # i.e. search in lib if the name of the current folder terminates with lib
            currentFolder = getcwd()
            if currentFolder.endswith(folder):
                filePath = self.recursivelyLookForFile(currentFolder)
                if filePath is not None:
                    return filePath
                    
            # Assume to be in the root of a folder 
            ## i.e. search in ./lib
            folderToSearch=path.join(currentFolder,folder)
            if path.exists(folderToSearch) and path.isdir(folderToSearch):
                filePath = self.recursivelyLookForFile(folderToSearch)
                if filePath is not None:
                    return filePath
            # Assume to be in a  module folder and look for the file in the passed folder
            # for example current folder is ../src, search in ../lib
            folderToSearch=path.join(currentFolder,"..",folder)
            if path.exists(folderToSearch) and path.isdir(folderToSearch):
                filePath = self.recursivelyLookForFile(folderToSearch)
                if filePath is not None:
                    return filePath
            # Finally search in IAS_ROOT
            folderToSearch=path.join(iasRoot,folder)
            if path.exists(folderToSearch) and path.isdir(folderToSearch):
                filePath = self.recursivelyLookForFile(folderToSearch)
                if filePath is not None:
                    return filePath
        # Bad luck!
        raise IOError(self.fileName+" not found")
    