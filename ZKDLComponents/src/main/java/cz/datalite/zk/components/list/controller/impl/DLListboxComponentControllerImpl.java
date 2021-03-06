package cz.datalite.zk.components.list.controller.impl;

import cz.datalite.dao.DLSortType;
import cz.datalite.helpers.ReflectionHelper;
import cz.datalite.zk.bind.ZKBinderHelper;
import cz.datalite.zk.components.list.controller.DLListboxComponentController;
import cz.datalite.zk.components.list.controller.DLListboxExtController;
import cz.datalite.zk.components.list.model.DLColumnModel;
import cz.datalite.zk.components.list.model.DLColumnUnitModel;
import cz.datalite.zk.components.list.view.DLListbox;
import cz.datalite.zk.components.list.view.DLListheader;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Library;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.metainfo.NodeInfo;
import org.zkoss.zk.ui.metainfo.PageDefinition;
import org.zkoss.zk.ui.metainfo.TemplateInfo;
import org.zkoss.zk.ui.metainfo.impl.MacroDefinition;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zk.xel.Evaluator;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;

import java.lang.reflect.Method;
import java.util.*;

/**
 *  Implementation of the listbox controller.
 * @param <T> master entity type
 * @author Karel Cemus
 */
public class DLListboxComponentControllerImpl<T> implements DLListboxComponentController<T> {

    /** logger */
    protected static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger( DLListboxComponentControllerImpl.class );

    // master controller
    protected final DLListboxExtController<T> masterController;
    // model
    /** data model - empty list on start */
    protected List<T> listboxModel = new ArrayList<T>();
    /** listheader models */
    protected final DLColumnModel columnModel;
    /** listheader - listheader model map */
    protected final Map<DLListheader, DLColumnUnitModel> columnMap = new HashMap<DLListheader, DLColumnUnitModel>();
    /** renderer template for changing when order of the column is changed */
    @Deprecated
    protected Listitem renderTemplate;
    /** renderer template for changing when order of the column is changed (new binding) */
    protected final Template template;
    /** map model - cell in the renderer because of breaking and creating relations */
    @Deprecated
    protected final Map<DLColumnUnitModel, Listcell> rendererCellTemplates = new HashMap<DLColumnUnitModel, Listcell>();
    /** map listheader - model for changing cols order model */
    protected final Map<DLColumnUnitModel, DLListheader> listheaderTemplates = new HashMap<DLColumnUnitModel, DLListheader>();
    /** current mapping of listcells against the default template. The array uses the catch at the beginning. It is the 0 */
    protected final List<Integer> listcellIndicies = new ArrayList<Integer>();
    /** selected item */
    protected T selectedItem;
    /** selected items */
    protected Set<T> selectedItems = Collections.emptySet();
    // default model
    protected final List<DLListheader> defaultHeaders = new LinkedList<DLListheader>();
    @Deprecated
    protected final List<Listcell> defaultRendererCellTemplates = new LinkedList<Listcell>();
    // view
    protected final DLListbox listbox;
    /** local flag used to enable/disable emitting the event on the select. */
    protected boolean notifyOnSelect = true;

