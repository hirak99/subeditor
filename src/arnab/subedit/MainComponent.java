package arnab.subedit;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

class Utils {

    static public long parseTime(String strTime) {
        final String filler = "00:00:00,000";
        strTime += filler.substring(strTime.length());
        int hours = Integer.parseInt(strTime.substring(0, 2));
        int minutes = Integer.parseInt(strTime.substring(3, 5));
        int seconds = Integer.parseInt(strTime.substring(6, 8));
        int millis = Integer.parseInt(strTime.substring(9, 12));
        return (((long) hours * 60 + minutes) * 60 + seconds) * 1000 + millis;
    }

    static private String padZeros(long value, int n) {
        String s = Long.toString(value);
        while (s.length() < n) {
            s = "0" + s;
        }
        return s;
    }

    static public String toTimeString(long time) {
        long millis = (time % 1000);
        time /= 1000;
        long seconds = (time % 60);
        time /= 60;
        long minutes = (time % 60);
        time /= 60;
        long hour = time;
        return padZeros(hour, 2) + ":" + padZeros(minutes, 2) + ":" + padZeros(seconds, 2) + "," + padZeros(millis, 3);
    }
    // enum LoadState is used to simulate FSM for loading the subtitle file
}

/**
 * This is the class for the main window. It contains 1) a JTable, which has
 * the main subtitle data, and 2) adjustor panel (which is connected directly to
 * the SubtitleFile).
 * @author Arnab
 *
 */
public class MainComponent extends JPanel {

    SubtitleFile subtitleFile = new SubtitleFile();

    public void openFile(String fileName) {
        subtitleFile.loadFile(fileName);
    }

    public MainComponent() {
        final JTable tableLines;
        setLayout(new BorderLayout());
        //openFile("C:\\Arnab\\Progs\\!Eclipse\\Subtitle Editor\\Experiment.srt");
        //setTransferHandler(new TransferHandler("File"));
        new DropTarget(this, new DropTargetListener() {

            public void dragEnter(DropTargetDragEvent dtde) {
                if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.rejectDrag();
                } else {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                }
            }

            public void dragExit(DropTargetEvent dte) {
            }

            public void dragOver(DropTargetDragEvent dtde) {
            }

            public void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable data = dtde.getTransferable();
                try {
                    @SuppressWarnings("unchecked")
                    List<java.io.File> fileList = (List<java.io.File>) data.getTransferData(DataFlavor.javaFileListFlavor);
                    openFile(fileList.get(0).toString());
                } catch ( UnsupportedFlavorException e) {
                    e.printStackTrace();
                } catch ( IOException e) {
                    e.printStackTrace();
                }
            }

            public void dropActionChanged(DropTargetDragEvent dtde) {
            }
        });
        tableLines = new JTable(subtitleFile.getTableModel());
        tableLines.getColumnModel().getColumn(0).setMinWidth(100);
        tableLines.getColumnModel().getColumn(0).setPreferredWidth(400);
        for (int i = 1; i <= 4; ++i) {
            tableLines.getColumnModel().getColumn(i).setPreferredWidth(80);
            tableLines.getColumnModel().getColumn(i).setMaxWidth(85);
        }
        tableLines.getColumnModel().getColumn(5).setPreferredWidth(30);
        tableLines.getColumnModel().getColumn(5).setMaxWidth(30);
        tableLines.getColumnModel().moveColumn(3, 1);
        tableLines.getColumnModel().moveColumn(4, 2);
        tableLines.getColumnModel().moveColumn(5, 0);
        tableLines.getColumnModel().getColumn(4).setPreferredWidth(0);
        tableLines.getColumnModel().getColumn(5).setPreferredWidth(0);
        final JScrollPane scrollTable = new JScrollPane(tableLines);
        add(scrollTable);
        add(subtitleFile.getAdjustPanel(), BorderLayout.PAGE_START);
        subtitleFile.getAdjustPanel().addPropertyChangeListener("needsLayout",
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        doLayout();
                    }
                });
        subtitleFile.getAdjustPanel().addPropertyChangeListener("selectionChange",
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        Adjustors adjustors = subtitleFile.getAdjustors();
                        int newRow = ((Integer) evt.getNewValue()).intValue();
                        tableLines.getSelectionModel().setSelectionInterval(newRow, newRow);
                        Rectangle rect = tableLines.getCellRect(newRow, 0, true);
                        tableLines.scrollRectToVisible(rect);
                    }
                });
    }
}
