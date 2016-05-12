'''
Created on 12.05.2016

@author: Michael Zimmermann
'''

import AppBusInvoker

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



result = AppBusInvoker.invoke(nodeInstanceID, serviceInstanceID, nodeTemplateID, interface, operation, params)

print(result)