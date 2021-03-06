package cz.datalite.zk.components.button;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Toolbarbutton;

/**
 * ZK Toolbarbutton extensions.
 *
 * List of extensions:
 * <ul>
 *   <li>i18n</li>
 *   <li>readonly - depends on disabledOnReadonly disable the button if ZKHelper.setReadonly() is set</li>
 *   <li>load image from classpath</li>
 *   <li>autodisable default value = true</li>
 * </ul>
 *
 * @author Michal Pavlusek
 */
public class DLToolbarButton extends Toolbarbutton  {

    public DLToolbarButton() {
        setAutodisable( "self" );
    }

    public DLToolbarButton( String label ) {
        super( label );
        setAutodisable( "self" );
    }

    public DLToolbarButton( String label, String image ) {
        super( label, image );
        setAutodisable( "self" );
    }

    /**
     * Arbitrary value associated to the button.
     */
    private Object value;

    /**
     * Disabled as set by the user. Parent disabled can be set according to
     * value and disabledOnNullValue.
     */
    private boolean userDisabled;

    /**
     * Disable the button if no value data is associated.
     *
     */
    private boolean disabledOnNullValue;

    /**
     * If you set readonly for whole page (or component tree) via
     * ZKHelper.setReadonly(), this will check if button has to be disabled as
     * well.
     */
    private boolean disabledOnReadonly;

    /**
     * Identifier of listbox which direct enabling/disabling of button
     */
    private String listenOnListbox;

    public boolean isDisabledOnNullValue() {
        return disabledOnNullValue;
    }

    public void setDisabledOnNullValue( boolean disabledOnNullValue ) {
        this.disabledOnNullValue = disabledOnNullValue;
    }

    /**
     * Arbitrary value associated to the button.
     *
     * @return Arbitrary value associated to the button.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Arbitrary value associated to the button. If the value is null/not null
     * and isDisabledOnNullValue is true, then the button will be
     * disabled/enabled (enabled only if setDisabled(true) was not set).
     *
     * @param value Arbitrary value associated to the button.
     */
    public void setValue( Object value ) {
        this.value = value;

        if ( isDisabledOnNullValue() && !userDisabled ) {
            super.setDisabled( value == null );
        }
    }

    /**
     * Like button setDisabled, only it checks value and disabledOnNullValue
     * params as well.
     *
     * @param disabled should be the button enabled or disabled
     */
    @Override
    public void setDisabled( boolean disabled ) {
        this.userDisabled = disabled;

        if ( isDisabledOnNullValue() && !disabled ) {
            // enable only if value is set
            if ( value != null ) {
                super.setDisabled( disabled );
            }
        } else {
            super.setDisabled( disabled );
        }
    }

    /**
     * If you set readonly for whole page (or component tree) via
     * ZKHelper.setReadonly(), this will check if button has to be disabled as
     * well.
     *
     * @return the disabledOnReadonly
     */
    public boolean isDisabledOnReadonly() {
        return disabledOnReadonly;
    }

    /**
     * If you set readonly for whole page (or component tree) via
     * ZKHelper.setReadonly(), this will check if button has to be disabled as
     * well.
     *
     * @param disabledOnReadonly the disabledOnReadonly to set
     */
    public void setDisabledOnReadonly( boolean disabledOnReadonly ) {
        this.disabledOnReadonly = disabledOnReadonly;

        if ( disabledOnReadonly ) {
            setAttribute( "DISABLED_ON_READONLY", Boolean.TRUE );
        } else {
            removeAttribute( "DISABLED_ON_READONLY" );
        }
    }

    /**
     * This setter is called from ZUL file to bind button to DLListbox
     * component. This component fires onSelectedShow and onSelectedHide events
     * which says if the selected item is currently shown or not.
     *
     * @param listenOnListbox
     */
    public void setListenOnListbox( final String listenOnListbox ) {
        Events.postEvent( "onInit", this, null );
        addEventListener( "onInit", new EventListener() {

            public void onEvent( Event event ) throws Exception {
                DLToolbarButton.this.removeEventListener( "onInit", this );
                Component cmp = DLToolbarButton.this.getFellow( listenOnListbox );
                cmp.addEventListener( "onSelectedShow", new EventListener() {

                    public void onEvent( Event event ) throws Exception {
                        DLToolbarButton.this.setDisabled( false );
                    }
                } );
                cmp.addEventListener( "onSelectedHide", new EventListener() {

                    public void onEvent( Event event ) throws Exception {
                        DLToolbarButton.this.setDisabled( true );
                    }
                } );
            }
        } );
    }
}
