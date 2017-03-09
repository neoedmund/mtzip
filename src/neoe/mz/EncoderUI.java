package neoe.mz;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import neoe.swing.Layout;
import neoe.util.U;

public class EncoderUI {

	public static class MainPanel extends JComponent {
		private JButton jb1;
		private JTextField jtf1;
		private JTextField jtf2;
		private JButton jb2;
		private JButton jb3;
		private JLabel jl11, jl12, jl13, jl21, jl22, jl23, jl31, jl32, jl33, jl41, jl42, jl43;
		private JLabel jlst;
		private JTextField jtf3;
		private JTextField jtf4;
		private JFrame frame;
		private JLabel jltime;
		private JCheckBox jchkconcat;

		MainPanel(JFrame frame) {
			this.frame = frame;
			Layout lay = new Layout(this);
			lay.addComponent(new JLabel("Source:"));
			lay.addComponent(jtf1 = new JTextField(10));
			lay.addComponent(jb2 = new JButton("..."));
			lay.commitLine();
			lay.addComponent(new JLabel("zip to:"));
			lay.addComponent(jtf2 = new JTextField(10));
			lay.addComponent(jb3 = new JButton("..."));
			lay.commitLine();
			lay.addComponent(new JLabel("Threads for file reading:"));
			lay.addComponent(jtf3 = new JTextField(3));
			lay.commitLine();
			int cores = Runtime.getRuntime().availableProcessors();
			jtf3.setText("" + cores);
			lay.addComponent(new JLabel("Threads for MZ encoding:"));
			lay.addComponent(jtf4 = new JTextField(3));
			jtf4.setText("" + cores);
			lay.commitLine();
			lay.addComponent(jchkconcat = new JCheckBox("Concat result file", false));
			lay.commitLine();
			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout(0, 4));
			p1.add(new JLabel());
			p1.add(new JLabel("Files"));
			p1.add(new JLabel("Directorys"));
			p1.add(new JLabel("Bytes"));
			p1.add(new JLabel("Total:"));
			p1.add(jl11 = new JLabel());
			p1.add(jl12 = new JLabel());
			p1.add(jl13 = new JLabel());
			p1.add(new JLabel("Submit:"));
			p1.add(jl21 = new JLabel());
			p1.add(jl22 = new JLabel());
			p1.add(jl23 = new JLabel());
			p1.add(new JLabel("Submit%:"));
			p1.add(jl31 = new JLabel());
			p1.add(jl32 = new JLabel());
			p1.add(jl33 = new JLabel());
			p1.add(new JLabel("Done:"));
			p1.add(jl41 = new JLabel());
			p1.add(jl42 = new JLabel());
			p1.add(jl43 = new JLabel());
			lay.addComponent(new JScrollPane(p1));
			lay.commitLine();
			lay.addComponent(jlst = new JLabel("ready."));
			lay.commitLine();
			lay.addComponent(jltime = new JLabel(" "));
			lay.commitLine();
			lay.addComponent(jb1 = new JButton("Start"));
			lay.commitLine();

