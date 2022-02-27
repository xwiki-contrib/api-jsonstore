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
package org.xwiki.contrib.jsonstore;

import org.xwiki.component.annotation.Role;

/**
 * Interface for saving and fetching an object serialized to JSON associated to a key.
 * 
 * @version $Id$
 */
@Role
public interface JsonStore
{
    /**
     * Serializes the passed data object as JSON and persists it in the store under the passed id. If another json is
     * already stored under the same id, the parameter forceOverwrite can be used to forcefully overwrite it.
     * 
     * @param data the data to store as JSON. TODO: implement a special handling of strings here, allowing caller to
     *            persist whatever they want, as long as they're ready to take responsibility on the fact that it's
     *            proper JSON.
     * @param id the id to store the data under
     * @param forceOverwrite whether the existing data under the same id (if any) should be overwritten or not. Returns
     *            false if id is already stored and the overwrite is not forced or if data is null.
     * @return true if write has happened or not
     * @throws Exception if any exception happens during persistence of the Json
     */
    boolean persistAsJson(Object data, String id, boolean forceOverwrite) throws Exception;

    /**
     * Fetches the data identified by {@code id} from the Json store, as a parsed object.
     * 
     * @param id the id to fetch the data for
     * @return the data stored in the store under the id {@code id}, parsed as an object. This assumes that the data
     *         stored is proper JSON (e.g. written by the {@link #persistAsJson(Object, String, boolean)} method of this
     *         API. Returns null if nothing is stored in the key under the passed id.
     * @throws Exception if any exception happens during the reading of the store or Json parsing.
     */
    Object getFromJsonStore(String id) throws Exception;

    /**
     * Tests whether a key is stored in the Json store, without loading, parsing or returning it. Notably, this only
     * checks that a key is 'occupied' but does not verify the validity of the data stored under that key.
     * 
     * @param id the id of the data
     * @return whether any data is stored for the given id, without checking validity.
     * @throws Exception if any error is encountered when reading the store
     */
    boolean exists(String id) throws Exception;
}
