/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.grande.giraph.pagerank.memory;

import java.util.HashSet;
import java.util.Hashtable;

public class Graph {

	private HashSet<String> nodes = new HashSet<String>() ;
	private Hashtable<String, HashSet<String>> outgoing_links = new Hashtable<String, HashSet<String>>();
	private Hashtable<String, HashSet<String>> incoming_links = new Hashtable<String, HashSet<String>>();
	private int count_links = 0 ;
	
	public boolean addNode ( String node ) {
		if ( nodes.contains(node) ) return false ;
		
		nodes.add(node) ;
		if ( !outgoing_links.containsKey ( node ) ) {
			outgoing_links.put ( node, new HashSet<String>() ) ;
		}
		if ( !incoming_links.containsKey ( node ) ) {
			incoming_links.put ( node, new HashSet<String>() ) ;
		}
		return true ;
	}
	
	public boolean addLink ( String source, String destination ) {
		if ( source.equals( destination ) ) return false ;

		addNode ( source ) ;
		addNode ( destination ) ;
		
		if ( outgoing_links.get ( source ).contains( destination ) ) {
			return false ;
		}
		
		outgoing_links.get ( source ).add( destination ) ;
		incoming_links.get ( destination ).add( source ) ;
		count_links++ ;

		return true ;
	}
	
	public int countNodes() {
		return nodes.size() ;
	}
	
	public int countLinks() {
		return count_links ;
	}
	
	public int countOutgoingLinks(String node) {
		return outgoing_links.get(node).size() ;
	}

	public HashSet<String> getIncomingLinks(String node) {
		return incoming_links.get(node) ;
	}
	
	public HashSet<String> getNodes() {
		return nodes ;
	}
	
}
