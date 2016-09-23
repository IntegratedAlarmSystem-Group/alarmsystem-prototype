'''
Created on Sep 22, 2016

@author: acaproni
'''

from os import environ, getcwd

class CommonDefs(object):
    """
    Common definitions and useful methods.
    """
    def __init__(self):
        self.__iasRoot=environ['IAS_ROOT']
        
    def getIASRoot(self):
        '''
        @return the IAS_ROOT
        '''
        return self.__iasRoot
    
    def getAISSearchPaths(self):
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
        return (getcwd(),self.__iasRoot)
    

if __name__ == '__main__':
    pass