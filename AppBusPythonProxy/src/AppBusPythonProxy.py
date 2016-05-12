'''
Created on 12.05.2016

@author: Michael Zimmermann
'''

from http.server import BaseHTTPRequestHandler, HTTPServer
import os
 
# HTTPRequestHandler class
class testHTTPServer_RequestHandler(BaseHTTPRequestHandler):
 
    # POST
    def do_GET(self):
        
        
        path = self.path
        
        print("Path: "+ path)
        
        if path == "/OTABProxy/v1/appInvoker":            
            print("yes")
        
        else:
            print("no")
            
        
        os.system("PrintHelloWorldScript.py")
        
         
        # Send response status code
        self.send_response(202)
 
        # Send headers
        self.send_header('Location','text/html')
        self.end_headers()
 
        # Send message back to client
        message = "nothing here"
        # Write content as utf-8 data
        self.wfile.write(bytes(message, "utf8"))
        return
 
def run():
    print('starting server...')
 
    # Server settings
    # Choose port 8080, for port 80, which is normally used for a http server, you need root access
    server_address = ('127.0.0.1', 8081)
    httpd = HTTPServer(server_address, testHTTPServer_RequestHandler)
    print('running server...')
    httpd.serve_forever()
 
 
run()