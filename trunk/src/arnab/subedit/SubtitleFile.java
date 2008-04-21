package arnab.subedit;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * The genreal class for storing the entire subtitle file internally.
 * Also contains a table model to display the information in a table.
 * This class has a vector calle 'entries' which stores the file, and another
 * vector adjustors to store the user input to adjust the times.
 * <p>
 * It also contains the adjustorPanel, which displays the adjustors.
 * @author Arnab
 *
 */
public class SubtitleFile {

    private String curFileName;

    public String getFileName() {
        return curFileName;
    }

    public long modifiedTime(int lineNo, boolean from) {
        double multiplier;
        // Obtain the required multiplier
        if (adjustors.size() <= 1) {
            multiplier = 1;
        } else {
            Adjustor adj1 = adjustors.get(0);
            Adjustor adj2 = adjustors.get(1);
            long newTime1 = adj1.getNewTime();
            long newTime2 = adj2.getNewTime();
            int line1 = adj1.getLineNum();
            int line2 = adj2.getLineNum();
            long oldTime1 = entries.get(line1).timeFrom;
            long oldTime2 = entries.get(line2).timeFrom;
            multiplier = ((double) (newTime2) - newTime1) / (oldTime2 - oldTime1);
        }
        // Obtain the centre, meaning the pivotal time which will be changed
        //  exactly to the new time specified
        long oldTime = 0, newTime = 0;
        if (adjustors.size() != 0) {
            // Initialize the reference with the first value since this will be the default
            Adjustor reference = adjustors.get(0);
            // Search for max line number <= lineNo
            int maxLine = 0;
            for (Adjustor adjustor : adjustors) {
                int thisLine = adjustor.getLineNum();
                if (thisLine <= lineNo && thisLine > maxLine) {
                    maxLine = thisLine;
                    reference = adjustor;
                }
            }
            oldTime = entries.get(reference.getLineNum()).timeFrom;
            newTime = reference.getNewTime();
        }
        long time = (from ? entries.get(lineNo).timeFrom : entries.get(lineNo).timeTo);
        return (long) ((time - oldTime) * multiplier + newTime + .5);
    }

    static public class SubtitleEntry {

        SubtitleEntry(String line_, long timeFrom_, long timeTo_) {
            line = line_;
            timeFrom = timeFrom_;
            timeTo = timeTo_;
            beingAdjusted = false;
        }
        String line;
        long timeFrom, timeTo;
        boolean beingAdjusted;
    }
    private Vector<SubtitleEntry> entries = new Vector<SubtitleEntry>();

