#!/usr/bin/python
'''
Created on Aug 18, 2016

@author: acaproni
'''

from os.path import exists, join
from os import makedirs
import argparse
from shutil import rmtree, copyfile
from iasFindFile import iasFindFile

class IASModuleSupport(object):
    """
    A class providing useful method for dealing with IAS
    modules including IAS_ROOT)
    
    @raise IOError: if the folder is not writable or the license file is not found
    @raise ValueError: if the passed folder name is None or Empty
    """
    @staticmethod
    def writeLicenseFile(rootOfModule):
        """
        Create a file with the license in the passed rootOfModule
        
        @param The root folder of the module
        """
        if not rootOfModule:
            raise ValueError("The root of the module can't be None nor empty")
        licenseFile = iasFindFile("LPGPv3License.txt","config")
        copyfile(licenseFile,join(rootOfModule,"LGPLv3.txt"))

    @staticmethod
    def createModule(name):
        """
        Create a IAS empty module.
        
        The text of the LGPL license will be copied in the root of the module.
        
        @param name: The name (full path) of the module to create.
        
        @return: 0 in case of success; -1 otherwise
        @see: self.writeLicenseFile
        """
        if not name:
            raise ValueError("The name of the module can't be None nor empty")
        # Read the list of folders to create from the template
        listOfFoldersFileName = iasFindFile("FoldersOfAModule.template","config")
        with open(listOfFoldersFileName) as f:
            folders = f.readlines()
        # Check if the module 
        if not exists(name):
            print "Creating module",name
            makedirs(name)
            IASModuleSupport.writeLicenseFile(name)
            for folder in folders:
                # Remove comments i.e. #...
                parts = folder.partition('#')
                folderName=parts[0].strip()
                if folderName:
                    makedirs(join(name,folderName))
            
            return 0
        else:
            raise IOError(name+"already exists!!!")

    @staticmethod
    def removeExistingModule(name):
        '''
        Remove an existing module
        
        @param name: The full path name of the module to remove
        '''
        if not name:
            raise ValueError("The name of the module can't be None nor empty")
        print "Removing moldule",name
        if exists(name):
            rmtree(name) 

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Creates a module for the Integrated Alarm System.')
    parser.add_argument(
                        '-e',
                        '--erase',
                        help='Erase the module if it already exists',
                        action='store_true',
                        default=False)
    parser.add_argument('moduleName', help='The name of the IAS module to create')
    args = parser.parse_args()
    
    if args.erase:
        try:
            IASModuleSupport.removeExistingModule(args.moduleName)
        except Exception, e:
            print "Error deleting the module: ",str(e)
            exit(-1)
    try:
        IASModuleSupport.createModule(args.moduleName)
    except Exception, e:
        print "Error creating the module: ",str(e)
        exit(-1)
    