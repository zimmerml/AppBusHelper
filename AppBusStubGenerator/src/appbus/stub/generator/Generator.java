package appbus.stub.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

import appbus.stub.model.TDocumentation;
import appbus.stub.model.TInterface;
import appbus.stub.model.TOperation;
import appbus.stub.model.TParameter;

/**
 * 
 * Class for generating the stub classes for using the OpenTOSCA JOSN/HTTP
 * Application Bus API.
 * 
 * @author Michael Zimmermann - zimmerml@studi.informatik.uni-stuttgart.de
 *
 */
public class Generator {

	final static String SUPERCLASS = "AppBusClient";
	final static String BASE_PACKAGE = "appbus.stub.base";
	final static String GENERATED_PACKAGE = "appbus.stub.generated";
	final static File SOURCE = new File("src");
	// Superclass which this class should extends from
	final static ClassName SUPER_CLASS = ClassName.get(BASE_PACKAGE, SUPERCLASS);

	final static String NODE_TEMPLATE_ID = "NODE_TEMPLATE_ID";
	final static String INTERFACE_NAME = "INTERFACE_NAME";

	static ArrayList<File> generatedFiles = new ArrayList<File>();

	/**
	 * 
	 * Generates the stub classes.
	 * 
	 * @param HashMap
	 *            containing NodeTemplate-ID as key and a List containing the
	 *            associated TInterface objects as value
	 * @param genClientStub
	 *            if a client stub should be generated
	 */
	public static void run(Map<String, List<TInterface>> nodeTemplateInterfaceListMap, boolean genClientStub) {

		for (Entry<String, List<TInterface>> nodeTypeInterfaceListMapEntry : nodeTemplateInterfaceListMap.entrySet()) {

			String nodeTemplateID = nodeTypeInterfaceListMapEntry.getKey();

			for (TInterface tInterface : nodeTypeInterfaceListMapEntry.getValue()) {

				// Interface name used as class name
				String interfaceName = tInterface.getName();

				System.out.println("Lets generate the java source file: " + interfaceName + ".java");

				List<MethodSpec> methods = new ArrayList<MethodSpec>();

				// Create Class
				Builder classBuilder = TypeSpec.classBuilder(interfaceName);

				// just for client stub
				if (genClientStub) {
					classBuilder
							.addField((FieldSpec.builder(String.class, NODE_TEMPLATE_ID)
									.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
									.initializer("$S", nodeTemplateID).build()))
							.addField((FieldSpec.builder(String.class, INTERFACE_NAME)
									.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
									.initializer("$S", interfaceName).build()))
							.superclass(SUPER_CLASS);
				}

				for (int i = 0; i < tInterface.getOperation().size(); i++) {

					methods.add(generateMethod(nodeTemplateID, tInterface.getOperation().get(i), genClientStub));

				}

				// add methods to class
				classBuilder.addModifiers(Modifier.PUBLIC).addMethods(methods);

				// Create File
				JavaFile javaFile = JavaFile.builder(GENERATED_PACKAGE, classBuilder.build()).build();

				try {
					javaFile.writeTo(SOURCE);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				File file = new File(
						SOURCE + "/" + javaFile.packageName.replace(".", "/") + "/" + interfaceName + ".java");
				if (file.exists() && !file.isDirectory()) {
					System.out.println(interfaceName + ".java was successfully generated.");
				} else {
					System.out.println("ERROR: " + interfaceName + ".java couldn't be generated.");
				}
			}
		}
	}

	/**
	 * 
	 * Generates the methods of the stub.
	 * 
	 * @param nodeTemplateID
	 * @param tOperation
	 * @param genClientStub
	 *            if a client stub should be generated
	 * @return generated method
	 */
	private static MethodSpec generateMethod(String nodeTemplateID, TOperation tOperation, boolean genClientStub) {

		String opName = tOperation.getName();

		System.out.println("Generating method: " + opName + " provided by NodeTemplate: " + nodeTemplateID);

		TypeName returnType;
		CodeBlock.Builder codeBlockBuilder1 = CodeBlock.builder();

		if (tOperation.getOutputParameters() == null
				|| tOperation.getOutputParameters().getOutputParameter().isEmpty()) {
			returnType = TypeName.VOID;

			System.out.println("The method: " + opName + " has 0 output params.");

		} else {
			String type = tOperation.getOutputParameters().getOutputParameter().get(0).getType();
			returnType = determineTypeName(type);
			if (genClientStub) {
				codeBlockBuilder1.addStatement("return " + getReturnString(type));
			} else {
				codeBlockBuilder1.addStatement("// TODO Auto-generated method stub");
				codeBlockBuilder1.addStatement("return null");
			}

			System.out.println("The method: " + opName + " has 1 output param of type: " + returnType);
		}

		ArrayList<ParameterSpec> params = new ArrayList<ParameterSpec>();

		CodeBlock.Builder codeBlockBuilder2 = CodeBlock.builder().addStatement(
				"LinkedHashMap<$T, $T> param = new $T<>()", String.class, Object.class, LinkedHashMap.class);

		if (tOperation.getInputParameters() == null) {
			System.out.println("The method: " + opName + " has 0 input params.");

		} else {

			System.out.println("The method: " + opName + " has "
					+ tOperation.getInputParameters().getInputParameter().size() + " input params:");

			for (TParameter param : tOperation.getInputParameters().getInputParameter()) {
				String paramName = param.getName();
				String paramType = param.getType();

				System.out.println("Name: " + paramName + " Type: " + paramType);

				ParameterSpec paramSpec = ParameterSpec.builder(determineTypeName(paramType), paramName).build();
				params.add(paramSpec);

				codeBlockBuilder2.addStatement("param.put($S, " + paramName + ")", paramName);

			}
		}

		String doc = getDoc(tOperation);

		MethodSpec.Builder method = MethodSpec.methodBuilder(opName).addJavadoc(doc)
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(returnType).addParameters(params);

		if (genClientStub) {
			method.addException(IOException.class).addCode(codeBlockBuilder2.build()).addStatement(
					"String res = invoke(NODE_TEMPLATE_ID, INTERFACE_NAME, $S, param).toString()", opName);
		}

		method.addCode(codeBlockBuilder1.build());

		return method.build();

	}

	/**
	 * 
	 * Helper method for the generateMethod. Checks which type the return value
	 * should have.
	 * 
	 * @param type
	 *            to check
	 * @return type name
	 */
	private static TypeName determineTypeName(String type) {

		if (type.contains("string")) {
			return TypeName.get(String.class);
		}
		if (type.contains("integer")) {
			return TypeName.get(Integer.class);
		}
		if (type.contains("boolean")) {
			return TypeName.get(Boolean.class);
		}
		// Fallback
		return TypeName.OBJECT;
	}

	/**
	 * Helper method for the generateMethod. Generates "Parsing-String" for the
	 * specified type.
	 * 
	 * @param type
	 * @return
	 */
	private static String getReturnString(String type) {

		if (type.contains("int")) {
			return "Integer.parseInt(res)";
		}
		if (type.contains("float")) {
			return "Float.parseFloat(res)";
		}
		if (type.contains("double")) {
			return "Double.parseDouble(res)";
		}
		if (type.contains("bool")) {
			return "Boolean.parseBoolean(res)";
		}
		return "res";
	}

	/**
	 * 
	 * Gets the documentation of an operation, if specified.
	 * 
	 * @param tOperation
	 * @return documentation
	 */
	private static String getDoc(TOperation tOperation) {
		String docu = "";
		if (tOperation.getDocumentation() != null && !tOperation.getDocumentation().isEmpty()) {
			for (TDocumentation doc : tOperation.getDocumentation()) {
				for (Object obj : doc.getContent()) {
					docu = docu + obj.toString() + "\n";
				}
			}
		}
		return docu;
	}

}