    public DLListboxComponentControllerImpl( final DLListboxExtController<T> masterController, final DLColumnModel columnModel, final DLListbox listbox ) {
        this.masterController = masterController;
        this.columnModel = columnModel;
        this.listbox = listbox;
        this.listbox.setController( DLListboxComponentControllerImpl.this );

        // on select updates model
        listbox.addEventListener( Events.ON_SELECT, new org.zkoss.zk.ui.event.EventListener<Event>() {
            public void onEvent( final org.zkoss.zk.ui.event.Event event ) throws Exception {
                try {
                    // ignore events from method on init render
                    if ( listbox.isOnInitRender() ) return;

                    // just selected, do not fire it again, prevent endless loop
                    notifyOnSelect = false;

                    T selectedItem;
                    if ( listbox.getSelectedIndex() == -1 ) {
                        selectedItem = null;
                    } else {
                        selectedItem = listboxModel.get( listbox.getSelectedIndex() );
                    }

                    // multiple selected items
                    final Set<T> selectedItems = new HashSet<T>();
                    for ( Listitem listitem : ( Set<Listitem> ) listbox.getSelectedItems() ) {
                        selectedItems.add( listboxModel.get( listitem.getIndex() ) );
                    }

                    // update selected
                    setSelected( selectedItem, selectedItems );

                    masterController.onSelect();
                } finally {
                    // allow notifing again
                    notifyOnSelect = true;
                }
            }
        } );

        // updates filter if onEasyFilter is captured
        listbox.addEventListener( "onEasyFilter", new org.zkoss.zk.ui.event.EventListener<Event>() {
            public void onEvent( final org.zkoss.zk.ui.event.Event event ) {
                masterController.getEasyFilterController().onEasyFilter();
            }
        } );

        // updates filter if onEasyFilterClear is captured
        listbox.addEventListener( "onEasyFilterClear", new org.zkoss.zk.ui.event.EventListener<Event>() {
            public void onEvent( final org.zkoss.zk.ui.event.Event event ) {
                masterController.getEasyFilterController().onClearEasyFilter( true );
            }
        } );

        // direct export listener
        listbox.addEventListener(DLListbox.ON_DIRECT_EXPORT, new org.zkoss.zk.ui.event.EventListener<Event>() {
            public void onEvent(Event event) throws Exception {
                masterController.onDirectExport();
            }
        });

        // init default headers model
        defaultHeaders.addAll( listbox.getListheaders() );

        // init column model
        initListheaderModels( listbox.getListheaders() );

        // test if the component is written using new databinding
        // if so, initiate it properly
        Set<String> templates = listbox.getTemplateNames();

        if (templates.isEmpty()) {
        	this.template = null;
        } else if (templates.size() > 1) {
			throw new RuntimeException("Only one template element is allowed as direct child of DLListbox element. Check zul file.");
		} else if (templates.size() == 1) {
			// zul uses new databinding, list of cells is defined as template
			this.template = listbox.getTemplate(templates.iterator().next());
			this.setRendererTemplate(this.template);
		} else {
			this.template = null;
		}

        applyDefaultConverters();

        initDragDropChangeOrder();
    }

    public void onSort( final DLListheader listheader ) {
        assert listheader != null : "Listheader cannot be null. Invalid argument for sort.";

        final DLColumnUnitModel model = columnMap.get( listheader );

        if ( !model.isSortable() ) {
            return;
        }

        // if this column isn't sorted then remove all sorts from other columns
        // it allows only one sorted column
        //------
        // if someone would like allow multiple soirted column generated from user click
        // you have to comment this rows
        //------
        // this model is cleared everytime when the sorttype is changed
        final DLSortType sortType = model.getSortType();
        columnModel.clearSortAll();

        // if it is database sort there are 3 kinds of the sorts
        // else it is zk sort so there is only 2 types
        if ( model.isSortZk() ) {
            model.setSortType( sortType.nextZK() );
        } else {
            model.setSortType( sortType.next() );
        }

        onSort( model.isSortZk(), model.getSortType(), listheader );
        fireChanges();
    }

    /**
     * Perform sort
     * @param zkSort is it zk sort or not
     * @param sortType type of sorting - ascending, descending, natural
     * @param listheader sorted column
     */
    protected void onSort( final boolean zkSort, final DLSortType sortType, final DLListheader listheader ) {
        if ( masterController.isLocked() ) {
            return;
        }

        if ( zkSort ) {
            final DLColumnUnitModel model = columnMap.get( listheader );
            model.setSortType( sortType.nextZK() );
            switch ( sortType ) {
                case ASCENDING:
                    listheader.sort( true );
                    break;
                case DESCENDING:
                    listheader.sort( false );
                    break;
                default:
                    throw new UnsupportedOperationException( "Unknown sortType." );
            }
            model.setSortType( model.getSortType().nextZK() );
        } else {
            masterController.onSortChange();
        }
    }