			// ---------------
			jb1.setEnabled(false);
			jb2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser jfc = new JFileChooser();
					String s = jtf1.getText().trim();
					if (s.length() == 0)
						s = ".";
					jfc.setCurrentDirectory(new File(s));
					jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					jfc.setMultiSelectionEnabled(false);
					int ret = jfc.showOpenDialog(MainPanel.this);
					if (ret == JFileChooser.APPROVE_OPTION) {
						jtf1.setText(jfc.getSelectedFile().getAbsolutePath());
						jtfChanged();
					}

				}
			});
			jtf1.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					jtfChanged();
				}
			});
			jtf2.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					jtfChanged();
				}
			});
			jb3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser jfc = new JFileChooser();
					String s = jtf2.getText().trim();
					if (s.length() == 0)
						s = ".";
					jfc.setCurrentDirectory(new File(s));
					jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jfc.setMultiSelectionEnabled(false);
					int ret = jfc.showSaveDialog(MainPanel.this);
					if (ret == JFileChooser.APPROVE_OPTION) {
						jtf2.setText(jfc.getSelectedFile().getAbsolutePath());
						jtfChanged();
					}
				}
			});
			jtfChanged();
			jb1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jb1.setEnabled(false);
					jb1.setVisible(false);
					jtf1.setEditable(false);
					jtf2.setEditable(false);
					jb2.setEnabled(false);
					jb3.setEnabled(false);
					jtf3.setEnabled(false);
					jtf4.setEnabled(false);

					new Thread() {
						public void run() {
							try {
								doStart();
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}.start();

				}
			});
		}

		protected void jtfChanged() {
			if (jtf1.getText().trim().length() > 0 && jtf2.getText().trim().length() > 0) {
				try {
					int v1 = Integer.parseInt(jtf3.getText().trim());
					if (v1 <= 0) {
						jb1.setEnabled(false);
						jtf3.grabFocus();
						alert("Thread numbers must be a positive number.", "Cannot go!");
						return;
					}
				} catch (NumberFormatException e) {
					jb1.setEnabled(false);
					jtf3.grabFocus();
					alert("Thread numbers must be a positive number.", "Cannot go!");
					return;
				}
				try {
					int v1 = Integer.parseInt(jtf4.getText().trim());
					if (v1 <= 0) {
						jb1.setEnabled(false);
						jtf4.grabFocus();
						alert("Thread numbers must be a positive number.", "Cannot go!");
						return;
					}
				} catch (NumberFormatException e) {
					jb1.setEnabled(false);
					jtf4.grabFocus();
					alert("Thread numbers must be a positive number.", "Cannot go!");
					return;
				}

				jb1.setEnabled(true);
			} else {
				jb1.setEnabled(false);
			}

		}

		int totalDir, totalFile;
		long totalBytes;
		private long startTime;

		protected void doStart() throws InterruptedException {
			startTime = System.currentTimeMillis();

			{ // check save file
				try {
					File fsave = new File(jtf2.getText().trim());
					if (fsave.exists()) {
						alert("Target File already exists!", "Cannot proceed");
						initState();
						return;
					}
					FileOutputStream check = new FileOutputStream(fsave);
					check.write(0);
					check.close();
					fsave.delete();
				} catch (Exception e) {
					alert("Cannot write to target file!", "Cannot proceed");
					initState();
					return;
				}
			}
			timer = true;
			{
				new Thread() {
					public void run() {
						while (timer) {
							long ms = (System.currentTimeMillis() - startTime) / 1000;
							jltime.setText(ms + " sec");
							jltime.repaint();
							U.sleep(1000);
						}
					}
				}.start();
			}
			{// 1. preview dir
				final PreviewFiles pf = new PreviewFiles();
				Thread preview = new Thread() {
					public void run() {
						try {
							Object[] result = pf.run(jtf1.getText().trim());
							totalDir = (int) result[0];
							totalFile = (int) result[1];
							totalBytes = (long) result[2];
							jl11.setText(U.numberStr(totalFile));
							jl12.setText(U.numberStr(totalDir));
							jl13.setText(U.numberStr(totalBytes));
							jl11.repaint();
							jl12.repaint();
							jl13.repaint();
						} catch (IOException e) {
							e.printStackTrace();
							alert(e.toString(), "Cannot scan source files");
							initState();
							return;
						}
					}
				};
				jlst.setText("scanning source dir...");
				preview.start();
				// monitor thread
				new Thread() {
					public void run() {
						while (!pf.finished) {
							jl11.setText(U.numberStr(pf.totalFile));
							jl12.setText(U.numberStr(pf.totalDir));
							jl13.setText(U.numberStr(pf.getTotalBytes()));
							jl11.repaint();
							jl12.repaint();
							jl13.repaint();
							U.sleep(300);
						}
						startZip();
					}
				}.start();

			}

		}

		private boolean timer;

		protected void startZip() {
			jlst.setText("making MZ file...");
			final MzEncoder enc = new MzEncoder();
			new Thread() {

				public void run() {

					try {
						String fn = jtf2.getText().trim();
						enc.run(jtf1.getText().trim(), fn, Integer.parseInt(jtf3.getText().trim()),
								Integer.parseInt(jtf4.getText().trim()));
						jlst.setText("concat files");
						jlst.repaint();
						if (concatFile()) {
							new ConcatFile().run(fn);
						} else {
							System.out.println("skip concating file");
						}
						timer = false;
						msg(String.format("Finished:%s in %,d sec.", jtf2.getText().trim(),
								(System.currentTimeMillis() - startTime) / 1000), "Done");
						msg("Program will exit", "Done");
						frame.dispose();
					} catch (Throwable e) {
						e.printStackTrace();
						alert("" + e, "error");
					}
				}

			}.start();
			// monitor thread
			new Thread() {
				public void run() {
					while (!enc.finished) {
						jl21.setText(U.numberStr(enc.totalFile));
						jl22.setText(U.numberStr(enc.totalDir));
						jl23.setText(U.numberStr(enc.getTotalBytes()));
						jl31.setText(U.percent(enc.totalFile, totalFile));
						jl32.setText(U.percent(enc.totalDir, totalDir));
						jl33.setText(U.percent(enc.getTotalBytes(), totalBytes));
						jl43.setText(U.numberStr(enc.getDoneBytes()));
						jl21.repaint();
						jl22.repaint();
						jl23.repaint();
						jl31.repaint();
						jl32.repaint();
						jl33.repaint();
						U.sleep(500);
					}
				}
			}.start();
		}

		private boolean concatFile() {
			return jchkconcat.isSelected();
		}

		protected void msg(String m, String title) {
			JOptionPane.showMessageDialog(MainPanel.this, m, title, JOptionPane.INFORMATION_MESSAGE);
		}

		private void initState() {
			jtf1.setEditable(true);
			jtf2.setEditable(true);
			jb2.setEnabled(true);
			jb3.setEnabled(true);
			jtf3.setEnabled(true);
			jtf4.setEnabled(true);
			totalDir = 0;
			totalFile = 0;
			totalBytes = 0;
			jtfChanged();
		}

		private void alert(String m, String title) {
			JOptionPane.showMessageDialog(MainPanel.this, m, title, JOptionPane.ERROR_MESSAGE);

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new EncoderUI().run();

	}

	public void run() {
		JFrame frame = new JFrame("MZ Encoder "+C.VER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new MainPanel(frame));
		frame.pack();
		frame.setVisible(true);
	}

}
