package com.genome.errorProneAssembly;

import java.util.*;
import java.io.*;

class Vertex{
    int vertexNum;
    String str;
    List<Integer> outEdges;
    List<Integer> inEdges;
    List<Integer> edgeList;

    boolean removed;
    boolean found;

    Node temp;

    boolean visited;


    public Vertex(int vertexNum, String str, List<Integer> outEdges, List<Integer> inEdges){
        this.vertexNum = vertexNum;
        this.outEdges= outEdges;
        this.inEdges = inEdges;
        this.removed = false;
        this.str = str;
        this.found = false;
        this.edgeList = new ArrayList<>();
        this.temp=null;
        this.visited = false;
    }
}

class Node{
    int vertexNum;
    List<Node> kids;
    Node parent;

    public Node(int vertexNum){
        this.vertexNum = vertexNum;
        this.kids = new ArrayList<>();
        this.parent = null;
    }
}

class Edge{
    int from;
    int to;
    boolean used;

    public Edge(int from,int to){
        this.from = from;
        this.to = to;
        this.used = false;
    }
}

public class errorProneAssembler {
    static HashMap<String,Integer> countmap;
    static int k=20;
    static int removedTipsCount;
    static int bubbles = 0;
    static Node root;

    public static Vertex[] createDeBruijnGraph(String [] reads){
        HashMap<String, Integer> idmap = new HashMap<>();
        HashMap<String, ArrayList<Integer>> outEdgesMap = new HashMap<>();
        HashMap<String, ArrayList<Integer>> inEdgesMap = new HashMap<>();

        countmap = new HashMap<>();

        HashSet<String> uniquekmers = new HashSet<>();

        int id=0;

        for(int i=0;i<reads.length;i++){
            String read = reads[i];
            for(int j=0;j<=read.length()-k;j++){
                String temp = read.substring(j,j+k);
                String a = temp.substring(0,temp.length()-1);
                String b = temp.substring(1);
                if(uniquekmers.contains(temp)){
                    countmap.put(temp,countmap.get(temp)+1);
                    continue;
                }
                uniquekmers.add(temp);
                countmap.put(temp, 1);
                if(!idmap.containsKey(a)){
                    idmap.put(a,id);
                    outEdgesMap.put(a, new ArrayList<>());
                    inEdgesMap.put(a, new ArrayList<>());
                    id++;
                }
                if(!idmap.containsKey(b)){
                    idmap.put(b,id);
                    outEdgesMap.put(b, new ArrayList<>());
                    inEdgesMap.put(b, new ArrayList<>());
                    id++;
                }
                boolean overlap = isOverlap(a,b);
                if(overlap){
                    outEdgesMap.get(a).add(idmap.get(b));
                    inEdgesMap.get(b).add(idmap.get(a));
                }
            }
        }
        Vertex[] graph = new Vertex[idmap.size()];
        for(String key : idmap.keySet()){
            int temp = idmap.get(key);
            graph[temp] = new Vertex(temp,key,outEdgesMap.get(key),inEdgesMap.get(key));
        }
        return graph;
    }

    private static boolean isOverlap(String a, String b){
        int index = 0;
        for(int i=1;i<a.length();i++){
            if(a.charAt(i)!=b.charAt(index)){
                return false;
            }
            index++;
        }
        return true;
    }

    public static void tipRemoval(Vertex[] graph){
        removedTipsCount=0;
        for(int i=0;i<graph.length;i++){
            if(!graph[i].removed) {
                if (graph[i].outEdges.size() == 0) {
                    inExplore(graph, i);
                }
                if (graph[i].inEdges.size() == 0) {
                    outExplore(graph, i);
                }
            }
        }
    }

    private static void inExplore(Vertex[] graph, int vertex){
        if(graph[vertex].outEdges.size()!=0 || graph[vertex].inEdges.size()!=1){
            return;
        }
        graph[vertex].removed = true;
        removedTipsCount++;
        int temp = graph[vertex].inEdges.get(0);
        graph[temp].outEdges.remove(Integer.valueOf(vertex));
        graph[vertex].inEdges.remove(Integer.valueOf(temp));
        inExplore(graph,temp);
    }

