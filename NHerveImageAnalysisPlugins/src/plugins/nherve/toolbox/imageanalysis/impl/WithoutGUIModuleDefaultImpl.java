package plugins.nherve.toolbox.imageanalysis.impl;

import icy.gui.util.GuiUtil;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import plugins.nherve.toolbox.imageanalysis.ImageAnalysisContext;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisModule;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameters;


public abstract class WithoutGUIModuleDefaultImpl extends ImageAnalysisModule {
	
	public WithoutGUIModuleDefaultImpl(String name) {
		super(name);
	}
	
	@Override
	public JPanel createGUI(ImageAnalysisParameters defaultParameters) {
		return GuiUtil.createLineBoxPanel(new Component[]{new JLabel(getName()), Box.createHorizontalGlue()});
	}

	@Override
	public void getParametersFromGui(JPanel p, ImageAnalysisContext c) {
	}
}