    public void onCreate() {
        // allocate the array of indicies
//        for( int i = 0; i < columnModel.getColumnModels().size() + 1; ++i ) {
//            listcellIndicies.add( null );
//        }

        masterController.onCreate();
    }

    public void fireChanges() {
        for ( DLListheader header : listbox.getListheaders() ) {
            header.fireChanges();
        }
    }

    public void fireDataChanges() {
        if ( listboxModel != null ) {

            if ( ZKBinderHelper.version( listbox ) == 1 ) {
                listbox.setListModel( listboxModel );
            } else if ( ZKBinderHelper.version( listbox ) == 2 ) {
                // notify the change on the model
                ZKBinderHelper.notifyChange( listbox, this, "listboxModel" );
            }

            // if selected item is null or not in new list and we should select first row
            if ( listbox.isSelectFirstRow() && !listboxModel.contains( getSelectedItem() ) && listboxModel.size() > 0 ) {
                notifyOnSelect = false;
                setSelected( listboxModel.get( 0 ) );
                notifyOnSelect = true;
            }

            listbox.setEmptyMessageVisible(true);

            updateSelectedItemsReference();
        }
    }

    public void fireColumnModelChanges() {
        initListheaderModels( defaultHeaders );

		if (ZKBinderHelper.version(listbox) == 1) {
			// if using old style template, init model from cell template
			this.initRendererTemplate(this.defaultHeaders, this.defaultRendererCellTemplates);
		} else if (ZKBinderHelper.version(listbox) == 2) {
			// otherwise init model from template
			this.setRendererTemplate(this.template);
		}

        applyDefaultConverters();
    }

    /**
     * If column does not have converter, apply default from library property (if set).
     */
    protected void applyDefaultConverters() {
        String booleanConverter = Library.getProperty("zk-dl.listbox.export.defaultConverter.boolean");

        for (DLColumnUnitModel columnUnitModel : columnModel.getColumnModels() ) {
            if (!columnUnitModel.isConverter() && columnUnitModel.isExportable()) {
                if (Boolean.class.equals(columnUnitModel.getColumnType()) && booleanConverter != null)
                    columnUnitModel.setConverter(booleanConverter, listbox, Collections.<String,String>emptyMap());
            }
        }
    }

    private void initListheaderModels( final List<DLListheader> headers ) {
        columnMap.clear();
        // loading listheades
        int i = 0;
        for ( DLListheader header : headers ) {
            columnMap.put( header, columnModel.getColumnModel( ++i ) );
            header.setModel( columnMap.get( header ) );
            header.setController( this );
            header.initModel();
        }
    }

    public void refreshBindingAll() {
         ZKBinderHelper.loadComponent(listbox);
    }

    @SuppressWarnings( "unchecked" )
    public void fireOrderChanges() {
        // update order of listheaders
        updateListhead();

        if ( ZKBinderHelper.version( listbox ) == 1 ) {
            // if using old style template, update it
            updateTemplateVersion1();
        } else if ( ZKBinderHelper.version( listbox ) == 2 ) {
            // otherwise update model
            updateTemplateVersion2();
        }

        // clear rendered items
        listbox.getItems().clear();

        fireChanges();

    }

    /** update the template if it uses old data binding (v1.0) */
    private void updateTemplateVersion1() {
        if ( renderTemplate == null ) {
            throw new UiException( "Template is not set. Is there data binding set on the component correctly?" );
        }

        // remove all children
        renderTemplate.getChildren().clear();

        // add children to template and listhead according the model
        List<DLColumnUnitModel> orderedModel = new LinkedList( columnModel.getColumnModels() );
        Collections.sort( orderedModel );

        for ( DLColumnUnitModel unit : orderedModel ) {
            if ( unit.isVisible() ) {
                renderTemplate.appendChild( rendererCellTemplates.get( unit ) );
            }
        }
    }

