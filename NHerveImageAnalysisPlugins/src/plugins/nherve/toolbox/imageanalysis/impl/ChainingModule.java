package plugins.nherve.toolbox.imageanalysis.impl;

import icy.gui.component.PopupPanel;
import icy.gui.dialog.MessageDialog;
import icy.gui.util.GuiUtil;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import plugins.nherve.maskeditor.MaskEditor;
import plugins.nherve.toolbox.NherveToolbox;
import plugins.nherve.toolbox.image.mask.MaskStack;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisContext;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisException;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisModule;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisModuleListener;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameters;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisProcessListener;

public abstract class ChainingModule extends ModuleGroup implements ActionListener, ImageAnalysisProcessListener {

	private class LaunchButton extends JButton implements ImageAnalysisModuleListener {
		private static final long serialVersionUID = -7597996048640565055L;

		public LaunchButton(ImageAnalysisModule module) {
			super(NherveToolbox.playIcon);

			setToolTipText("Launch " + module.getName());
			setName("Launch");

			module.addListener(this);
		}

		@Override
		public void statutsChanged(ImageAnalysisModule m) {
			final ImageAnalysisModule m2 = m;
			ThreadUtil.invokeLater(new Runnable() {
				@Override
				public void run() {
					switch (m2.getState()) {
					case ImageAnalysisModule.STOPPED:
						setEnabled(true);
						break;
					case ImageAnalysisModule.RUNNING:
						setEnabled(false);
						break;
					case ImageAnalysisModule.FINISHED:
						setEnabled(false);
						break;
					default:
						break;
					}
				}
			});
		}

	}

	private class ClearButton extends JButton implements ImageAnalysisModuleListener {
		private static final long serialVersionUID = 1111149655073178441L;

		public ClearButton(ImageAnalysisModule module) {
			super(NherveToolbox.crossIcon);

			setToolTipText("Clear " + module.getName() + " results");
			setName("Clear");

			module.addListener(this);
		}

		@Override
		public void statutsChanged(ImageAnalysisModule m) {
			final ImageAnalysisModule m2 = m;
			ThreadUtil.invokeLater(new Runnable() {
				@Override
				public void run() {
					switch (m2.getState()) {
					case ImageAnalysisModule.STOPPED:
						setEnabled(false);
						break;
					case ImageAnalysisModule.RUNNING:
						setEnabled(false);
						break;
					case ImageAnalysisModule.FINISHED:
						setEnabled(true);
						break;
					default:
						break;
					}
				}
			});

		}
	}

	private class TrafficLights extends JPanel implements ImageAnalysisModuleListener {
		private static final long serialVersionUID = -7469348910886935148L;
		private int state;

		public TrafficLights() {
			super();
			state = ImageAnalysisModule.STOPPED;
		}

		@Override
		public Dimension getMaximumSize() {
			return TL_DIM;
		}

		@Override
		public Dimension getMinimumSize() {
			return TL_DIM;
		}

		@Override
		public Dimension getPreferredSize() {
			return TL_DIM;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			switch (state) {
			case ImageAnalysisModule.STOPPED:
				g2.setColor(Color.RED);
				break;
			case ImageAnalysisModule.RUNNING:
				g2.setColor(Color.ORANGE);
				break;
			case ImageAnalysisModule.FINISHED:
				g2.setColor(Color.GREEN);
				break;
			default:
				g2.setColor(getBackground());
				break;
			}
			
			g2.fillOval((getWidth() - DIM) / 2, (getHeight() - DIM) / 2, DIM - 1, DIM - 1);
		}

		@Override
		public void statutsChanged(ImageAnalysisModule m) {
			final ImageAnalysisModule m2 = m;
			ThreadUtil.invokeLater(new Runnable() {
				@Override
				public void run() {
					state = m2.getState();
					repaint();
				}
			});

		}
	}

	private Map<JButton, ImageAnalysisModule> moduleLaunchers;
	private Map<ImageAnalysisModule, JCheckBox> moduleSelected;
	private boolean usePopUp;
	private boolean popUpExpandedByDefault;
	private boolean parallelProcessingActivated;
	private static final int DIM = 15;

	private static final Dimension TL_DIM = new Dimension(DIM + 2, DIM + 2);

	public ChainingModule(String name) {
		this(name, true);
	}

