package plugins.nherve.toolbox.imageanalysis.modules;

import icy.image.IcyBufferedImage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import plugins.nherve.toolbox.concurrent.TaskManager;
import plugins.nherve.toolbox.image.feature.SegmentableIcyBufferedImage;
import plugins.nherve.toolbox.image.feature.SignatureExtractor;
import plugins.nherve.toolbox.image.feature.IcySupportRegion;
import plugins.nherve.toolbox.image.feature.descriptor.MultiThreadedSignatureExtractor;
import plugins.nherve.toolbox.image.feature.descriptor.MultiThreadedSignatureExtractor.Listener;
import plugins.nherve.toolbox.image.feature.lbp.LBPToolbox;
import plugins.nherve.toolbox.image.feature.lbp.LocalBinaryPattern;
import plugins.nherve.toolbox.image.feature.region.IcyPixel;
import plugins.nherve.toolbox.image.feature.signature.DefaultVectorSignature;
import plugins.nherve.toolbox.image.feature.signature.SignatureException;
import plugins.nherve.toolbox.image.feature.signature.VectorSignature;
import plugins.nherve.toolbox.image.toolboxes.ColorSpaceTools;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisContext;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisException;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameters;
import plugins.nherve.toolbox.imageanalysis.impl.WithoutGUIModuleDefaultImpl;

public class LBPSignaturesExtractionModule extends WithoutGUIModuleDefaultImpl {
	public final static String PRM_P = "P";
	public final static String PRM_R = "R";
	public final static String PRM_T = "T";
	public final static String PRM_V = "V";
	public final static String PRM_I = "I";
	public final static String PRM_REGIONS = "REGIONS";
	public final static String PRM_PIXELS = "PIXELS";
	public final static String PRM_COLOR = "COLOR";
	public final static String GRAY = "GRAY";
	public final static String RES_SIGNATURES = "SIGNATURES";

	private final static String OBJ_TM = "OBJ_TM";

	private List<MultiThreadedSignatureExtractor.Listener> listeners;

	public LBPSignaturesExtractionModule(String name) {
		super(name);

		addNeededParameter(PRM_P);
		addNeededParameter(PRM_R);
		addNeededParameter(PRM_T);
		addNeededParameter(PRM_V);
		addNeededParameter(PRM_I);
		addNeededParameter(PRM_REGIONS);
		addNeededParameter(PRM_PIXELS);
		addNeededParameter(PRM_COLOR);

		listeners = new ArrayList<MultiThreadedSignatureExtractor.Listener>();
	}

	public boolean add(MultiThreadedSignatureExtractor.Listener e) {
		return listeners.add(e);
	}

	@Override
	public void populateWithDefaultParameterValues(ImageAnalysisParameters parameters) {
		setParameter(parameters, PRM_P, 8);
		setParameter(parameters, PRM_R, 1);
		setParameter(parameters, PRM_T, 25);
		setParameter(parameters, PRM_V, 1);
		setParameter(parameters, PRM_I, false);
		setParameter(parameters, PRM_COLOR, ColorSpaceTools.COLOR_SPACES[ColorSpaceTools.RGB_TO_I1H2H3]);
	}

	@Override
	public boolean analyze(ImageAnalysisContext context) throws ImageAnalysisException {
		int p = getParameterAsInt(context, PRM_P);
		int v = getParameterAsInt(context, PRM_V);
		boolean i = getParameterAsBoolean(context, PRM_I);
		double r = getParameterAsDouble(context, PRM_R);
		double t = getParameterAsDouble(context, PRM_T);

		info("LBP P("+p+") R("+r+") T("+t+") V("+v+") I("+i+")");
		
		LocalBinaryPattern desc = new LocalBinaryPattern(p, r, LBPToolbox.FUZZY_FUNCTION_STANDARD, t, isLogEnabled(), true, v, i);
		desc.setFuzzyColorSpace(ColorSpaceTools.RGB_TO_I1H2H3);
		
		if (getParameterAsString(context, PRM_COLOR).equalsIgnoreCase(GRAY)) {	
			info("LBP on gray levels");
			desc.setFuzzyAllChannels(false);
			desc.setFuzzyChannel(0);
		} else {
			desc.setFuzzyAllChannels(true);
			int ics = 0;
			for (String cs : ColorSpaceTools.COLOR_SPACES) {
				if (getParameterAsString(context, PRM_COLOR).equalsIgnoreCase(cs)) {
					desc.setFuzzyColorSpace(ics);
					info("LBP on 3 color channels ("+cs+")");
					break;
				}
			}
		}

		IcyBufferedImage image = context.getWorkingImage();
		SegmentableIcyBufferedImage simg = new SegmentableIcyBufferedImage(image);

		try {
			@SuppressWarnings("unchecked")
			List<IcySupportRegion> squares = (List<IcySupportRegion>) getObject(context, getParameterAsString(context, PRM_REGIONS));

			desc.preProcess(simg);
			MultiThreadedSignatureExtractor<SegmentableIcyBufferedImage> signatureExtractor = new MultiThreadedSignatureExtractor<SegmentableIcyBufferedImage>(desc);

			for (Listener l : listeners) {
				signatureExtractor.add(l);
			}

			TaskManager tm = TaskManager.create();
			signatureExtractor.setTm(tm);

			putObject(context, getParameterInternalName(OBJ_TM), tm);
			List<DefaultVectorSignature> sigs = SignatureExtractor.cast(signatureExtractor.extractSignatures(simg, squares));
			if (Thread.currentThread().isInterrupted() || (sigs == null)) {
				return false;
			}
			tm.shutdownNow();
			removeObject(context, getParameterInternalName(OBJ_TM));

			@SuppressWarnings("unchecked")
			List<IcyPixel> pixels = (List<IcyPixel>) getObject(context, getParameterAsString(context, PRM_PIXELS));

			List<PixelSignatureData> myData = new ArrayList<PixelSignatureData>();
			Iterator<IcyPixel> iPixels = pixels.iterator();
			Iterator<DefaultVectorSignature> iSigs = sigs.iterator();
			while (iPixels.hasNext()) {
				PixelSignatureData aData = new PixelSignatureData();
				aData.pix = iPixels.next();
				aData.sig = (DefaultVectorSignature) (iSigs.next());
				myData.add(aData);
			}

			putObject(context, getParameterInternalName(RES_SIGNATURES), myData);
			desc.postProcess(simg);
		} catch (SignatureException e) {
			throw new ImageAnalysisException(e);
		}

		return true;
	}

	@Override
	public void stopAnalyze(ImageAnalysisContext context) throws ImageAnalysisException {
		super.stopAnalyze(context);

		TaskManager tm = (TaskManager) getObject(context, getParameterInternalName(OBJ_TM));
		if (tm != null) {
			tm.shutdownNow();
			removeObject(context, getParameterInternalName(OBJ_TM));
		}
	}
}
