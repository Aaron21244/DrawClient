/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drawclient;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author Aaron
 */
public class Points implements Serializable {
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
