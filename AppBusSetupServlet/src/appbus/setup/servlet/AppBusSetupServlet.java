package appbus.setup.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * HttpServlet for setting up a deployed applications with selfInstanceID,
 * hostInstanceID and containerURL.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
@WebServlet(value = "/OTABSetup/v1/*", loadOnStartup = 1)
public class AppBusSetupServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	// just for logging
	private final static String SETUP = "AppBusSetup: ";

	private static Integer hostNodeInstanceID = null;
	private static Integer selfNodeInstanceID = null;
	private static Integer selfServiceInstanceID = null;
	private static Integer hostServiceInstanceID = null;
	private static URL containerURL = null;

	// Example:
	// http://localhost:8080/OTABSetup/v1/setup
	// body (application/x-www-form-urlencoded):
	// container-url=http://localhost:1337&self-service-instance-id=5&host-service-instance-id=1

	private final static String PATH_INFO = "/setup";
	private final static String SERVICE_INSTANCE_ID_SELF_KEY = "self-service-instance-id";
	private final static String SERVICE_INSTANCE_ID_HOST_KEY = "host-service-instance-id";
	private final static String NODE_INSTANCE_ID_SELF_KEY = "self-node-instance-id";
	private final static String NODE_INSTANCE_ID_HOST_KEY = "host-node-instance-id";
	private final static String CONAINTER_URL_KEY = "container-url";

	/**
	 * 
	 * For setting up with selfInstanceID, hostInstanceID and containerURL.
	 * Example call would be:
	 * <tt>http://localhost:8080/OTABSetup/v1/setup</tt> with
	 * <tt>application/x-www-form-urlencoded</tt> body:
	 * <tt>container-url=http://localhost:1337&self-service-instance-id=5&host-service-instance-id=1</tt>
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println(SETUP + "POST handling: Setting up Application");

		PrintWriter out = response.getWriter();

		String pathInfo = request.getPathInfo();
		System.out.println(SETUP + "Path info: " + pathInfo);

		String queryString = request.getQueryString();
		System.out.println(SETUP + "Query String: " + queryString);

		if (pathInfo.equals(PATH_INFO)) {

			Map<String, String[]> paramsMap = request.getParameterMap();

			if (paramsMap.containsKey(CONAINTER_URL_KEY)) {
				try {
					containerURL = new URL(paramsMap.get(CONAINTER_URL_KEY)[0]);
					System.out.println(SETUP + "Container-URL set : " + containerURL);
				} catch (MalformedURLException e) {
					System.out.println(SETUP + "Can't read Container-URL.");
					response.setStatus(400);
					response.resetBuffer();
					e.printStackTrace();
					out.println(SETUP + "Can't read Container-URL.");
					return;
				}
			} else {
				System.out.println(SETUP + "No Container-URL specified.");
			}

			if (paramsMap.containsKey(SERVICE_INSTANCE_ID_SELF_KEY)) {
				try {
					selfServiceInstanceID = Integer.parseInt(paramsMap.get(SERVICE_INSTANCE_ID_SELF_KEY)[0]);
					System.out.println(SETUP + "Self-Service-InstanceID set : " + selfServiceInstanceID);

				} catch (NumberFormatException e) {
					System.out.println(SETUP + "Can't read Self-Service-InstanceID.");
					response.setStatus(400);
					response.resetBuffer();
					e.printStackTrace();
					out.println(SETUP + "Can't read Self-Service-InstanceID.");
					return;
				}
			} else {
				System.out.println(SETUP + "No Self-Service-InstanceID specified.");
			}

			if (paramsMap.containsKey(SERVICE_INSTANCE_ID_HOST_KEY)) {
				try {
					hostServiceInstanceID = Integer.parseInt(paramsMap.get(SERVICE_INSTANCE_ID_HOST_KEY)[0]);
					System.out.println(SETUP + "Host-Service-InstanceID set : " + hostServiceInstanceID);

				} catch (NumberFormatException e) {
					System.out.println(SETUP + "Can't read Host-Service-InstanceID.");
					response.setStatus(400);
					response.resetBuffer();
					e.printStackTrace();
					out.println(SETUP + "Can't read Host-Service-InstanceID.");
					return;
				}
			} else {
				System.out.println(SETUP + "No Host-Service-InstanceID specified.");
			}

			if (paramsMap.containsKey(NODE_INSTANCE_ID_HOST_KEY)) {
				try {
					hostNodeInstanceID = Integer.parseInt(paramsMap.get(NODE_INSTANCE_ID_HOST_KEY)[0]);
					System.out.println(SETUP + "Host-Node-InstanceID set : " + hostNodeInstanceID);

				} catch (NumberFormatException e) {
					System.out.println(SETUP + "Can't read Host-Node-InstanceID.");
					response.setStatus(400);
					response.resetBuffer();
					e.printStackTrace();
					out.println(SETUP + "Can't read Host-Node-InstanceID.");
					return;
				}
			} else {
				System.out.println(SETUP + "No Host-Node-InstanceID specified.");
			}

			if (paramsMap.containsKey(NODE_INSTANCE_ID_SELF_KEY)) {
				try {
					selfNodeInstanceID = Integer.parseInt(paramsMap.get(NODE_INSTANCE_ID_SELF_KEY)[0]);
					System.out.println(SETUP + "Self-Node-InstanceID set : " + selfNodeInstanceID);

				} catch (NumberFormatException e) {
					System.out.println(SETUP + "Can't read Self-Node-InstanceID.");
					response.setStatus(400);
					response.resetBuffer();
					e.printStackTrace();
					out.println(SETUP + "Can't read Self-Node-InstanceID.");
					return;
				}
			} else {
				System.out.println(SETUP + "No Self-Node-InstanceID specified.");
			}

		} else {
			System.out.println(SETUP + "Can't process request.");
			response.setStatus(400);
			response.resetBuffer();
			out.println(SETUP + "Can't process request.");
			return;

		}

		response.setStatus(200);
	}

	/**
	 * For requesting selfInstanceID, hostInstanceID and containerURL.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println(SETUP + "GET handling");

		PrintWriter out = response.getWriter();

		String pathInfo = request.getPathInfo();
		System.out.println(SETUP + "Path info: " + pathInfo);

		if (pathInfo.equals("/" + SERVICE_INSTANCE_ID_SELF_KEY)) {
			System.out.println(SETUP + "Returning Self-Service-InstanceID: " + selfServiceInstanceID);
			response.setStatus(200);
			out.print(selfServiceInstanceID);

		} else if (pathInfo.equals("/" + SERVICE_INSTANCE_ID_HOST_KEY)) {
			System.out.println(SETUP + "Returning Host-Service-InstanceID: " + hostServiceInstanceID);
			response.setStatus(200);
			out.print(hostServiceInstanceID);

		} else if (pathInfo.equals("/" + NODE_INSTANCE_ID_SELF_KEY)) {
			System.out.println(SETUP + "Returning Self-Node-InstanceID: " + selfNodeInstanceID);
			response.setStatus(200);
			out.print(selfNodeInstanceID);

		} else if (pathInfo.equals("/" + NODE_INSTANCE_ID_HOST_KEY)) {
			System.out.println(SETUP + "Returning Host-Node-InstanceID: " + hostNodeInstanceID);
			response.setStatus(200);
			out.print(hostNodeInstanceID);

		} else if (pathInfo.equals("/" + CONAINTER_URL_KEY)) {
			System.out.println(SETUP + "Returning Container-URL: " + containerURL);
			response.setStatus(200);
			out.print(containerURL);

		} else {
			System.out.println(SETUP + "Can't process request.");
			response.setStatus(400);
			response.resetBuffer();
			out.println(SETUP + "Can't process request.");
		}

	}

	/**
	 * @return selfServiceInstanceID
	 */
	public static Integer getSelfServiceInstanceID() {
		return selfServiceInstanceID;
	}

	/**
	 * @return hostServiceInstanceID
	 */
	public static Integer getHostServiceInstanceID() {
		return hostServiceInstanceID;
	}

	/**
	 * @return selfNodeInstanceID
	 */
	public static Integer getSelfNodeInstanceID() {
		return selfNodeInstanceID;
	}

	/**
	 * @return hostNodeInstanceID
	 */
	public static Integer getHostNodeInstanceID() {
		return hostNodeInstanceID;
	}

	/**
	 * @return containerURL
	 */
	public static URL getContainerURL() {
		return containerURL;
	}

}
