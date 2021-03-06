/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drawclient;

import static drawclient.DrawClient.drawingPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 *
 * @author Aaron
 */
public class DrawClient{

    /**
     * @param args the command line arguments
     */
    
    //Declare the JFrames and JPanels for use
    static JFrame window = new JFrame("Drawing Client");
    static JPanel drawingPanel;
    static JPanel buttonsPanel;
    static JPanel colorsPanel;
    static JPanel playBackToolsPanel;
    static JPanel menuPanel;
    static JPanel container;

    
    //Declare the JButtons for use
    JButton saveButton;
    JButton recordButton;
    JButton nextPageButton;
    JButton prevPageButton;
    JButton undoButton;
    JButton redoButton;
    JMenuBar menuBar;
    JColorChooser colChs;
    JComboBox strokeSizesBox;
    JFileChooser jFC;
    JTextArea jTxtArea;
    
    //undoBuffer holds all the lines Undone so they may be readded
    Stack undoBuffer;
    
    //Stroke Sizes holds the current selections of brush sizes available
    Vector strokeSizes;
    ArrayDeque pointsQueue;
    
    //Mouselisteners declared global so they can be add 
    //  and removed during runtime
    MouseListener ml;
    MouseMotionListener mml;
    
    //Holds the MenuItems and their sub menus. Such as menuItems.get(0).get(1)
    //  would be at Edit->Redo. Allows Enabling and disabling of items
    ArrayList<ArrayList<JMenuItem>> menuItems;
    
    //Book of pages, lines, cords
    ArrayList<ArrayList<ArrayList<Points>>> book;
    private int currentPage;
    private int playBackPage;
    private int totalPages;
    private int strokeSizeSel; 
    final byte FILE_VALIDATE_BYTE = 0x0F;

    
    //isProducerState holds wether the client is in producer or consumer mode
    private boolean isProducerState;
    
    //Recording Variables
    private boolean isRecordingState;
    private long playBackStartTime;
    private long recordStartTime;
    private long recordEndTime;
    
    //File IO Variables
    FileOutputStream fileOut;
    FileInputStream fileIn;
    ObjectInputStream objIS;
    ObjectOutputStream objOS;    
    File fName;
    
    //Sound Variables
    //Create new recording thread
    Thread audioRecorderThread;
    AudioRecorder audioRecorder = new AudioRecorder();
    
    DrawClient()
    {
        //Initalize the book of Points
        book = new ArrayList<>();
        //Add a new page to the book of points
        book.add(new ArrayList<>());
        
        //Initalize the current page to 0
        currentPage = 0;
        //Set the current total pages to 1
        totalPages = 1;
        //strokeSizeSel set to 1 for stroke size
        strokeSizeSel = 1;
        //Initalize the undoBuffer stack
        undoBuffer = new Stack();
        //Init client to Producer state by default
        isProducerState = true;
        //Init Recording state to false
        isRecordingState = false;
        
        //Add elements to there respective JPanels and add them to the Container
        GridBagConstraints cons = new GridBagConstraints();
        GridBagLayout gbl = new GridBagLayout();
        
        //Container holds all the JPanels inside the JFrame
        container = new JPanel(gbl);
        
        //InitDrawPanel and override the repaint method
        drawingPanel = new JPanel(gbl)
        {
            
            @Override
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                //G2 will be used for drawing the lines and setting stroke width
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL
                        , RenderingHints.VALUE_STROKE_NORMALIZE);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING
                        , RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING
                        , RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                if (!book.isEmpty())
                {
                    //We are producing in this state, keep the page being drawn
                    //  constant.
//                    if (isProducerState)
//                    {
                        for(int i = 0; i < book.get(currentPage).size(); i++)
                        {
                            for (int j = 0; j < book.get(currentPage).get(i).size(); j++)
                            {
                                try 
                                {

                                    Points pt2 = book.get(currentPage).get(i).get(j);
                                    g2.setColor(book.get(currentPage).get(i).get(j).getDrawColor());
                                    g2.setStroke(new BasicStroke(pt2.getStrokeSize()));

                                    if(j != 0)
                                    {
                                        Points pt1 = book.get(currentPage).get(i).get(j-1);
                                        g2.drawLine(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
                                    }
                                    else
                                    {
                                        g2.drawLine(pt2.getX(), pt2.getY(), pt2.getX(), pt2.getY());
                                    }
                                }
                                catch(Throwable e)
                                { 
                                    e.printStackTrace(); 
                                }
                            }
                        }
                   // }
//                    //We are consuming! Let the user switch pages at will
//                    else
//                    {
//                        for(int i = 0; i < book.get(playBackPage).size(); i++)
//                        {
//                            for (int j = 0; j < book.get(playBackPage).get(i).size(); j++)
//                            {
//                                try 
//                                {
//
//                                    Points pt2 = book.get(playBackPage).get(i).get(j);
//                                    g2.setColor(book.get(playBackPage).get(i).get(j).getDrawColor());
//                                    g2.setStroke(new BasicStroke(pt2.getStrokeSize()));
//
//                                    if(j != 0)
//                                    {
//                                        Points pt1 = book.get(playBackPage).get(i).get(j-1);
//                                        g2.drawLine(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
//                                    }
//                                    else
//                                    {
//                                        g2.drawLine(pt2.getX(), pt2.getY(), pt2.getX(), pt2.getY());
//                                    }
//                                }
//                                catch(Throwable e)
//                                { 
//                                    e.printStackTrace(); 
//                                }
//                            }
//                        }
//                    }
                }
            }
            
        };
        
