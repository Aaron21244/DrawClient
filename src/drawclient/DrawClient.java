/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drawclient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Aaron
 */
public class DrawClient{

    /**
     * @param args the command line arguments
     */
    
    static JFrame window = new JFrame("Drawing Client");
    static JPanel drawingPanel;
    static JPanel buttonsPanel;
    static JPanel colorsPanel;
    static JPanel container;

    
    //Book of pages, lines, cords
    ArrayList<ArrayList<ArrayList<Points>>> book;
    //CurrentPage holds the current drawing page of the client
    int currentPage;
    //currentColor holds the current color being used
    Color currentColor;

    //constructor
    DrawClient()
    {
        //Initalize the book of Points
        book = new ArrayList<>();
        
        book.add(new ArrayList<>());
        
        //Initalize the current page to 0
        currentPage = 0;
        //Initalize the current Color to Black
        currentColor = Color.BLACK;

        GridBagConstraints cons = new GridBagConstraints();
        GridBagLayout gbl = new GridBagLayout();
        
        container = new JPanel(gbl);
        drawingPanel = new JPanel(gbl)
        {
            
            @Override
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                System.out.println("in the repaint");
                for(int i = 0; i < book.get(currentPage).size(); i++)
                {
                    for (int j = 0; j < book.get(currentPage).get(i).size(); j++)
                    {
                        try 
                        {
                            System.out.println(book.get(currentPage).get(i).size());
                            Points pt2 = book.get(currentPage).get(i).get(j);
                            if(j != 0)
                            {                System.out.println("in the pos");

                                Points pt1 = book.get(currentPage).get(i).get(j-1);
                               
                                g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
                            }
                            else
                            {
                                System.out.println("in the pos2");
                                g.drawLine(pt2.x,pt2.y,pt2.x,pt2.y);
                            }
                        }
                        catch(Throwable e)
                        { e.printStackTrace(); }
                    }
                }
            }
            
        };
        
        buttonsPanel = new JPanel(gbl);
        colorsPanel = new JPanel(gbl);
                
        //Set the constraints for the drawingPanel were about 
        //  to add to the window
        drawingPanel.setBackground(Color.WHITE);
        drawingPanel.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent evt) {
                    try {
                        System.out.println("MouseDragged");
                        Points points = new Points(evt.getX(),evt.getY(),
                                currentColor, currentPage);
                        book.get(currentPage).get(book.get(currentPage).size()-1)
                                .add(points);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        System.out.println("Error in mouseDragged");

                    }
                    drawingPanel.repaint();
                }
            }
        );
        drawingPanel.addMouseListener(new MouseAdapter() {
            
                @Override
                public void mousePressed(MouseEvent evt) {
                    //Attempt to send the cords and boolean value
                    try {
                        Points points = new Points(evt.getX(),evt.getY(),
                                currentColor, currentPage);

                        book.get(currentPage).add(new ArrayList<>());
                        book.get(currentPage).get(book.get(currentPage).size()-1)
                                .add(points);


                    } catch (Throwable ex) 
                    {
                        ex.printStackTrace();
                        System.out.println("Error in MousePressed");
                    }
                    drawingPanel.repaint();
                }
            }
        );
        
        cons.gridx = 0;
        cons.gridy = 0;
        cons.weightx = .8;
        cons.weighty = .8;
        cons.fill = GridBagConstraints.BOTH;
        container.add(drawingPanel, cons);
        
        //Buttons Panel
        buttonsPanel.setBackground(Color.lightGray);
        cons.gridx = 0;
        cons.gridy = 1;
        cons.weightx = .2;
        cons.weighty = .8;
        cons.fill = GridBagConstraints.BOTH;
        container.add(buttonsPanel, cons);
        
        //Colors panel
        colorsPanel.setBackground(Color.GRAY);
        cons.gridx = 1;
        cons.gridy = 0;
        cons.weighty = .2;
        cons.gridwidth = 2;
        cons.fill = GridBagConstraints.BOTH;
        container.add(colorsPanel, cons);
        
        
    }
    
    public JPanel getPanel()
    {
        return container;
    }
    
    //Cordinate class
    public static class Points implements Serializable {
        int x;
        int y;
        int page;
        Color drawColor;
        
        Points(int x, int y, Color drawColor, int page) {
            this.x = x;
            this.y = y;
            this.drawColor = drawColor;
            this.page = page;
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
        
        public void setDrawColor(Color color)
        {
            this.drawColor = color;
        }
        
                
        public void setPage(int page)
        {
            this.page = page;
        }
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
