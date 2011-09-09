package plugins.nherve.toolbox.imageanalysis.impl;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import plugins.nherve.toolbox.imageanalysis.ImageAnalysisContext;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisException;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisModule;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameters;

public abstract class ChoiceModule extends ModuleGroup {
	private Map<ImageAnalysisModule, JRadioButton> moduleSelected;
	
	public ChoiceModule(String name) {
		super(name);
		
		moduleSelected = null;
	}
	
	@Override
	public boolean analyze(ImageAnalysisContext context) throws ImageAnalysisException {
		for (ImageAnalysisModule module : this) {
			if (moduleSelected.get(module).isSelected()) {
				return context.processAndWait(module, isLogEnabled());
			}
		}
		
		return false;
	}
	
	@Override
	public JPanel createGUI(ImageAnalysisParameters defaultParameters) {
		moduleSelected = new HashMap<ImageAnalysisModule, JRadioButton>();

		JPanel mainPanel = null;
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder(getName()));

		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		modulePanels.clear();
		moduleSelected.clear();

		ButtonGroup bg = new ButtonGroup();
		boolean first = true;
		for (ImageAnalysisModule m : this) {
			JPanel modulePanel = new JPanel();
			modulePanel.setOpaque(false);
			modulePanel.setLayout(new BorderLayout());

			JPanel moduleGui = m.createGUI(defaultParameters);
			if (moduleGui != null) {
				modulePanels.put(m, moduleGui);

				JRadioButton rb = new JRadioButton();
				bg.add(rb);
				rb.setSelected(first);
				moduleSelected.put(m, rb);

				modulePanel.add(rb, BorderLayout.WEST);
				modulePanel.add(moduleGui, BorderLayout.CENTER);
				mainPanel.add(modulePanel);
			}
			
			if (first) {
				first = false;
			}
		}

		return mainPanel;
	}

	@Override
	public void populateGUI(ImageAnalysisParameters defaultParameters, JPanel panel) {
		// Nothing to do here, everything is in createGUI.
	}
}