    /** update the template if it uses new data binding (v2.0) */
    private void updateTemplateVersion2() {
        final List<DLColumnUnitModel> unorderedModel = new LinkedList( columnModel.getColumnModels() );

        for( int i = 0; i < columnModel.getColumnModels().size() + 1; ++i ) {
            listcellIndicies.add( null );
        }

        // update the listcell mapping
        // points to the first unused field
        int max = 0;
        for ( int i = 0; i < unorderedModel.size(); ++i ) {
            if ( ( unorderedModel.get( i ).isVisible() ) && ( unorderedModel.get( i ).getOrder() != 0 ) )  {
                // the order is indexed from 1            	
                listcellIndicies.set( unorderedModel.get( i ).getOrder() - 1, i );
                // update index of max
                max = unorderedModel.get( i ).getOrder() > max ? unorderedModel.get( i ).getOrder() : max;
            }
        }
        listcellIndicies.set( max, null );
    }

    private void updateListhead() {
        // model is not initialized
        if ( listheaderTemplates.isEmpty() ) return;

        final Listhead listhead = listbox.getListhead();
        // remove all children
        listhead.getChildren().clear();

        // add children to template and listhead according the model
        List<DLColumnUnitModel> orderedModel = new LinkedList(columnModel.getColumnModels());
        Collections.sort(orderedModel);

        for ( DLColumnUnitModel unit : orderedModel ) {
            if ( unit.isVisible() ) listhead.appendChild( listheaderTemplates.get( unit ) );
        }
    }

    /** updates the listitem (order of cells) when it uses the new databinding */
    public void updateListItem( Listitem item ) {
        // test wheather the listcell indicies is initialized
        if (listcellIndicies.size() == 1 ) return;

        List<Component> listcellBuffer = new ArrayList<Component>();
        listcellBuffer.addAll( item.getChildren() );

        // remove all current listcells from the listitem
        for (Component c : listcellBuffer)
            c.setParent(null);

        // insert them back in correct order
        for ( int i = 0; listcellIndicies.get( i ) != null; ++i ) {
            listcellBuffer.get( listcellIndicies.get( i ) ).setParent(item);
        }
    }

    public void setListboxModel( final List<T> model ) {
        this.listboxModel = model;
    }

    @Deprecated
    @SuppressWarnings( "unchecked" )
    public void setRendererTemplate( final Listitem item ) {
        renderTemplate = listbox.getItemAtIndex( 0 );
        for ( final Component cmp : renderTemplate.getChildren() ) {
            defaultRendererCellTemplates.add( ( Listcell ) cmp );
        }
        initRendererTemplate( listbox.getListheaders(), defaultRendererCellTemplates );
    }

    public void setRendererTemplate( final Template template ) {
        try {
            this.listbox.setAttribute( "cmpCtl", DLListboxComponentControllerImpl.this );
            ZKBinderHelper.registerAnnotation( listbox, "model", "load", "cmpCtl.listboxModel" );

            if (!ReflectionHelper.hasField( template.getClass(), "_tempInfo" ) ) {
                // listbox doesn't have a template but it can has some configuration on listheaders
                // try to determine data types based on listheader configuration
                for( DLColumnUnitModel unit : columnModel.getColumnModels() ) {
                    if ( unit.isColumn() && unit.getColumnType() == null ) {
                        unit.setColumnType( getFieldType( masterController.getEntityClass(), unit.getColumn() ) );
                    }
                }

                return;
            }

            final List<NodeInfo> cellTemplates = new ArrayList<NodeInfo>();

            TemplateInfo info = ReflectionHelper.getForcedFieldValue( "_tempInfo", template );
            // start from <listitem> children
            resolveTemplateListcells(cellTemplates, info.getChildren().get(0).getChildren());

            if (defaultHeaders.size() != cellTemplates.size()) {
                throw new IllegalStateException("Listbox template parsing error: different size of " +
                        "listheaders (" + defaultHeaders.size() + ") and listitem cells (" +
                        cellTemplates.size() + ").");
            }

            initTemplate( defaultHeaders, cellTemplates );

        } catch ( Exception ex ) {
            LOGGER.error( "Template couldn't be loaded from the databinding.", ex );
            throw new RuntimeException( "Template couldn't be loaded from the databinding.", ex );
        }

    }

