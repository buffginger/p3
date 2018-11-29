package com.company;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

import java.io.IOException;
import java.util.Arrays;

// Input: ./p3 <GMLfile> <k iterations> <s scale down factor>
public class p3 {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Please enter 3 args: <GMLfile> <k iterations> <s scale down factor>");
            System.exit(1);
        }
        int k = Integer.parseInt(args[1]) + 1;
        double s = Double.parseDouble(args[2]);
        String file = args[0];

        if (k < 1 || s > 1 || s <= 0) {
            System.out.println("Iterations k should be greater than 1 and/or scale down s should be 1 > s > 0");
        }
//        int k = 2;
//        double s = 0.8;
//        String file = "/Users/ethananderson/Downloads/graph-Fig14.6.gml";


        // Read the GML file
        Graph g = new DefaultGraph("g");
        FileSource fs = FileSourceFactory.sourceFor(file);
        fs.addSink(g);

        // Create a graph from the GML file
        try {
            fs.readAll(file);
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            fs.removeSink(g);
        }

        // Create adjacency matrix
        int n = g.getNodeCount();
        byte adjacencyMatrix[][] = new byte[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                adjacencyMatrix[i][j] = g.getNode(i).hasEdgeBetween(j) ? (byte) 1 : (byte) 0;
            }
        }

        // Initial assignment of 1/n
        double pageRankMatrix[][] = new double[n][k + 1];
        for (int i = 0; i < n; i++) {
            pageRankMatrix[i][0] = (double) 1 / n;
        }

        double currentRank;
        double outgoingRank;
        Node column[] = new Node [0];

        // Perform sequence of k updates.   i = iteration, j = node
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                // If page has no outgoing links, pass current PageRank to itself
                if (g.getNode(j).getOutDegree() == 0) {
                    pageRankMatrix[j][i + 1] += pageRankMatrix[j][i];
                    continue;
                }
                // Each page divides its current PageRank equally across its outoging links
                currentRank = pageRankMatrix[j][i];
                outgoingRank = currentRank / g.getNode(j).getOutDegree();

                // for each edge for node j
                for (int w = 0; w < n; w++) { // adj matrix
                    if (adjacencyMatrix[j][w] == 1) {
                        pageRankMatrix[w][i + 1] += outgoingRank;
                    }
                }
            }
            // Apply Scale down to each node
            for (int f = 0; f < n; f++) {
                pageRankMatrix[f][i + 1] = pageRankMatrix[f][i + 1] * s + ((1 - s) / n);
            }
        }
        // Sort based on the last value
        column = getColumn(pageRankMatrix, k);
        Arrays.sort(column);

        // Print the appropriate list by iterating through the sorted list
        for (int i = 0; i < column.length; i++) {
            // print the row of pagerank at index i
            System.out.printf("Node %d: ", column[i].getName() + 1);

            for (int j = 0; j < pageRankMatrix[i].length; j++) {
                System.out.printf("%.8f, ", pageRankMatrix[column[i].getName()][j]);
            }
            System.out.println();
        }
    }

    public static Node[] getColumn(double[][] array, int index){
        Node[] column = new Node[array.length];
        for(int i=0; i<column.length; i++){
            Node node = new Node(i, array[i][index]);
            column[i] = node;
        }
        return column;
    }
}

class Node implements Comparable<Node> {
    int name;

    public double getValue() {
        return value;
    }

    double value;

    Node (int name, double value) {
        this.name = name;
        this.value = value;
    }
    public int getName() {
        return name;
    }

    @Override
    public int compareTo(Node o) {
        if (this.getValue() > o.getValue()) return -1;
        if (this.getValue() < o.getValue()) return 1;
        else return 0;
    }
}
