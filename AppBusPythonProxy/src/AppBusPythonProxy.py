'''
Created on 12.05.2016

@author: Michael Zimmermann
'''

from http.server import BaseHTTPRequestHandler, HTTPServer
import os
import json
import itertools
import re
import importlib
from difflib import Match


port = 8081
invoker_path = "/OTABProxy/v1/appInvoker"

queue_regex = "/activeRequests/([0-9]*)$"
response_regex = "/activeRequests/([0-9]*)/response$"

cont = itertools.count(1)
queue = {}
results = {}

 
# HTTPRequestHandler class
class testHTTPServer_RequestHandler(BaseHTTPRequestHandler):
 
    # POST
    def do_POST(self):
        
        
        path = self.path
        
        print("Path: " + path)
        
        if path == invoker_path:            
            
            content_len = int(self.headers['Content-Length'])
            post_body = self.rfile.read(content_len)
            json_body = json.loads(post_body.decode("utf-8"))
            
            print("body: ", json_body)
            
            
            ii_json = json_body["invocation-information"] 
            script = ii_json["class"] 
            operation = ii_json["operation"]
            
            print("Script: ", script, "Operation: ", operation)
            
            params_json = json_body["params"] 
            
            params = []
            
            for key, value in params_json.items():
                
                print("Key: ", key)
                print("Value: ", value)
                              
                params.append(value)
                
            print("Params: ", params)
            
            
            invoke_operation = getattr(importlib.import_module(script), operation)

            result = invoke_operation(*params)
            
            # to invoke pure script files without functions
            # result = os.system(script)
            
            print("RESULT: ", result)

   
            nextID = next(cont)
            
            results[nextID] = result
                           
            queue[nextID] = True       
            
            location = self.address_string() + ":" + str(port) + invoker_path + "/activeRequests/" + str(nextID)
            
            # send response code
            self.send_response(202)
            # Send headers
            self.send_header("Location", location)
            self.send_header("Content-Length", 0)
            self.end_headers()
            


        else:
            print("Not supported!")
            # Send response status code
            self.send_response(400)  
        
    
    
        print ("do_POST end")

    
    # GET
    def do_GET(self):
        
        path = self.path
        
             
        print("Path: " + path)
        
        
        match = re.search(queue_regex, path);
                
        if match is not None:
            
            print("Polling...")
            
            matchString = match.group()
            requestID = (int(re.search(r'\d+', matchString).group()))
            
            print("Current queue: ", queue)
            
            print("RequestID: ", requestID)
            
            if requestID in queue:                
                print("Id ", requestID , "is known.")
                
                
                if queue[requestID]:
                    print("Invocation finished!")
   
                    #location = self.address_string() + ":" + str(port) + invoker_path + "/activeRequests/" + str(requestID) + "/response"
                    location2 = invoker_path + "/activeRequests/" + str(requestID) + "/response"
                    
                    #print(location)
                    print(location2)
      
                    self.send_response(303)                                     
                    self.send_header("Location", location2)
                    self.send_header("Content-Length", 0)
                    self.end_headers()      
                   
                                       
                                                    
                else:
                    print("Invocation not finished yet!") 
                    answer = {'status': 'PENDING'}
                    self.wfile.write(bytes(json.dumps(answer), "utf-8"))
                    self.send_response(200) 
                    
                
            else:
                
                print("Id ", requestID , "is unknown.")
                self.send_response(404) 
                      
                   
        else:
                        
            match = re.search(response_regex, path)
            
            if match is not None:
                
                matchString = match.group()
       
                requestID = (int(re.search(r'\d+', matchString).group()))
            
                print("Current queue: ", queue)
            
                print("RequestID: ", requestID)
            
                print("Requesting the result...")
            
                if requestID in results:
                    
                    print("Returning results...")
                    
                    result = results.get(requestID)
                    
                    
                    print("Result: ", result)
                    
                    answer = {'result': result}
        
                
                    self.send_response(200) 
                    self.send_header('Content-Type', "application/json")
                    self.end_headers()
                    self.wfile.write(bytes(json.dumps(answer), "utf-8"))
                    
                
                elif requestID not in queue:  
                    
                    print("Id ", requestID , "is unknown.")
                    self.send_response(404) 
                    
                else:
                    
                    print("Error while invoking specified method.")
                    self.send_response(404) 
       
     
        print ("do_GET end")

def run():
    print('starting server...')
         
    # Server settings
    server_address = ('127.0.0.1', port)
    httpd = HTTPServer(server_address, testHTTPServer_RequestHandler)
    print('running server...')
    httpd.serve_forever()

 
run()