    // parse template definition and recursively search for listcells / expand macros
    private void resolveTemplateListcells(List<NodeInfo> cellTemplates, List<NodeInfo> nodeInfoList) {
        for (NodeInfo nodeInfo : nodeInfoList) {
            if (nodeInfo instanceof ComponentInfo) {
                ComponentInfo componentInfo = (ComponentInfo) nodeInfo;

                if ( componentInfo.withCondition() ) //Pokud komponenta obsahuje podmínku vyskytu tak ji vyhodnotím a podle výsledku pokračovat
                {
                    if ( ! componentInfo.getCondition().isEffective( componentInfo.getPageDefinition().getEvaluator(), listbox ) )
                    {
                        continue ;
                    }
                }

                if (componentInfo.getComponentDefinition() instanceof MacroDefinition) {
                    MacroDefinition macro = (MacroDefinition) componentInfo.getComponentDefinition();
                    PageDefinition pageDefinition = Executions.getPageDefinition(null, macro.getMacroURI());
                    resolveTemplateListcells(cellTemplates, pageDefinition.getChildren());
                } else if (componentInfo.getTag() != null) {
                    // skip components without tag - it might be only \n in source zul
                    // otherwise assume listcell - tag check is not safe, because the component might be subclass of listcell
                    cellTemplates.add(nodeInfo);
                }
            } else {
                // otherwise descend to children
                resolveTemplateListcells(cellTemplates, nodeInfo.getChildren());
            }
        }
    }

    protected void initTemplate( final List<DLListheader> headers, final List<NodeInfo>  listcells ) {
        // loading renderer templates and column name from the binding.
        // ---------------
        // if column name was already loaded will be now skipped.
        // ---------------
        // if column isn't loaded from the listheader nor from binding
        // this column will be very handicapped
         listheaderTemplates.clear();

        int i = 0;
        for ( NodeInfo cell : listcells ) {
            final DLListheader header = headers.get( i++ );
            listheaderTemplates.put( columnMap.get( header ), header );

            ComponentInfo info = ( ComponentInfo ) cell;

            // resolve binding text to
            String bindingText = null;
            String bindingProperty = "label";
            if ( info.getAnnotationMap() != null ) {
                bindingText = getBindingText(info, bindingProperty);
                if (bindingText == null) {
                    bindingProperty = "checked";
                    bindingText = getBindingText(info, bindingProperty);
                }
            }

            String converter = null;
            Map<String,String> converterArgs = Collections.<String,String>emptyMap();

            if ( info.getAnnotationMap() != null && info.getAnnotationMap().getAnnotation( bindingProperty, "converter" ) != null ) {
                converter = info.getAnnotationMap().getAnnotation( bindingProperty, "converter" ) != null
                        ? info.getAnnotationMap().getAnnotation( bindingProperty, "converter" ).getAttribute( "value" )
                        : null;
                converterArgs = new HashMap<String, String>();
                Map<String, String[]> attrs = info.getAnnotationMap().getAnnotation( bindingProperty, "converter" ).getAttributes();
                for ( String key : attrs.keySet() ) {
                    if ( attrs.get( key ).length > 0 )
                        // strip quotes
                        converterArgs.put( key, attrs.get( key )[0].replaceAll( "^'(.*)'$", "$1") );
                }
            }

            if ( columnMap.get( header ).getColumn() == null && bindingText != null ) // set column from binding

                if ( bindingText.indexOf( '.' ) == -1 ) // if there is whole entity without any property
                    columnMap.get( header ).setColumn( null );
                else // if there is define some property
                    columnMap.get( header ).setColumn( bindingText.substring( bindingText.indexOf( '.' ) + 1 ) );
            if ( !columnMap.get( header ).isConverter() && converter != null ) // set converter from binding

                columnMap.get( header ).setConverter( converter, listbox, converterArgs );
            if ( columnMap.get( header ).getColumnType() == null && columnMap.get( header ).isColumn() ) // set type if it is not explicitly setted

                columnMap.get( header ).setColumnType( getFieldType( masterController.getEntityClass(), columnMap.get( header ).getColumn() ) );
        }
    }

