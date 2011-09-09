package plugins.nherve.toolbox.imageanalysis.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import plugins.nherve.toolbox.imageanalysis.ImageAnalysisContext;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisGUI;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisModule;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameterException;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameters;

public abstract class ModuleGroup extends WithGUIModuleDefaultImpl implements Iterable<ImageAnalysisModule> {
	private final List<ImageAnalysisModule> modules;
	protected final Map<ImageAnalysisModule, JPanel> modulePanels;

	public ModuleGroup(String name) {
		super(name);
		modules = new ArrayList<ImageAnalysisModule>();
		modulePanels = new HashMap<ImageAnalysisModule, JPanel>();
	}
	
	public boolean addModule(ImageAnalysisModule e) {
		e.setParentModule(this);
		return modules.add(e);
	}

	public boolean addModuleAndLinkWithFollowing(ImageAnalysisModule e) {
		e.setLinkWithFollowing(true);
		return addModule(e);
	}

	@Override
	public void checkParametersBeforeAnalysis(ImageAnalysisContext context) throws ImageAnalysisParameterException {
		// TODO : verification en cascade
	}

	public void clearModules() {
		modules.clear();
	}
	
	@Override
	public void getParametersFromGui(JPanel p, ImageAnalysisContext c) {
		for (ImageAnalysisModule m : this) {
			if (modulePanels.containsKey(m)) {
				m.getParametersFromGui(modulePanels.get(m), c);
			}
		}
	}
	
	public abstract void initModules(ImageAnalysisParameters defaultParameters);

	@Override
	public Iterator<ImageAnalysisModule> iterator() {
		return modules.iterator();
	}
	
	@Override
	public boolean needSequence() {
		for (ImageAnalysisModule m : this) {
			if (m.needSequence()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void populateWithDefaultParameterValues(ImageAnalysisParameters parameters) {

	}

	@Override
	public void setGui(ImageAnalysisGUI gui) {
		super.setGui(gui);

		if (modules != null) {
			for (ImageAnalysisModule m : modules) {
				if (m instanceof WithGUIModuleDefaultImpl) {
					((WithGUIModuleDefaultImpl) m).setGui(gui);
				}
			}
		}
	}

	@Override
	public void setLogEnabled(boolean display) {
		super.setLogEnabled(display);

		if (modules != null) {
			for (ImageAnalysisModule m : modules) {
				m.setLogEnabled(display);
			}
		}
	}

	@Override
	public String toString() {
		String fullName = getName() + " (";
		boolean first = true;
		for (ImageAnalysisModule m : this) {
			if (first) {
				first = false;
			} else {
				fullName += " - ";
			}
			fullName += m.getName();
		}
		fullName += ")";
		return fullName;
	}
}
