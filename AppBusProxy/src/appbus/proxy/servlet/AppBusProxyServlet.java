package appbus.proxy.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import appbus.proxy.AppBusProxy;
import appbus.proxy.model.requests.GetRequest;
import appbus.proxy.model.requests.PostRequest;
import appbus.proxy.model.resources.QueueMap;
import appbus.proxy.model.resources.ResultMap;

/**
 * 
 * HttpServlet supporting <tt>doPost</tt> method in order to invoke a specified
 * method. <tt>doGet</tt> method in order to poll if the invocation has finished
 * already and to request the result of the invocation.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
@WebServlet(value = "/OTABProxy/v1/*", loadOnStartup = 1, asyncSupported = true)
public class AppBusProxyServlet extends HttpServlet {

	// just for logging
	private final static String PROXY = "AppBusProxy: ";

	private static final long serialVersionUID = 1L;

	// MAX.VALUE: 2147483647. Could use BigInteger. But since this is a
	// prototype this should work.
	private static AtomicInteger incrementer = new AtomicInteger(0);
	private static final QueueMap queue = new QueueMap();
	private static final ResultMap results = new ResultMap();

	/**
	 * 
	 * For invoking a method. Supported URI: <tt>{@literal [/appInvoker]}</tt>.
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println(PROXY + "POST request handling");

		PrintWriter out = response.getWriter();

		PostRequest postRequest;
		try {
			postRequest = new PostRequest(request);
		} catch (ServletException | ParseException | IOException e) {
			response.setStatus(400);
			response.resetBuffer();
			e.printStackTrace();
			out.println(e.toString());
			return;
		}

		Integer id = incrementer.getAndIncrement();

		// Begin at 0 again. Assumption: old requests were processed.
		// (Prototype)
		if (id == Integer.MAX_VALUE) {
			incrementer.set(0);
		}
		queue.put(id, false);

		response.setStatus(202);
		response.setHeader("Location", request.getRequestURL() + "/activeRequests/" + id);

		ThreadPoolExecutor executor = (ThreadPoolExecutor) request.getServletContext().getAttribute("executor");
		executor.execute(new AppBusProxy(postRequest, id));

	}

	/**
	 * 
	 * For polling if the invocation has finished already and to request the
	 * result of the invocation. Supported URIs:
	 * <tt>{@literal [/appInvoker/activeRequests/([0-9]*)$]}</tt> for polling &
	 * <tt>{@literal [/appInvoker/activeRequests/([0-9]*)$/response$]}</tt> for
	 * requesting the result.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println(PROXY + "GET request handling");
		System.out.println(PROXY + "PATH INFO: " + request.getPathInfo());

		PrintWriter out = response.getWriter();

		try {
			GetRequest getRequest = new GetRequest(request.getPathInfo());

			Integer id = getRequest.getRequestID();
			System.out.println(PROXY + "ID: " + id);

			Boolean isQueuePolling = getRequest.isQueuePolling();

			if (isQueuePolling) {
				System.out.println(PROXY + "Queue polling");

				if (queue.containsID(id)) {
					System.out.println(PROXY + "ID is known.");

					if (queue.hasFinished(id)) {
						System.out.println(PROXY + "Invocation is finished, send location of Result.");
						response.setStatus(303);
						response.setHeader("Location", request.getRequestURL() + "/response");

					} else {
						System.out.println(PROXY + "Invocation is not finished yet.");
						JSONObject obj = new JSONObject();
						obj.put("status", "PENDING");
						response.setStatus(200);
						response.setContentType("application/json");
						obj.writeJSONString(out);
					}
				} else {
					System.out.println(PROXY + "There is no entry for this id in the queue.");
					response.setStatus(404);
					response.resetBuffer();
					out.println("There is no entry for this id in the queue.");
				}

			} else {
				System.out.println(PROXY + "Getting Results");

				if (results.containsID(id)) {

					System.out.println(PROXY + "Returning Result.");
					response.setStatus(200);
					response.setContentType("application/json");
					results.get(id).writeJSONString(out);

					// Remove polled responses.
					results.remove(id);
					queue.remove(id);

				} else if (!queue.containsID(id)) {
					System.out.println(PROXY + "Unknown id.");
					response.setStatus(404);
					response.resetBuffer();
					response.setContentType("text/plain");
					out.println("Unknown id.");
				} else {
					System.out.println(PROXY + "Error while invoking specified method.");
					response.setStatus(404);
					response.resetBuffer();
					response.setContentType("text/plain");
					out.println("Error while invoking specified method.");

					// Remove polled responses.
					results.remove(id);
					queue.remove(id);
				}

			}

		} catch (ServletException e) {
			response.setStatus(400);
			response.resetBuffer();
			out.println("Invocation failed: " + e);
			e.printStackTrace();
		}
	}

}