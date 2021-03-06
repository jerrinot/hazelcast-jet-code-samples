/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package list;

import com.hazelcast.jet.DAG;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.processor.Processors;
import com.hazelcast.jet.processor.Sinks;
import com.hazelcast.jet.processor.Sources;
import com.hazelcast.jet.Vertex;
import com.hazelcast.jet.stream.IStreamList;

import static com.hazelcast.jet.Edge.between;

/**
 * A DAG which reads from a Hazelcast Ilist,
 * converts the item to string,
 * and writes to another Hazelcast IList
 */
public class ReadWriteList {

    private static final int ITEM_COUNT = 10;
    private static final String SOURCE_LIST_NAME = "sourceList";
    private static final String SINK_LIST_NAME = "sinkList";

    public static void main(String[] args) throws Exception {
        JetInstance instance = Jet.newJetInstance();

        try {
            IStreamList<Integer> sourceList = instance.getList(SOURCE_LIST_NAME);
            for (int i = 0; i < ITEM_COUNT; i++) {
                sourceList.add(i);
            }

            DAG dag = new DAG();

            Vertex source = dag.newVertex("source", Sources.readList(SOURCE_LIST_NAME)).localParallelism(1);
            Vertex transform = dag.newVertex("transform", Processors.map((Integer i) -> Integer.toString(i)));
            Vertex sink = dag.newVertex("sink", Sinks.writeList(SINK_LIST_NAME));

            dag.edge(between(source, transform));
            dag.edge(between(transform, sink));

            instance.newJob(dag).execute().get();

            IStreamList<String> sinkList = instance.getList(SINK_LIST_NAME);
            System.out.println("Sink list size: " + sinkList.size());
            System.out.println("Sink list items: ");
            for (String s : sinkList) {
                System.out.println(s);
            }
        } finally {
            Jet.shutdownAll();
        }

    }
}
