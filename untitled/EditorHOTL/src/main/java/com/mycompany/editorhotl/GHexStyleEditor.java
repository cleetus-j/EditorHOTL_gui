package com.mycompany.editorhotl;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HexFormat;

public class GHexStyleEditor extends JFrame {
  private HexTableModel model;
  private JTable dataTable;
  private JTable offsetTable;
  private JLabel statusBar;
  private File currentFile;
  private boolean unsavedChanges = false;
  
  public GHexStyleEditor() {
    setTitle("Hex Editor");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setSize(1000, 600);
    
    // Initialize with empty data
    model = new HexTableModel(new byte[0]);
    
    // Create tables
    dataTable = new JTable(model);
    offsetTable = createOffsetTable();
    
    // Customize UI
    customizeDataTable();
    setupMenuBar();
    setupKeyBindings();
    
    // Layout
    JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(offsetTable),
            new JScrollPane(dataTable)
    );
    splitPane.setDividerLocation(80);
    
    add(splitPane, BorderLayout.CENTER);
    
    statusBar = new JLabel("Ready");
    statusBar.setBorder(BorderFactory.createEtchedBorder());
    add(statusBar, BorderLayout.SOUTH);
    
    setLocationRelativeTo(null);
  }
  
  private void customizeDataTable() {
    dataTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
    dataTable.setRowHeight(20);
    dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    // Set column widths
    for (int i = 0; i < model.getHexColumnCount(); i++) {
      dataTable.getColumnModel().getColumn(i).setPreferredWidth(30);
    }
    dataTable.getColumnModel().getColumn(model.getHexColumnCount()).setPreferredWidth(160);
    
    // Custom renderers and editors
    dataTable.setDefaultRenderer(Byte.class, new HexCellRenderer());
    dataTable.setDefaultRenderer(String.class, new AsciiCellRenderer());
    dataTable.setDefaultEditor(Byte.class, new HexCellEditor());
    
    // Selection highlighting
    dataTable.setSelectionBackground(new Color(180, 220, 255));
    dataTable.setSelectionForeground(Color.BLACK);
    
    // Selection listener
    dataTable.getSelectionModel().addListSelectionListener(e -> updateStatusBar());
  }
  
  private JTable createOffsetTable() {
    JTable table = new JTable(new OffsetTableModel());
    table.setFont(new Font("Monospaced", Font.PLAIN, 14));
    table.setRowHeight(20);
    table.getColumnModel().getColumn(0).setPreferredWidth(80);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setEnabled(false);
    table.setBackground(Color.LIGHT_GRAY);
    
    // Sync selection with main table
    dataTable.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        int row = dataTable.getSelectedRow();
        if (row >= 0) {
          table.setRowSelectionInterval(row, row);
        }
      }
    });
    
    return table;
  }
  
  private void setupMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    
    JMenuItem openItem = new JMenuItem("Open");
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    openItem.addActionListener(e -> openFile());
    
    JMenuItem saveItem = new JMenuItem("Save");
    saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
    saveItem.addActionListener(e -> saveFile());
    
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(e -> dispose());
    
    fileMenu.add(openItem);
    fileMenu.add(saveItem);
    fileMenu.addSeparator();
    fileMenu.add(exitItem);
    menuBar.add(fileMenu);
    
    setJMenuBar(menuBar);
  }
  
  private void setupKeyBindings() {
    JRootPane rootPane = getRootPane();
    
    // Ctrl+O for Open
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), "open");
    rootPane.getActionMap().put("open", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { openFile(); }
    });
    
    // Ctrl+S for Save
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save");
    rootPane.getActionMap().put("save", new AbstractAction() {
      public void actionPerformed(ActionEvent e) { saveFile(); }
    });
  }
  
  private void openFile() {
    JFileChooser fc = new JFileChooser();
    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      currentFile = fc.getSelectedFile();
      try (InputStream is = new FileInputStream(currentFile)) {
        byte[] fileData = is.readAllBytes();
        model = new HexTableModel(fileData);
        dataTable.setModel(model);
        ((OffsetTableModel)offsetTable.getModel()).setRowCount(model.getRowCount());
        customizeDataTable();
        unsavedChanges = false;
        statusBar.setText("Loaded: " + currentFile.getName() + " (" + fileData.length + " bytes)");
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Error reading file", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private void saveFile() {
    if (currentFile == null) {
      JFileChooser fc = new JFileChooser();
      if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        currentFile = fc.getSelectedFile();
      } else {
        return;
      }
    }
    
    try (OutputStream os = new FileOutputStream(currentFile)) {
      os.write(model.getData());
      unsavedChanges = false;
      statusBar.setText("Saved: " + currentFile.getName());
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  private void updateStatusBar() {
    int start = dataTable.getSelectedRow() * 16 + dataTable.getSelectedColumn();
    int end = start;
    if (dataTable.getSelectedColumnCount() > 1) {
    
    }
    statusBar.setText(String.format("Offset: 0x%08X - 0x%08X | Size: %d bytes",
            start, end, end - start + 1));
  }
  
  // Table model classes
  class HexTableModel extends AbstractTableModel {
    private byte[] data;
    private final int bytesPerRow = 16;
    
    public HexTableModel(byte[] data) {
      this.data = data;
    }
    
    public byte[] getData() {
      return data;
    }
    
    public int getHexColumnCount() {
      return bytesPerRow;
    }
    
    @Override public int getRowCount() {
      return (int) Math.ceil((double)data.length / bytesPerRow);
    }
    
    @Override public int getColumnCount() {
      return bytesPerRow + 1;
    }
    
    @Override public Object getValueAt(int row, int col) {
      int index = row * bytesPerRow + col;
      if (col < bytesPerRow) {
        return index < data.length ? data[index] : null;
      } else {
        // ASCII column
        StringBuilder sb = new StringBuilder();
        int start = row * bytesPerRow;
        int end = Math.min(start + bytesPerRow, data.length);
        for (int i = start; i < end; i++) {
          char c = (char) (data[i] & 0xFF);
          sb.append(Character.isISOControl(c) || c > 0x7F ? '.' : c);
        }
        return sb.toString();
      }
    }
    
    @Override public String getColumnName(int col) {
      return col < bytesPerRow ? String.format("%02X", col) : "ASCII";
    }
    
    @Override public Class<?> getColumnClass(int col) {
      return col < bytesPerRow ? Byte.class : String.class;
    }
    
    @Override public boolean isCellEditable(int row, int col) {
      return col < bytesPerRow;
    }
    
    @Override public void setValueAt(Object value, int row, int col) {
      if (col < bytesPerRow && value != null) {
        try {
          int index = row * bytesPerRow + col;
          byte newValue;
          
          if (value instanceof Byte) {
            newValue = (Byte)value;
          } else if (value instanceof String) {
            String str = ((String)value).trim();
            if (str.isEmpty()) return;
            
            if (str.startsWith("0x")) {
              newValue = HexFormat.of().parseHex(str.substring(2))[0];
            } else {
              newValue = (byte)Integer.parseInt(str);
            }
          } else {
            return;
          }
          
          if (index < data.length) {
            data[index] = newValue;
            unsavedChanges = true;
            fireTableCellUpdated(row, col);
            fireTableCellUpdated(row, bytesPerRow);
          }
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(GHexStyleEditor.this,
                  "Invalid value. Enter hex (0xAB) or decimal (171)",
                  "Input Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }
  
  class HexCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      
      if (value instanceof Byte) {
        setText(String.format("%02X", (Byte)value));
        setHorizontalAlignment(SwingConstants.CENTER);
      }
      return this;
    }
  }
  
  class HexCellEditor extends DefaultCellEditor {
    public HexCellEditor() {
      super(new JTextField());
      ((JTextField)getComponent()).setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
      Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
      if (value instanceof Byte) {
        ((JTextField)c).setText(String.format("%02X", (Byte)value));
      }
      return c;
    }
    
    @Override
    public Object getCellEditorValue() {
      String text = ((JTextField)getComponent()).getText().trim();
      if (text.isEmpty()) return null;
      
      try {
        if (text.startsWith("0x")) {
          return HexFormat.of().parseHex(text.substring(2))[0];
        } else {
          return (byte)Integer.parseInt(text);
        }
      } catch (NumberFormatException e) {
        return null;
      }
    }
  }
  
  class AsciiCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      setFont(new Font("Monospaced", Font.PLAIN, 14));
      return this;
    }
  }
  
  class OffsetTableModel extends AbstractTableModel {
    private int rowCount = 0;
    
    public void setRowCount(int count) {
      rowCount = count;
      fireTableDataChanged();
    }
    
    @Override public int getRowCount() {
      return rowCount;
    }
    
    @Override public int getColumnCount() {
      return 1;
    }
    
    @Override public Object getValueAt(int row, int col) {
      return String.format("0x%08X", row * 16);
    }
    
    @Override public String getColumnName(int col) {
      return "Offset";
    }
  }
}