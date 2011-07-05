package plugins.nherve.toolbox.imageanalysis;

import java.util.HashMap;
import java.util.Iterator;


public class ImageAnalysisParameters implements Iterable<String> {
	private HashMap<String, String> parameters;
	
	public ImageAnalysisParameters() {
		super();
		
		parameters = new HashMap<String, String>();
	}

	public boolean containsParameter(String p) {
		return parameters.containsKey(p);
	}

	public String getParameterAsString(String p) throws ImageAnalysisParameterException {
		if (!containsParameter(p)) {
			throw new ImageAnalysisParameterException("Parameter " + p + " not found");
		}
		return parameters.get(p);
	}

	public int getParameterAsInt(String p) throws ImageAnalysisParameterException {
		try {
			return Integer.parseInt(parameters.get(p));
		} catch (NumberFormatException e) {
			throw new ImageAnalysisParameterException(e);
		}
	}

	public double getParameterAsDouble(String p) throws ImageAnalysisParameterException {
		try {
			return Double.parseDouble(parameters.get(p));
		} catch (NumberFormatException e) {
			throw new ImageAnalysisParameterException(e);
		}
	}

	public boolean getParameterAsBoolean(String p) throws ImageAnalysisParameterException {
		try {
			return Boolean.parseBoolean(parameters.get(p));
		} catch (Exception e) {
			throw new ImageAnalysisParameterException(e);
		}
	}

	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	public void setParameter(String p, String v) {
		parameters.put(p, v);
	}

	public void setParameter(String p, int v) {
		setParameter(p, Integer.toString(v));
	}

	public void setParameter(String p, double v) {
		setParameter(p, Double.toString(v));
	}

	public void setParameter(String p, boolean v) {
		setParameter(p, Boolean.toString(v));
	}

	public void removeParameter(String p) {
		parameters.remove(p);
	}

	public int size() {
		return parameters.size();
	}

	public void addAll(ImageAnalysisParameters other, boolean replace) throws ImageAnalysisParameterException {
		for (String p : other) {
			if (replace || !containsParameter(p)) {
				setParameter(p, other.getParameterAsString(p));
			}
		}
	}

	public Iterator<String> iterator() {
		return parameters.keySet().iterator();
	}

	public ImageAnalysisParameters clone() throws CloneNotSupportedException {
		return (ImageAnalysisParameters)(super.clone());
	}	
}