    // Returns binding text of a component and property - check @load and @bind annotations
    private String getBindingText(ComponentInfo info, String property) {
        String bindingText;
        bindingText = info.getAnnotationMap().getAnnotation( property, "load" ) != null
                ? info.getAnnotationMap().getAnnotation( property, "load" ).getAttribute( "value" )
                : null;

        bindingText = info.getAnnotationMap().getAnnotation( property, "bind" ) != null
                ? info.getAnnotationMap().getAnnotation( property, "bind" ).getAttribute( "value" )
                : bindingText;
        return bindingText;
    }

    protected void initRendererTemplate( final List<DLListheader> headers, final List<Listcell> listcells ) {
        // loading renderer templates and column name from the binding.
        // ---------------
        // if column name was already loaded will be now skipped.
        // ---------------
        // if column isn't loaded from the listheader nor from binding
        // this column will be very handicapped
        rendererCellTemplates.clear();
        listheaderTemplates.clear();
        int i = 0;
        for ( Listcell cell : listcells ) {
            final DLListheader header = headers.get( i++ );
            rendererCellTemplates.put( columnMap.get( header ), cell );
            listheaderTemplates.put( columnMap.get( header ), header );
            final String bindingText = cz.datalite.helpers.ZKBinderHelper.getDefaultAnnotation( cell, "label", "value" );
            final String converter = cz.datalite.helpers.ZKBinderHelper.getDefaultAnnotation( cell, "label", "converter" );
            if ( columnMap.get( header ).getColumn() == null && bindingText != null ) // set column from binding
            {
                if ( bindingText.indexOf( '.' ) == -1 ) { // if there is whole entity without any property
                    columnMap.get( header ).setColumn( null );
                } else { // if there is define some property
                    columnMap.get( header ).setColumn( bindingText.substring( bindingText.indexOf( '.' ) + 1 ) );
                }
            }
            if ( !columnMap.get( header ).isConverter() && converter != null ) // set converter from binding
            {
                columnMap.get( header ).setConverter( converter, listbox, Collections.<String,String>emptyMap() );
            }
            if ( columnMap.get( header ).getColumnType() == null && columnMap.get( header ).isColumn() ) // set type if it is not explicitly setted
            {
                columnMap.get( header ).setColumnType( getFieldType( masterController.getEntityClass(), columnMap.get( header ).getColumn() ) );
            }
        }
    }

