package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import appbus.stub.builder.Builder;
import appbus.stub.compiler.Compiler;
import appbus.stub.generator.Generator;
import appbus.stub.model.TInterface;
import appbus.stub.parser.Parser;

public class Main {

	private static File toscaFile;

	private static JFileChooser chooser;

	private final static File genDir = new File("src/appbus/stub/generated");
	private final static File baseClient = new File("src/appbus/stub/base/AppBusClient.class");

	public static void main(String[] args) {

		System.out.println("Stub Generator started!");

		// Initiate cleaning
		cleanUp();

		// Ask for xml
		System.out.println("Select location of TOSCA file(s).");

		String toscaLocation;
		chooser = new JFileChooser();

		int returnValue = chooser.showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			toscaLocation = chooser.getSelectedFile().getAbsolutePath();
		} else {
			System.out.println("Stub Generator stopped!");
			return;
		}

		toscaFile = new File(toscaLocation);

		System.out.println("Entered location of the tosca file(s): " + toscaFile.toString());

		// Ask what kind of stub should be generated
		boolean genClientStub = genClientStub();

		// Parse xml
		Map<String, List<TInterface>> map = null;
		try {
			map = Parser.run(toscaFile);
		} catch (ParserConfigurationException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (map != null) {

			// Generate source classes
			Generator.run(map, genClientStub);

			// Compile generated source classes (only for clieant stub)
			if (genClientStub) {
				Compiler.run();
			}

			// Build jar for every NodeTemplate
			for (Entry<String, List<TInterface>> entry : map.entrySet()) {
				String nodeTemplateID = entry.getKey();
				List<String> classNameList = new ArrayList<String>();
				for (TInterface tInterface : entry.getValue()) {
					classNameList.add(tInterface.getName());
				}

				// Select location and name of jar(s)
				chooser.setSelectedFile(new File(nodeTemplateID + ".jar"));
				returnValue = chooser.showSaveDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String saveLocation = chooser.getSelectedFile().getParent();
					String jarName = chooser.getSelectedFile().getName();

					Builder.run(jarName, classNameList, saveLocation, genClientStub);
				}

			}
		}

		cleanUp();

		System.out.println("Stub Generator stopped!");
	}

	private static boolean genClientStub() {

		JPanel panel = new JPanel();

		JRadioButton cleanStubButton = new JRadioButton("Skeleton");
		JRadioButton clientStubButton = new JRadioButton("Stub");

		ButtonGroup group = new ButtonGroup();

		group.add(cleanStubButton);
		group.add(clientStubButton);

		panel.add(cleanStubButton);
		panel.add(clientStubButton);

		cleanStubButton.setSelected(true);

		int selected = JOptionPane.showOptionDialog(null, panel, "What should be generated?",
				JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (selected != 0) {
			System.out.println("Stub Generator stopped!");
			System.exit(0);
		}

		return clientStubButton.isSelected();

	}

	private static void cleanUp() {

		if (genDir.exists()) {

			for (File file : genDir.listFiles()) {
				file.delete();
			}
		}
		baseClient.delete();
	}

}
