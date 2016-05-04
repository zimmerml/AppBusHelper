package appbus.proxy.model.requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * This class checks if the URI can be mapped to the supported RESTful URI
 * <tt>{@literal [/appInvoker]}</tt> of the servlet. Also reads the className,
 * methodName as well as the transfered parameters.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public class PostRequest {

	// Checks if url is supported
	private static String invocationPattern = "/appInvoker";

	// just for logging
	private final static String PROXY = "AppBusProxy: ";

	private String className;
	private String methodName;
	private Class<?>[] classesList;
	private Object[] paramsList;

	/**
	 * 
	 * PostRequest containing the className, methodName as well as the
	 * transfered parameters.
	 * 
	 * @param request
	 * @throws ServletException
	 * @throws IOException
	 * @throws ParseException
	 */
	public PostRequest(HttpServletRequest request) throws ServletException, IOException, ParseException {

		String pathInfo = request.getPathInfo();

		System.out.println(PROXY + "PATH INFO: " + pathInfo);

		if (pathInfo != null && pathInfo.equals(invocationPattern)) {

			LinkedHashMap<String, LinkedHashMap<String, Object>> requestMap = requestToMap(request);

			LinkedHashMap<String, Object> infosMap = (LinkedHashMap<String, Object>) requestMap
					.get("invocation-information");

			if (infosMap != null) {

				className = (String) infosMap.get("class");
				methodName = (String) infosMap.get("operation");

				System.out.println(PROXY + "Class: " + className);
				System.out.println(PROXY + "Method: " + methodName);

				if (className != null && methodName != null) {

					LinkedHashMap<String, Object> paramsMap = (LinkedHashMap<String, Object>) requestMap.get("params");

					if (paramsMap != null) {

						int size = paramsMap.size();

						classesList = new Class<?>[size];
						paramsList = new Object[size];

						int i = 0;
						for (Entry<String, Object> set : paramsMap.entrySet()) {
							System.out.println(PROXY + "Key: " + set.getKey());
							Object obj = set.getValue();
							System.out.println(PROXY + "Value: " + set.getValue() + " Type: " + obj.getClass());
							classesList[i] = obj.getClass();
							paramsList[i] = obj;
							i++;
						}

					} else {
						classesList = new Class<?>[0];
						System.out.println(PROXY + "No parameter specified.");
					}
				} else {
					System.out.println(PROXY + "Class and/or operation not specified.");
					throw new ServletException(PROXY + "Class and/or operation not specified.");
				}
			} else {
				System.out.println(PROXY + "Needed information not specified.");
				throw new ServletException(PROXY + "Needed information not specified.");
			}
			return;
		}
		System.out.println(PROXY + "Invalid URI.");
		throw new ServletException(PROXY + "Invalid URI.");
	}

	/**
	 * 
	 * Maps a HttpServletRequest with JSON body to a
	 * {@literal LinkedHashMap<String, LinkedHashMap<String, Object>>}.
	 * 
	 * @param request
	 * @return LinkedHashMap
	 * @throws IOException
	 * @throws ParseException
	 */
	public LinkedHashMap<String, LinkedHashMap<String, Object>> requestToMap(HttpServletRequest request)
			throws IOException, ParseException {

		StringBuffer jb = new StringBuffer();
		String json = null;

		BufferedReader reader = request.getReader();
		while ((json = reader.readLine()) != null) {
			jb.append(json);
		}

		json = jb.toString();
		ContainerFactory orderedKeyFactory = new ContainerFactory() {
			public Map<String, LinkedHashMap<String, Object>> createObjectContainer() {
				return new LinkedHashMap<String, LinkedHashMap<String, Object>>();
			}

			@Override
			public List<?> creatArrayContainer() {
				// TODO Auto-generated method stub
				return null;
			}

		};

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(json, orderedKeyFactory);

		return (LinkedHashMap<String, LinkedHashMap<String, Object>>) obj;
	}

	/**
	 * @return name of the class in which the specified method is located
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return method that should be invoked
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param method
	 */
	public void setMethod(String method) {
		this.methodName = method;
	}

	/**
	 * @return transfered parameter
	 */
	public Object[] getParamsList() {
		return paramsList;
	}

	/**
	 * @return classes of the transfered parameter
	 */
	public Class<?>[] getClassesList() {
		return classesList;
	}

}