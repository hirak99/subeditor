package arnab.subedit;

import java.awt.Dimension;
import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.MaskFormatter;

/**
 * This class maintains the editing interface for a single line of the
 * subtitle. The actual control is returned by the function getPanel().
 * @author Arnab
 *
 */
class Adjustor {
	private int lineNum;
	public int getLineNum() {return lineNum;}
	public Adjustor(int line,String text,long time) {
		lineNum=line;
		panel=new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
		panel.add(new JLabel((lineNum+1)+". "+text));
		ftf=new JFormattedTextField(CreateMask());
		ftf.setValue(Utils.toTimeString(time));
		ftf.setMaximumSize(new Dimension(80,20));
		panel.add(ftf);
//		ftf.addFocusListener(new FocusListener() {
//			public void focusGained(FocusEvent e) {	}
//			public void focusLost(FocusEvent e) {
//				panel.firePropertyChange("valueChanged", false, true);
//			}
//		});
	}
	private JPanel panel;
	private JFormattedTextField ftf;
	public JFormattedTextField getTextBox() {return ftf;}
	public JPanel getPanel() {
		return panel;
	}
	private MaskFormatter CreateMask() {
		MaskFormatter format=null;
		try {
			format=new MaskFormatter("##:##:##,###");
		} catch (ParseException e) {
			System.out.println("Error: Could not create format.");
		}
		return format;
	}
	public long getNewTime() {
		return Utils.parseTime((String)ftf.getValue());
	}
}

class Adjustors extends Vector<Adjustor>{
	public void sort() {
		Collections.sort(this,new Comparator<Adjustor>() {
		    public int compare(Adjustor o1, Adjustor o2) {
		    	return o2.getLineNum()-o1.getLineNum();
		    }
		});
	}
}
