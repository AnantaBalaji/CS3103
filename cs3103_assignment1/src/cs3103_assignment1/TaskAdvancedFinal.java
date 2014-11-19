/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cs3103_assignment1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Clement
 */
// java cmd: cs3103_assignment1.Task3
public class TaskAdvancedFinal {
    private static byte S2S = 1;
    private static byte P2C = 2;
    private static byte C2P = 3;
    private static byte P2P = 4;
    
    private static final String s_S2S = "s2s";
    private static final String s_P2C = "p2c";
    private static final String s_C2P = "c2p";
    private static final String s_P2P = "p2p";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // args: <input file> <L value> <R value>

        if (args.length != 1) {
            System.out.println("#Please specify three input parameters: <input file>");
        }

        String inputFileName = args[0]; // 2014_task1_output_sample.txt

        Graph graph = new Graph();
        Set<String> uniqueASSet = new HashSet<>();
        int stubCount, ispCount, denseCoreCount, transitCoreCount, outerCoreCount;
        
        System.out.printf("#Filename: %s\n", inputFileName);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFileName))) {
            System.out.println("#[Building directed graph] Opening file for building directed graph graph ... Please wait ...");

            int linecount = 0;

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

                    String[] ASPairRelationship = line.split(" ");

                    if (ASPairRelationship.length != 3) {
                        // something is wrong! skip
                        continue;
                    }

                    String firstASToken = ASPairRelationship[0];
                    String secondASToken = ASPairRelationship[1];
                    String relationship = ASPairRelationship[2];
                    
                    // if relationship is "unknown" or "non-existent" in this direction of AS1->AS2
                    if (relationship.equals("?")){
                        continue;
                    }
                    
                    Vertex v1 = graph.getVertex(firstASToken);
                    Vertex v2 = graph.getVertex(secondASToken);
                    
                    if (v1 == null){
                        v1 = new Vertex();
                        v1.setName(firstASToken);
                        graph.addVertex(v1);
                    }
                    
                    if (v2 == null){
                        v2 = new Vertex();
                        v2.setName(secondASToken);
                        graph.addVertex(v2);
                    }
                    
                    switch(relationship){
                        case s_P2P: 
                            Edge e1 = v1.addNeighbour(v2);
                            Edge e2 = v2.addNeighbour(v1);
                            e1.setType(P2P);
                            e2.setType(P2P);
                            break;
                        case s_S2S: 
                            Edge e3 = v1.addNeighbour(v2);
                            Edge e4 = v2.addNeighbour(v1);
                            e3.setType(S2S);
                            e4.setType(S2S);
                            break;
                        case s_P2C: 
                            // 1(P) -> 2(C)
                            Edge e5 = v1.addNeighbour(v2);
                            e5.setType(P2C);

                            // add a transient link C2P in addition to P2C - to capture the number of Providers the Customer has -> more efficient algorithm only
                            Edge t_e1 = v2.addNeighbour(v1);
                            t_e1.setType(C2P);
                            
                            break;
                        case s_C2P: 
                            // 1(C) -> 2(P) , change to
                            // 2(P) -> 1(C)
                            Edge e6 = v2.addNeighbour(v1);
                            e6.setType(P2C);
                            
                            // add a transient link C2P in addition to P2C - to capture the number of Providers the Customer has -> more efficient algorithm only
                            Edge t_e2 = v1.addNeighbour(v2);
                            t_e2.setType(C2P);

                            break;
                    }
                    
                    if (linecount % 50000 == 0) {
                        System.out.printf("#Read %d lines\n", linecount);
                    }

                } while (true);
            }
            
            System.out.println("#Finished building directed graph!");
            
            System.out.println("#[Extracting stubs' C2P degrees (identifying the number of providers each customer has)]");
            
            List<Vertex> stubList = getStubs(graph);
            stubCount = stubList.size();
            
            int smallCustomerCount = 0;
            int enterpriseCustomerCount = 0;
            for(Vertex v : stubList){
                int c = 0;
                for(Edge e : v.getEdges()){
                    if (e.getType() == C2P){
                        c++;
                    }
                }
                if (c != 0){
                    if (c == 1){
                        // small customer
                        smallCustomerCount++;
                    }else{
                        // large customer
                        enterpriseCustomerCount++;
                    }
                }
            }
            System.out.printf("Small customer count: %d\n", smallCustomerCount);
            System.out.printf("Enterprise customer count: %d\n", enterpriseCustomerCount);
            
            // prune graph
            graph.removeVertice(stubList);
            
            System.out.println("#[Extracting region ISPs]");
            
            List<Vertex> regionalISPList = getRegionalISPs(graph);
            ispCount = regionalISPList.size();
            for(Vertex v : regionalISPList){
                //System.out.printf("%s %s\n", v.getName(), "regional_ISP");
            }
            
            // prune graph
            graph.removeVertice(regionalISPList);
            
            System.out.println("#[Extracting dense cores]");
            
            List<Vertex> denseCoresList = getDenseCores(graph);
            denseCoreCount = denseCoresList.size();
            for(Vertex v : denseCoresList){
                //System.out.printf("%s %s\n", v.getName(), "dense_core");
            }
            
            // do not prune graph for now
            
            System.out.println("#[Extracting transit cores]");
            
            List<Vertex> transitCoresList = getTransitCores(graph, denseCoresList);
            transitCoreCount = transitCoresList.size();
            
            int smallTransitProviderCount = 0;
            int largeTransitProviderCount = 0;
            for (Vertex v : transitCoresList) {
                int c = 0;
                for (Edge e : v.getEdges()) {
                    if (e.getType() == S2S || e.getType() == P2P) {
                        c++;
                    }
                }
                if (c != 0){
                    if (c <= 2){
                        // small customer
                        smallTransitProviderCount++;
                    }else{
                        // large customer
                        largeTransitProviderCount++;
                    }
                }
            }
            System.out.printf("Small transit count: %d\n", smallTransitProviderCount);
            System.out.printf("Big transit count: %d\n", largeTransitProviderCount);
            
            //vca.printValueCount();
            
