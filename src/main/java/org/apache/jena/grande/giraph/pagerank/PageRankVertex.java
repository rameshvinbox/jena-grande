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

package org.apache.jena.grande.giraph.pagerank;

import java.util.Iterator;

import org.apache.giraph.graph.Aggregator;
import org.apache.giraph.graph.EdgeListVertex;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageRankVertex extends EdgeListVertex<Text, DoubleWritable, NullWritable, DoubleWritable> {

	private static final Logger log = LoggerFactory.getLogger(PageRankVertex.class); 
	public static final int NUM_ITERATIONS = 30;

	@Override
	public void compute(Iterator<DoubleWritable> msgIterator) {
		log.debug("{}#{} - compute(...) vertexValue={}", new Object[]{getVertexId(), getSuperstep(), getVertexValue()});

		Aggregator<DoubleWritable> danglingAggegator = (Aggregator<DoubleWritable>)getAggregator("dangling");
		Aggregator<DoubleWritable> pagerankAggegator = (Aggregator<DoubleWritable>)getAggregator("pagerank");
		Aggregator<?> errorAggegator = (Aggregator<DoubleWritable>)getAggregator("error");

		Aggregator<LongWritable> countVerticesAggegator = (Aggregator<LongWritable>)getAggregator("count");
		long numVertices = countVerticesAggegator.getAggregatedValue().get();
		
		if ( getSuperstep() == 0 ) {
			log.debug("{}#{} - compute(...): {}", new Object[]{getVertexId(), getSuperstep(), "sending fake messages, just to count vertices including dangling ones"});
			sendMsgToAllEdges ( new DoubleWritable() );
		} else if ( getSuperstep() == 1 ) {
			log.debug("{}#{} - compute(...): {}", new Object[]{getVertexId(), getSuperstep(), "counting vertices including dangling ones"});
			countVerticesAggegator.aggregate(new LongWritable(1L));
		} else if ( getSuperstep() == 2 ) {
			log.debug("{}#{} - compute(...): numVertices={}", new Object[]{getVertexId(), getSuperstep(), numVertices});
			log.debug("{}#{} - compute(...): {}", new Object[]{getVertexId(), getSuperstep(), "initializing pagerank scores to 1/N"});
			DoubleWritable vertexValue = new DoubleWritable ( 1.0 / numVertices );
			setVertexValue(vertexValue);			
			log.debug("{}#{} - compute(...) vertexValue={}", new Object[]{getVertexId(), getSuperstep(), getVertexValue()});
			send( danglingAggegator, pagerankAggegator );
		} else if ( getSuperstep() > 2 ) {
			log.debug("{}#{} - compute(...): numVertices={}", new Object[]{getVertexId(), getSuperstep(), numVertices});
			double sum = 0;
			while (msgIterator.hasNext()) {
				double msgValue = msgIterator.next().get(); 
				log.debug("{}#{} - compute(...) <-- {}", new Object[]{getVertexId(), getSuperstep(), msgValue});				
				sum += msgValue;
			}
			log.debug("{}#{} - compute(...) danglingNodesContribution={}", new Object[]{getVertexId(), getSuperstep(), getVertexValue(), danglingAggegator.getAggregatedValue().get()});
			DoubleWritable vertexValue = new DoubleWritable( ( 0.15f / numVertices ) + 0.85f * ( sum + danglingAggegator.getAggregatedValue().get() / numVertices ) );
//			DoubleWritable vertexValue = new DoubleWritable( ( 0.15f / numVertices ) + 0.85f * sum );
			setVertexValue(vertexValue);
			log.debug("{}#{} - compute(...) vertexValue={}", new Object[]{getVertexId(), getSuperstep(), getVertexValue()});
			send( danglingAggegator, pagerankAggegator );
		}
	}
	
	private void send( Aggregator<DoubleWritable> danglingAggegator, Aggregator<DoubleWritable> pagerankAggegator ) {
		if ( getSuperstep() < NUM_ITERATIONS ) {
			long edges = getNumOutEdges();
			log.debug("{}#{} - compute(...) numOutEdges={}", new Object[]{getVertexId(), getSuperstep(), edges});
			if ( edges > 0 ) {
				sendMsgToAllEdges ( new DoubleWritable(getVertexValue().get() / edges) );
			} else {
				danglingAggegator.aggregate( getVertexValue() );
			}
		} else {
			pagerankAggegator.aggregate(getVertexValue());
			voteToHalt();
			log.debug("{}#{} - compute(...) --> halt", new Object[]{getVertexId(), getSuperstep()});
		}
	}

}