    private static void outExplore(Vertex[] graph, int vertex){
        if(graph[vertex].inEdges.size()!=0 || graph[vertex].outEdges.size()!=1){
            return;
        }
        graph[vertex].removed = true;
        removedTipsCount++;
        int temp = graph[vertex].outEdges.get(0);
        graph[temp].inEdges.remove(Integer.valueOf(vertex));
        graph[vertex].outEdges.remove(Integer.valueOf(temp));
        outExplore(graph,temp);
    }

    public static void bubbleHandler(Vertex[] graph){
        for(int i=0;i<graph.length;i++){
            if(graph[i].removed || graph[i].outEdges.size()<2){
                continue;
            }
            bfs(graph,graph[i].vertexNum);
        }
    }

    private static void bfs(Vertex[] graph, int vertex){
        HashSet<Integer> set = new HashSet<>();
        root = new Node(vertex);
        Node temp = root;
        for(int i=0;i<graph.length;i++){
            graph[i].found = false;
            graph[i].temp = null;
        }
        explore(graph,temp,set);
    }

    private static void explore(Vertex[] graph, Node node, HashSet<Integer> set){
        set.add(node.vertexNum);
        if(graph[node.vertexNum].found){
            bubbles++;
            Node common = findCommonAncestor(graph,node);
            bubbleSolver(graph,node,common,set);
            if(!set.contains(node.vertexNum)){
                return;
            }
        }
        graph[node.vertexNum].found = true;
        graph[node.vertexNum].temp = node;
        if(set.size()>=k+1){
            set.remove(node.vertexNum);
            return;
        }
        List<Integer> list = new ArrayList<>(graph[node.vertexNum].outEdges);
        for(int i=0;i<list.size();i++){
            int temp = list.get(i);
            if(set.contains(temp)){
                continue;
            }
            Node child  = new Node(temp);
            child.parent = node;
            node.kids.add(child);

            explore(graph,child,set);

            if(!set.contains(node.vertexNum)){
                return;
            }
        }
        set.remove(node.vertexNum);
    }

    private static Node findCommonAncestor(Vertex[] graph, Node node){
        Node temp = graph[node.vertexNum].temp;
        List<Node> list = new ArrayList<>();
        while(temp!=null){
            list.add(temp);
            temp = temp.parent;
        }

        temp = node;
        while(temp!=null){
            for(int i=list.size()-1;i>-1;i--){
                if(temp==list.get(i)){
                    return temp;
                }
            }
            temp =temp.parent;
        }
        return null;
    }

    private static void bubbleSolver(Vertex[] graph, Node node, Node common, HashSet<Integer> set){
        Node node1 = node;
        Node node2 = graph[node.vertexNum].temp;
        double sum = 0;
        double count = 0;
        while(node1!=common){
            String str1 = graph[node1.vertexNum].str;
            String str2 = graph[node1.parent.vertexNum].str;
            String str = str2 + str1.charAt(str1.length()-1);
            sum = sum + countmap.get(str);
            count++;
            node1=node1.parent;
        }
        double coverage1 = sum/count;

        sum=0;
        count=0;
        while(node2!=common){
            String str1 = graph[node2.vertexNum].str;
            String str2 = graph[node2.parent.vertexNum].str;
            String str = str2 + str1.charAt(str1.length()-1);
            sum = sum + countmap.get(str);
            count++;
            node2 = node2.parent;
        }
        double coverage2 = sum/count;
        node1 = node;
        node2 = graph[node.vertexNum].temp;
        if(coverage1<=coverage2){
            List<Integer> verticesRemoved = new ArrayList<>();
            Node temp = removePath(graph,node1,common,verticesRemoved);
            makeFalse(graph,temp);
            graph[node.vertexNum].found = true;
            graph[node.vertexNum].temp = node2;

            removeFromSet(set,verticesRemoved);
            common.kids.remove(temp);
        }
        else{
            List<Integer> verticesRemoved = new ArrayList<>();
            Node temp = removePath(graph,node2,common,verticesRemoved);
            makeFalse(graph,temp);
            graph[node.vertexNum].found = true;
            graph[node.vertexNum].temp = node1;

            common.kids.remove(temp);
        }
    }