	public ChainingModule(String name, boolean usePopUp) {
		super(name);

		this.usePopUp = usePopUp;
		moduleLaunchers = new HashMap<JButton, ImageAnalysisModule>();
		moduleSelected = null;

		setParallelProcessingActivated(false);
		setPopUpExpandedByDefault(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();

		if (o == null) {
			return;
		}

		if (o instanceof JButton) {
			JButton b = (JButton) o;
			ImageAnalysisModule m = moduleLaunchers.get(b);

			if (getGui().hasCurrentSequence()) {
				Sequence current = getGui().getCurrentSequence();
				if (current != null) {
					ImageAnalysisContext context = getGui().getBackupObject();

					MaskEditor me = MaskEditor.getRunningInstance(true);
					MaskStack stack = me.getBackupObject();
					context.setStack(stack);

					if (b.getName().equals("Launch")) {
						m.getParametersFromGui(modulePanels.get(m), context);

						try {
							b.setEnabled(false);
							if (!context.processAndNotify(m, this, isLogEnabled())) {
								b.setEnabled(true);
								MessageDialog.showDialog("Unable to launch", MessageDialog.INFORMATION_MESSAGE);
							}
						} catch (ImageAnalysisException e1) {
							throw new RuntimeException(e1);
						}
					} else if (b.getName().equals("Clear")) {
						stack.removeMaskWithTag(m.getName());
						m.setState(ImageAnalysisModule.STOPPED, true);
					}

					me.refreshInterface();
				}
			}
		}
	}



	@Override
	public boolean analyze(ImageAnalysisContext context) throws ImageAnalysisException {
		Iterator<ImageAnalysisModule> it = iterator();
		ImageAnalysisModule module = null;
		while (it.hasNext()) {
			module = it.next();
			if ((moduleSelected == null) || (moduleSelected.get(module).isSelected())) {
				if (isParallelProcessingActivated() && module.isLinkWithFollowing()) {
					List<ImageAnalysisModule> mods = new ArrayList<ImageAnalysisModule>();
					mods.add(module);
					while (it.hasNext() && module.isLinkWithFollowing()) {
						module = it.next();
						if ((moduleSelected == null) || (moduleSelected.get(module).isSelected())) {
							mods.add(module);
						}
					}

					if (!context.processParallelAndWait(mods, isLogEnabled())) {
						return false;
					}
				} else {
					if (!context.processAndWait(module, isLogEnabled())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public JPanel createGUI(ImageAnalysisParameters defaultParameters) {
		moduleSelected = new HashMap<ImageAnalysisModule, JCheckBox>();

		JPanel panelToReturn = null;
		JPanel mainPanel = null;

		if (usePopUp) {
			PopupPanel popup = new PopupPanel(getName());
			popup.setExpanded(isPopUpExpandedByDefault());
	        mainPanel = popup.getMainPanel();
	 
	        JPanel container = new JPanel();
	        container.setLayout(new BorderLayout());
	        container.add(popup, BorderLayout.NORTH);
	        container.add(Box.createGlue(), BorderLayout.CENTER);
	        
	        popup.setBorder(null);
	        mainPanel.setBorder(BorderFactory.createTitledBorder(""));
	 
	        panelToReturn = container;
			
		} else {
			mainPanel = new JPanel();
			panelToReturn = mainPanel;
			mainPanel.setBorder(new TitledBorder(getName()));
		}

		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		moduleLaunchers.clear();
		modulePanels.clear();
		moduleSelected.clear();

		for (ImageAnalysisModule m : this) {
			JPanel modulePanel = new JPanel();
			modulePanel.setOpaque(false);
			modulePanel.setLayout(new BorderLayout());

			JPanel moduleGui = m.createGUI(defaultParameters);
			if (moduleGui != null) {
				JButton btStart = new LaunchButton(m);
				btStart.addActionListener(this);
				moduleLaunchers.put(btStart, m);
				modulePanels.put(m, moduleGui);

				JButton btClear = new ClearButton(m);
				btClear.addActionListener(this);
				btClear.setEnabled(false);
				moduleLaunchers.put(btClear, m);

				JCheckBox bx = new JCheckBox();
				bx.setSelected(true);
				moduleSelected.put(m, bx);

				TrafficLights lights = new TrafficLights();
				m.addListener(lights);

				JPanel buttons = GuiUtil.createLineBoxPanel(new Component[] { Box.createHorizontalGlue(), bx, lights, btStart, btClear, Box.createHorizontalStrut(10), Box.createHorizontalGlue() });

				modulePanel.add(buttons, BorderLayout.WEST);
				modulePanel.add(moduleGui, BorderLayout.CENTER);
				mainPanel.add(modulePanel);
			}
		}

		return panelToReturn;
	}

	@Override
	public void notifyProcessEnded(ImageAnalysisModule module) {

	}

	@Override
	public void populateGUI(ImageAnalysisParameters defaultParameters, JPanel panel) {
		// Nothing to do here, everything is in createGUI.
	}

	@Override
	public void setState(int state, boolean propagate) {
		super.setState(state, propagate);

		if (propagate) {
			for (ImageAnalysisModule m : this) {
				m.setState(state, propagate);
			}
		}
	}

	public boolean isParallelProcessingActivated() {
		return parallelProcessingActivated;
	}

	public void setParallelProcessingActivated(boolean parallelProcessingActivated) {
		this.parallelProcessingActivated = parallelProcessingActivated;
	}

	public boolean isPopUpExpandedByDefault() {
		return popUpExpandedByDefault;
	}

	public void setPopUpExpandedByDefault(boolean popUpExpandedByDefault) {
		this.popUpExpandedByDefault = popUpExpandedByDefault;
	}

}