    /**
     * Returns data type of the fild according to the main entity and address.
     * Search all public fields and getter methods.
     *
     * @param cls class with entity
     * @param key address
     * @return type
     */
    protected static Class getFieldType( final Class cls, final String key ) {
        if ( key.length() == 0 ) {
            return cls;
        }
        final int index = key.indexOf( '.' );
        final String name = index == -1 ? key : key.substring( 0, index );
        final String newKey = index == -1 ? "" : key.substring( index + 1 );

        for ( java.lang.reflect.Field field : ReflectionHelper.getAllFields(cls) ) {
            if ( field.getName().equals( name ) ) {
                return getFieldType( field.getType(), newKey );
            }
        }

        String getterMethodName = "get" + name.substring(0,1).toUpperCase() + name.substring(1);
        for ( Method method : ReflectionHelper.getAllMethods(cls) ) {
            if ( method.getName().equals( getterMethodName )) {
                return getFieldType( method.getReturnType(), newKey );
            }
        }
        return null;
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    public void setSelected( final T selectedItem ) {
        setSelected( selectedItem, selectedItem == null ? Collections.<T>emptySet() : Collections.singleton( selectedItem ) );
    }

    public void setSelected( final T selectedItem, final Set<T> selectedItems ) {
        setSelectedItem( selectedItem );
        setSelectedItems( selectedItems );

        if (notifyOnSelect) { // if the notification is enabled

            // synchronize listbox component
            for (T sel : selectedItems) {
                int index = listboxModel.indexOf(sel);
                if (index == -1)
                    throw new IllegalArgumentException("Item " + sel + " for selection not found in the model.");
                listbox.addItemToSelection(listbox.getItemAtIndex(index));
            }
            if (selectedItem == null) {
                listbox.clearSelection();
            } else {
                int index = listboxModel.indexOf(selectedItem);
                if (index == -1)
                    throw new IllegalArgumentException("selectedItem " + selectedItem + " not found in the model.");
                listbox.setSelectedIndex( index );
            }

            Events.postEvent( new SelectEvent ( Events.ON_SELECT, listbox, listbox.getSelectedItems(), selectedItems, listbox, null, 0 ) );
        }
    }

    public void setSelectedItem( final T selectedItem ) {
        if ( masterController.isLocked() || listboxModel == null ) return;

        this.selectedItem = selectedItem;
    }

    public Set<T> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems( Set<T> selectedItems ) {
        if ( masterController.isLocked() ) return;

        this.selectedItems = selectedItems;
    }

    public int getSelectedIndex() {
        return listboxModel.indexOf(selectedItem);
    }

    public void setSelectedIndex( final int selectedIndex ) {
        if ( listboxModel == null ) {
            return;
        }
        if ( listboxModel.size() <= selectedIndex || selectedIndex < 0 ) {
             setSelected( null );
        } else {
            setSelected( listboxModel.get( selectedIndex ) );
        }
    }

    public boolean updateItem(T item) {

        // we need to set new value directly via ListModelList (or subclass), because it calls the internal
        // event on Listbox to rerender new value. If we use underlying getListboxModel() directly, there is no
        // public accessor to call the event.
        if (!(getListbox().getModel() instanceof ListModelList))
            throw new IllegalStateException("updateItem can be used only if the listbox model is of type ListModelList " +
                    "(it is true for normal use with <listbox apply='listboxController'>). For special use, " +
                    "you have to manage the model manually.");

        ListModelList<T> listboxModel = (ListModelList<T>) getListbox().getModel();


        int index = listboxModel.indexOf(item);

        if (index == -1) {
            // if the listbox model is not live (i.e. holds only copy of original list), we need to modify it as well
            if (listboxModel.getInnerList() != getListboxModel())
                getListboxModel().add(0, item);

            // add a new row and select it
            listboxModel.add(0, item);
            setSelected(item);

            return false;
		} else {
            // if the listbox model is not live (i.e. holds only copy of original list), we need to update it as well
            if (listboxModel.getInnerList() != getListboxModel())
                getListboxModel().set(index, item);

            // update the value
            listboxModel.set(index, item);

            // ensure selected item(s) does not point to the old reference
            updateSelectedItemsReference();

            return true;
        }
    }

    public Component getFellow( final String compId ) {
        if ( listbox.getFellow( compId ) == null ) {
            throw new ComponentNotFoundException( "Component " + compId + " not found" );
        }
        return listbox.getFellow( compId );
    }

    public void addForward( final String originEvent, final String targetComponent, final String targetEvent ) {
        addForward( originEvent, getFellow( targetComponent ), targetEvent );
    }

    public void addForward( final String originalEvent, final Component target, final String targetEvent ) {
        target.addForward( originalEvent, listbox, targetEvent );
    }

    /**
     * It is not easy to find composer to a component, we need to use some best practices to find it.<br/>
     * * find spaceOwner and get composer as spaceOwnerId$composer - set in GenericComposer with wire() components<br/>
     * * use ctl attribute (set by DLComposer if not redefined) <br/>
     *
     * @return composer or null if not found
     */
    public Composer getWindowCtl() {
        Composer composer = null;

        IdSpace idSpace = listbox.getSpaceOwner();
        if (idSpace instanceof Component)
        {
            String id = ((Component)idSpace).getId();
            Object composerCandidate = listbox.getAttribute(id + "$composer", true );

            if (composerCandidate != null && composerCandidate instanceof Composer)
                composer = (Composer) composerCandidate;
        }

        if (composer == null)
        {
            Object composerCandidate = listbox.getAttribute("ctl", true);
            if (composerCandidate != null && composerCandidate instanceof Composer)
                composer = (Composer) composerCandidate;
        }

        return composer;
    }

    public void focus() {
        listbox.focus();
    }

    public boolean isLoadDataOnCreate() {
        return listbox.isLoadDataOnCreate();
    }

    public DLListbox getListbox() {
        return listbox;
    }

    public String getUuidsForTest() {
        final StringBuilder builder = new StringBuilder( 40 );
        builder.append( listbox.getUuid() ).append( ',' );
        builder.append( masterController.getPagingUuid() );
        builder.append( ',' );
        builder.append( masterController.getQuickFilterUuid() );
        return builder.toString();
    }

     public List<T> getListboxModel() {
        return listboxModel;
    }

    @Override
    public boolean clearInPagingEvents()
    {
        return masterController.clearInPagingEvents() ;
    }

    /**
     * Enable drag&drop on column headers to create a convenient way for the user to reorder visible columns.
     */
    private void initDragDropChangeOrder() {
        for (final DLListheader listheader : listbox.getListheaders()) {
            String draggableText = getClass().getName() + ".draggable";
            listheader.setDraggable(draggableText);
            listheader.setDroppable(draggableText);

            listheader.addEventListener(Events.ON_DROP, new org.zkoss.zk.ui.event.EventListener<DropEvent>() {
                @Override
                public void onEvent(DropEvent event) throws Exception {
                    if (event.getDragged() instanceof DLListheader) {
                        DLColumnUnitModel otherModel = ((DLListheader)event.getDragged()).getModel();
                        otherModel.setOrder(listheader.getModel().getOrder());
                        fireOrderChanges();
                        // we need to reload data and autosave model, onSortChange can be utilized to do this
                        masterController.onSortChange();
                    }
                }
            });
        }
    }

    /**
     * After change of listbox model, reference to selected items should be updated.
     * Although selected item may contain same entity in terms of .equals(), it is still another reference.
     * We need to find new object instance in the list and update selected item with the new instance.
     * This is the case usually after data reload from database. The selected item contains exiting row
     * which is same in terms of equals, but is a stale instance.
     */
    private void updateSelectedItemsReference() {
        T selectedItem = getSelectedItem();
        if (selectedItem != null) {
            int index = getListboxModel().indexOf(selectedItem);
            if (index >= 0) {
                // found, update reference only. This is the same object by .equals, but another reference
                setSelectedItem(getListboxModel().get(index));
            } // not found scenario is ok as well! selected item is not on current page or even not in current filter
        }

        // the same for other items
        Set<T> newSelectedItems = new HashSet<>();
        for (T item : getSelectedItems()) {
            int index = getListboxModel().indexOf(selectedItem);
            if (index >= 0) {
                newSelectedItems.add(getListboxModel().get(index));
            }
        }
        // update the references
        setSelectedItems(newSelectedItems);
    }
}
