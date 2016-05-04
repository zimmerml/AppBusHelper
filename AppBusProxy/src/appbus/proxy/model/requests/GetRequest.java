package appbus.proxy.model.requests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

/**
 * 
 * This class checks if the URI can be mapped to the supported RESTful URIs (
 * <tt>{@literal [/appInvoker/activeRequests/([0-9]*)$]}</tt> &
 * <tt>{@literal [/appInvoker/activeRequests/([0-9]*)$/response$]}</tt>) of the
 * AppInvoker JsonHTTTP API. Furthermore reads the requestID as well as if the
 * queue should be polled or the result should be returned.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public class GetRequest {

	// Checks if url is supported
	private static Pattern regExQueuePattern = Pattern.compile("^/appInvoker/activeRequests/([0-9]*)$");
	private static Pattern regExResponsePattern = Pattern.compile("^/appInvoker/activeRequests/([0-9]*)/response$");

	// just for logging
	private final static String PROXY = "AppBusProxy: ";

	private Integer requestID;
	private Boolean isQueuePolling;

	/**
	 * 
	 * GetRequest including the requestID as well as if the queue should be
	 * polled or the result should be returned.
	 * 
	 * @param pathInfo
	 * @throws ServletException
	 */
	public GetRequest(String pathInfo) throws ServletException {

		Matcher matcher;

		if (pathInfo != null) {

			matcher = regExQueuePattern.matcher(pathInfo);
			if (matcher.find()) {
				requestID = Integer.parseInt(matcher.group(1));
				isQueuePolling = true;
				return;
			}

			matcher = regExResponsePattern.matcher(pathInfo);
			if (matcher.find()) {
				requestID = Integer.parseInt(matcher.group(1));
				isQueuePolling = false;
				return;
			}
		}
		System.out.println(PROXY + "Invalid URI.");
		throw new ServletException(PROXY + "Invalid URI.");
	}

	/**
	 * @return requestID of the GetRequest
	 */
	public Integer getRequestID() {
		return requestID;
	}

	/**
	 * @param requestID
	 */
	public void setRequestID(Integer requestID) {
		this.requestID = requestID;
	}

	/**
	 * For checking if the queue should be polled or the result should be
	 * returned.
	 * 
	 * @return <tt>true</tt> if <tt>
		 *         {@literal [/appInvoker/activeRequests/([0-9]*)$]}</tt> was
	 *         called. <tt>false</tt> if <tt>
	 *         {@literal [/appInvoker/activeRequests/([0-9]*)$/response]}</tt>
	 *         was called.
	 */
	public Boolean isQueuePolling() {
		return isQueuePolling;
	}

	/**
	 * @param isQueuePolling
	 */
	public void setQueuePolling(Boolean isQueuePolling) {
		this.isQueuePolling = isQueuePolling;
	}
}
