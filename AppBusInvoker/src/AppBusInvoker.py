'''
Created on 11.05.2016

@author: Michael Zimmermann
'''

import requests
import time
import json


def invoke(nodeInstanceID, serviceInstanceID, nodeTemplateID, interface, operation, params):    
    
    print("Invocation...")
    
    
    print("nodeInstanceID: ", nodeInstanceID, "serviceInstanceID: ", serviceInstanceID, "nodeTemplateID: ", nodeTemplateID, "interface: ", interface, "operation: ", operation)
    
    url="http://localhost:8083/OTABService/v1/appInvoker"
    
    if nodeInstanceID is not None:
        invocationInformation = {'interface': interface, 'operation': operation, 'nodeInstanceID': nodeInstanceID}
     
    elif serviceInstanceID is not None:
        invocationInformation = {'interface': interface, 'operation': operation, 'serviceInstanceID': serviceInstanceID, 'nodeTemplateID': nodeTemplateID}  
        
    
    payload = {'invocation-information': invocationInformation, 'params': params}

    # send invoke request
    r = requests.post(url, json=payload)
    
    statusCode = r.status_code
    
    print("Status Code: ", statusCode)    
    
    if statusCode != 202:
        print("Something went wrong!")
        return;
    
    locationHeader=r.headers['Location']
    
    print("Location Header: ", locationHeader)
            
    while statusCode != 303:
        
        print("polling...")
        
        # polling if invocation finished
        r = requests.get(locationHeader, allow_redirects=False)
    
        statusCode=r.status_code
    
        print("Status Code: ", statusCode)  
        
        print("result: ", r.text)
        
        if statusCode!=303:
            
            time.sleep(5)
        
    
    locationHeader=r.headers['Location']
    
    print("requesting the result...")
    
    # requesting the result
    r = requests.get(locationHeader)
    
    statusCode=r.status_code
    
    print("Status Code: ", statusCode)  
    
    resultText = r.text
    
    print("Result text: ", resultText)
    
    jsonResult = json.loads(resultText)
    
    result = jsonResult["result"]    
    
    return result;

