package appbus.proxy.model.resources;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Map that manages the requests. RequestID is used as <tt>key</tt> of the map.
 * The <tt>value</tt> of the map indicates if the invocation has finished or
 * not.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public class QueueMap {

	private static ConcurrentHashMap<Integer, Boolean> queue = new ConcurrentHashMap<Integer, Boolean>();

	/**
	 * Inserts an entry into the queue.
	 * 
	 * @param id
	 *            of the request
	 * @param isFinished
	 *            specifies if the invocation has finished or not
	 */
	public void put(Integer id, Boolean isFinished) {
		queue.put(id, isFinished);
	}

	/**
	 * @param id
	 *            of the request
	 * @return <tt>true</tt> if the invocation has finished. Otherwise
	 *         <tt>false</tt>
	 */
	public boolean hasFinished(Integer id) {
		return queue.get(id);
	}

	/**
	 * @param id
	 *            of the request
	 * @return <tt>true</tt> if the queue contains the specified requestID.
	 *         Otherwise <tt>false</tt>
	 */
	public boolean containsID(Integer id) {
		return queue.containsKey(id);
	}

	/**
	 * Removes the entry with the specified requestID from the queue.
	 * 
	 * @param id
	 *            of the request
	 */
	public void remove(Integer id) {
		queue.remove(id);
	}

}
