package appbus.proxy.servlet;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 *
 * ServletListener that initiates a ThreadPoolExecutor when the servlet starts.
 * 
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
@WebListener
public class AppBusProxyServletListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent servletContextEvent) {

		// Thread pool
		ThreadPoolExecutor executor = new ThreadPoolExecutor(50, 100, 10, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(50));
		servletContextEvent.getServletContext().setAttribute("executor", executor);

	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) servletContextEvent.getServletContext()
				.getAttribute("executor");
		executor.shutdown();
	}

}
