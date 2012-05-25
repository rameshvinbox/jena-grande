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

import java.io.IOException;

import org.apache.giraph.graph.BasicVertex;
import org.apache.giraph.lib.TextVertexOutputFormat.TextVertexWriter;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;

public class PageRankVertexWriter extends TextVertexWriter<Text, DoubleWritable, NullWritable> {

	public PageRankVertexWriter ( RecordWriter<Text, Text> lineRecordWriter ) {
		super ( lineRecordWriter );
	}

	@Override
	public void writeVertex ( BasicVertex<Text, DoubleWritable, NullWritable, ?> vertex ) throws IOException, InterruptedException {
		getRecordWriter().write ( vertex.getVertexId(), new Text(vertex.getVertexValue().toString()) );
	}

}