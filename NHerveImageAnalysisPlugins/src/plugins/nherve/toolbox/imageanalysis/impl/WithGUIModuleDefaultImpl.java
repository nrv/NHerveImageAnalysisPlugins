package plugins.nherve.toolbox.imageanalysis.impl;

import icy.gui.component.ComponentUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import plugins.nherve.toolbox.imageanalysis.ImageAnalysisGUI;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisModule;
import plugins.nherve.toolbox.imageanalysis.ImageAnalysisParameters;
import plugins.nherve.toolbox.plugin.PluginHelper;


public abstract class WithGUIModuleDefaultImpl extends ImageAnalysisModule {
	private DecimalFormat df;
	private ImageAnalysisGUI gui;
	
	public WithGUIModuleDefaultImpl(String name) {
		super(name);
		setGui(null);
		df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	}

	public abstract void populateGUI(ImageAnalysisParameters defaultParameters, JPanel panel);
	
	@Override
	public JPanel createGUI(ImageAnalysisParameters defaultParameters) {
		JPanel mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(new TitledBorder(getName()));

		populateGUI(defaultParameters, mainPanel);

		return mainPanel;
	}

	public ImageAnalysisGUI getGui() {
		return gui;
	}
	
	public void setGui(ImageAnalysisGUI gui) {
		this.gui = gui;
	}

	protected JPanel createComponent(String label, JComponent jc) {
		JPanel smp = new JPanel();
		smp.setOpaque(false);
		smp.setLayout(new BoxLayout(smp, BoxLayout.LINE_AXIS));
	
		JLabel lb = new JLabel(label);
		smp.add(lb);
		smp.add(jc);
		smp.add(Box.createHorizontalGlue());
	
		return smp;
	}

	protected JPanel createTextField(String label, JTextField tf, double val) {
		return createTextField(label, tf, df.format(val));
	}

	protected JPanel createTextField(String label, JTextField tf, int val) {
		return createTextField(label, tf, Integer.toString(val));
	}

	protected JPanel createTextField(String label, JTextField tf, String val) {
		tf.setName(label);
		tf.setText(val);
		ComponentUtil.setFixedSize(tf, new Dimension(100, 25));
		return createComponent(label, tf);
	}
	
	protected JPanel createFileSelectionField(String label, final JTextField tf, final String ext, final String pref) {
		JPanel smp = new JPanel();
		smp.setOpaque(false);
		smp.setLayout(new BoxLayout(smp, BoxLayout.LINE_AXIS));
	
		JLabel lb = new JLabel(label);
		smp.add(lb);
		
		tf.setName(label);
		smp.add(tf);
		
		JButton bt = new JButton("Browse");
		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File defaultFile = null;
				File f = new File(tf.getText());
				if (f.isFile() && f.exists()) {
					defaultFile = f;
				}

				PluginHelper.fileChooserTF(JFileChooser.FILES_ONLY, ext, "*" + ext, getPreferences().node(pref), "Choose file", tf, defaultFile);
				return;
			}
		});
		smp.add(bt);
		
		smp.add(Box.createHorizontalGlue());
	
		return smp;
	}
}