    private class SubtitleTableModel extends AbstractTableModel {

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 5) {
                return true;
            } else {
                return false;
            }
        }

        public int getColumnCount() {
            return 6;
        }

        public int getRowCount() {
            return entries.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            SubtitleEntry entry = entries.get(rowIndex);
            if (columnIndex == 0) {
                return (rowIndex + 1) + ". " + entry.line;
            } else if (columnIndex == 1) {
                return Utils.toTimeString(entry.timeFrom);
            } else if (columnIndex == 2) {
                return Utils.toTimeString(entry.timeTo);
            } else if (columnIndex == 3) {
                return Utils.toTimeString(modifiedTime(rowIndex, true));
            } else if (columnIndex == 4) {
                return Utils.toTimeString(modifiedTime(rowIndex, false));
            } else if (columnIndex == 5) {
                return Boolean.valueOf(entry.beingAdjusted);
            } else {
                return null;
            }
        }

        @Override
        public String getColumnName(int column) {
            final String[] names = {"Line", "Original From", "Original To", "From", "To", "Adjust"};
            return names[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 5) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 5) {
                boolean value = ((Boolean) aValue).booleanValue();
                entries.get(rowIndex).beingAdjusted = value;
                if (value) {
                    addToAdjustor(rowIndex);
                } else {
                    removeFromAdjustor(rowIndex);
                }
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
    private SubtitleTableModel subtitleTableModel = new SubtitleTableModel();

    public TableModel getTableModel() {
        return subtitleTableModel;
    }

    public boolean loadFile(String fileName) {
        entries.removeAllElements();
        while (adjustors.size() > 0) {
            adjustorPanel.remove(adjustors.get(0).getPanel());
            adjustors.remove(0);
        }
        adjustorPanel.validate();
        subtitleTableModel.fireTableDataChanged();
        adjustorPanel.firePropertyChange("needsLayout", false, true);
        if (fileName.toLowerCase().endsWith(".sub")) {
            return loadSub(fileName);
        } else {
            return loadSrt(fileName);
        }
    }
    /**
     * Function to facilitate extraction of numbers from strings of the
     * form "{123}..." for reading sub files. The result is stored in the
     * variable extractNumResult, due to the stupidity and short sightedness
     * of java developers resulting in a language with no elegant way of
     * returning two values out of a function.
     * @param line
     * @return the string without the first number
     */
    private long extractNumResult;

    private String extractNum(String line) {
        extractNumResult = 0;
        if (line.charAt(0) != '{') {
            return line;
        }
        int p = line.indexOf('}');
        if (p == -1) {
            return line;
        }
        String theNum = line.substring(1, p);
        extractNumResult = Long.parseLong(theNum);
        return line.substring(p + 1);
    }

    private boolean loadSub(String fileName) {
        //final double fps=23.976;
        final double fps = 25;
        RandomAccessFile raf;
        try {
            curFileName = fileName.substring(0, fileName.length() - 4) + ".srt";
            raf = new RandomAccessFile(fileName, "r");
            String line;
            while ((line = raf.readLine()) != null) {
                line = extractNum(line);
                long num1 = extractNumResult;
                line = extractNum(line);
                long num2 = extractNumResult;
                long time1 = (long) (num1 * 1000 / fps);
                long time2 = (long) (num2 * 1000 / fps);
                while (line.charAt(line.length() - 1) == '|') {
                    line = line.substring(0, line.length() - 1);
                }
                entries.add(new SubtitleEntry(line.replace('|', '\n'), time1, time2));
            }
            raf.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // Simple enum to assist the loading process
    private enum LoadState {
        TIME, TITLE, NUMBER
    }

    private boolean loadSrt(String fileName) {
        RandomAccessFile raf;
        LoadState state = LoadState.NUMBER;
        try {
            curFileName = fileName;
            raf = new RandomAccessFile(fileName, "r");
            String line, title = null;
            long time1 = 0, time2 = 0;
            while ((line = raf.readLine()) != null) {
                if (state == LoadState.NUMBER) {
                    if (line.trim().length() > 0) {
                        title = "";
                        state = LoadState.TIME;
                    }
                } else if (state == LoadState.TIME) {
                    // Read the times here
                    time1 = Utils.parseTime(line.substring(0, 12));
                    time2 = Utils.parseTime(line.substring(17));
                    state = LoadState.TITLE;
                } else if (state == LoadState.TITLE) {
                    if (line.trim().length() == 0) {
                        // Do the storing here
                        if (title.length() > 0) {
                            entries.add(new SubtitleEntry(title, time1, time2));
                        }
                        state = LoadState.NUMBER;
                    } else if (title.length() > 0) {
                        title = title + "\n" + line;
                    } else {
                        title += line;
                    }
                }
            }
            raf.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found.");
            curFileName = null;
            JOptionPane.showMessageDialog(null, "Error: file not found.");
            return false;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: Could not read from file.");
            curFileName = null;
            return false;
        } catch (Exception e) {
            int errorLocation = entries.size();
            entries.clear();
            curFileName = null;
            JOptionPane.showMessageDialog(null, "Parsing error at entry " + errorLocation + "\r\n[ " + e.toString() + e.getMessage() + " ]");
            return false;
        }
    }

    public void saveFile(String fileName) {
        try {
            FileWriter fw = new FileWriter(fileName);
            for (int i = 0; i < entries.size(); ++i) {
                SubtitleEntry e = entries.get(i);
                fw.write(Integer.toString(i + 1));
                fw.write('\n');
                fw.write(Utils.toTimeString(modifiedTime(i, true)));
                fw.write(" --> ");
                fw.write(Utils.toTimeString(modifiedTime(i, false)));
                fw.write('\n');
                fw.write(e.line);
                fw.write("\n\n");
            }
            fw.close();
            curFileName = fileName;
        } catch (IOException e1) {
            System.out.println("Error: Could not write to file " + fileName);
            e1.printStackTrace();
        }
    }

    public SubtitleFile() {
        adjustorPanel = new JPanel();
        adjustorPanel.setLayout(new BoxLayout(adjustorPanel, BoxLayout.PAGE_AXIS));
    }
    private JPanel adjustorPanel;

    public JPanel getAdjustPanel() {
        return adjustorPanel;
    }
    private Adjustors adjustors = new Adjustors();

    public Adjustors getAdjustors() {
        return adjustors;
    }

    private void addToAdjustor(int subtitleLine) {
        for (int i = 0; i < adjustors.size(); ++i) {
            if (adjustors.get(i).getLineNum() == subtitleLine) {
                return;
            }
        }
        SubtitleEntry entry = entries.get(subtitleLine);
        final Adjustor newAdjustor = new Adjustor(subtitleLine, entry.line, modifiedTime(subtitleLine, true));
        newAdjustor.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                adjustorPanel.firePropertyChange("selectionChange", 0, newAdjustor.getLineNum());
            }

            public void focusLost(FocusEvent e) {
                subtitleTableModel.fireTableDataChanged();
            }
        });
        int afterWhich;
        for (afterWhich = 0; afterWhich < adjustors.size(); ++afterWhich) {
            if (adjustors.get(afterWhich).getLineNum() > subtitleLine) {
                break;
            }
        }
        adjustors.insertElementAt(newAdjustor, afterWhich);
        adjustorPanel.add(newAdjustor.getPanel(), afterWhich);
        adjustorPanel.validate();
        newAdjustor.getPanel().validate();
        adjustorPanel.firePropertyChange("needsLayout", false, true);
    }

    private void removeFromAdjustor(int subtitleLine) {
        for (int i = 0; i < adjustors.size(); ++i) {
            Adjustor a = adjustors.get(i);
            if (a.getLineNum() == subtitleLine) {
                subtitleTableModel.fireTableDataChanged();
                adjustorPanel.remove(adjustors.get(i).getPanel());
                adjustorPanel.validate();
                adjustorPanel.firePropertyChange("needsLayout", false, true);
                adjustors.remove(i);
                return;
            }
        }
    }
}

