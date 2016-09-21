#!/usr/bin/python
'''
Created on Aug 18, 2016

@author: acaproni
'''

from os.path import exists, sep, join
from os import makedirs
import argparse
from shutil import rmtree, copyfile
from iasFindFile import iasFindFile

def writeLicenseFile(folder):
    """
    Create a file with the license in the passed folder
    """
    licenseFile = iasFindFile("LPGPv3License.txt")
    copyfile(licenseFile,join(folder,"LGPLv3.txt"))

def createModule(name):
    """
    Create a IAS empty module.
    
    The text of the LGPL license will be copied in the root of the module.
    
    @param name: The name (full path) of the module to create.
    
    @return: 0 in case of success; -1 otherwise
    """
    
    # Check if the module 
    if not exists(name):
        print "Creating module",name
        makedirs(name)
        writeLicenseFile(name)
        makedirs(name+sep+"src")
        makedirs(name+sep+"test")
        makedirs(name+sep+"bin")
        makedirs(name+sep+"lib")
        makedirs(name+sep+"extTools")
        makedirs(name+sep+"classes")
        makedirs(name+sep+"config")
        
        return 0
    else:
        print
        print name,"already exists!!!"
        print "Use -e|--erase to remove before creating the module."
        print
        return -1

def removeExistingModule(name):
    '''
    Remove an existing module
    
    @param name: The full path name of the module to remove
    '''
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
        removeExistingModule(args.moduleName)
    createModule(args.moduleName)
    