        //Initalize the button and Color Panels
        buttonsPanel = new JPanel(gbl);
        colorsPanel = new JPanel(gbl);
        playBackToolsPanel = new JPanel(gbl);
        //Disable the playBackToolsPanel by default
        //  Because the client starts in Producer mode
        playBackToolsPanel.setVisible(true);
        playBackToolsPanel.setEnabled(false);
        
        //Initalize the JMenuBar and its various member
        //  Each newMenuItem is pushed into an arrayList for manipulating during
        //      Runtime.
        //Add elemtents to the menuBar
        //Initalize the menuItems arraylist
        menuItems = new ArrayList<>();
        menuBar = new JMenuBar();
        
        // File Menu, F - Mnemonic
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);
        menuItems.add(new ArrayList<>()); //Add menu list
        
        // File->New, N - Mnemonic
        JMenuItem newMenuItem = new JMenuItem("New", KeyEvent.VK_N);
        newMenuItem.addActionListener(ae -> {
            isProducerState = true;
            newBoard();
            drawingPanel.addMouseListener(ml);
            drawingPanel.addMouseMotionListener(mml);
        });
        fileMenu.add(newMenuItem);
        //Add this menu item to the menuItem list
        menuItems.get(menuItems.size()-1).add(newMenuItem);
        
        // File->Open, 0 - Mnemonic
        newMenuItem = new JMenuItem("Open", KeyEvent.VK_O);
        newMenuItem.addActionListener(ae ->{
            openFile();
        });
        fileMenu.add(newMenuItem);
        //Add this menu item to the menuItem list
        menuItems.get(menuItems.size()-1).add(newMenuItem);
        
        // File->Sava as Image, I - Mnemonic
        jFC = new JFileChooser();
        newMenuItem = new JMenuItem("Save as Image", KeyEvent.VK_I);
        newMenuItem.addActionListener(ae ->{
               exportImage();
        });
        fileMenu.add(newMenuItem);
        //Add this menu item to the menuItem list
        menuItems.get(menuItems.size()-1).add(newMenuItem);
        
        // File->Record, R - Mnemonic
        newMenuItem = new JMenuItem("Record", KeyEvent.VK_R);
        newMenuItem.addActionListener(ae -> initRecording());
        fileMenu.add(newMenuItem);
        //Add this menu item to the menuItem list
        menuItems.get(menuItems.size()-1).add(newMenuItem);

        
        // File->Exit, Q - Mnemonic
        newMenuItem = new JMenuItem("Exit", KeyEvent.VK_E);
        newMenuItem.addActionListener(ae -> System.exit(0));
        fileMenu.addSeparator();
        fileMenu.add(newMenuItem);
        
        menuItems.get(menuItems.size()-1).add(newMenuItem);
        
        // Edit Menu, E - Mnemonic
        fileMenu = new JMenu("Edit");
        fileMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(fileMenu);
        //Add this menu item to the menuItem list
        menuItems.add(new ArrayList<>()); //Add New menu bar
        
        // Edit->undo, U - Mnemonic
        newMenuItem = new JMenuItem("Undo", KeyEvent.VK_U);
        newMenuItem.addActionListener(ae -> lineUndo());
        fileMenu.add(newMenuItem);
        //Add this menu item to the menuItem list
        menuItems.get(menuItems.size()-1).add(newMenuItem);
        
        // Edit->Exit, R - Mnemonic
        newMenuItem = new JMenuItem("Redo", KeyEvent.VK_R);
        newMenuItem.addActionListener(ae -> lineRedo());
        fileMenu.add(newMenuItem);
        //Add this menu item to the menuItem list
        menuItems.get(menuItems.size()-1).add(newMenuItem);
        
        // View Menu, V - Mnemonic
        fileMenu = new JMenu("View");
        fileMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(fileMenu);
        //Add this menu item to the menuItem list
        menuItems.add(new ArrayList<>());

        // View->Toolbar, T - Mnemonic
        newMenuItem = new JMenuItem("ToolBar", KeyEvent.VK_T);
        newMenuItem.addActionListener(ae -> {
            buttonsPanel.setVisible(!buttonsPanel.isVisible());
        });
        fileMenu.add(newMenuItem);
        //Add this menu item to the menuItem list
        menuItems.get(menuItems.size()-1).add(newMenuItem);
        
        // View->Toolbar, C - Mnemonic
        newMenuItem = new JMenuItem("ColorBar", KeyEvent.VK_C);
        newMenuItem.addActionListener(ae -> {
            colorsPanel.setVisible(!colorsPanel.isVisible());
        });
        fileMenu.add(newMenuItem);
        menuItems.get(menuItems.size()-1).add(newMenuItem);
        
        //Mouse Listener
        ml = new MouseAdapter() 
        {
            @Override
            public void mousePressed(MouseEvent evt) 
            {

                //call the clearUndoBuffer function
                clearUndoBuffer();
                try {
                    Points point;
                    //Check if the client is recording
                    if (isRecordingState)
                    {
                        //Store the current point into a temp Point varible
                        //  with the current elapsed time of the point
                        point = new Points(evt.getX(),evt.getY(),
                                colChs.getColor(), currentPage
                                , System.nanoTime() - recordStartTime
                                , strokeSizeSel);
                        objOS.writeObject("\\newLine");
                        flushOS(objOS);
                        
                        System.out.println("Printing with time");
                        objOS.writeObject(point);
                        flushOS(objOS);
                    }
                    {
                        //Store the current point into a temp Point varible
                        //  with out any order
                        point = new Points(evt.getX(),evt.getY(),
                                colChs.getColor(), currentPage
                                , System.nanoTime(), strokeSizeSel);
                    }
                    //Add a new line
                    book.get(currentPage).add(new ArrayList<>());
                    //Add the point varible to the book->page->line
                    book.get(currentPage).get(book.get(currentPage).size()-1)
                            .add(point);
                } catch (Throwable ex) 
                {
                    ex.printStackTrace();
                    System.out.println("Error in MousePressed");
                }
                drawingPanel.repaint();
                updateRedoButton();
                updatePageButton();
            }
        };
        
        //Mouse Motion Listener
        mml = new MouseAdapter() 
        {
            @Override
            public void mouseDragged(MouseEvent evt) 
            {

                //call the clearUndoBuffer function
                clearUndoBuffer();

                try 
                {
                    Points point;
                    //Check if the client is recording
                    if (isRecordingState)
                    {
                        //Store the current point into a temp Point varible
                        //  with the current elapsed time of the point
                        point = new Points(evt.getX(),evt.getY(),
                                colChs.getColor(), currentPage
                                , System.nanoTime() - recordStartTime
                                , strokeSizeSel);
                        
                        System.out.println("Printing with time");
                        objOS.writeObject(point);
                        flushOS(objOS);
                    }
                    {
                        //Store the current point into a temp Point varible
                        //  with out any order
                        point = new Points(evt.getX(),evt.getY(),
                                colChs.getColor(), currentPage
                                , System.nanoTime(), strokeSizeSel);
                    }

                    //Add the point varible to the book->page->line
                    book.get(currentPage).get(book.get(currentPage).size()-1)
                            .add(point);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    System.out.println("Error in mouseDragged");

                }
                drawingPanel.repaint();
                updateRedoButton();
                updatePageButton();
            }
        };
        //Set the constraints for the drawingPanel for the drawing window
        //  to add to the container
        drawingPanel.setBackground(Color.WHITE);
        drawingPanel.addMouseListener(ml);
        drawingPanel.addMouseMotionListener(mml);
        
        saveButton = new JButton("Save");
        saveButton.setEnabled(false);
        recordButton = new JButton("Record");
        recordButton.addActionListener(ae -> initRecording());
        
        prevPageButton = new JButton("<");
        prevPageButton.addActionListener(ae -> prevPage());
        
        nextPageButton = new JButton(">");
        nextPageButton.addActionListener(ae -> nextPage());
        
        undoButton = new JButton("Undo");
        undoButton.addActionListener(ae -> lineUndo());
        
        redoButton = new JButton("Redo");
        redoButton.addActionListener(ae -> lineRedo());
        
        jTxtArea = new JTextArea("C:" + String.valueOf(currentPage+1) 
                + " T:" + String.valueOf(totalPages));
        jTxtArea.setEditable(false);
        jTxtArea.setBackground(Color.LIGHT_GRAY);
        jTxtArea.setToolTipText("Current and Total Pages");
        
        //Create Dropdown list for stroke Size selection
        strokeSizes = new Vector();
        strokeSizes.add(1);
        strokeSizes.add(2);
        strokeSizes.add(3);
        strokeSizes.add(4);
        strokeSizes.add(5);
        strokeSizes.add(50);
        
        strokeSizesBox = new JComboBox(strokeSizes);
        strokeSizesBox.setSelectedIndex(0);
        strokeSizesBox.setEnabled(false);
        strokeSizesBox.addActionListener(ae -> {
            JComboBox cb = (JComboBox)ae.getSource();
            strokeSizeSel = (int)cb.getSelectedItem();
        });
        strokeSizesBox.setEnabled(true);
        //Menu Panel
        cons.gridx = 0;
        cons.gridy = 0;
        cons.gridwidth = 2;   //2 columns wide
        cons.fill = GridBagConstraints.HORIZONTAL;
        container.add(menuBar, cons);
        
        //Drawing Panel
        cons.gridx = 0;
        cons.gridy = 3;
        cons.weightx = .8;
        cons.weighty = .8;
        cons.gridwidth = 1;   //2 columns wide
        cons.fill = GridBagConstraints.BOTH;
        container.add(drawingPanel, cons);
        
        //Color Panel
        colorsPanel.setBackground(Color.LIGHT_GRAY);
        cons.gridx = 0;
        cons.gridy = 2;
        cons.weightx = .2;
        cons.weighty = 0;
        cons.fill = GridBagConstraints.BOTH;
        container.add(colorsPanel, cons);
        
        //Buttons Panel
        buttonsPanel.setBackground(Color.lightGray);
        cons.gridx = 0;
        cons.gridy = 1;
        cons.weightx = .5;
        cons.weighty = .01;
        cons.gridwidth = 1;   //2 columns wide
        cons.fill = GridBagConstraints.BOTH;
        container.add(buttonsPanel, cons);
        
        //playBackToolsPanel Panel
        playBackToolsPanel.setBackground(Color.lightGray);
        cons.gridx = 0;
        cons.gridy = 4;
        cons.weightx = .5;
        cons.weighty = .01;
        cons.gridwidth = 1;   //2 columns wide
        cons.fill = GridBagConstraints.HORIZONTAL;
        container.add(playBackToolsPanel, cons);
        
        cons.gridx = 0;
        cons.gridy = 0;
        cons.weightx = 0;
        cons.weighty = 0;
        cons.fill = GridBagConstraints.HORIZONTAL;
        buttonsPanel.add(saveButton, cons);
        
        cons.gridx = 1;
        cons.gridy = 0;
        buttonsPanel.add(recordButton, cons);
        
        cons.gridx = 2;
        cons.gridy = 0;
        buttonsPanel.add(undoButton, cons);
        
        cons.gridx = 3;
        cons.gridy = 0;
        buttonsPanel.add(redoButton, cons);
        
        cons.gridx = 4;
        cons.gridy = 0;
        buttonsPanel.add(prevPageButton, cons);
        
        cons.gridx = 5;
        cons.gridy = 0;
        buttonsPanel.add(nextPageButton, cons);
        
        cons.gridx = 6;
        cons.gridy = 0;
        buttonsPanel.add(strokeSizesBox, cons);
        
        cons.gridx = 2;
        cons.gridy = 0;
        playBackToolsPanel.add(jTxtArea, cons);
        
        cons.gridx = 0;
        cons.gridy = 0;
        playBackToolsPanel.add(prevPageButton, cons);
        
        cons.gridx = 1;
        cons.gridy = 0;
        playBackToolsPanel.add(nextPageButton, cons);
        
        //Color Chooser implementation
        colChs = new JColorChooser();
        colChs.setPreviewPanel(new JPanel());
        colChs.setColor(Color.BLACK);
        //Remove the unused color chooser panes
        AbstractColorChooserPanel[] panels=colChs.getChooserPanels();
        for(AbstractColorChooserPanel p:panels)
        {
            String displayName=p.getDisplayName();
            switch (displayName) {
                case "HSV":
                    colChs.removeChooserPanel(p);
                    break;
                case "HSL":
                    colChs.removeChooserPanel(p);
                    break;
                case "CMYK":
                    colChs.removeChooserPanel(p);
                    break;
                //Uncomment to Disable the RGB Panel
                //                case "RGB":
                //                    colChs.removeChooserPanel(p);
                //                    break;
            }
        }
        colorsPanel.add(colChs);
        //Set the state of the buttons to be enabled or disabled
        updateRedoButton();
        updatePageButton();
    }
    
    /**
     * Returns the container JPanel which is used to set the contentPane for
     * the Window.
     * 
     * @return JPanel container
     */
    public JPanel getPanel()
    {
        return container;
    }
    
    /**
     * Exports the image of the currently drawn page to a file of the
     * Users choice.
     * 
     */
    private void exportImage()
    {
        BufferedImage bi = new BufferedImage(container.getSize().width,
        container.getSize().height, BufferedImage.TYPE_INT_ARGB); 
        Graphics g = bi.createGraphics();
        drawingPanel.paint(g);
        g.dispose(); //Clear up system resources
        File f = saveMenu();
        if (f != null)
        {
            try 
            {
                ImageIO.write(bi,"png", f);
            } catch (Exception e) 
            {}
        }        
    }
    
    /**
     * Opens a open menu dialog and returns a file with the cannonical path 
     * and filename set.
     * 
     * @return  A File with the cannonical name and path if saved.   Else the
     *          Method will return null.
     */
    public File openMenu()
    {
        int returnVal = jFC.showOpenDialog(menuItems.get(0).get(1));
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            File file2 = jFC.getSelectedFile();
            return file2;
        }
        return null;
    }
    
    /**
     * Opens a save menu dialog and returns a file with the cannonical path 
     * and filename set.
     * 
     * @return  A File with the cannonical name and path if saved.   Else the
     *          Method will return null.
     */
    public File saveMenu()
    {
        int returnVal = jFC.showSaveDialog(menuItems.get(0).get(2));
        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            File file2 = jFC.getSelectedFile();
            return file2;
        }
        return null;
    }
    
    /**
     *  openFile() prompts the user to select a folder and begins to playback
     *  the file chosen if it is valid. If no action is taken by the user
     *  to provide a file, nothing will happen. This function also loads the
     *  points.txt. into memory.
     * 
     */
    public void openFile()
    {
        fName = openMenu();
        if (fName != null)
        {
            if (fName.exists())
            {
                try {
                    isProducerState = false;
                    drawingPanel.removeMouseListener(ml);
                    drawingPanel.removeMouseMotionListener(mml);
                    newBoard();
                    //Disable buttons panel and colors panel
                    updatePanels();
                    //Disable the Record button
                    menuItems.get(0).get(3).setEnabled(false);

                    //Initialize file input variables with fName
                    fileIn = new FileInputStream(fName);
                    objIS = new ObjectInputStream(fileIn);
                    //Temporary object delcared for reading in objects
                    Object temp;
                    boolean fileEnd = false;
                    System.out.println("Reading Object");

                    //Initalize the ArrayDeque by reading from it store objects 
                    pointsQueue = new ArrayDeque<Object>();

                    //Validate the file. The first line will always be a long
                    temp = objIS.readObject();
                    
                    //Validate the file
                    if ((byte)temp == FILE_VALIDATE_BYTE)
                    {
                        while (!fileEnd)
                        {
                            temp = objIS.readObject();

                            //That last line of the file will always be a long
                            //  stop if this case is hit
                            if (temp instanceof Long)
                            {
                                //Read the ending time and exit the while loop
                                System.out.println("Last: " + (long) temp);
                                //Stop reading file
                                fileEnd = true;
                            }
                            else if (temp instanceof Points)
                            {
                                pointsQueue.add(temp);
                            }
                            else if (temp instanceof String)
                            {
                                String command = (String) temp;
                                switch ((String) temp)
                                {
                                    case "\\newLine":
                                        pointsQueue.add(temp);
                                        break;
                                    case "\\lineUndo":
                                        pointsQueue.add(temp);
                                        break;
                                    case "\\lineRedo":
                                        pointsQueue.add(temp);
                                        break;
                                    case "\\nextPage":
                                        pointsQueue.add(temp);
                                        updatePageButton();
                                        break;
                                    case "\\prevPage":
                                        pointsQueue.add(temp);
                                        updatePageButton();
                                        break;
                                    default:
                                        System.out.println("Unknown Command: " 
                                                + command);
                                }
                            }
                            else if (temp instanceof Integer)
                            {
                               pointsQueue.add(temp);
                               //System.out.println((int)temp);
                            }
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    System.out.println("Error in openFile() inputStream");
                }
                //Set the playStartTime
                playBackStartTime = System.nanoTime();
                
                //Create new recording thread
                Thread startPlayBackThread;
                startPlayBack startplayBack = new startPlayBack();
                startPlayBackThread = new Thread(startplayBack);
                startPlayBackThread.start();
                
                //Create new audio play back thread
                Thread audioPlayBackThread;
                AudioPlayback audioPlayBack = new AudioPlayback("audio.wav");
                audioPlayBackThread = new Thread(audioPlayBack);
                audioPlayBackThread.start();
            }
            else
            {
                //File is not valid, do nothing
            }
        }
        System.out.println("Exiting openFile()");
    }
    
    /**
     *  
     *  startPlayBack begins file playback from reading the points.txt.
     * 
     */
    public class startPlayBack implements Runnable
    {
        Object temp;
        Points tempPoint;
        boolean shouldPop = true;
        
        public void run()
        {
            playBackPage = 0;
            //Start playback
            while (!pointsQueue.isEmpty())
            {
                //Get the first element on the queue
                if (shouldPop)
                {
                    temp = pointsQueue.pop();
                    shouldPop = false;
                }

                if (temp instanceof Points)
                {
                    tempPoint = (Points) temp;
                    
                    if (tempPoint.getTimeDrawn() <= (System.nanoTime()-playBackStartTime))
                    {
                        shouldPop = true;
                        //Add the point varible to the book->page->line
                        book.get(playBackPage).get(book.get(playBackPage).size()-1)
                                .add(tempPoint);
                        drawingPanel.repaint();
                    }
                }
                else if (temp instanceof String)
                {
                    String command = (String) temp;
                    switch ((String) temp)
                    {
                        case "\\newLine":
                            System.out.println("Newline!");
                            book.get(currentPage).add(new ArrayList<>());
                            shouldPop = true;
                            break;
                        case "\\lineUndo":
                            shouldPop = true;
                            lineUndo();
                            break;
                        case "\\lineRedo":
                            shouldPop = true;
                            lineRedo();
                            break;
                        case "\\nextPage":
                            shouldPop = true;
                            nextPage();
                            break;
                        case "\\prevPage":
                            shouldPop = true;
                            prevPage();
                            break;
                        default:
                            System.out.println("Unknown Command: " 
                                    + command);
                    }
                }
                else if (temp instanceof Integer)
                {
                    shouldPop = true;
                    playBackPage = (int) temp;
                    updatePageButton();
                    System.out.println(currentPage + " : " + totalPages + " : " + playBackPage);
                }
            }
            System.out.println("Done playing back");
        }
    }
    
    /**
     *  The first time initRecording() is called, it sets the client into a 
     *  recording state. When called again, it takes it out of the 
     *  recording state.
     * 
     */
    public void initRecording()
    {
       
        //If the client is NOT in a recording state
        if (!isRecordingState)
        {
            //Clear the board if anything currently exists before recording
            newBoard();
            isRecordingState = true;
            try 
            {
                audioRecorderThread = new Thread(audioRecorder);
                //File (0) -> Open(2)
                menuItems.get(0).get(1).setEnabled(false);
                                drawingPanel.setEnabled(false);
                colorsPanel.setEnabled(false);
                //File (0) -> Record (2)
                menuItems.get(0).get(3).setText("Stop Recording");

                recordButton.setText("Stop Recording");
                recordEndTime = 0;
                recordStartTime = System.nanoTime();
                
                //Create the file to holds the serialized points class
                fName = new File("points.txt");
                fileOut = new FileOutputStream(fName);
                objOS = new ObjectOutputStream(fileOut);
                
                //Write the file Validation byte to the first line of the .txt
                objOS.writeObject(FILE_VALIDATE_BYTE);
                //Start Recording
                audioRecorderThread.start();
            } 
            catch (Throwable ex) 
            {
                System.out.println("File Not Found or error in thread!");
                ex.printStackTrace();
                //File (0) -> Open(2)
                menuItems.get(0).get(1).setEnabled(true);
                //File (0) -> Stop Recording (2)
                menuItems.get(0).get(3).setText("Record");
                recordButton.setText("Record");
            }
        }
        //Else this function is called to finalize recording and close up lose
        //  ends.
        else
        {
            try 
            {
                //Set the final time the program stopped recording
                recordEndTime = System.nanoTime() - recordStartTime;
                objOS.writeObject(recordEndTime);
                flushOS(objOS);
                objOS.close();
                audioRecorder.stopRecording();
                
                //Reset Recording State and subMenu
                isRecordingState = false;
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error Closing objectOutputStream");
            }
            //File (0) -> Open(2)
            menuItems.get(0).get(1).setEnabled(true);
            //File (0) -> Stop Recording (2)
            menuItems.get(0).get(3).setText("Record");
            //Set the button back to normal
            recordButton.setText("Record");
//            //Call saveMenu() to prompt the user for a file to save as
//            fName = saveMenu();
//            System.out.println(fName.getAbsolutePath());
//]
//            if(fName.renameTo(new File("C:\\folderB\\" + fName.getName())))
//            {
//                System.out.println("File is moved successful!");
//            }
//            else
//            {
//                System.out.println("File is failed to move!");   
//            }
        }
    }
    
    /**
     * Flushes the ObjectOutputStream passed to it
     */
    private void flushOS(ObjectOutputStream objOS)
    {
        try {
            objOS.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error flushing output stream");
        }
    }
    
    /**
     *  lineUndo() removes the last line drawn from the book on the currentPage
     *  off the client and pushes that line onto a stack.
     * 
     */
    public void lineUndo()
    {
        if (!book.get(currentPage).isEmpty())
        {   
            undoBuffer.push(book.get(currentPage).get(book.get(currentPage).size()-1));
            book.get(currentPage).remove(book.get(currentPage).size()-1);
            drawingPanel.repaint();
            updateRedoButton();
            //If the client is in Recording mode
            //  Output a keyword to the file for input reading later on
            //      to let the client know a the lineUndo() has been called.
            if (isRecordingState)
            {
                try {
                    objOS.writeObject("\\lineUndo");
                    flushOS(objOS);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Error writing to file in lineRedo()");
                }
            }
        }
    }
    
    /**
     *  lineRedo() pops the last element pushed onto the stack and adds it
     *  back into the line of the current page.
     * 
     */
    public void lineRedo()
    {
        if (!undoBuffer.empty())
        {
            book.get(currentPage).add((ArrayList<Points>)undoBuffer.pop());
            drawingPanel.repaint();
            updatePageButton();
            //If the client is in Recording mode
            //  Output a keyword to the file for input reading later on
            //      to let the client  know a the lineRedo() has been called.
            if (isRecordingState)
            {
                try {
                    objOS.writeObject("\\lineRedo");
                    flushOS(objOS);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Error writing to file in lineRedo()");
                }
            }
        }
    }
    
    /**
     *  nextPage() adds a new page to the book if needed repaints the drawPanel
     *  to the current page. Calls updatePageButton() and updatePageCounter();
     * 
     */
    private void nextPage()
    {
        try
        {
            currentPage++;
            //Check if a new Page needs to be added or wether the user is just
            //  cycling between existing ones.
            if (currentPage == totalPages)
            {
                book.add(new ArrayList<>());
                totalPages++;
            }
            updatePageButton();
            updatePageCounter();
            drawingPanel.repaint();
        } catch(Throwable ex)
        {
            System.out.println("Error in nextPage");
        }
        
        //If the client is in Recording mode
        //  Output a keyword to the file for input reading later on
        //      to let the client know a page has been changed.
        if (isRecordingState)
        {
            try {
                objOS.writeObject("\\nextPage");
                flushOS(objOS);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error writing to file in lineRedo()");
            }
        }
    }
    
    /**
     *  prevPage() adds repaints the drawPanel to the last page. 
     *  Calls updatePageButton() and updatePageCounter();
     * 
     */
    private void prevPage()
    {
        if (currentPage != 0)
        {
            try
            {
                currentPage--;
                updatePageButton();
                updatePageCounter();
                drawingPanel.repaint();
            } catch(Throwable ex)
            {
                System.out.println("Error in nextPage");
            }
        }
        //If the client is in Recording mode
        //  Output a keyword to the file for input reading later on
        //      to let the client know a page has been changed.
        if (isRecordingState)
        {
            try {
                objOS.writeObject("\\prevPage");
                flushOS(objOS);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error writing to file in lineRedo()");
            }
        }
    }
    
    /**
     *  clearUndoBuffer() clears the undoBuffer stack and is usually called
     *  when an action is performed that overwrite previous lines.
     * 
     */
    private void clearUndoBuffer()
    {
        if (!undoBuffer.empty())
        {
            undoBuffer.clear();
        }
    }
    
    /**
     *  newBoard() initializes all the Container items to a brand new state,
     *  along with variables.
     * 
     */
    private void newBoard()
    {
        currentPage = 0;
        totalPages = 1;
        book.clear();
        book = new ArrayList<>();
        book.add(new ArrayList<>());
        clearUndoBuffer();
        buttonInit();
        updatePageButton();
        updateRedoButton();
        updatePageCounter();
        setMenuItemStates(); //Re-enable menu items if they were disabled!
        drawingPanel.repaint();
    }
    
    /**
     *  setMenuState() Disables or enables menuItem options based on 
     *  the isProducerState
     * 
     */
    private void setMenuItemStates()
    {
        //Disable or enable Edit options and view option based on isProducerState
        menuItems.get(1).get(0).setEnabled(isProducerState);
        menuItems.get(1).get(1).setEnabled(isProducerState);

        //Disable or enable View Bar
        menuItems.get(2).get(0).setEnabled(isProducerState);
        menuItems.get(2).get(1).setEnabled(isProducerState);
        
        //Disable or Enable Record button
        menuItems.get(0).get(3).setEnabled(isProducerState);

    }
    
    /**
     *  updateRedoButton() checks whether to enable or disable the Redo button
     *  based on if the undoBuffer has items or not.
     * 
     */
    private void updateRedoButton()
    {
        if (isProducerState)
        {
            if (undoBuffer.isEmpty())
            {
                redoButton.setEnabled(false);
                menuItems.get(1).get(1).setEnabled(false);
            }
            else
            {
                redoButton.setEnabled(true);
                menuItems.get(1).get(1).setEnabled(true);
            }
        }
    }
    
    /**
     *  updatePageButton() checks if the client is in a producing state.
     *  isProducerState == true - Client is Producing
     *          Disable or enable the page browse buttons.
     *          Checks if the client is Recording
     *          isRecordingState == true -
     *                  Output a keyword to the file for input reading later on
     *                  to let the client know a page has been changed.
     * 
     *  isProducerState == false - Client is Consuming
     *          Disable or enable the page browse buttons based on input.
     */
    private void updatePageButton()
    {
        //The Client is in a Producing State
        if (isProducerState)
        {

            if (currentPage == 0)
            {
                nextPageButton.setEnabled(true);
                prevPageButton.setEnabled(false);
            }
            else
                prevPageButton.setEnabled(true);
            
            //If the client is in Recording mode
            //  Output a keyword to the file for input reading later on
            //      to let the client know a page has been changed.
            if (isRecordingState)
            {
                try {
                    objOS.writeObject(currentPage);
                    flushOS(objOS);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Error writing to file in updatePageButton()");
                }
            }
        }
        //Else the Client is in a Consuming State
        else
        {
            System.out.println("Not in prod state");
            //If the Current total pages are greater than 1
            //  Disable the previous button if we're on page 0
            //      Else Enable and enable the next page button
            if (totalPages > 1)
            {
                if (currentPage == 0)
                    prevPageButton.setEnabled(false);
                else
                    prevPageButton.setEnabled(true);
                
                //Disable the next Page button if the user is on the most recent
                //  page
                if (currentPage == totalPages)
                    nextPageButton.setEnabled(false);
                else
                    nextPageButton.setEnabled(true);
            }
            //Else if there is only one page
            //  Disable both the previous and the next Page Buttons
            else
            {
                prevPageButton.setEnabled(false);
                nextPageButton.setEnabled(false);
            }
        }
    }
    
    /**
     *  updatePanels() updates the viewing panels based on whether the client is
     *  consuming or not.
     */
    private void updatePanels()
    {
        if (isProducerState)
        {
            buttonsPanel.setEnabled(true);
            colorsPanel.setEnabled(true);
        }
        else
        {
            buttonsPanel.setEnabled(false);
            colorsPanel.setEnabled(false);
        }
    }
    
    /**
     *  updatePageCount() updates the jTxtArea field with the current page
     *  and the running total of pages.
     * 
     */
    private void updatePageCounter()
    {
        jTxtArea.setText("C:" + String.valueOf(currentPage+1) 
                + " T:" + String.valueOf(totalPages));
    }
    
    /**
     *  updatePageCount() initializes buttons to the current state of 
     *  isProducerState. Used to quickly initialize.
     * 
     */
    private void buttonInit()
    {
        prevPageButton.setEnabled(isProducerState);
        nextPageButton.setEnabled(isProducerState);
//        saveButton.setEnabled(isProducerState);
        recordButton.setEnabled(isProducerState);
        undoButton.setEnabled(isProducerState);
        redoButton.setEnabled(isProducerState);
        colorsPanel.setVisible(isProducerState);
        buttonsPanel.setVisible(isProducerState);
    }
    
    /**
     *  getIsProducerState() returns the current state of the client.
     *  true - producing.
     *  false - consuming.
     * 
     * @returns isProducerState
     */
    public boolean getIsProducerState() 
    {
        return isProducerState;
    }
    
    /**
     *  getIsRecordingState() returns the current state of the client.
     *  true - recording.
     *  false - not-recording.
     * 
     * @returns isRecordingState
     */
    public boolean getIsRecordingState() 
    {
        return isRecordingState;
    }
    
    public static void main(String[] args) 
    {
        window.setLayout(new GridBagLayout());
        DrawClient panel = new DrawClient();
        window.setContentPane(panel.getPanel());
        window.setVisible(true);    
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(600, 600);
    }    
}
