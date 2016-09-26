'''
Created on Sep 22, 2016

@author: acaproni
'''

from os import environ, getcwd, walk, path

class CommonDefs(object):
    
    #IAS root from the environment
    __iasRoot=environ['IAS_ROOT']
    
    # Classpath separator for jars
    __classPathSeparator=":"
    
    @classmethod
    def getIASRoot(cls):
        '''
        @return the IAS_ROOT
        '''
        return cls.__iasRoot
    
    @classmethod
    def getAISSearchPaths(cls):
        '''
        Return the hierarchy of IAS folders.
        At the present it only contains the IAS root and the
        current module but in future can be expanded to contain 
        one or more integration folders, if needed.
        
        The tuple returned is ordered: i.e first the current module, 
        then the root (again it could be useful in future if 
        an integration area will be added).
        
         @return A ordered tuple with the hierarchy of IAS folders
                 (root of current module,IAS_ROOT)
        '''
        return (getcwd(),cls.getIASRoot())
    
    @classmethod
    def buildClasspath(cls):
        """
        Build the class path by reading the jars from the
        IAS hierarchy of folders
        
        @return: A string with the jars in the classpath
        """
        classpath=""
        for folder in cls.getAISSearchPaths():
            for root, subFolders, files in walk(folder):
                for file in files:
                    if (file.lower().endswith('.jar')):
                        print file, root
                        filePath=path.join(root,file)
                        if classpath:
                            classpath=classpath+cls.__classPathSeparator
                        classpath=classpath+filePath
        return classpath
                            
                        