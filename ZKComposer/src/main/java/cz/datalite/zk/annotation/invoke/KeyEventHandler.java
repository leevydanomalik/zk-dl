/*
 * Copyright (c) 2011, DataLite. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package cz.datalite.zk.annotation.invoke;

import cz.datalite.zk.annotation.Keys;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.KeyEvent;

/**
 * <p>Handler filters key events if they are set. ZK
 * doesnt offer specific event for each hot key so if
 * there are more hot keys set on one component then
 * there is only one fired event. But each source of
 * event can be handled in a different way (different
 * method, payload etc.) so there has to be key filter.
 * Otherwise user would need to filter it himself.</p>
 *
 * @author Karel Čemus <cemus@datalite.cz>
 */
public class KeyEventHandler extends Handler {

    /** has to be used ctrl or not */
    private final boolean ctrl;

    /** has to be used alt or not */
    private final boolean alt;

    /** has to be used shift or not */
    private final boolean shift;

    /** ascii code of key */
    private final int code;

    private Keys key = null;

    public KeyEventHandler( Invoke inner, String event ) {
        super( inner );
        final String[] haystack = event.split( "\\+" );
        ctrl = find( "CTRL", haystack );
        alt = find( "ALT", haystack );
        shift = find( "SHIFT", haystack );

        int ascii = 0;
        try {
            key = Keys.valueOf( haystack[haystack.length - 1] );
            ascii = key.getCode();
        } catch ( IllegalArgumentException ex ) {
            ascii = haystack[haystack.length - 1].charAt( 0 );
        }
        code = ascii;
    }

    @Override
    protected boolean doBeforeInvoke( Event event ) {
        if ( code == -1 ) {
            return true;
        } else if ( event instanceof KeyEvent ) {
            final KeyEvent keyEvent = ( KeyEvent ) event;
            // conditions check
            return keyEvent.isAltKey() == alt
                    && keyEvent.isCtrlKey() == ctrl
                    && keyEvent.isShiftKey() == shift
                    && keyEvent.getKeyCode() == code;
        }
        return false; // onKey filter but not onKey event
    }

    /** 
     * Přeloží user-friendly syntaxi do syntaxe ZK
     * @return ZK syntaxe eventu
     */
    public String getKeys() {
        if ( code == -1 ) {
            return "";
        }
        final StringBuffer s = new StringBuffer( "" );
        if ( ctrl ) {
            s.append( '^' );
        }
        if ( alt ) {
            s.append( '@' );
        }
        if ( shift ) {
            s.append( '$' );
        }
        s.append( key == null ? ( char ) code : key.getName() );
        return s.toString().toLowerCase();
    }

    private boolean find( final String needle, final String[] haystack ) {
        for ( String string : haystack ) {
            if ( needle.equals( string ) ) {
                return true;
            }
        }
        return false;
    }
}
