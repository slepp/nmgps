/*
 * NStyle.java
 *
 * Created on February 22, 2004, 2:48 AM
 *
 * Copyright (c) 2004 Stephen Olesen <slepp@netmonks.ca>
 */

package ca.netmonks.j2me.ui;

import javax.microedition.lcdui.Font;
import com.motorola.iden.lnf.*;

/**
 * Stylistic constants for NetMonks Widgets.
 *
 * @author  Stephen Olesen
 * @version $Revision: 1.1 $
 */
public final class NStyle {
    /** System defined colorpalette */
    private final static ColorPalette pal = ColorPalette.getCurrent();
    
    /** General Colours */
    public final static int COLOR_BLACK = 0x000000;
    public final static int COLOR_WHITE = 0xffffff;
    public final static int COLOR_GRAY  = 0xaaaaaa;
    public final static int COLOR_RED   = 0xff0000;
    public final static int COLOR_GREEN = 0x00ff00;
    public final static int COLOR_BLUE  = 0x0000ff;
    
    /** General Fonts */
    public final static Font FONT_SMALL_PROPORTIONAL = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    
    /** List Boxes */
    public final static Font LIST_FONT = FONT_SMALL_PROPORTIONAL;
    
    public final static int LIST_FOCUSED_TEXT = pal.getColor(ColorPalette.FOREGROUND_COLOR);
    public final static int LIST_FOCUSED_BACKGROUND = pal.getColor(ColorPalette.BACKGROUND_COLOR);
    public final static int LIST_FOCUSED_BORDER = pal.getColor(ColorPalette.BORDER_COLOR);
    
    public final static int LIST_UNFOCUSED_TEXT = pal.getColor(ColorPalette.FOREGROUND_COLOR, ColorPalette.DARKER_COLOR_MODIFIER);
    public final static int LIST_UNFOCUSED_BACKGROUND = pal.getColor(ColorPalette.BACKGROUND_COLOR, ColorPalette.DARKER_COLOR_MODIFIER);
    public final static int LIST_UNFOCUSED_BORDER = pal.getColor(ColorPalette.BORDER_COLOR);
    
/*    public final static int LIST_SELECTED_TEXT = COLOR_BLACK;
    public final static int LIST_SELECTED_BACKGROUND = 0xaaaaff;*/
    public final static int LIST_SELECTED_TEXT = pal.getColor(ColorPalette.HIGHLIGHTED_FOREGROUND_COLOR);
    public final static int LIST_SELECTED_BACKGROUND = pal.getColor(ColorPalette.HIGHLIGHTED_FILL_COLOR);

    public final static int LIST_SCROLL_BACKGROUND = pal.getColor(ColorPalette.HIGHLIGHTED_FILL_COLOR);
    public final static int LIST_SCROLL_FOREGROUND = pal.getColor(ColorPalette.HIGHLIGHTED_FOREGROUND_COLOR);
    public final static int LIST_SCROLL_SHADED     = pal.getColor(ColorPalette.DISABLED_COLOR);
    public final static int LIST_SCROLL_SLIDER     = pal.getColor(ColorPalette.HIGHLIGHTED_FOREGROUND_COLOR, ColorPalette.LIGHTER2x_COLOR_MODIFIER);
    
    /** Creates a new instance of NStyle */
    protected NStyle() {
    }
    
}