//            System.out.println("#[Extracting outer cores]");
//            
//            List<Vertex> outerCoresList = getOuterCores(graph, denseCoresList , transitCoresList);
//            outerCoreCount = outerCoresList.size();
//            for(Vertex v : outerCoresList){
//                if(!uniqueASSet.add(v.getName())){System.out.println("!"+v.getName() + " is a duplicate!");};
//                //System.out.printf("%s %s\n", v.getName(), "outer_core");
//            }
        }

        // done!
        System.out.printf("#Stub count: %d, Regional-ISP count: %d, Dense-cores count: %d, Transit-cores count: %d\n", 
                stubCount, ispCount, denseCoreCount, transitCoreCount);
    }

    public static List<Vertex> getStubs(Graph g) {
        return getLeafNodes(g);
    }

    public static List<Vertex> getRegionalISPs(Graph g) {
        return getLeafNodes(g);
    }

    public static List<Vertex> getDenseCores(Graph g) {
        return getDenseCoresNodes(g);
    }

    public static List<Vertex> getTransitCores(Graph g, List<Vertex> denseCoreList) {
        return getTransitCoresNodes(g, denseCoreList);
    }
    
    public static List<Vertex> getOuterCores(Graph g, List<Vertex> denseCoreList, List<Vertex> transitCoreList) {
        return getOuterCoresNodes(g, denseCoreList, transitCoreList);
    }

    private static List<Vertex> getLeafNodes(Graph g) {
        List<Vertex> stubList = new ArrayList<>();

        for (Vertex v : g.getVertices()) {
            
            boolean isLeaf = true;
            
            s1:
            for(Edge e : v.getEdges()){
                if (e.getType() == P2C){
                    // check if destination vertex still exists in graph
                    Vertex childNode = g.getVertex(e.getDestination().getName());
                    
                    // if it still exists, then this is definitely not a leaf
                    if (childNode != null) {
                        isLeaf = false;
                        break;
                    }
                }
            }
            
            if (isLeaf){
                stubList.add(v);
            }
        }
        return stubList;
    }

    private static List<Vertex> getDenseCoresNodes(Graph g) {
        Set<Vertex> globalSet = new HashSet<>(g.getVertices());
        Set<Vertex> notDenseCoreSet = new HashSet<>();
        
        for (Vertex v : g.getVertices()) {
            for(Edge e : v.getEdges()){
                if (e.getType() == P2C){
                    // if this edge is P2C, the destination AS is definitely not a Dense Core
                    notDenseCoreSet.add(e.getDestination());
                }
            }
        }
        
        // once done, subtract the non-densecores from the globalset to get the dense cores
        globalSet.removeAll(notDenseCoreSet);

        return new ArrayList<>(globalSet);
    }
    
    private static List<Vertex> getTransitCoresNodes(Graph g, List<Vertex> denseCoreList) {
        Set<Vertex> denseCoreSet = new HashSet<>(denseCoreList);
        Set<Vertex> transitCoreSet = new HashSet<>();
        
        // find all AS with transit links
        for (Vertex v : g.getVertices()) {
            for(Edge e : v.getEdges()){
                if (e.getType() == S2S || e.getType() == P2P){
                    // s2s or p2p implies that it is bi-directional from the logic implemented earlier, so there is no need to check for incoming edge for the destination
                    // but before adding, check if the destination is pruned initially. edges may exist, but destination nodes may not be
                    if (g.getVertex(e.getDestination().getName()) != null){
                        transitCoreSet.add(e.getDestination());
                    }
                }
            }
        }
        
        // once done, some transit cores may contain dense cores that needs to be removed
        transitCoreSet.removeAll(denseCoreSet);
        
        return new ArrayList<>(transitCoreSet);
    }
    
    private static List<Vertex> getOuterCoresNodes(Graph g, List<Vertex> denseCoreList, List<Vertex> transitCoreList) {
        Set<Vertex> globalSet = new HashSet<>(g.getVertices());
        Set<Vertex> denseCoreSet = new HashSet<>(denseCoreList);
        
        // combine dense cores and transit cores
        denseCoreSet.addAll(new HashSet<>(transitCoreList));
        
        // subtract all dense cores, transit cores from all remaining AS CORES
        globalSet.removeAll(denseCoreSet);
 
        return new ArrayList<>(globalSet);
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

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Vertex other = (Vertex) obj;
            return (this.getName().equalsIgnoreCase(other.getName()));
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

        public Vertex getVertex(String vertexName){
            for (Vertex vertex : vertices) {
                if (vertex.getName().equalsIgnoreCase(vertexName)) {
                    return vertex;
                }
            }
            return null;
        }
        
        public void removeVertice(List<Vertex> vlist){
            Set<Vertex> globalSet = new HashSet<>(vertices);
            globalSet.removeAll(vlist);
            vertices = new ArrayList<>(globalSet);
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
