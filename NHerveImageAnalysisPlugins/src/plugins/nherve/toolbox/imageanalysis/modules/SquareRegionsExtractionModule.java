package plugins.nherve.toolbox.imageanalysis.modules;

import icy.image.IcyBufferedImage;

import java.util.ArrayList;
import java.util.List;

import plugins.nherve.toolbox.image.feature.SegmentableBufferedImage;
import plugins.nherve.toolbox.image.feature.SupportRegion;
import plugins.nherve.toolbox.image.feature.region.GridFactory;
import plugins.nherve.toolbox.image.feature.region.Pixel;
import plugins.nherve.toolbox.image.feature.region.RectangleSupportRegion;
import plugins.nherve.toolbox.image.mask.Mask;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisContext;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisException;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameterException;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameters;
import plugins.nherve.toolbox.imageanalysis.impl.WithoutGUIModuleDefaultImpl;

public class SquareRegionsExtractionModule extends WithoutGUIModuleDefaultImpl {
	public final static String PRM_MASK_LABEL = "MASK_LABEL";
	public final static String PRM_W = "W";
	public final static String PRM_KEEP_PIXELS = "KEEP_PIXELS";
	public final static String RES_REGIONS = "REGIONS";
	public final static String RES_PIXELS = "PIXELS";

	public SquareRegionsExtractionModule(String name) {
		super(name);
		
		addNeededParameter(PRM_W);
		addNeededParameter(PRM_KEEP_PIXELS);
	}

	@Override
	public void populateWithDefaultParameterValues(ImageAnalysisParameters parameters) {
		setParameter(parameters, PRM_W, 5);
		setParameter(parameters, PRM_KEEP_PIXELS, true);
	}

	@Override
	public boolean analyze(ImageAnalysisContext context) throws ImageAnalysisException {
		Mask msk = null;
		try {
			String mn = getParameterAsString(context, PRM_MASK_LABEL);
			msk = context.getStack().getByLabel(mn);
		} catch (ImageAnalysisParameterException e) {
		}

		
		IcyBufferedImage image = context.getWorkingImage();
		SegmentableBufferedImage simg = new SegmentableBufferedImage(image);
		
		int w = getParameterAsInt(context, PRM_W);
		
		List<Pixel> pixels = null;
		if (msk != null) {
			pixels = GridFactory.getMaskAsPixels(msk);
		} else {
			pixels = GridFactory.getAllPixels(simg);
		}
		
		List<SupportRegion> squares = new ArrayList<SupportRegion>();
		for (Pixel px : pixels) {
			RectangleSupportRegion rsr = new RectangleSupportRegion(simg, (int) px.x, (int) px.y, w);
			squares.add(rsr);
		}
		
		putObject(context, getParameterInternalName(RES_REGIONS), squares);
		
		if (getParameterAsBoolean(context, PRM_KEEP_PIXELS)) {
			putObject(context, getParameterInternalName(RES_PIXELS), pixels);
		}
		
		return true;
	}
}
