/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.catalina;

import java.security.Principal;
import java.util.Iterator;

/**
 * <p>Abstract representation of a group of {@link User}s in a
 * {@link UserDatabase}.  Each user that is a member of this group
 * inherits the {@link Role}s assigned to the group.</p>
 *
 * @author Craig R. McClanahan
 * @since 4.1
 */
public interface Group extends Principal {
    /**
     * @return the group name of this group, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getGroupname();


    /**
     * Set the group name of this group, which must be unique
     * within the scope of a {@link UserDatabase}.
     *
     * @param groupname The new group name
     */
    public void setGroupname(String groupname);

    /**
     * @return the {@link UserDatabase} within which this Group is defined.
     */
    public UserDatabase getUserDatabase();
}