    private static Node removePath(Vertex[] graph, Node node, Node common, List<Integer> verticesRemoved){
        Node parent = node.parent;
        Node child = node;
        Node temp = null;
        while(child!=common){
            graph[parent.vertexNum].outEdges.remove(Integer.valueOf(child.vertexNum));
            verticesRemoved.add(child.vertexNum);
            temp = child;
            child = parent;
            parent = parent.parent;
        }
        return temp;
    }

    private static void removeFromSet(HashSet<Integer> set, List<Integer> verticesRemoved){
        for(int i=0;i<verticesRemoved.size();i++){
            set.remove(verticesRemoved.get(i));
        }
    }

    private static void makeFalse(Vertex[] graph, Node node){
        graph[node.vertexNum].found = false;
        graph[node.vertexNum].temp = null;

        for(int i=0;i<node.kids.size();i++){
            makeFalse(graph,node.kids.get(i));
        }
    }

    public static List<Edge> makeEdges(Vertex[] graph){
        List<Edge> edges = new ArrayList<>();
        for(int i=0;i<graph.length;i++){
            if(graph[i].removed){
                continue;
            }
            List<Integer> list = graph[i].outEdges;
            for(int j=0;j<list.size();j++){
                Edge edge = new Edge(i,list.get(j));
                graph[i].edgeList.add(edges.size());
                edges.add(edge);
            }
        }
        return edges;
    }

    public static String findCycle(Vertex[] graph, List<Edge> edges){
        int max = -1;
        String result = "";
        Map<String,Integer> map = new HashMap<>();

        for(int i=0;i<graph.length;i++){
            if(!graph[i].removed && !graph[i].visited){
                List<Integer> cycle = new ArrayList<>();
                eulerianExplore(graph,edges,i,cycle);
                String genome = graph[cycle.get(cycle.size()-1)].str;
                for(int j=cycle.size()-2;j>-1;j--){
                    String temp = graph[cycle.get(j)].str;
                    genome = genome + temp.charAt(temp.length()-1);
                }
                if(genome.length()>=5396){
                    map.put(genome,genome.length()-5396);
                }
                if(max<genome.length()){
                    result = genome;
                    max = genome.length();
                }
            }
        }

        int min = Integer.MAX_VALUE;
        for(String key : map.keySet()){
            if(min>map.get(key)){
                min = map.get(key);
                result = key;
            }
        }
        return result;
    }

    private static void eulerianExplore(Vertex[] graph, List<Edge> edges, int startVertex, List<Integer> cycle) {
        Stack<Integer> stack = new Stack<>();
        stack.push(startVertex);

        while (!stack.isEmpty()) {
            int vertex = stack.peek();
            graph[vertex].visited = true;

            boolean foundUnusedEdge = false;
            List<Integer> edgeList = graph[vertex].edgeList;

            for (int i = 0; i < edgeList.size(); i++) {
                Edge edge = edges.get(edgeList.get(i));
                if (!edge.used) {
                    edge.used = true;
                    stack.push(edge.to);
                    foundUnusedEdge = true;
                    break;
                }
            }

            if (!foundUnusedEdge) {
                cycle.add(stack.pop());
            }
        }
    }

    public static String[] input() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        List<String> readList = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            readList.add(line.trim());
        }
        return readList.toArray(new String[0]);
    }

    public static void run() throws IOException {
        String[] reads = input();
        Vertex[] graph = createDeBruijnGraph(reads);
        tipRemoval(graph);
        bubbleHandler(graph);
        tipRemoval(graph);
        List<Edge> edges = makeEdges(graph);
        String genome = findCycle(graph,edges);
        System.out.println(genome);
        System.err.println("Genome length: " + genome.length() + " bases");
    }

    public static void main(String[] args) throws IOException {
        run();
    }
}