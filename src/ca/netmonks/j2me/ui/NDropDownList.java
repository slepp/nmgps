/*
 * NDropDownList.java
 *
 * Created on February 22, 2004, 2:36 AM
 */

package ca.netmonks.j2me.ui;

import com.motorola.lwt.*;
import javax.microedition.lcdui.*;
import ca.netmonks.j2me.Debug;
import com.mot.iden.math.Float;

/**
 *
 * @author  slepp
 */
public class NDropDownList
extends Component {
    private String[] items = null;
    private int selectedIndex = 0;
    
    private int preferredWidth = 0;
    
    private Font textFont = NStyle.LIST_FONT;
    
    private String label = null;
    
    private boolean popupEnabled;
    
    private boolean first = true;
    private int lineHeight = 0;
    private int displayLines = 0;
    private int totalHeight = 0;
    private int startPos = 0;
    private int endPos = 0;
    
    /** Creates a new instance of NDropDownList */
    public NDropDownList(String label, String[] items) {
        this.label = label;
        this.items = items;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public int getPreferredHeight() {
        return textFont.getHeight() + 4;
    }
    
    public void setPreferredWidth(int width) {
        preferredWidth = width;
    }
    
    public int getPreferredWidth() {
        if(preferredWidth != 0)
            return preferredWidth;
        
        int max = 0;
        for(int i = 0; i < items.length; i++) {
            if(textFont.stringWidth(items[i]) > max)
                max = textFont.stringWidth(items[i]);
        }
        
        int labelWidth = textFont.stringWidth(getLabel()) + 3;
                
        return (labelWidth + (max < 5 ? 20 : max) + 18);
    }
 
    public void paint(Graphics g) {
        int width = g.getClipWidth() - 1;
        int height = g.getClipHeight() - 1;
        
        // Figure out the height of our box
        if(first) {
            lineHeight = textFont.getHeight() + 2;
            totalHeight = (lineHeight * items.length);
            displayLines = (getParent().getHeight() - 20) / lineHeight;
            if(items.length > displayLines) {
                totalHeight = lineHeight * displayLines;
            }
            first = false;
            startPos = 0;
            endPos = (displayLines >= items.length ? items.length - 1 : displayLines);
        }

        g.setFont(textFont);
        
        if(getLabel() != null) {
            g.setColor(NStyle.LIST_FOCUSED_TEXT);
            g.drawString(getLabel(), 0, 2 + textFont.getBaselinePosition(), Graphics.BASELINE | Graphics.LEFT);
            g.translate(textFont.stringWidth(getLabel()) + 3, 0);
            width -= textFont.stringWidth(getLabel()) + 3;
        }

        if(popupEnabled) {
            paintPopup(g, width);
            return;
        }
        
        if(hasFocus()) {
            g.setColor(NStyle.LIST_FOCUSED_BACKGROUND);
            g.fillRect(0, 0, width, height);
            g.setColor(NStyle.LIST_FOCUSED_BORDER);
        } else {
            g.setColor(NStyle.LIST_UNFOCUSED_BACKGROUND);
            g.fillRect(0, 0, width, height);        
            g.setColor(NStyle.LIST_UNFOCUSED_BORDER);
        }
        
        // Draw the bounding rectangle
        g.drawRect(0, 0, width, height);
        
        // Draw the little bitty arrow
        g.drawLine(width - 10, 0, width - 10, height);
        g.fillTriangle(width - 8, 4, width - 2, 4, width - 5, height - 4);
        
        g.drawString(items[selectedIndex], 2, 2 + textFont.getBaselinePosition(), Graphics.BASELINE | Graphics.LEFT);
        
    }
    
    public void paintPopup(Graphics g, int width) {
        // Increase our clipping area and give us full control
        int x = g.getTranslateX();
        int y = g.getTranslateY();
        
        g.translate(0 - x, 0 - y);
        g.setClip(0, 0, getParent().getWidth(), getParent().getHeight());
        
        // Try to put it somewhere nice
        if(y + totalHeight < getParent().getHeight()) {
            g.translate(x, y);
        } else if(y + getPreferredHeight() - totalHeight > 0) {
            g.translate(x, y + getPreferredHeight() - totalHeight);
        } else {
            g.translate(x, 0);
        }
        
        // Draw the backdrop
        g.setColor(NStyle.LIST_FOCUSED_BACKGROUND);
        g.fillRect(0, 0, width, totalHeight);
        // And a drop shadow
        g.setColor(NStyle.COLOR_GRAY);
        g.fillRect(3, 2, width, totalHeight);
        
        // Draw the borders
        g.setColor(NStyle.LIST_FOCUSED_BORDER);
        g.drawRect(0, 0, width, totalHeight);
        
        // Set the clipping and translate to position text easily
        g.setClip(1, 1, width - 1, totalHeight - 1);
        g.translate(1, 1);
        
        // Draw out the available text strings
        for(int i = 0; i < endPos - startPos; i++) {
            if(i + startPos == selectedIndex)
                g.setColor(NStyle.LIST_SELECTED_BACKGROUND);
            else
                g.setColor(NStyle.LIST_FOCUSED_BACKGROUND);
            g.fillRect(0, (i * lineHeight) - (i + startPos == selectedIndex ? 1 : 0), width, lineHeight);
            if(i + startPos == selectedIndex)
                g.setColor(NStyle.LIST_SELECTED_TEXT);
            else
                g.setColor(NStyle.LIST_FOCUSED_TEXT);
            g.drawString(items[i + startPos], 2, (i * lineHeight) + textFont.getBaselinePosition(), Graphics.BASELINE | Graphics.LEFT);
        }
        
        if(displayLines < items.length - 1) {
            // Draw the scrollbar
            g.setColor(NStyle.LIST_SCROLL_BACKGROUND);
            g.fillRect(width - 10, 0, width, totalHeight);
        
            if(startPos > 0) {
                g.setColor(NStyle.LIST_SCROLL_FOREGROUND);
            } else {
                g.setColor(NStyle.LIST_SCROLL_SHADED);
            }
            g.fillTriangle(width - 9, 6, width - 3, 6, width - 6, 2);
            if(endPos <= items.length - 1) {
                g.setColor(NStyle.LIST_SCROLL_FOREGROUND);
            } else {
                g.setColor(NStyle.LIST_SCROLL_SHADED);
            }
            g.fillTriangle(width - 9, totalHeight - 8, width - 3, totalHeight - 8, width - 6, totalHeight - 4);
            
            g.setColor(NStyle.LIST_SCROLL_SLIDER);
            int top = 10 + (startPos * (totalHeight/items.length));
            int len = ((totalHeight / items.length) * displayLines) - 10;
            // Fucking degenerate case and lack of FP
            if(items.length == endPos) {
                len = totalHeight - top - 10;
            }
            g.fillRect(width - 10, top, 10, len);
            
            g.setColor(NStyle.LIST_FOCUSED_BORDER);
            g.drawLine(width - 10, 0, width - 10, totalHeight);
            g.drawLine(width - 10, 10, width, 10);
            g.drawLine(width - 10, totalHeight - 10, width, totalHeight - 10);
        }
    }
    
    public boolean acceptsFocus() {
        return true;
    }
    
    protected boolean keyRepeated(int keyCode) {
        return keyPressed(keyCode);        
    }
    
    protected boolean keyPressed(int keyCode) {
        int gameAction = getParent().getGameAction(keyCode);
        if(keyCode == NConstants.KEY_OK) {
            if(popupEnabled) {
                popupEnabled = false;
                getParent().repaint();
            } else {
                popupEnabled = true;
                getParent().repaint();
            }
            return true;
        } else if(gameAction == Canvas.DOWN) {
            if(popupEnabled) {
                if(selectedIndex < items.length-1) {
                    ++selectedIndex;
                    if(selectedIndex >= endPos) {
                        startPos++;
                        endPos++;
                    }
                    getParent().repaint();
                }
            } else {
                getParent().setFocusNext();
            }
            return true;
        } else if(gameAction == Canvas.UP) {
            if(popupEnabled) {
                if(selectedIndex > 0) {                
                    --selectedIndex;
                    if(selectedIndex < startPos) {
                        startPos--;
                        endPos--;
                    }
                    getParent().repaint();
                }
            } else {
                getParent().setFocusPrevious();
            }
            return true;
        } else if(gameAction == Canvas.LEFT) {
            getParent().setFocusPrevious();
            return true;
        } else if(gameAction == Canvas.RIGHT) {
            getParent().setFocusNext();            
            return true;
        }
        return false;
    }
    
/*    public void gainedFocus() {
        super.gainedFocus();
    }*/
    
/*    public void lostFocus() {
        super.lostFocus();
    }*/
}
