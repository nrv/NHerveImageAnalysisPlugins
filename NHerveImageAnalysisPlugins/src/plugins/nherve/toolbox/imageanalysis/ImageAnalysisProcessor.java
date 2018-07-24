package plugins.nherve.toolbox.imageanalysis;

import icy.image.IcyBufferedImage;
import icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import plugins.nherve.toolbox.image.mask.MaskStack;

public class ImageAnalysisProcessor {

	private class IAPWorker extends Thread {
		public IAPWorker(ImageAnalysisContext context, ImageAnalysisModule module) {
			super();
			this.module = module;
			this.context = context;
			lst = null;
		}

		public IAPWorker(ImageAnalysisContext context, ImageAnalysisModule module, ImageAnalysisProcessListener lst) {
			super();
			this.module = module;
			this.context = context;
			this.lst = lst;
		}

		private ImageAnalysisModule module;
		private ImageAnalysisContext context;
		private ImageAnalysisProcessListener lst;

		private void process() throws ImageAnalysisException {
			module.checkParametersBeforeAnalysis(context);
			module.info("About to launch " + module.getName());
			module.setState(ImageAnalysisModule.RUNNING, false);
			if (module.analyze(context)) {
				module.info("Just after analyze " + module.getName());
				module.setState(ImageAnalysisModule.FINISHED, false);
			}
		}

		private void processAndNotify() {
			try {
				process();

				ThreadUtil.invokeLater(new Runnable() {
					@Override
					public void run() {
						lst.notifyProcessEnded(module);
					}
				});
			} catch (ImageAnalysisException e) {
				module.setState(ImageAnalysisModule.STOPPED, false);
				e.printStackTrace();
			}
		}

		private void processAndWait() {
			try {
				process();
			} catch (ImageAnalysisException e) {
				module.setState(ImageAnalysisModule.STOPPED, false);
				e.printStackTrace();
			}
		}

		@Override
		public void interrupt() {
			try {
				module.stopAnalyze(context);
			} catch (ImageAnalysisException e) {
				e.printStackTrace();
			}
			super.interrupt();
		}

		@Override
		public void run() {
			if (lst == null) {
				processAndWait();
			} else {
				processAndNotify();
			}
		}

	}

	private ImageAnalysisParameters defaultParameters;
	private boolean stopping;
	private List<IAPWorker> running;

	public ImageAnalysisProcessor() {
		super();

		running = Collections.synchronizedList(new ArrayList<IAPWorker>());
		stopping = false;
	}

	public ImageAnalysisProcessor(ImageAnalysisParameters defaultParameters) {
		this();

		setDefaultParameters(defaultParameters);
	}

	public MaskStack process(ImageAnalysisModule module, IcyBufferedImage image, boolean display) throws ImageAnalysisException {
		if (!stopping) {
			ImageAnalysisContext context = new ImageAnalysisContext();
			context.addAllParameters(defaultParameters, true);
			context.setWorkingImage(image);
			context.setWorkingName("");
			MaskStack stack = new MaskStack(image.getWidth(), image.getHeight());
			context.setStack(stack);

			processAndWait(context, module, display);

			return stack;
		}

		return null;
	}

	public void process(ImageAnalysisModule module, boolean display) throws ImageAnalysisException {
		if (!stopping) {
			ImageAnalysisContext context = new ImageAnalysisContext();
			context.addAllParameters(defaultParameters, true);

			processAndWait(context, module, display);
		}
	}

	public boolean processAndNotify(final ImageAnalysisContext context, final ImageAnalysisModule module, final ImageAnalysisProcessListener lst, final boolean display) throws ImageAnalysisException {
		if (!stopping) {
			IAPWorker t = new IAPWorker(context, module, lst);

			synchronized (running) {
				running.add(t);
			}
			t.start();
			return true;
		} else {
			return false;
		}
	}

	public boolean processAndWait(final ImageAnalysisContext context, final ImageAnalysisModule module, final boolean display) throws ImageAnalysisException {
		if (!stopping) {
			IAPWorker t = new IAPWorker(context, module);

			synchronized (running) {
				running.add(t);
			}
			t.start();
			try {
				t.join();
			} catch (InterruptedException e) {
				module.stopAnalyze(context);
				module.setState(ImageAnalysisModule.STOPPED, false);
			}
			synchronized (running) {
				running.remove(t);
			}
			return true;
		} else {
			return false;
		}
	}

	public boolean processParallelAndWait(final ImageAnalysisContext context, final List<ImageAnalysisModule> modules, final boolean display) throws ImageAnalysisException {
		if (!stopping) {

			List<IAPWorker> localWorkers = new ArrayList<ImageAnalysisProcessor.IAPWorker>();

			for (ImageAnalysisModule module : modules) {
				IAPWorker t = new IAPWorker(context, module);
				localWorkers.add(t);
				synchronized (running) {
					running.add(t);
				}
				t.start();
			}

			for (IAPWorker t : localWorkers) {
				try {
					t.join();
				} catch (InterruptedException e) {
					t.module.stopAnalyze(context);
					t.module.setState(ImageAnalysisModule.STOPPED, false);
				} finally {
					synchronized (running) {
						running.remove(t);
					}
				}
			}
			

			return true;
		} else {
			return false;
		}
	}

	public ImageAnalysisParameters getDefaultParameters() {
		return defaultParameters;
	}

	public void setDefaultParameters(ImageAnalysisParameters defaultParameters) {
		this.defaultParameters = defaultParameters;
	}

	public void stopRunningProcesses() {
		stopping = true;
		synchronized (running) {
			for (IAPWorker t : running) {
				t.interrupt();
			}
			running.clear();
		}
	}

	public void reInit() {
		stopping = false;
	}
}
