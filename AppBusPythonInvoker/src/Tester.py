'''
Created on 12.05.2016

@author: Michael Zimmermann
'''

import AppBusPythonInvoker

#
nodeInstanceID = 4
#or
serviceInstanceID = None
nodeTemplateID = None
#
interface = "NumberManipulatorApplicationInterface"
operation = "reset"
#params = {'name': 'Michael'}
params = {}



result = AppBusPythonInvoker.invoke(nodeInstanceID, serviceInstanceID, nodeTemplateID, interface, operation, params)

print(result)