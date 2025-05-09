package com.mycompany.editorhotl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
/*
* TODO list:
*   Levels:
*     -Export for metatiles, both graphics, and raw data.
*     -Export whole columns of metatiles. (To use larger pieces of levels.)
*     -Support for the second palette.
*     -Fix the boxes on the level draw.
*     -Remove the open menu, we aint loading pictures here to show.
*   Companions:
*     - Do the Companions menu, along with the charstats. It should have all the methods needed.
*   Doors:
*     -Complete menu, along with door drawing, data import\export on the GUI, show on the drawn
* map and so forth.
* */
public class GUI extends JFrame {
  private static int lvlNr;
  private final JButton reloadButton;
  private final JButton gridButton;
  private final JButton saveButton;
  private final JButton zoomButton;
  private final JLabel imageLabel;
  private final JLabel statusLabel;
  private BufferedImage originalImage, displayedImage;
  private File currentFile;
  private boolean gridVisible = false;
  private boolean zoomed = false;
  private final int GRID_SIZE = 16;
  private final Color gridColor = Color.RED;
  private BufferedImage levelImg;
  private BufferedImage copyLevelImg;
  public rom currentROM=new rom();
  public levelRAMArray currLvl=new levelRAMArray();
  public GUI() {
    setTitle("Heroes of the Lance Map Editor\\Viewer");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 600);
    setResizable(false);
    setLayout(new BorderLayout());
    // Create menu bar
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenu charstatMenu=new JMenu("Chrstat");
    JMenu companions=new JMenu("Companions");
    JMenu doors=new JMenu("Doors");
    
    JMenuItem openItem = new JMenuItem("Open");
    JMenuItem reloadItem = new JMenuItem("Reload");
    JMenuItem saveItem = new JMenuItem("Save");
    JMenuItem zoomItem = new JMenuItem("Zoom 2x");
    JMenuItem exitItem = new JMenuItem("Exit");
    JMenuItem ldLevel  = new JMenuItem("Load Level");
    JMenuItem openHex1=new JMenuItem("Open Hex");
    openItem.addActionListener(_ -> openImage());
    reloadItem.addActionListener(_ -> reloadImage());
    saveItem.addActionListener(_ -> saveImage());
    zoomItem.addActionListener(_ -> toggleZoom());
    exitItem.addActionListener(_ -> System.exit(0));
    openHex1.addActionListener(_ -> SwingUtilities.invokeLater(() -> {
      GHexStyleEditor editor = new GHexStyleEditor();
      editor.setVisible(true);
    }));
   
    fileMenu.add(openItem);
    fileMenu.add(reloadItem);
    fileMenu.add(saveItem);
    fileMenu.add(zoomItem);
    fileMenu.addSeparator();
    fileMenu.add(exitItem);
    fileMenu.add(ldLevel);
    fileMenu.add(openHex1);
    menuBar.add(fileMenu);
    menuBar.add(charstatMenu);
    menuBar.add(companions);
    menuBar.add(doors);
    setJMenuBar(menuBar);
    
    // Create toolbar
    JToolBar toolBar = new JToolBar();
    JButton openButton = new JButton("Open");
    reloadButton = new JButton("Reload");
    gridButton = new JButton("Grid");
    saveButton = new JButton("Save");
    zoomButton = new JButton("Zoom 2x");
    JButton loadLevelButton = new JButton("Load Level");
    JButton openHex = new JButton("Open HexEdit");
    JButton openDumps = new JButton("Open Dumps");
    JButton drawLevel = new JButton("Draw from Dumps");
    JButton redrawLevel=new JButton("Redraw Level");
    JButton showHelp=new JButton("Show Help");
    int lvlNr;
    redrawLevel.addActionListener(_ ->{
      
      currentROM.getDumps(GUI.lvlNr);
      drawLevel();
      
    });
    String helpText = """
            ===== Help Menu =====
            
            The open menu opens an image. Still in progress.
            Reload reloads the current picture, if there's a mistake with it.
            The grid button drawn a 16x16 red grid to show the metatiles of the level.
            Zoom 2x will, zoom in the picture, and also the grid IIRC.
            Open hex edit will open the internal hex editor, but it's better to use an external one.
            Open dumps will ask you to enter a number. This will load the given dumps into memory. 1-88 is valid, as it's the same as in the game.
            Draw from dumps will use both RAM and VRAM dumps and show how a level would look like.
            Redraw level opens the same dumps, and redraw the level.
            """;
    
