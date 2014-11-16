/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs3103_assignment1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Clement
 */
// java cmd: cs3103_assignment1.Task3
public class Task3 {

    private static byte S2S = 1;
    private static byte P2C = 2;
    private static byte C2P = 3;
    private static byte P2P = 4;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // args: <input file> <L value> <R value>

        if (args.length != 3) {
            System.out.println("#Please specify three input parameters: <input file> <L value> <R value>");
        }

        String inputFileName = args[0]; // 2014_task1_output_sample.txt
        int L_value = Integer.parseInt(args[1]);
        double R_value = Double.parseDouble(args[2]);
        double R_inverse_value = 1 / R_value;
        Graph graph = new Graph();
        
        System.out.printf("#Filename: %s, L: %d, R: %f\n", inputFileName, L_value, R_value);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFileName))) {
            System.out.println("#[Refined Algorithm Phase 1, 2] Opening file for building graph ... Please wait ...");

            int linecount = 0;
            List<Vertex> asPathVertexList = new ArrayList<>();
            List<Edge> asPathEdgesRightwardList = new ArrayList<>();
            List<Edge> asPathEdgesLeftwardList = new ArrayList<>();

            start:
            while (fileReader.ready()) {
                System.out.println("#File is ready to be read!");

                do {
                    String line = fileReader.readLine();
                    linecount++;

                    if (line == null) {
                        break start;
                    }

                    // skip comments
                    if (line.charAt(0) == '#') {
                        continue;
                    }

                    String[] ASes = line.split(" ");

                    if (ASes.length < 2) {
                        // something is wrong!
                    }

                    for (int i = 0; i < ASes.length - 1; i++) {
                        String firstASToken = ASes[i];
                        String secondASToken = ASes[i + 1];

                        Vertex v1 = graph.getVertex(firstASToken);
                        Edge e1;
                        if (v1 == null) {
                            v1 = new Vertex();
                            v1.setName(firstASToken);
                            // add to graph
                            graph.addVertex(v1);
                        }

                        Vertex v2 = graph.getVertex(secondASToken);
                        Edge e2;
                        if (v2 == null) {
                            v2 = new Vertex();
                            v2.setName(secondASToken);
                            // add to graph
                            graph.addVertex(v2);
                        }

                        // check for neighbors
                        e1 = v1.hasNeighbourEdge(v2.getName());
                        if (e1 == null) {
                            e1 = v1.addNeighbour(v2);
                        }

                        e2 = v2.hasNeighbourEdge(v1.getName());
                        if (e2 == null) {
                            e2 = v2.addNeighbour(v1);
                        }


                        asPathVertexList.add(v1);
                        asPathEdgesRightwardList.add(e1);
                        asPathEdgesLeftwardList.add(e2);

                        if (i == ASes.length - 2) { // last iteration
                            asPathVertexList.add(v2); // then add v2

                            /* Refined algorithm Phase 2 */
                            // get the index of the AS with the largest degree
                            int topProviderAsIndex = getASIndexHighestDegree(asPathVertexList);

                            if (topProviderAsIndex != -1) {
                                // V: 0 1 2 3* 4 5 6
                                // E:  0>1>2  3<4<5
                                // Size = 7
                                for (int j = 0; j < topProviderAsIndex; j++) {
                                    asPathEdgesRightwardList.get(j).incrementTransit();
                                }
                                for (int j = topProviderAsIndex; j < asPathVertexList.size() - 1; j++) {
                                    asPathEdgesLeftwardList.get(j).incrementTransit();
                                }
                            } else {
                                // something went wrong
                            }
                        }

                    }// end for

                    asPathVertexList.clear();
                    asPathEdgesRightwardList.clear();
                    asPathEdgesLeftwardList.clear();

                    if (linecount % 200000 == 0) {
                        System.out.printf("#Read %d lines\n", linecount);
                    }

                } while (true);
            }
        }

        try (BufferedReader fileReader2 = new BufferedReader(new FileReader(inputFileName))) {
            System.out.println("#[Refined Algorithm Phase 3] Opening file to assign edge relationships... Please wait ...");

            int linecount = 0;
            
            start:
            while (fileReader2.ready()) {
                System.out.println("#File is ready to be read!");

                do {
                    String line = fileReader2.readLine();
                    linecount++;

                    if (line == null) {
                        break start;
                    }

                    // skip comments
                    if (line.charAt(0) == '#') {
                        continue;
                    }

                    String[] ASes = line.split(" ");

                    if (ASes.length < 2) {
                        // something is wrong!
                    }

                    for (int i = 0; i < ASes.length - 1; i++) {
                        String firstASToken = ASes[i];
                        String secondASToken = ASes[i + 1];

                        // v1 -- e1 --> v2
                        // v1 <- e2 --- v2
                        Vertex v1 = graph.getVertex(firstASToken);
                        Vertex v2 = graph.getVertex(secondASToken);
                        Edge e1 = v1.hasNeighbourEdge(secondASToken);
                        Edge e2  = v2.hasNeighbourEdge(firstASToken);
                        
                        // if edge 1 has not been 'configured' before
                        // there are many multiple duplicate AS pairs
                        if (e1.getType() == 0){
                            if ((e1.getTransit() > L_value && e2.getTransit() > L_value) || 
                                    (e1.getTransit() <= L_value && e1.getTransit() > 0 && e2.getTransit() <= L_value && e2.getTransit() > 0)){
                                e1.setType(S2S);
                            }else if (e2.getTransit() > L_value || e1.getTransit() == 0){
                                e1.setType(P2C);
                            }else if (e1.getTransit() > L_value || e2.getTransit() == 0){
                                e1.setType(C2P);
                            }
                        }

                    }// end for

                    if (linecount % 200000 == 0) {
                        System.out.printf("#Read %d lines\n", linecount);
                    }

                } while (true);
            }
        }
        
        try (BufferedReader fileReader3 = new BufferedReader(new FileReader(inputFileName))) {
            System.out.println("#[Final Algorithm Phase 2, 3] Opening file for assigning non-peering edges ... Please wait ...");

            int linecount = 0;
            List<Vertex> asPathVertexList = new ArrayList<>();
            List<Edge> asPathEdgesRightwardList = new ArrayList<>();
            List<Edge> asPathEdgesLeftwardList = new ArrayList<>();

            start:
            while (fileReader3.ready()) {
                System.out.println("#File is ready to be read!");

                do {
                    String line = fileReader3.readLine();
                    linecount++;

                    if (line == null) {
                        break start;
                    }

                    // skip comments
                    if (line.charAt(0) == '#') {
                        continue;
                    }

                    String[] ASes = line.split(" ");

                    if (ASes.length < 2) {
                        // something is wrong!
                    }

                    for (int i = 0; i < ASes.length - 1; i++) {
                        String firstASToken = ASes[i];
                        String secondASToken = ASes[i + 1];

                        Vertex v1 = graph.getVertex(firstASToken);
                        Edge e1 = v1.hasNeighbourEdge(secondASToken);

                        Vertex v2 = graph.getVertex(secondASToken);
                        Edge e2 = v2.hasNeighbourEdge(firstASToken);

                        asPathVertexList.add(v1);
                        asPathEdgesRightwardList.add(e1);
                        asPathEdgesLeftwardList.add(e2);

                        if (i == ASes.length - 2) { // last iteration
                            asPathVertexList.add(v2); // then add v2

                            /* Final algorithm Phase 2 */
                            // get the index of the AS with the largest degree
                            int topProviderAsIndex = getASIndexHighestDegree(asPathVertexList);

                            if (topProviderAsIndex != -1) {
                                // V: 0 1 2 3* 4 5 6
                                // E:  0>1>2  3<4<5
                                // Size = 7
                                for (int j = 0; j < topProviderAsIndex - 1; j++) {
                                    asPathEdgesRightwardList.get(j).setNonPeering(true);
                                }
                                
                                for (int j = topProviderAsIndex + 1; j < asPathVertexList.size() - 1; j++) {
                                    asPathEdgesRightwardList.get(j).setNonPeering(true);
                                }
                                
                                // check to prevent list outofbound index
                                if (asPathEdgesRightwardList.size() >= topProviderAsIndex + 1 /*check rightwards*/ 
                                        && topProviderAsIndex != 0 /*check leftwards*/){
                                    
                                    Edge e_before = asPathEdgesRightwardList.get(topProviderAsIndex - 1);
                                    Edge e_after = asPathEdgesRightwardList.get(topProviderAsIndex);
                                    
                                    if(e_before.getType() != S2S && e_after.getType() != S2S){
                                        Vertex v_before = asPathVertexList.get(topProviderAsIndex - 1);
                                        Vertex v_after = asPathVertexList.get(topProviderAsIndex + 1);
                                        
                                        if (v_before.getEdges().size() > v_after.getEdges().size()){
                                            e_after.setNonPeering(true);
                                        }else{
                                            e_before.setNonPeering(true);
                                        }
                                    }
                                }
                                
                                /* Final algorithm Phase 3 */
                                for(int j = 0; j < asPathVertexList.size() - 1; j++){
                                    Vertex v3 = asPathVertexList.get(j);
                                    Vertex v4 = asPathVertexList.get(j+1);
                                    
                                    Edge e3 = asPathEdgesRightwardList.get(j);
                                    Edge e4 = asPathEdgesLeftwardList.get(j);
                                    
                                    if(!e3.isNonPeering() && !e4.isNonPeering() && 
                                            (v3.getEdges().size()/v4.getEdges().size()) < R_value && (v4.getEdges().size()/v3.getEdges().size()) > R_inverse_value){
                                        e3.setType(P2P);
                                    }
                                }
                            } else {
                                // something went wrong
                            }
                        }
                    }// end for

                    asPathVertexList.clear();
                    asPathEdgesRightwardList.clear();
                    asPathEdgesLeftwardList.clear();

                    if (linecount % 200000 == 0) {
                        System.out.printf("#Read %d lines\n", linecount);
                    }

                } while (true);
            }
        }
        
        // done! print out all vertices' edges with edge type and neighbour vertex name
        graph.printVertexEdgeRelationship();
    }

    private static int getASIndexHighestDegree(List<Vertex> vertices) {
        int highest = 0;
        int index = -1;
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);

            if (v.getEdges().size() > highest) {
                highest = v.getEdges().size();
                index = i;
            }
        }

        return index;
    }

    private static class Edge {

        private short transit;
        private boolean nonPeering = false;
        private byte type;
        private Vertex origin;
        private Vertex destination;

        public Edge(Vertex o, Vertex d) {
            this.origin = o;
            this.destination = d;
        }

        /**
         * @return the origin
         */
        public Vertex getOrigin() {
            return origin;
        }

        /**
         * @param origin the origin to set
         */
        public void setOrigin(Vertex origin) {
            this.origin = origin;
        }

        /**
         * @return the destination
         */
        public Vertex getDestination() {
            return destination;
        }

        /**
         * @param destination the destination to set
         */
        public void setDestination(Vertex destination) {
            this.destination = destination;
        }

        /**
         * @return the transit
         */
        public short getTransit() {
            return transit;
        }

        /**
         * @param transit the transit to set
         */
        public void setTransit(short transit) {
            this.transit = transit;
        }

        /**
         * @return the nonPeering
         */
        public boolean isNonPeering() {
            return nonPeering;
        }

        /**
         * @param nonPeering the nonPeering to set
         */
        public void setNonPeering(boolean nonPeering) {
            this.nonPeering = nonPeering;
        }

        /**
         * @return the type
         */
        public byte getType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(byte type) {
            this.type = type;
        }

        public void incrementTransit() {
            transit++;
        }
    }

    private static class Vertex implements Comparable<Vertex> {

        private String name;
        private List<Edge> edges;

        public Vertex() {
            this.edges = new ArrayList<>();
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the edges
         */
        public List<Edge> getEdges() {
            return edges;
        }

        /**
         * @param edges the edges to set
         */
        public void setEdges(List<Edge> edges) {
            this.edges = edges;
        }

        public Edge addNeighbour(Vertex neighbour) {
            Edge newEdge = new Edge(this, neighbour);
            this.edges.add(newEdge);
            return newEdge;
        }

        public Edge hasNeighbourEdge(String verticeName) {
            for (Edge edge : edges) {
                if (edge.getDestination().getName().equals(verticeName)) {
                    return edge;
                }
            }
            return null;
        }

        @Override
        public int compareTo(Vertex o) {
            if (this.edges.size() < o.getEdges().size()) {
                return -1;
            } else if (this.edges.size() > o.getEdges().size()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private static class Graph {

        private List<Vertex> vertices;

        public Graph() {
            vertices = new ArrayList<>();
        }

        /**
         * @return the vertices
         */
        public List<Vertex> getVertices() {
            return vertices;
        }

        /**
         * @param vertices the vertices to set
         */
        public void setVertices(List<Vertex> vertices) {
            this.vertices = vertices;
        }

        public void addVertex(Vertex v) {
            vertices.add(v);
        }

        public Vertex getVertex(String vertexName) {
            for (Vertex vertex : vertices) {
                if (vertex.getName().equalsIgnoreCase(vertexName)) {
                    return vertex;
                }
            }

            return null;
        }

        public void sortVerticesDegreesAsc() {
            Collections.sort(vertices);
        }

        public void printGraph() {
            for (Vertex v : vertices) {
                System.out.printf("Vertex %s\n", v.getName());

                for (Edge e : v.getEdges()) {
                    System.out.printf("\tEdge %s -> %s \n", e.getOrigin().getName(), e.getDestination().getName());
                }

                System.out.println();
            }
        }
        
        public void printVertexEdgeRelationship(){
            int s2s_count = 0;
            int p2c_count = 0;
            int c2p_count = 0;
            int p2p_count = 0;
            
            for (Vertex v : vertices) {
                for (Edge e : v.getEdges()) {
                    
                    String edgeType = "?";
                    
                    switch(e.getType()){
                        case 1:
                            edgeType = "s2s"; 
                            s2s_count++;
                            break;
                        case 2:
                            edgeType = "p2c"; 
                            p2c_count++;
                            break;
                        case 3:
                            edgeType = "c2p";
                            c2p_count++;
                            break;
                        case 4:
                            edgeType = "p2p"; 
                            p2p_count++;
                            break;
                    }
                    
                    System.out.printf("%s %s %s\n", v.getName(), e.getDestination().getName(), edgeType);
                }
            }
            System.out.printf("# s2s count: %d, p2c count: %d, c2p count: %d, p2p count: %d\n", s2s_count, p2c_count, c2p_count, p2p_count);
        }
    }
}