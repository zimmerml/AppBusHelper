package appbus.stub.compiler;

import java.io.File;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * 
 * Class for compiling the generated stub classes for using the OpenTOSCA
 * JOSN/HTTP Application Bus API.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public class Compiler {

	final static String BASE_CLIENT = "src/appbus/stub/base/AppBusClient";
	final static String GEN_DIR = "src/appbus/stub/generated/";

	/**
	 * 
	 * Compiles all generated classes as well as the base client class.
	 * 
	 */
	public static void run() {

		File[] files = new File(GEN_DIR).listFiles();

		if (files != null) {

			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

			File f = new File(BASE_CLIENT + ".class");
			if (!f.exists()) {
				if (compiler.run(null, null, null, BASE_CLIENT + ".java") != 0) {
					System.out.println("ERROR: " + BASE_CLIENT + " couldn't be compiled.");
				}
			}

			for (File file : files) {

				System.out.println("Lets compile the java file: " + file);

				if (compiler.run(null, null, null, file.getPath()) != 0) {
					System.out.println("ERROR: " + file.getName() + " couldn't be compiled.");
				} else {
					System.out.println(file.getName() + "was successfully compiled.");
				}
			}

		}
	}
}
