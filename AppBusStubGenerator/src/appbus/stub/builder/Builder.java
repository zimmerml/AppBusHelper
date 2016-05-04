package appbus.stub.builder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * 
 * Class for building a jar containing the generated AppInvokerStub for using
 * the OpenTOSCA JSON/HTTP Application Bus API.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public class Builder {

	final static String SOURCE_FOLDER = "src";
	final static String BASE_PACKAGE = SOURCE_FOLDER + "/appbus/stub/base";
	final static String GENERATED_PACKAGE = SOURCE_FOLDER + "/appbus/stub/generated";

	/**
	 * 
	 * Creates the jar.
	 * 
	 * @param fileName
	 *            of the jar
	 * @param classNames
	 *            names of classes that should be contained in the jar
	 * @param outputLocation
	 *            where the jar should be saved
	 * @param genClientStub
	 *            if a client stub should be generated
	 */
	public static void run(String fileName, List<String> classNames, String outputLocation, boolean genClientStub) {

		System.out.println("Lets build the jar: " + fileName + " containing the following interfaces: " + classNames
				+ " and save it at: " + outputLocation);

		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		JarOutputStream target;

		File outputFile = null;

		try {

			outputFile = new File(outputLocation, fileName);

			target = new JarOutputStream(new FileOutputStream(outputFile), manifest);

			if (genClientStub) {
				add(new File(BASE_PACKAGE), target);
			}

			for (String className : classNames) {
				if (genClientStub) {
					add(new File(GENERATED_PACKAGE, className + ".class"), target);
				}
				add(new File(GENERATED_PACKAGE, className + ".java"), target);
			}

			target.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (outputFile.exists() && !outputFile.isDirectory()) {
			System.out.println("Jar: " + fileName + " successfully created at: " + outputLocation);
		} else {
			System.out.println("ERROR: Jar: " + fileName + "  couldn't be created at: " + outputLocation);
		}
	}

	/**
	 * Checks if source is a file or a directory and in case of a file adds it
	 * to the jar.
	 * 
	 * @param source
	 *            file to add
	 * @param target
	 * @throws IOException
	 */
	private static void add(File source, JarOutputStream target) throws IOException {

		if (source.exists()) {

			BufferedInputStream in = null;
			try {

				File parentDir = new File(SOURCE_FOLDER);
				String relPath = source.getCanonicalPath().substring(parentDir.getCanonicalPath().length() + 1,
						source.getCanonicalPath().length());

				if (source.isDirectory()) {
					String name = relPath.replace("\\", "/");
					if (!name.isEmpty()) {
						if (!name.endsWith("/"))
							name += "/";
						JarEntry entry = new JarEntry(name);
						entry.setTime(source.lastModified());
						target.putNextEntry(entry);
						target.closeEntry();
					}
					for (File nestedFile : source.listFiles()) {
						add(nestedFile, target);
					}
					return;
				}

				JarEntry entry = new JarEntry(relPath.replace("\\", "/"));
				entry.setTime(source.lastModified());
				target.putNextEntry(entry);
				in = new BufferedInputStream(new FileInputStream(source));

				byte[] buffer = new byte[1024];
				while (true) {
					int count = in.read(buffer);
					if (count == -1)
						break;
					target.write(buffer, 0, count);
				}

				target.closeEntry();
			} finally {
				if (in != null)
					in.close();
			}
		}
	}
}
