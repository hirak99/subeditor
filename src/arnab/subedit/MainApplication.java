package arnab.subedit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainApplication {
	private MainComponent mainComponent=new MainComponent();
	private JFileChooser fileChooser=new JFileChooser();
	private String lastDirectory;
	private void showLoadDialog() {
        fileChooser.setCurrentDirectory(new File(lastDirectory==null?System.getProperty("user.dir"):lastDirectory));
		if (fileChooser.showOpenDialog(mainComponent)==JFileChooser.APPROVE_OPTION) {
			String fileName=fileChooser.getSelectedFile().getPath();
			lastDirectory=fileName.substring(0,fileName.lastIndexOf(File.separatorChar));
			mainComponent.getSubtitleFile().loadFile(fileName);
                        mainComponent.invalidate();
		}
	}
	private void showUpdateDialog() {
        fileChooser.setCurrentDirectory(new File(lastDirectory==null?System.getProperty("user.dir"):lastDirectory));
        fileChooser.setSelectedFile(new File(mainComponent.getSubtitleFile().getFileName()));
		if (fileChooser.showSaveDialog(mainComponent)==JFileChooser.APPROVE_OPTION) {
			String fileName=fileChooser.getSelectedFile().getPath();
			if (fileName.indexOf('.')==-1) fileName+=".srt"; 
			lastDirectory=fileName.substring(0,fileName.lastIndexOf(File.separatorChar));
			mainComponent.getSubtitleFile().saveFile(fileName);
		}
	}
	private JMenuBar mainMenu() {
        final JMenuBar menuBar=new JMenuBar();
        final JMenu menuFile=new JMenu("File"); menuFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menuFile);
        final JMenuItem fileLoad=new JMenuItem("Load Subtitle...",KeyEvent.VK_L);
        menuFile.add(fileLoad);
        fileLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showLoadDialog();
			}
        });
        final JMenuItem fileUpdate=new JMenuItem("Save Subtitle...",KeyEvent.VK_S);
        menuFile.add(fileUpdate);
        fileUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showUpdateDialog();
			}
        });
        menuFile.addSeparator();
        
        final JMenuItem fileExit=new JMenuItem("Exit",KeyEvent.VK_X);
        //fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,ActionEvent.ALT_MASK));
        menuFile.add(fileExit);
        fileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	System.exit(0);
            }
        });
        return menuBar;
	}
	public void runApplication() {
		JFrame frame=new JFrame("Resync");
		frame.setJMenuBar(mainMenu());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.add(mainComponent,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
	}
	public static void main(String[] args) {
		new MainApplication().runApplication();
	}
}
