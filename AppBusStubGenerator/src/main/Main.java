package main;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

import appbus.java.builder.Builder;
import appbus.java.compiler.Compiler;
import appbus.java.generator.Generator;
import appbus.stub.model.TInterface;
import appbus.stub.parser.Parser;

public class Main {
	
	
	private static File toscaFile;
	
	private static JFileChooser chooser;
	
	private final static File javaGenDir = new File("src/appbus/java/generated");
	private final static File javaBaseClient = new File("src/appbus/java/base/AppBusClient.class");
	
	private final static File pythonGenDir = new File("src/appbus/python/generated/");
	private final static File pythonBaseClient = new File("src/appbus/python/base/AppBusClient.py");
	
	private final static String JAVA = "JAVA";
	private final static String PYTHON = "PYTHON";
	
	
	public static void main(String[] args) {
		
		System.out.println("Stub Generator started!");
		
		// Initiate cleaning
		Main.cleanUp();
		
		// Ask for xml
		System.out.println("Select location of TOSCA file(s).");
		
		String toscaLocation;
		Main.chooser = new JFileChooser();
		
		int returnValue = Main.chooser.showOpenDialog(null);
		
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			toscaLocation = Main.chooser.getSelectedFile().getAbsolutePath();
		} else {
			System.out.println("Stub Generator stopped!");
			return;
		}
		
		Main.toscaFile = new File(toscaLocation);
		
		System.out.println("Entered location of the tosca file(s): " + Main.toscaFile.toString());
		
		// Ask what kind of stub should be generated
		boolean genClientStub = Main.genClientStub();
		
		// Ask what language should be generated
		String language = Main.requestLanguage();
		
		System.out.println("Selected language: " + language);
		
		// Parse xml
		Map<String, List<TInterface>> interfaceMap = null;
		try {
			interfaceMap = Parser.run(Main.toscaFile);
		} catch (ParserConfigurationException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (interfaceMap != null) {
			
			if (language.equals(Main.JAVA)) {
				
				// Generate source classes
				Generator.run(interfaceMap, genClientStub);
				
				// Compile generated source classes (only for client stub)
				if (genClientStub) {
					Compiler.run();
				}
				
				// Build jar for every NodeTemplate
				for (Entry<String, List<TInterface>> entry : interfaceMap.entrySet()) {
					String nodeTemplateID = entry.getKey();
					List<String> classNameList = new ArrayList<String>();
					for (TInterface tInterface : entry.getValue()) {
						classNameList.add(tInterface.getName());
					}
					
					// Select location and name of jar(s)
					String ending = "_Skeleton";
					if (genClientStub) {
						ending = "_Stub";
					}
					Main.chooser.setSelectedFile(new File(nodeTemplateID + ending + ".jar"));
					returnValue = Main.chooser.showSaveDialog(null);
					
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						String saveLocation = Main.chooser.getSelectedFile().getParent();
						String jarName = Main.chooser.getSelectedFile().getName();
						
						Builder.run(jarName, classNameList, saveLocation, genClientStub);
					}
					
				}
			} else if (language.equals(Main.PYTHON)) {
				appbus.python.generator.Generator.run(interfaceMap, genClientStub);
				
				File[] files = Main.pythonGenDir.listFiles();
				
				for (File file : files) {
					Main.chooser.setSelectedFile(file);
					returnValue = Main.chooser.showSaveDialog(null);
					
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						String saveLocation = Main.chooser.getSelectedFile().getParent();
						String fileName = Main.chooser.getSelectedFile().getName();
						
						try {
							Files.copy(file.toPath(), new File(saveLocation + "/" + fileName).toPath(), REPLACE_EXISTING);
							
							// also copy BaseClient
							if (genClientStub) {
								Files.copy(Main.pythonBaseClient.toPath(), new File(saveLocation + "/" + Main.pythonBaseClient.getName()).toPath(), REPLACE_EXISTING);
							}
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		Main.cleanUp();
		
		System.out.println("Stub Generator stopped!");
	}
	
	/**
	 * @return true in case a Stub should be generated; false if Skeleton should
	 *         be generated
	 */
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
		
		int selected = JOptionPane.showOptionDialog(null, panel, "What should be generated?", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, null, null);
		
		if (selected != 0) {
			System.out.println("Stub Generator stopped!");
			System.exit(0);
		}
		
		return clientStubButton.isSelected();
		
	}
	
	private static String requestLanguage() {
		
		JPanel panel = new JPanel();
		
		JRadioButton javaButton = new JRadioButton("Java");
		javaButton.setActionCommand(Main.JAVA);
		JRadioButton pythonButton = new JRadioButton("Python");
		pythonButton.setActionCommand(Main.PYTHON);
		
		ButtonGroup group = new ButtonGroup();
		
		group.add(javaButton);
		group.add(pythonButton);
		
		panel.add(javaButton);
		panel.add(pythonButton);
		
		javaButton.setSelected(true);
		
		int selected = JOptionPane.showOptionDialog(null, panel, "What language should be generated?", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, null, null);
		
		if (selected != 0) {
			System.out.println("Stub Generator stopped!");
			System.exit(0);
		}
		
		return group.getSelection().getActionCommand();
		
	}
	
	private static void cleanUp() {
		
		if (Main.javaGenDir.exists()) {
			
			for (File file : Main.javaGenDir.listFiles()) {
				file.delete();
			}
		}
		
		if (Main.pythonGenDir.exists()) {
			
			for (File file : Main.pythonGenDir.listFiles()) {
				file.delete();
			}
		}
		Main.javaBaseClient.delete();
	}
	
}