    showHelp.addActionListener(_ -> showHelp("Help", helpText));
    openButton.addActionListener(_ -> openImage());
    reloadButton.addActionListener(_ -> reloadImage());
    gridButton.addActionListener(_ -> toggleGrid());
    saveButton.addActionListener(_ -> saveImage());
    zoomButton.addActionListener(_ -> toggleZoom());
    loadLevelButton.addActionListener(_ ->openLevel());
    openHex.addActionListener(_ -> SwingUtilities.invokeLater(() -> {
      GHexStyleEditor editor = new GHexStyleEditor();
      editor.setVisible(true);
    }));
    openDumps.addActionListener(_ ->showNumberInputDialog());
    // Disable buttons until image is loaded
    drawLevel.addActionListener(_ -> drawLevel());
    toolBar.add(openButton);
    toolBar.add(reloadButton);
    toolBar.add(gridButton);
    toolBar.add(saveButton);
    toolBar.add(zoomButton);
    toolBar.add(loadLevelButton);
    toolBar.add(openHex);
    toolBar.add(openDumps);
    toolBar.add(drawLevel);
    toolBar.add(redrawLevel);
    toolBar.add(showHelp);
    add(toolBar, BorderLayout.NORTH);
    
    // Create image display area
    imageLabel = new JLabel();
    imageLabel.setHorizontalAlignment(JLabel.CENTER);
    JScrollPane scrollPane = new JScrollPane(imageLabel);
    add(scrollPane, BorderLayout.CENTER);
    
    // Status bar
    statusLabel = new JLabel(" Ready");
    statusLabel.setBorder(BorderFactory.createEtchedBorder());
    add(statusLabel, BorderLayout.SOUTH);
    
