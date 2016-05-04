package appbus.stub.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import appbus.setup.servlet.AppBusSetupServlet;

/**
 * 
 * Client for using the JSON/HTTP API of the OpenTOSCA Application Bus component.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public abstract class AppBusClient {

	static final Integer APPINVOKER_PORT = 8083;
	static final String APPINVOKER_URL_SUFFIX = "/OTABService/v1/appInvoker";

	// just for logging
	private final static String STUB = "AppBusStub: ";

	static URL appInvokerURL;

	/**
	 * @param nodeTemplateID
	 *            the method belongs to
	 * @param interfaceName
	 *            name of the interface the method is located at
	 * @param operation
	 *            name of the method that should be invoked
	 * @param params
	 *            parameters
	 * @return result of invocation
	 * @throws IOException
	 */
	public static Object invoke(String nodeTemplateID, String interfaceName, String operation,
			LinkedHashMap<String, Object> params) throws IOException {

		URL containerURL = AppBusSetupServlet.getContainerURL();

		Object output = null;

		if (containerURL != null) {

			System.out.println(STUB + "Container URL: " + containerURL);

			appInvokerURL = new URL(containerURL.getProtocol(), containerURL.getHost(), APPINVOKER_PORT,
					APPINVOKER_URL_SUFFIX);

			System.out.println(STUB + "AppInvoker JSON-HTTP-API URL: " + appInvokerURL);

			output = doPost(nodeTemplateID, interfaceName, operation, params);

		} else {
			System.out.println(STUB + "No Container URL specified! Can't invoke operation: " + operation + " of: "
					+ nodeTemplateID);
		}

		return output;
	}

	/**
	 * 
	 * Sends a post request to the AppInvoker JsonHTTP API. For invoking a
	 * method.
	 * 
	 * @param nodeTemplateID
	 *            the method belongs to
	 * @param interfaceName
	 *            name of the interface the method is located at
	 * @param operation
	 *            name of the method that should be invoked
	 * @param params
	 *            parameters
	 * @return result of invocation
	 */
	public static Object doPost(String nodeTemplateID, String interfaceName, String operation,
			LinkedHashMap<String, Object> params) {

		Integer selfServiceInstanceID = AppBusSetupServlet.getSelfServiceInstanceID();
		Integer hostServiceInstanceID = AppBusSetupServlet.getHostServiceInstanceID();
		Integer selfNodeInstanceID = AppBusSetupServlet.getSelfNodeInstanceID();
		Integer hostNodeInstanceID = AppBusSetupServlet.getHostNodeInstanceID();

		Object output = null;

		if (hostNodeInstanceID != null || selfServiceInstanceID != null || hostServiceInstanceID != null) {

			System.out.println(STUB + "Invoking operation: " + operation + " of: " + nodeTemplateID);

			try {

				System.out.println(STUB + "Invocations call URL: " + appInvokerURL.toString());

				HttpURLConnection httpConnection = (HttpURLConnection) appInvokerURL.openConnection();
				httpConnection.setDoOutput(true);
				httpConnection.setRequestMethod("POST");
				httpConnection.setRequestProperty("Content-Type", "application/json");

				// JSON body creation
				JSONObject infoJSON = new JSONObject();
				infoJSON.put("interface", interfaceName);
				infoJSON.put("operation", operation);

				if (hostNodeInstanceID != null) {
					infoJSON.put("nodeInstanceID", hostNodeInstanceID);
					System.out.println(STUB + "HostNodeInstanceID: " + hostNodeInstanceID);
				} else if (hostServiceInstanceID != null) {
					infoJSON.put("serviceInstanceID", hostServiceInstanceID);
					infoJSON.put("nodeTemplateID", nodeTemplateID);
					System.out.println(STUB + "HostServiceInstanceID: " + hostServiceInstanceID);
				} else if (selfServiceInstanceID != null) {
					infoJSON.put("serviceInstanceID", selfServiceInstanceID);
					infoJSON.put("nodeTemplateID", nodeTemplateID);
					System.out.println(STUB + "SelfServiceInstanceID: " + selfServiceInstanceID);
				}

				LinkedHashMap<String, Object> finalJSON = new LinkedHashMap<String, Object>();
				finalJSON.put("invocation-information", infoJSON);
				finalJSON.put("params", params);

				String finalJSONString = JSONValue.toJSONString(finalJSON);

				System.out.println(STUB + "JSON Body: " + finalJSONString);

				OutputStream outputStream = httpConnection.getOutputStream();
				outputStream.write(finalJSONString.getBytes());
				outputStream.flush();

				if (httpConnection.getResponseCode() != 202) {
					throw new RuntimeException("Failed : HTTP error code : " + httpConnection.getResponseCode());
				}

				String queueLocation = httpConnection.getHeaderField("Location");

				output = pollQueue(new URL(queueLocation));

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (hostNodeInstanceID == null)
				System.out.println(STUB + "No HostNodeInstanceID specified!");
			if (selfServiceInstanceID == null)
				System.out.println(STUB + "No SelfServiceInstanceID specified!");
			if (hostServiceInstanceID == null)
				System.out.println(STUB + "No HostServiceInstanceID specified!");
			System.out.println(STUB + "Can't invoke method: " + operation + " of: " + nodeTemplateID);
		}
		return output;
	}

	/**
	 * 
	 * Sends a get request to the AppInvoker JsonHTTP API. For polling if the
	 * invocation has finished already.
	 * 
	 * @param queueLocation
	 * @return result of invocation
	 */
	public static Object pollQueue(URL queueURL) {

		Object output = null;

		try {

			System.out.println(STUB + "Polling Queue: " + queueURL.toString());

			HttpURLConnection httpConnection = null;
			int responseCode = 200;

			while (responseCode == 200) {

				httpConnection = (HttpURLConnection) queueURL.openConnection();
				httpConnection.setInstanceFollowRedirects(false);
				httpConnection.setRequestMethod("GET");
				httpConnection.setRequestProperty("Accept", "application/json");

				responseCode = httpConnection.getResponseCode();
				System.out.println(STUB + "ResponseCode: " + responseCode);

				BufferedReader responseBuffer = new BufferedReader(
						new InputStreamReader((httpConnection.getInputStream())));

				System.out.println(STUB + "Output from Server: ");

				String temp;
				while ((temp = responseBuffer.readLine()) != null) {
					System.out.println(temp);
				}

				responseBuffer.close();

				if (responseCode == 200) {
					System.out.println(STUB + "Client waiting for next polling...");
					try {
						// polling every 5 sec
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			if (responseCode == 303) {

				String resultLocation = httpConnection.getHeaderField("Location");
				output = getResult(new URL(resultLocation));

			} else {
				System.out.println(
						STUB + "HTTP GET Request Failed with Error code : " + httpConnection.getResponseCode());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;

	}

	/**
	 * 
	 * Sends a get request to the AppInvoker JsonHTTP API. For requesting the
	 * result of the invocation.
	 * 
	 * @param resultLocation
	 * @return result of invocation
	 */
	public static Object getResult(URL resultURL) {

		Object result = null;
		StringBuffer response = new StringBuffer();

		try {

			System.out.println(STUB + "Requesting Result: " + resultURL);

			HttpURLConnection httpConnection = (HttpURLConnection) resultURL.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("Accept", "application/json");

			int responseCode = httpConnection.getResponseCode();
			System.out.println(STUB + "ResponseCode: " + responseCode);

			if (responseCode == 200) {

				BufferedReader responseBuffer = new BufferedReader(
						new InputStreamReader((httpConnection.getInputStream())));

				System.out.println(STUB + "Output from Server: ");

				String temp;
				while ((temp = responseBuffer.readLine()) != null) {
					System.out.println(temp);
					response.append(temp);
				}

				responseBuffer.close();

			} else {
				System.out.println(STUB + "HTTP GET Request Failed with Error code : " + responseCode);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject obj = (JSONObject) JSONValue.parse(response.toString());
		result = obj.get("result");

		return result;
	}

}
