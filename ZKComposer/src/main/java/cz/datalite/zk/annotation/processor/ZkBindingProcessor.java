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
package cz.datalite.zk.annotation.processor;

import cz.datalite.zk.annotation.ZkBinding;
import cz.datalite.zk.annotation.invoke.ZkBindingHandler;
import cz.datalite.zk.annotation.invoke.Invoke;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ComponentNotFoundException;

/**
 * <p>Annotation processor which handles 
 * processing of {@link ZkBinding}</p>
 *
 * @author Karel Čemus <cemus@datalite.cz>
 */
public class ZkBindingProcessor extends AbstractProcessor<ZkBinding> {

    public static final ZkBindingProcessor INSTANCE = new ZkBindingProcessor();

    public Invoke process( ZkBinding annotation, Invoke invoke, Method method, Component master, Object controller ) {
        try {
            Component component = annotation.component().length() == 0 ? master : master.getFellow( annotation.component() );
            List<Component> loadAfter = annotation.loadAfter() ? Collections.singletonList( component ) : Collections.EMPTY_LIST;
            List<Component> saveBefore = annotation.saveBefore() ? Collections.singletonList( component ) : Collections.EMPTY_LIST;

            return new ZkBindingHandler( invoke, saveBefore, loadAfter );
        } catch ( ComponentNotFoundException ex ) {
            throw new ComponentNotFoundException( "ZkBinding could not be registered on component \"" + annotation.component() + "\" because component wasn\'t found.", ex );
        }
    }
}
