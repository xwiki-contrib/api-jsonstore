/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.jsonstore.script;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.jsonstore.JsonStore;
import org.xwiki.stability.Unstable;

/**
 * Script service for the XWiki Permanent Directory implementation of the json store.
 * 
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named(JsonStoreScriptService.ROLEHINT + ".permdir")
@Unstable
public class XWikiPermdirJsonStoreScriptService extends AbstractPRJsonStoreScriptService
{
    @Inject
    @Named("xwikipermdir")
    private JsonStore permDirJsonStore;

    @Inject
    private Logger logger;

    @Override
    protected JsonStore getJsonStore()
    {
        return this.permDirJsonStore;
    }

    @Override
    protected Logger getLogger()
    {
        return this.logger;
    }

}
