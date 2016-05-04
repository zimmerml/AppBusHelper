package appbus.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.json.simple.JSONObject;

import appbus.proxy.model.requests.PostRequest;
import appbus.proxy.model.resources.QueueMap;
import appbus.proxy.model.resources.ResultMap;

/**
 * 
 * Class where the invocation of the specified <tt>method</tt> is done.
 * Reflection is used to get the <tt>class</tt> that implements the specified
 * <tt>method</tt> as well as to invoke the <tt>method</tt>. Class is
 * implementing <tt>Runnable</tt> interface in order that the invocations can
 * easily be executed in separate threads.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public class AppBusProxy implements Runnable {

	// just for logging
	private final static String PROXY = "AppBusProxy: ";

	private static final QueueMap queue = new QueueMap();
	private static final ResultMap results = new ResultMap();

	private PostRequest postRequest;
	private Integer requestID;

	public AppBusProxy(PostRequest postRequest, Integer id) {
		this.postRequest = postRequest;
		this.requestID = id;
	}

	@Override
	public void run() {

		String clazz = postRequest.getClassName();
		String methodName = postRequest.getMethodName();
		Class<?>[] classes = postRequest.getClassesList();
		Object[] params = postRequest.getParamsList();

		Method method = null;
		Object returnValue = null;
		Class<?> cls = null;
		JSONObject obj = null;
		Object classInstance = null;

		try {
			cls = Class.forName(clazz);

			Method tempMethod = null;
			Class<?>[] c = null;

			for (Method m : cls.getMethods()) {
				if (m.getName().equals(methodName)) {

					c = m.getParameterTypes();
					if (Arrays.equals(c, classes)) {
						System.out.println(PROXY + "Method with matching paramter types found.");
						method = m;
						break;
					}
					tempMethod = m;
				}
			}

			if (method == null && tempMethod != null) {
				method = tempMethod;

				if (params != null && params.length == c.length) {

					// Some "dumb" prototype parameter type casting
					System.out.println(PROXY + "Casting needed:");

					for (int i = 0; i < params.length; i++) {

						if (!params[i].getClass().getName().equals(c[i].getName())) {

							System.out.println(PROXY + "Actual type: " + params[i].getClass().getName());
							System.out.println(PROXY + "Needed type: " + c[i].getName());

							if (params[i] instanceof String) {
								if (c[i].getName().equals("java.lang.Integer") || c[i].getName().equals("int")) {
									params[i] = Integer.parseInt((String) params[i]);
								} else if (c[i].getName().equals("java.lang.Float") || c[i].getName().equals("float")) {
									params[i] = Float.parseFloat((String) params[i]);
								} else if (c[i].getName().equals("java.lang.Double")
										|| c[i].getName().equals("double")) {
									params[i] = Double.parseDouble((String) params[i]);
								} else if (c[i].getName().equals("java.lang.Boolean")
										|| c[i].getName().equals("boolean")) {
									params[i] = Boolean.parseBoolean((String) params[i]);
								}

							} else if (params[i].getClass().getName().contains("Long")) {

								if (c[i].getName().equals("java.lang.Integer") || c[i].getName().equals("int")) {
									params[i] = ((Long) params[i]).intValue();
								} else if (c[i].getName().equals("java.lang.Float") || c[i].getName().equals("float")) {
									params[i] = (float) ((Long) params[i]);
								} else if (c[i].getName().equals("java.lang.Double")
										|| c[i].getName().equals("double")) {
									params[i] = (double) ((Long) params[i]);
								}

							} else {
								if (c[i].isInstance(params[i])) {
									params[i] = convertInstanceOfObject(params[i], c[i]);
								}
							}

							System.out.println(PROXY + "New Type: " + params[i].getClass().getName());
						}
					}

				} else {
					System.out.println(PROXY + "Number of specified parameter (" + params.length
							+ ") are not equal with number of expected paramters (" + c.length + ") of method: "
							+ methodName);
				}
			}

			if (method != null) {

				if (method.getModifiers() != Modifier.STATIC) {
					classInstance = cls.newInstance();
				}

				System.out.println(PROXY + "Invoking the method: " + methodName);

				returnValue = method.invoke(classInstance, params);

				System.out.println(PROXY + "Invocation of method: " + methodName + " finished.");

				obj = new JSONObject();
				if (returnValue != null) {
					obj.put("result", returnValue);
				} else {
					obj.put("result", "void");
				}

				results.put(requestID, obj);
			} else {
				System.out.println(PROXY + "No method with name " + methodName + " found.");
			}

		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| InstantiationException | ClassNotFoundException e) {
			System.out.println(PROXY + "Inovcation of method: " + methodName + " failed.");
			e.printStackTrace();
		} finally {
			queue.put(requestID, true);
		}
	}

	private static <T> T convertInstanceOfObject(Object o, Class<T> clazz) throws ClassCastException {
		try {
			return clazz.cast(o);
		} catch (ClassCastException e) {
			System.out.println(PROXY + "Can't cast: " + o.toString() + " of type: " + o.getClass() + " to: " + clazz);
		}
		return null;
	}

}