    // Mouse motion listener for coordinates
    imageLabel.addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        if (originalImage != null) {
          int x = e.getX() / (zoomed ? 2 : 1);
          int y = e.getY() / (zoomed ? 2 : 1);
          statusLabel.setText(String.format(" X: %d, Y: %d | Grid Cell: [%d,%d]",
                  x, y, x/GRID_SIZE, y/GRID_SIZE));
        }
      }
    });
  }
  private void drawLevel(){ BufferedImage metaTileImg= levelRAMArray.ConvertVRAMDump(currentROM.VRAMdump);
    BufferedImage[] tileArray= levelRAMArray.splitImageInto8x8Tiles(metaTileImg,false);
    BufferedImage[] metaTileArray=new BufferedImage[0xFF];
    for (int i = 0; i <255 ; i++) {
      metaTileArray[i]= levelRAMArray.combineTiles(tileArray,currentROM.ramDump,false,i);
    }
    BufferedImage finalLevelImg= levelRAMArray.stitchImages(metaTileArray,currentROM.ramDump);
    displayImage(finalLevelImg);
  levelImg=finalLevelImg;
  copyLevelImg=finalLevelImg;}
  private void openImage() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif", "bmp"));
    
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      currentFile = fileChooser.getSelectedFile();
      loadImage(currentFile);
    }
  }
  private void openLevel(){
    JFileChooser jFileChooser=new JFileChooser();
    jFileChooser.setFileFilter(new FileNameExtensionFilter("RAM dump file","ram","ramtrunc"));
    if(jFileChooser.showOpenDialog(this)== JFileChooser.APPROVE_OPTION){
      currentFile=jFileChooser.getSelectedFile();
      
    }
  }
  private void showNumberInputDialog() {
    // Create the dialog
    JDialog dialog = new JDialog();
    dialog.setTitle("Level?");
    dialog.setModal(true);
    dialog.setLayout(new GridLayout(1, 1));
    
    // Create components
    JLabel label = new JLabel("Number:");
    JTextField numberField = new JTextField();
    JPanel buttonPanel = new JPanel();
    
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");
    
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    
    dialog.add(label);
    dialog.add(numberField);
    dialog.add(buttonPanel);
    
    // Add action listeners
    okButton.addActionListener(_ -> {
      try {
        // Parse the number
        int number = Integer.parseInt(numberField.getText());
        
        // Call your method
        if (currentROM != null) {
          currentROM.getDumps(number);
          GUI.lvlNr=number;
        }
        
        // Close the dialog
        dialog.dispose();
      } catch (NumberFormatException ex) {
        // Show error message if input is not a number
        JOptionPane.showMessageDialog(dialog,
                "Not a number.",
                "Sneaky kaka",
                JOptionPane.ERROR_MESSAGE);
      }
    });
    
    cancelButton.addActionListener(_ -> {
      dialog.dispose(); // Just close the dialog
    });
    
    // Set dialog size and show it
    dialog.setSize(200, 200);
    dialog.setLocationRelativeTo(null); // Center on screen
    dialog.setVisible(true);
  }
  private void loadImage(File file) {
    try {
      originalImage = ImageIO.read(file);
      displayedImage = copyImage(originalImage);
      displayImage(displayedImage);
      
      // Enable buttons
      reloadButton.setEnabled(true);
      gridButton.setEnabled(true);
      saveButton.setEnabled(true);
      zoomButton.setEnabled(true);
      
      gridVisible = false;
      zoomed = false;
      zoomButton.setText("Zoom 2x");
      statusLabel.setText(" Loaded: " + file.getName());
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
              "Error loading image: " + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  private void reloadImage() {
    if (currentFile == null) return;
    
    try {
      BufferedImage newImage = ImageIO.read(currentFile);
      if (newImage != null) {
        originalImage = newImage;
        if (gridVisible) {
          applyGrid();
        } else {
          displayedImage = copyImage(originalImage);
          displayImage(displayedImage);
        }
        statusLabel.setText(" Reloaded: " + currentFile.getName());
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this,
              "Error reloading image: " + ex.getMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  private void toggleGrid() {
    gridVisible = !gridVisible;
    
    if (gridVisible) {
      applyGrid();
      statusLabel.setText(" Grid ON (16x16 pixels)");
    } else {
      displayedImage = copyImage(levelImg);
      
      displayImage(displayedImage);
      statusLabel.setText(" Grid OFF");
    }
  }
  private void toggleZoom() {
    zoomed = !zoomed;
    
    if (zoomed) {
      zoomButton.setText("Zoom 1x");
      statusLabel.setText(" Zoomed 2x");
    } else {
      zoomButton.setText("Zoom 2x");
      statusLabel.setText(" Normal size");
    }
    
    // Redraw with current settings
    if (gridVisible) {
      applyGrid();
    } else {
      displayedImage = copyImage(originalImage);
      displayImage(displayedImage);
    }
  }
  private void applyGrid() {
    float gridAlpha = 0.5f;
    if (zoomed) {
      // Create zoomed version with grid
      int newWidth = levelImg.getWidth() * 2;
      int newHeight = levelImg.getHeight() * 2;
      displayedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
      
      Graphics2D g2d = displayedImage.createGraphics();
      // Draw scaled original image
      g2d.drawImage(levelImg, 0, 0, newWidth, newHeight, null);
      
      // Draw scaled grid
      g2d.setColor(gridColor);
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, gridAlpha));
      g2d.setStroke(new BasicStroke(2)); // Thicker lines when zoomed
      
      // Vertical lines (spaced 32 pixels when zoomed 2x)
      for (int x = 0; x <= newWidth; x += GRID_SIZE * 2) {
        g2d.drawLine(x, 0, x, newHeight);
      }
      
      // Horizontal lines (spaced 32 pixels when zoomed 2x)
      for (int y = 0; y <= newHeight; y += GRID_SIZE * 2) {
        g2d.drawLine(0, y, newWidth, y);
      }
      
      g2d.dispose();
    } else {
      // Normal size with grid
      displayedImage = copyImage(levelImg);
      Graphics2D g2d = displayedImage.createGraphics();
      
      // Set grid drawing properties
      g2d.setColor(gridColor);
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, gridAlpha));
      g2d.setStroke(new BasicStroke(1));
      
      // Draw vertical lines
      for (int x = 0; x <= levelImg.getWidth(); x += GRID_SIZE) {
        g2d.drawLine(x, 0, x, levelImg.getHeight());
      }
      
      // Draw horizontal lines
      for (int y = 0; y <= levelImg.getHeight(); y += GRID_SIZE) {
        g2d.drawLine(0, y, levelImg.getWidth(), y);
      }
      
      g2d.dispose();
    }
    
    displayImage(displayedImage);
  }
  private void saveImage() {
    if (displayedImage == null) return;
    
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileNameExtensionFilter(
            "PNG images", "png"));
    fileChooser.setSelectedFile(new File(
            currentFile != null ?
                    currentFile.getName().replaceFirst("[.][^.]+$", "") +
                            (gridVisible ? "_grid" : "") +
                            (zoomed ? "_zoomed" : "") + ".png" :
                    "image.png"));
    
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        File file = fileChooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
          file = new File(file.getPath() + ".png");
        }
        ImageIO.write(displayedImage, "png", file);
        statusLabel.setText(" Image saved to: " + file.getName());
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Error saving image: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  private BufferedImage copyImage(BufferedImage source) {
    BufferedImage copy = new BufferedImage(
            source.getWidth() * (zoomed ? 2 : 1),
            source.getHeight() * (zoomed ? 2 : 1),
            BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = copy.createGraphics();
    g2d.drawImage(source, 0, 0,
            copy.getWidth(), copy.getHeight(), null);
    g2d.dispose();
    return copy;
  }
  private void displayImage(BufferedImage image) {
    ImageIcon icon = new ImageIcon(image);
    imageLabel.setIcon(icon);
    
  }
  public static void showHelp(String title, String helpText) {
    // Create the window
    JFrame helpFrame = new JFrame(title);
    helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    helpFrame.setSize(500, 400);
    helpFrame.setLocationRelativeTo(null); // Center on screen
    
    // Create a scrollable text area
    JTextArea textArea = new JTextArea(helpText);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setMargin(new Insets(10, 10, 10, 10)); // Padding
    
    // Use a scroll pane in case text is long
    JScrollPane scrollPane = new JScrollPane(textArea);
    helpFrame.add(scrollPane, BorderLayout.CENTER);
    
    // Optional: Add a close button
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> helpFrame.dispose());
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(closeButton);
    helpFrame.add(buttonPanel, BorderLayout.SOUTH);
    
    helpFrame.setVisible(true);
  }
  public static void main() {
    SwingUtilities.invokeLater(() -> {
      GUI viewer = new GUI();
      viewer.setLocationRelativeTo(null);
      viewer.setVisible(true);
    });
  }
}