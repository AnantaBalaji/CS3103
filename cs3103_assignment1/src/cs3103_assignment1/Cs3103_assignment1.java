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

// java cmd: cs3103_assignment1.Cs3103_assignment1

public class Cs3103_assignment1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        try (BufferedReader fileReader = new BufferedReader(new FileReader("task1_output.txt"))) {
            System.out.println("Opening file ... Please wait ...");
            
            Graph graph = new Graph();
            int linecount = 0;
            
            start:
            while(fileReader.ready()){
                System.out.println("File is ready to be read!");

                do{
                    String line = fileReader.readLine();
                    linecount++;
                    
                    if (line == null){
                        break start;
                    }
                    
                    String[] ASes = line.split(" ");
                    
                    if (ASes.length < 2){
                        // something is wrong!
                    }
                    
                    for(int i = 0; i < ASes.length - 1 ; i++){
                        String firstASToken = ASes[i];
                        String secondASToken = ASes[i+1];
                        
                        Vertex v1 = graph.getVertex(firstASToken);
                        if (v1 == null){
                            v1 = new Vertex();
                            v1.setName(firstASToken);
                            // add to graph
                            graph.addVertex(v1);
                        }
                        
                        Vertex v2 = graph.getVertex(secondASToken);
                        if (v2 == null){
                            v2 = new Vertex();
                            v2.setName(secondASToken);
                            // add to graph
                            graph.addVertex(v2);
                        }
                        
                        // check for neighbors
                        if (! v1.hasNeighbourVertice(v2.getName())){
                            v1.addNeighbour(v2, (byte)1);
                        }
                        
                        if (! v2.hasNeighbourVertice(v1.getName())){
                            v2.addNeighbour(v1, (byte)1);
                        }
                    }
                    
                    if (linecount % 100000 == 0){
                        System.out.printf("Read %d lines\n", linecount);
                    }
                    
                }while(true);
            }
            graph.sortVerticesDegreesAsc();
            
            List<Vertex> vertices = graph.getVertices();
            for (int i = vertices.size() - 1; i > vertices.size() - 11; i--){
                System.out.printf("AS %s has %d degrees\n", vertices.get(i).getName(), vertices.get(i).getEdges().size());
            }
                    
            // graph.printGraph();
        }
    }
    
    private static class Edge{
        private byte cost;
        private Vertex origin;
        private Vertex destination;

        public Edge(Vertex o, Vertex d, byte cost){
            this.cost = cost;
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
         * @return the cost
         */
        public byte getCost() {
            return cost;
        }

        /**
         * @param cost the cost to set
         */
        public void setCost(byte cost) {
            this.cost = cost;
        }

    }
    
    private static class Vertex implements Comparable<Vertex>{
  
        private String name;
        private List<Edge> edges;

        public Vertex(){
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
        
        public void addNeighbour(Vertex neighbour, byte cost){
            this.edges.add(new Edge(this, neighbour, cost));
        }
        
        public boolean hasNeighbourVertice(String verticeName){
            for(Edge edge : edges){
                if (edge.getDestination().getName().equals(verticeName)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public int compareTo(Vertex o) {
            if (this.edges.size() < o.getEdges().size()){
                return -1;
            }else if (this.edges.size() > o.getEdges().size()){
                return 1;
            }else{
                return 0;
            }
        }
    }
    
    private static class Graph{
        private List<Vertex> vertices;

        public Graph(){
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
        
        public void addVertex(Vertex v){
            vertices.add(v);
        }
        
        public Vertex getVertex(String vertexName){
            for(Vertex vertex : vertices){
                if (vertex.getName().equalsIgnoreCase(vertexName)){
                    return vertex;
                }
            }
            
            return null;
        }
        
        public void sortVerticesDegreesAsc(){
            Collections.sort(vertices);
        }
        
        public void printGraph(){
            for(Vertex v : vertices){
                System.out.printf("Vertex %s\n", v.getName());
                
                for(Edge e : v.getEdges()){
                    System.out.printf("\tEdge %s -> %s (Cost: %d)\n", e.getOrigin().getName(), e.getDestination().getName(), e.getCost());
                }
                
                System.out.println();
            }
        }
    }
}
