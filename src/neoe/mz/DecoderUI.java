package neoe.mz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import neoe.swing.Layout;
import neoe.util.U;

public class DecoderUI {
	public static class MainPanel extends JComponent {
		private JButton jb1;
		private JTextField jtf1;
		private JTextField jtf2;
		private JButton jb2;
		private JButton jb3;
		private JLabel jl11, jl12, jl13, jl21, jl22, jl23, jl31, jl32, jl33;
		private JLabel jlst;
		private JTextField jtf3;
		private JTextField jtf4;
		private JFrame frame;
		private JLabel jltime;
		private JRadioButton jrb1;
		private JRadioButton jrb2;
		private JLabel jlst1;
		private JLabel jlst2;
		private JCheckBox jcb1;
		private MzDecoder dec;
		private CheckMz check;

		MainPanel(JFrame frame) {
			dec = new MzDecoder();
			check = new CheckMz();
			this.frame = frame;
			Layout lay = new Layout(this);
			ButtonGroup bg = new ButtonGroup();
			lay.addComponent(new JLabel("MZ file:"));
			lay.addComponent(jtf1 = new JTextField(10));
			lay.addComponent(jb2 = new JButton("..."));
			installSelectFile(jb2, jtf1, JFileChooser.FILES_ONLY, false);
			lay.commitLine();
			lay.addComponent(jrb1 = new JRadioButton("Test Archive"));
			lay.commitLine();
			lay.addComponent(jrb2 = new JRadioButton("Extract to:"));
			lay.commitLine();
			bg.add(jrb1);
			bg.add(jrb2);
			jrb2.setSelected(true);
			lay.addComponent(jtf2 = new JTextField(10));
			lay.addComponent(jb3 = new JButton("..."));
			installSelectFile(jb3, jtf2, JFileChooser.DIRECTORIES_ONLY, false);
			lay.commitLine();
			lay.addComponent(jlst1 = new JLabel(" "));
			lay.commitLine();
			lay.addComponent(jlst2 = new JLabel(" "));
			lay.commitLine();
			lay.addComponent(jb1 = new JButton("start"));
			lay.addComponent(jcb1 = new JCheckBox("pause", false));
			lay.commitLine();
			jcb1.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					boolean p = jcb1.isSelected();
					dec.pause = p;
					check.pause = p;
				}
			});
			jb1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jb1.setEnabled(false);
					try {
						if (jrb1.isSelected()) {
							testMxz();
						} else if (jrb2.isSelected()) {
							decodeMxz();
						}
					} catch (Throwable e1) {
						e1.printStackTrace();
						alert(e1.toString(), "error");
						jb1.setEnabled(true);
					}

				}
			});

		}

		protected void testMxz() {
			final String from = jtf1.getText().trim();

			new Thread() {
				public void run() {
					try {
						check.run(from);
					} catch (Throwable e) {
						e.printStackTrace();
						alert(e.toString(), "error");
						jb1.setEnabled(true);
					}
				}
			}.start();
			new Thread() {
				public void run() {
					try {
						// monitor
						while (!check.finished) {
							jlst1.setText(String.format(
									"Checking files(part):%,d(%,d), dirs:%,d, bytes:%,d, archives:%,d",
									check.totalfile.longValue(), check.totalpartfile.longValue(),
									check.totaldir.longValue(), check.totalbs.longValue(), check.archive.longValue()));
							jlst2.setText(check.extraFn);
							jlst1.repaint();
							jlst2.repaint();
							U.sleep(1000);
						}
						jlst1.setText(String.format("Checking files(part):%,d(%,d), dirs:%,d, bytes:%,d, archives:%,d",
								check.totalfile.longValue(), check.totalpartfile.longValue(),
								check.totaldir.longValue(), check.totalbs.longValue(), check.archive.longValue()));
						jlst2.setText("Check finished!");
						jlst1.repaint();
						jlst2.repaint();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();

		}

		protected void decodeMxz() {
			final String from = jtf1.getText().trim();
			final String to = jtf2.getText().trim();

			new Thread() {
				public void run() {
					try {
						dec.run(from, to);
					} catch (Throwable e) {
						e.printStackTrace();
						alert(e.toString(), "error");
						jb1.setEnabled(true);
					}
				}
			}.start();
			new Thread() {
				public void run() {
					try {
						// monitor
						while (!dec.finished) {
							long sec = (System.currentTimeMillis() - dec.t1) / 1000;
							jlst1.setText(String.format(
									"Extracted files(part):%,d(%,d), dirs:%,d, bytes:%,d, archives:%,d, time:%,d sec",
									dec.totalfile.longValue(), dec.totalpartfile.longValue(), dec.totaldir.longValue(),
									dec.totalbs.longValue(), dec.archive.longValue(), sec));
							jlst2.setText(dec.extraFn);
							jlst1.repaint();
							jlst2.repaint();
							U.sleep(1000);
						}
						jlst1.setText(
								String.format("Total extracted files(part):%,d(%,d), dirs:%,d, bytes:%,d, archives:%,d",
										dec.totalfile.longValue(), dec.totalpartfile.longValue(),
										dec.totaldir.longValue(), dec.totalbs.longValue(), dec.archive.longValue()));
						long sec2 = (System.currentTimeMillis() - dec.t1) / 1000;
						jlst2.setText(String.format("Extract finished! Used:%,d sec", sec2));
						jlst1.repaint();
						jlst2.repaint();
						msg("Program will exit", "Done");
						frame.dispose();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();

		}

		private void installSelectFile(final JButton jb, final JTextField jtf, final int fileDirMode,
				final boolean multi) {
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser jfc = new JFileChooser();
					String s = jtf.getText().trim();
					if (s.length() == 0)
						s = ".";
					jfc.setCurrentDirectory(new File(s));
					jfc.setFileSelectionMode(fileDirMode);
					jfc.setMultiSelectionEnabled(multi);
					int ret = jfc.showSaveDialog(MainPanel.this);
					if (ret == JFileChooser.APPROVE_OPTION) {
						jtf.setText(jfc.getSelectedFile().getAbsolutePath());
					}
				}
			});
		}

		protected void msg(String m, String title) {
			JOptionPane.showMessageDialog(MainPanel.this, m, title, JOptionPane.INFORMATION_MESSAGE);
		}

		private void alert(String m, String title) {
			JOptionPane.showMessageDialog(MainPanel.this, m, title, JOptionPane.ERROR_MESSAGE);

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("MZ Decoder " + C.VER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MainPanel(frame));
		frame.pack();
		frame.setSize(600, frame.getHeight() + 20);
		frame.setVisible(true);
	}

}
