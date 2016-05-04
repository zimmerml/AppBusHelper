package appbus.stub.base;

import java.io.IOException;
import java.util.LinkedHashMap;

public class Test {

	public static void main(String[] args) {

		LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
		params.put("number", new Integer(12));
		params.put("bool", true);
		params.put("name", "Ernst");

		try {
			Object result = AppBusClient.invoke("AppName", "AppInterface", "testMethod1", params);
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
