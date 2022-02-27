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

import org.slf4j.Logger;
import org.xwiki.contrib.jsonstore.JsonStore;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.util.Programming;

/**
 * Abstract superclass for all Json Stores script services, implementing convenient methods for calling from scripting
 * context and rights checks (programming rights are required for all apis), but without binding to an implementation or
 * other of the store.
 * 
 * @version $Id$
 */
public abstract class AbstractPRJsonStoreScriptService implements ScriptService
{
    /**
     * Serializes the passed data object as JSON and persists it in the store under the passed id. If another json is
     * already stored under the same id, the parameter forceOverwrite can be used to forcefully overwrite it.
     * 
     * @param data the data to store as JSON. TODO: implement a special handling of strings here, allowing caller to
     *            persist whatever they want, as long as they're ready to take responsibility on the fact that it's
     *            proper JSON.
     * @param id the identifier to store the data under
     * @param forceOverwrite whether the existing data under the same id (if any) should be overwritten or not. Returns
     *            false if id is already stored and the overwrite is not forced or if data is null.
     * @return true if write has happened or not. Will also return false if there is an exception writing to the store
     *         or serializing the data object to json. The exception will be logged in the server logs.
     */
    @Programming
    public boolean persistAsJson(Object data, String id, boolean forceOverwrite)
    {
        try {
            return getJsonStore().persistAsJson(data, id, forceOverwrite);
        } catch (Exception e) {
            getLogger().warn("Exception while writing data " + data + " to the store for id " + id, e);
            return false;
        }
    }

    /**
     * Persist data as Json without overwriting anything in the store. Returns false if data is already stored under the
     * passed {@code id}.
     * 
     * @param data the data to store as JSON.
     * @param id the identifier to store the data under
     * @return true if write has happened or not. Will also return false if there is an exception writing to the store
     *         or serializing the data object to json. The exception will be logged in the server logs.
     */
    @Programming
    public boolean persistAsJson(Object data, String id)
    {
        return this.persistAsJson(data, id, false);
    }

    /**
     * Fetches the data identified by {@code id} from the Json store, as a parsed object.
     * 
     * @param id the id to fetch the data for
     * @return the data stored in the store under the id {@code id}, parsed as an object. This assumes that the data
     *         stored is proper JSON (e.g. written by the {@link #persistAsJson(Object, String, boolean)} method of this
     *         API. Will return null both if there is nothing stored under the id in the store and when there is an
     *         exception accessing the store or deserializing the Json to an object (the exception will be logged in the
     *         server logs).
     */
    @Programming
    public Object getFromJsonStore(String id)
    {
        try {
            return getJsonStore().getFromJsonStore(id);
        } catch (Exception e) {
            getLogger().warn("Exception while fetching data from the store for id " + id, e);
            return null;
        }
    }

    /**
     * Tests whether a key is stored in the Json store, without loading, parsing or returning it. Notably, this only
     * checks that a key is 'occupied' but does not verify the validity of the data stored under that key.
     * 
     * @param id the id of the data
     * @param defaultIfException the value to return if there's an exception in accessing the store and thus the
     *            existence cannot be really checked
     * @return whether any data is stored for the given id, without checking validity.
     */
    @Programming
    public boolean exists(String id, boolean defaultIfException)
    {
        try {
            return getJsonStore().exists(id);
        } catch (Exception e) {
            getLogger().warn("Exception while checking whether there is data stored under " + id, e);
            return defaultIfException;
        }
    }

    /**
     * @return implementation of the json store to use.
     */
    protected abstract JsonStore getJsonStore();

    /**
     * @return logger to log all exceptions.
     */
    protected abstract Logger getLogger();
}
