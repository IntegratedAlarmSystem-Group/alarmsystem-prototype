'''
Created on Sep 22, 2016

@author: acaproni
'''

from os import environ, getcwd, walk, path
import FileSupport

class CommonDefs(object):
    
    # Classpath separator for jars
    __classPathSeparator=":"
    
    @classmethod
    def buildClasspath(cls):
        """
        Build the class path by reading the jars from the
        IAS hierarchy of folders
        
        @return: A string with the jars in the classpath
        """
        classpath=""
        FileSupport.FileSupport.getIASFolders()
        for folder in FileSupport.FileSupport.getIASSearchFolders('lib'):
            for root, subFolders, files in walk(folder):
                for file in files:
                    if (file.lower().endswith('.jar')):
                        filePath=path.join(root,file)
                        if classpath:
                            classpath=classpath+cls.__classPathSeparator
                        classpath=classpath+filePath
        return classpath
                            
                        