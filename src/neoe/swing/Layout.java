package neoe.swing;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** a layout helper. very convience */
public class Layout {

	private JComponent container;

	JPanel row;

	private Font font;

	private Color color;

	private Color bkColor;

	private List<JComponent> compList;

	public Layout(JComponent container) {
		this.container = container;
		initContainer();
		compList = new ArrayList<JComponent>();
	}

	public void addComponent(JComponent comp) {
		confirmRow();
		row.add(comp);
		if (font != null)
			comp.setFont(font);
		if (color != null)
			comp.setForeground(color);
		if (bkColor != null) {
			comp.setBackground(bkColor);
			comp.setOpaque(true);
		}
		compList.add(comp);
	}

	private void confirmRow() {
		if (row == null) {
			row = new JPanel();
			row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
		}
	}

	void initContainer() {
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
	}

	public void commitLine() {
		if (row != null) {
			container.add(row);
			row = null;
		}
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setBkColor(Color color) {
		this.bkColor = color;
	}

	public List<JComponent> getCompList() {
		return compList;
	}

}
