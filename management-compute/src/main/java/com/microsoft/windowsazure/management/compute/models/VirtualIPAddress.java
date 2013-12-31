/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

// Warning: This code was generated by a tool.
// 
// Changes to this file may cause incorrect behavior and will be lost if the
// code is regenerated.

package com.microsoft.windowsazure.management.compute.models;

import java.net.InetAddress;

/**
* The virtual IP address of the deployment.
*/
public class VirtualIPAddress
{
    private InetAddress address;
    
    /**
    * The virtual IP address of the deployment.
    */
    public InetAddress getAddress() { return this.address; }
    
    /**
    * The virtual IP address of the deployment.
    */
    public void setAddress(InetAddress address) { this.address = address; }
    
    private Boolean isDnsProgrammed;
    
    /**
    * Indicates whether the IP address is DNS programmed.
    */
    public Boolean getIsDnsProgrammed() { return this.isDnsProgrammed; }
    
    /**
    * Indicates whether the IP address is DNS programmed.
    */
    public void setIsDnsProgrammed(Boolean isDnsProgrammed) { this.isDnsProgrammed = isDnsProgrammed; }
    
    private String name;
    
    /**
    * The name of the virtual IP.
    */
    public String getName() { return this.name; }
    
    /**
    * The name of the virtual IP.
    */
    public void setName(String name) { this.name = name; }
    
    /**
    * Initializes a new instance of the VirtualIPAddress class.
    *
    */
    public VirtualIPAddress()
    {
    }
}
