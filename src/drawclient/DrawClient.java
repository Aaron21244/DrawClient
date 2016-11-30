/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drawclient;

import static drawclient.DrawClient.drawingPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
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
    
    //undoBuffer holds all the lines Undone so they may be readded
    Stack undoBuffer;
    
    //Stroke Sizes holds the current selections of brush sizes available
    Vector strokeSizes;
    
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
    private int totalPages;
    private int strokeSizeSel; 
    
    //isProducerState holds wether the client is in producer or consumer mode
    private boolean isProducerState;
    
    //Recording Variables
    private boolean isRecordingState;
    long recordStartTime;
    long recordEndTime;
    
    //File IO Variables
    FileOutputStream fileOut;
    FileInputStream fileIn;
    File fName;
    

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
                System.out.println("in the repaint");
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
                    for(int i = 0; i < book.get(currentPage).size(); i++)
                    {
                        for (int j = 0; j < book.get(currentPage).get(i).size(); j++)
                        {
                            try 
                            {
                               // System.out.println(book.get(currentPage).get(i).size());
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
                }
            }
            
        };
        
        //Initalize the button and Color Panels
        buttonsPanel = new JPanel(gbl);
        colorsPanel = new JPanel(gbl);
        
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
        
        
        ml = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {

                //call the clearUndoBuffer function
                clearUndoBuffer();
                try {
                    System.out.println("In MousePressed");
                    //Store the current point into a temp Point varible
                    Points point = new Points(evt.getX(),evt.getY(),
                            colChs.getColor(), currentPage
                            , System.nanoTime(), strokeSizeSel);

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
        
        mml = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {

                //call the clearUndoBuffer function
                clearUndoBuffer();

                try {
                    System.out.println("MouseDragged");

                    //Store the current point into a temp Point varible
                    Points point = new Points(evt.getX(),evt.getY(),
                            colChs.getColor(), currentPage, 
                            System.nanoTime(), strokeSizeSel);

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
        
        recordButton = new JButton("Record");
        
        prevPageButton = new JButton("<");
        prevPageButton.addActionListener(ae -> prevPage());
        
        nextPageButton = new JButton(">");
        nextPageButton.addActionListener(ae -> nextPage());
        
        undoButton = new JButton("Undo");
        undoButton.addActionListener(ae -> lineUndo());
        
        redoButton = new JButton("Redo");
        redoButton.addActionListener(ae -> lineRedo());
        
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
        
        //Color Chooser implementation
        colChs = new JColorChooser();
        colChs.setPreviewPanel(new JPanel());
        colChs.setColor(Color.BLACK);
        //Remove the unused color chooser panes
        AbstractColorChooserPanel[] panels=colChs.getChooserPanels();
        for(AbstractColorChooserPanel p:panels){
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
    
    public JPanel getPanel()
    {
        return container;
    }
    
    //Cordinate class
    public static class Points implements Serializable {
        private int x;
        private int y;
        private int page;
        private int strokeSize;
        private Color drawColor;
        private long timeDrawn;
        
        
        Points(int x, int y, Color drawColor, int page, long timeDrawn
                , int strokeSize) {
            this.x = x;
            this.y = y;
            this.drawColor = drawColor;
            this.page = page;
            this.timeDrawn = timeDrawn;
            this.strokeSize = strokeSize;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Color getDrawColor()
        {
            return drawColor;
        }
        
        public int getPage()
        {
            return page;
        }
        
        public int getStrokeSize()
        {
            return strokeSize;
        }
        
        public void setDrawColor(Color color)
        {
            this.drawColor = color;
        }
        
                
        public void setPage(int page)
        {
            this.page = page;
        }
    }
    
    public void openFile()
    {
        isProducerState = false;
        drawingPanel.removeMouseListener(ml);
        drawingPanel.removeMouseMotionListener(mml);
        newBoard();
        menuItems.get(0).get(2).setEnabled(false);
        /**
         * Implement File Opening Code here
         * 
         * 
         * 
         * 
         * 
         * 
         * 
         * 
         * 
         */
    }
    
    public void initRecording()
    {
        isRecordingState = !isRecordingState;
        if (isRecordingState)
        {
            try {
                //File (0) -> Open(2)
                menuItems.get(0).get(1).setEnabled(false);
                //File (0) -> Record (2)
                menuItems.get(0).get(2).setText("Stop Recording");
                recordEndTime = 0;
                recordStartTime = System.nanoTime();
                fName = new File("Points.txt");
                fName.createNewFile();
                fileIn = new FileInputStream(fName);
            } catch (FileNotFoundException ex) {
                System.out.println("File Not Found!");
                
                //Reset Recording State and subMenu
                isRecordingState = !isRecordingState;
                //File (0) -> Open(2)
                menuItems.get(0).get(1).setEnabled(true);
                //File (0) -> Stop Recording (2)
                menuItems.get(0).get(2).setText("Record");
            }
        }
        else
        {
            //File (0) -> Open(2)
            menuItems.get(0).get(1).setEnabled(true);
            //File (0) -> Stop Recording (2)
            menuItems.get(0).get(2).setText("Record");
            recordEndTime = System.nanoTime() - recordStartTime;
        }
    }
    
    private void lineUndo()
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
                /**
                 * Implement code for outputting keywords to a file for the
                 * lineUndo here
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 */
            }
        }
    }
    
    private void lineRedo()
    {
        if (!undoBuffer.empty())
        {
            book.get(currentPage).add((ArrayList<Points>)undoBuffer.pop());
            drawingPanel.repaint();
            updatePageButton();
            //If the client is in Recording mode
            //  Output a keyword to the file for input reading later on
            //      to let the client know a the lineRedo() has been called.
            if (isRecordingState)
            {
                /**
                 * Implement code for outputting keywords to a file for the
                 * lineRedo here
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 */
            }
        }
    }
    
    private void nextPage()
    {
        try
        {
            book.add(new ArrayList<>());
            currentPage++;
            totalPages++;
            updatePageButton();
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
            /**
             * Implement code for outputting keywords to a file for the
             * Next button here
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             */
        }
    }
    
    private void prevPage()
    {
        if (currentPage != 0)
        {
            try
            {
                currentPage--;
                updatePageButton();
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
            /**
             * Implement code for outputting keywords to a file for the
             * Previous button here
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             * 
             */
        }
    }
    
    private void clearUndoBuffer()
    {
        if (!undoBuffer.empty())
        {
            undoBuffer.clear();
        }
    }
    
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
        setMenuItemStates(); //Re-enable menu items if they were disabled!
        drawingPanel.repaint();
    }
    
    private void setMenuItemStates()
    {
        //Disable or enable Edit options and view option based on isProducerState
        menuItems.get(1).get(0).setEnabled(isProducerState);
        menuItems.get(1).get(1).setEnabled(isProducerState);

        //Disable or enable View Bar
        menuItems.get(2).get(0).setEnabled(isProducerState);
        menuItems.get(2).get(1).setEnabled(isProducerState);
        
        //Disable or Enable Record button
        menuItems.get(0).get(2).setEnabled(isProducerState);

    }
    
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
                /**
                 * Implement code for outputting keywords to a file for the
                 * Undo button here
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 * 
                 */
            }
        }
        //Else the Client is in a Consuming State
        else
        {
            //If the Current total pages are greater than 1
            //  Disable the previous button if we're on page 0
            //      Else Enable and enable the next page button
            if (totalPages > 1)
            {
                if (currentPage == 0)
                    prevPageButton.setEnabled(false);
                else
                    prevPageButton.setEnabled(true);
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
    
    private void buttonInit()
    {
        prevPageButton.setEnabled(isProducerState);
        nextPageButton.setEnabled(isProducerState);
        saveButton.setEnabled(isProducerState);
        recordButton.setEnabled(isProducerState);
        undoButton.setEnabled(isProducerState);
        redoButton.setEnabled(isProducerState);
        colorsPanel.setVisible(isProducerState);
        buttonsPanel.setVisible(isProducerState);
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
