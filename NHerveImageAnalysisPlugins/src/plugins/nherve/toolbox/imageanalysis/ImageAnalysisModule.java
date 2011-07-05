package plugins.nherve.toolbox.imageanalysis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import plugins.nherve.toolbox.Algorithm;
import plugins.nherve.toolbox.image.mask.Mask;
import plugins.nherve.toolbox.image.mask.MaskException;
import plugins.nherve.toolbox.image.mask.MaskStack;


public abstract class ImageAnalysisModule extends Algorithm {
	public final static int STOPPED = 1;
	public final static int RUNNING = 2;
	public final static int FINISHED = 3;
	
	private int state;
	private String name;
	private List<String> neededParameters;
	private ImageAnalysisModule parentModule;
	private boolean linkWithFollowing;
	
	private Map<ImageAnalysisModuleListener, ImageAnalysisModuleListener> listeners;
	
	public ImageAnalysisModule(String name) {
		super();
		state = STOPPED;
		setName(name);
		neededParameters = new ArrayList<String>();
		listeners = new HashMap<ImageAnalysisModuleListener, ImageAnalysisModuleListener>();
		parentModule = null;
		setLinkWithFollowing(false);
	}
	
	public void addListener(ImageAnalysisModuleListener l) {
		listeners.put(l, l);
	}
	
	protected boolean addNeededParameter(String p) {
		return neededParameters.add(getParameterInternalName(p));
	}
	
	public abstract boolean analyze(ImageAnalysisContext context) throws ImageAnalysisException;
	
	public void checkParametersBeforeAnalysis(ImageAnalysisContext context) throws ImageAnalysisParameterException {
		String missing = "";
		for (String p : neededParameters) {
			if (!context.containsParameter(p)) {
				if (missing.length() > 0) {
					missing += ", ";
				}
				missing += p;
			}
		}
		if (missing.length() > 0) {
			throw new ImageAnalysisParameterException("Missing parameters for " + getName() + ": " + missing);
		}
	}
	
	public Mask createBackgroundMask(ImageAnalysisContext context, String label) throws MaskException {
		MaskStack stack = context.getStack();
		Mask msk = stack.createBackgroundMask(label, Color.WHITE);
		tag(context, msk);
		return msk;
	}

	public abstract JPanel createGUI(ImageAnalysisParameters defaultParameters);
	
	
	public Mask createNewMask(ImageAnalysisContext context, String label) throws MaskException {
		MaskStack stack = context.getStack();
		Mask msk = stack.createNewMask(label, true, Color.WHITE, 1.0f);
		tag(context, msk);
		return msk;
	}
	
	protected void fireChangeEvent() {
		// log("** " + getName() + " fireChangeEvent("+state+")");
		for (ImageAnalysisModuleListener l : listeners.keySet()) {
			l.statutsChanged(this);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public Object getObject(ImageAnalysisContext context, String k) {
		return context.getObject(k);
	}
	
	public boolean getParameterAsBoolean(ImageAnalysisContext context, String p) throws ImageAnalysisParameterException {
		return context.getParameterAsBoolean(getParameterInternalName(p));
	}
	
	public boolean getParameterAsBoolean(ImageAnalysisParameters parameters, String p) throws ImageAnalysisParameterException {
		return parameters.getParameterAsBoolean(getParameterInternalName(p));
	}

	public double getParameterAsDouble(ImageAnalysisContext context, String p) throws ImageAnalysisParameterException {
		return context.getParameterAsDouble(getParameterInternalName(p));
	}

	public double getParameterAsDouble(ImageAnalysisParameters parameters, String p) throws ImageAnalysisParameterException {
		return parameters.getParameterAsDouble(getParameterInternalName(p));
	}

	public int getParameterAsInt(ImageAnalysisContext context, String p) throws ImageAnalysisParameterException {
		return context.getParameterAsInt(getParameterInternalName(p));
	}

	public int getParameterAsInt(ImageAnalysisParameters parameters, String p) throws ImageAnalysisParameterException {
		return parameters.getParameterAsInt(getParameterInternalName(p));
	}

	public String getParameterAsString(ImageAnalysisContext context, String p) throws ImageAnalysisParameterException {
		return context.getParameterAsString(getParameterInternalName(p));
	}
	
	public String getParameterAsString(ImageAnalysisParameters parameters, String p) throws ImageAnalysisParameterException {
		return parameters.getParameterAsString(getParameterInternalName(p));
	}

	public String getParameterInternalName(String p) {
		return getName() + "_" + p;
	}
	
	public abstract void getParametersFromGui(JPanel p, ImageAnalysisContext c);
	
	public ImageAnalysisModule getParentModule() {
		return parentModule;
	}

	public int getState() {
		return state;
	}
	
	public boolean needSequence() {
		return true;
	}
	
	public abstract void populateWithDefaultParameterValues(ImageAnalysisParameters parameters);
	
	public void populateWithDefaultParameterValues(ImageAnalysisContext ctx) {
		populateWithDefaultParameterValues(ctx.getParameters());
	}
	
	public void putObject(ImageAnalysisContext context, String k, Object v) {
		context.putObject(k, v);
	}



	public void removeListener(ImageAnalysisModuleListener l) {
		listeners.remove(l);		
	}

	public void removeObject(ImageAnalysisContext context, String k) {
		context.removeObject(k);
	}

	protected void setName(String name) {
		this.name = name;
	}

	public void setParameter(ImageAnalysisContext context, String p, boolean v) {
		context.setParameter(getParameterInternalName(p), v);
	}

	public void setParameter(ImageAnalysisContext context, String p, double v) {
		context.setParameter(getParameterInternalName(p), v);
	}

	public void setParameter(ImageAnalysisContext context, String p, int v) {
		context.setParameter(getParameterInternalName(p), v);
	}

	public void setParameter(ImageAnalysisContext context, String p, String v) {
		context.setParameter(getParameterInternalName(p), v);
	}

	public void setParameter(ImageAnalysisParameters parameters, String p, boolean v) {
		parameters.setParameter(getParameterInternalName(p), v);
	}

	public void setParameter(ImageAnalysisParameters parameters, String p, double v) {
		parameters.setParameter(getParameterInternalName(p), v);
	}

	public void setParameter(ImageAnalysisParameters parameters, String p, int v) {
		parameters.setParameter(getParameterInternalName(p), v);
	}

	public void setParameter(ImageAnalysisParameters parameters, String p, String v) {
		parameters.setParameter(getParameterInternalName(p), v);
	}

	public void setParentModule(ImageAnalysisModule parentModule) {
		this.parentModule = parentModule;
	}

	public void setState(int state, boolean propagate) {
		this.state = state;
		fireChangeEvent();
	}

	public void stopAnalyze(ImageAnalysisContext context) throws ImageAnalysisException {
	}

	public void tag(ImageAnalysisContext context, Mask msk) {
		if (parentModule != null) {
			parentModule.tag(context, msk);
		}
		msk.addTag(getName());
	}
	
	public String toString() {
		return getName();
	}

	public boolean isLinkWithFollowing() {
		return linkWithFollowing;
	}

	public void setLinkWithFollowing(boolean linkWithFollowing) {
		this.linkWithFollowing = linkWithFollowing;
	}
}
