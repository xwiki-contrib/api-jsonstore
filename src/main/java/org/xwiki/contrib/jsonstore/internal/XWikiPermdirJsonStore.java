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
package org.xwiki.contrib.jsonstore.internal;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.jsonstore.JsonStore;
import org.xwiki.environment.Environment;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Implementation of the Json store storing data as files in the XWiki permanent directory. All data will be stored
 * under the the {@code org.xwiki.contrib.jsonstore} folder, in a file computed from the id as follows: the passed ids
 * are parsed as paths separated by {@code /} in which the last particle will be used as a filename. e.g. for the id
 * {@code mydata/mycategory/itemname} , the following file will be created in the xwiki permanent directory:
 * {@code org.xwiki.contrib.jsonstore/mydata/mycategory/itemname.json}. If the id ends by a / , it will be ignored. It's
 * the caller's responsibility to avoid special characters in the ids (that would not be accepted on file systems). <br>
 * This uses the same serializing technique as the JSONTool and so it has some limitations, for the serialization /
 * deserialization of calendar dates (java.util.Date), which will be serialized as the long values and parsed the same
 * (as longs instead of dates). Examples:
 * <ul>
 * <li>numbers and boolean values: 23, 13.5, true, false</li>
 * <li>strings: "one\"two'three" (quotes included)</li>
 * <li>arrays and collections: [1, 2, 3]</li>
 * <li>maps: {"number": 23, "boolean": false, "string": "value"}</li>
 * <li>beans: {"enabled": true, "name": "XWiki"} for a bean that has #isEnabled() and #getName() getters</li>
 * </ul>
 * 
 * @version $Id$
 */
@Component
@Singleton
@Named("xwikipermdir")
public class XWikiPermdirJsonStore implements JsonStore
{
    /**
     * The home of the json store in the permanent directory.
     */
    private static final String PERMDIR_FOLDERKEY = "org.xwiki.contrib.jsonstore";

    /**
     * The file path separator.
     */
    private static final String PATH_SEPARATOR = "/";

    /**
     * Used to get permanent directory.
     */
    @Inject
    private Environment environment;

    /**
     * Logger to log unexpected results and debug.
     */
    @Inject
    private Logger logger;

    /**
     * The store to the permanent directory.
     */
    private File store;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.jsonstore.JsonStore#persistAsJson(java.lang.Object, java.lang.String, boolean)
     */
    @Override
    public boolean persistAsJson(Object data, String id, boolean forceOverwrite) throws Exception
    {
        logger.debug("Starting storing data {} with id {}. Overwrite is forced: {}", data, id, forceOverwrite);
        File jsonStorageFile = getStorageFile(id);
        if (jsonStorageFile == null) {
            logger.trace("Json storage file for id {} resolved to null, returning false", id);
            return false;
        }
        // if the file exists and overwriting is not forced, return false
        if (jsonStorageFile.exists() && !forceOverwrite) {
            logger.debug("File exists and overwrite is not forced for id {}, returning false", id);
            return false;
        }
        // file doesn't exist yet or overwrite is forced, write JSON to file
        // (technique from org.xwiki.velocity.tools.JSONTool)
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule m = new SimpleModule("org.json.* serializer", new Version(1, 0, 0, "", "org.json", "json"));
        m.addSerializer(JSONObject.class, new JSONObjectSerializer());
        m.addSerializer(JSONArray.class, new JSONArraySerializer());
        mapper.registerModule(m);

        if (jsonStorageFile.getParentFile().exists() || jsonStorageFile.getParentFile().mkdirs()) {
            logger.trace("Serializing on disk the json for id {}", id);
            mapper.writeValue(jsonStorageFile, data);
        } else {
            logger.error("Could not create the path to store id {}: Unknown error", id);
            return false;
        }

        return true;
    }

    class JSONObjectSerializer extends JsonSerializer<JSONObject>
    {
        @Override
        public void serialize(JSONObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {
            jgen.writeRawValue(value.toString());
        }
    }

    class JSONArraySerializer extends JsonSerializer<JSONArray>
    {
        @Override
        public void serialize(JSONArray value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {
            jgen.writeRawValue(value.toString());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.jsonstore.JsonStore#getFromJsonStore(java.lang.String)
     */
    @Override
    public Object getFromJsonStore(String id) throws Exception
    {
        logger.debug("Starting fetching data for id {}", id);
        File jsonStorageFile = getStorageFile(id);
        if (jsonStorageFile == null) {
            logger.trace("Json storage file for id {} resolved to null, returning null", id);
            return null;
        }
        if (!jsonStorageFile.exists()) {
            logger.trace("Nothing stored on disk in the file computed for id {}, returning null", id);
            return null;
        }
        // get data (technique from org.xwiki.velocity.tools.JSONTool)
        ObjectMapper objectMapper = new ObjectMapper();
        logger.trace("Reeading json from disk for id {} and parsing as object", id);
        return objectMapper.readValue(jsonStorageFile, Object.class);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.jsonstore.JsonStore#exists(java.lang.String)
     */
    @Override
    public boolean exists(String id) throws Exception
    {
        logger.trace("Checking if id {} is stored in the json store", id);
        File jsonStorageFile = getStorageFile(id);
        if (jsonStorageFile == null) {
            logger.trace("Json storage file for id {} resolved to null, returning false (invalid ids don't 'exist')",
                id);
            return false;
        }
        return jsonStorageFile.exists();
    }

    /**
     * @param id a json store id (key)
     * @return the File corresponding to that store id on disk in the permdir or null if the id is empty
     */
    private File getStorageFile(String id)
    {
        logger.debug("Computing storage file for id {}", id);
        // secure the path a little bit, avoid tree traversal based on this id being used as a filename
        String filePathForId = FilenameUtils.normalizeNoEndSeparator(id, true);
        // this will return null if somebody tries to travserse, so let's log a special log ;)
        if (filePathForId == null) {
            logger.warn("Producing a path for the json returned null for id {}", id);
            return null;
        }
        if (StringUtils.isEmpty(filePathForId) || filePathForId.equals(PATH_SEPARATOR)) {
            logger.trace("Id {} is empty or equal to path separator, no file can be computed", id);
            return null;
        }
        // return the json path in the storage
        File jsonStorageFile = new File(getStorage(), filePathForId + ".json");
        logger.debug("Computed file storage for id {} which is {} with path {}", id, jsonStorageFile,
            jsonStorageFile != null ? jsonStorageFile.toPath() : null);
        return jsonStorageFile;
    }

    private File getStorage()
    {
        if (this.store == null) {
            this.store = new File(this.environment.getPermanentDirectory(), PERMDIR_FOLDERKEY + PATH_SEPARATOR);
        }

        return this.store;
    }
}
