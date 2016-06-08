'''
Created on 12.05.2016

@author: Michael Zimmermann
'''

import AppBusPythonInvoker

#
nodeInstanceID = 1
#or
serviceInstanceID = None
nodeTemplateID = None
#
interface = "PythonTestInterface"
operation = "helloWorld"
params = {'name': 'Michael'}
#params = {}



result = AppBusPythonInvoker.invoke(nodeInstanceID, serviceInstanceID, nodeTemplateID, interface, operation, params)

print("RESULT:")
print(result)