#!/usr/bin/python
'''
Created on Sep 26, 2016

@author: acaproni
'''

import argparse
from IASTools.CommonDefs import CommonDefs
from subprocess import call

if __name__ == '__main__':
    """
    Run a java or scala tool.
    """
    parser = argparse.ArgumentParser(description='Run a java or scala program.')
    parser.add_argument(
                        '-l',
                        '--language',
                        help='The programming language: one between scala (or shortly s) and java (or j)',
                        action='store',
                        choices=['java', 'j', 'scala','s'],
                        required=True)
    parser.add_argument('className', help='The name of the class to run the program')
    parser.add_argument('params', metavar='param', nargs='*',
                    help='Command line parameters')
    args = parser.parse_args()
    
    # Build the command line
    if args.language=='s' or args.language=='scala':
        cmd=['scala']
    else:
        cmd=['java']
    
    #add the classpath
    cmd.append("-cp")
    cmd.append(CommonDefs.buildClasspath())
    
    # Add the class
    cmd.append(args.className)
    
    # Finally the command line parameters
    if len(args.params)>0:
        cmd.extend(args.params)
        
    call(cmd)
    
    