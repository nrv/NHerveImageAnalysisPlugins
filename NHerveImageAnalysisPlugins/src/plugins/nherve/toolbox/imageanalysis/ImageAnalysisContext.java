package plugins.nherve.toolbox.imageanalysis;

import icy.image.IcyBufferedImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plugins.nherve.toolbox.image.mask.MaskStack;

public class ImageAnalysisContext {
	private ImageAnalysisParameters parameters;
	private MaskStack stack;
	private IcyBufferedImage workingImage;
	private IcyBufferedImage backupImage;
	private Map<String, Object> objects;
	private ImageAnalysisProcessor processor;

	public ImageAnalysisContext() {
		super();
		processor = new ImageAnalysisProcessor();
		parameters = new ImageAnalysisParameters();
		objects = new HashMap<String, Object>();
	}

	public void addAllParameters(ImageAnalysisParameters other, boolean replace) throws ImageAnalysisParameterException {
		parameters.addAll(other, replace);
	}

	public boolean containsParameter(String p) {
		return parameters.containsParameter(p);
	}

	public boolean getParameterAsBoolean(String p) throws ImageAnalysisParameterException {
		return parameters.getParameterAsBoolean(p);
	}

	public double getParameterAsDouble(String p) throws ImageAnalysisParameterException {
		return parameters.getParameterAsDouble(p);
	}

	public int getParameterAsInt(String p) throws ImageAnalysisParameterException {
		return parameters.getParameterAsInt(p);
	}

	public String getParameterAsString(String p) throws ImageAnalysisParameterException {
		return parameters.getParameterAsString(p);
	}

	public MaskStack getStack() {
		return stack;
	}

	public void setParameter(String p, boolean v) {
		parameters.setParameter(p, v);
	}

	public void setParameter(String p, double v) {
		parameters.setParameter(p, v);
	}

	public void setParameter(String p, int v) {
		parameters.setParameter(p, v);
	}

	public void setParameter(String p, String v) {
		parameters.setParameter(p, v);
	}

	public void setStack(MaskStack stack) {
		this.stack = stack;
	}

	public Object getObject(String k) {
		return objects.get(k);
	}

	public void putObject(String k, Object v) {
		objects.put(k, v);
	}

	public void removeObject(String k) {
		objects.remove(k);
	}

	public IcyBufferedImage getBackupImage() {
		return backupImage;
	}

	public void setBackupImage(IcyBufferedImage backupImage) {
		this.backupImage = backupImage;
	}

	public IcyBufferedImage getWorkingImage() {
		return workingImage;
	}

	public void setWorkingImage(IcyBufferedImage workingImage) {
		this.workingImage = workingImage;
	}

	public Set<String> getObjectNames() {
		return objects.keySet();
	}

	public boolean processAndWait(final ImageAnalysisModule module, final boolean display) throws ImageAnalysisException {
		return processor.processAndWait(this, module, display);
	}
	
	public boolean processAndNotify(final ImageAnalysisModule module, final ImageAnalysisProcessListener lst, final boolean display) throws ImageAnalysisException {
		return processor.processAndNotify(this, module, lst, display);
	}
	
	public boolean processParallelAndWait(final List<ImageAnalysisModule> modules, final boolean display) throws ImageAnalysisException {
		return processor.processParallelAndWait(this, modules, display);
	}

	public void stopRunningProcesses() {
		processor.stopRunningProcesses();
	}

	public void reInitProcessor() {
		processor.reInit();
	}

	ImageAnalysisParameters getParameters() {
		return parameters;
	}
}
