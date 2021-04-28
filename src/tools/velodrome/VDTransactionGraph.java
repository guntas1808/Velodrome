package tools.velodrome;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class VDTransactionGraph {

  private HashMap<VDTransactionNode, HashSet<VDTransactionNode> > graph;
  
  public VDTransactionGraph() {
    graph = new HashMap<>();
  }

  /**
   * Add an edge to the transaction graph.
   * Also taking care of null boundary cases.
   * @param src Source Node
   * @param dest Destination Node
   */
  public synchronized void addEdge(
    VDTransactionNode src,
    VDTransactionNode dest
  ) {
    if(src == dest || src == null || dest == null)
      return;

    HashSet<VDTransactionNode> neighbours = graph.get(src);

    if(neighbours == null) neighbours = new HashSet<VDTransactionNode>();

    neighbours.add(dest);
    graph.put(src, neighbours);
  }

  // public synchronized void addNode(VDTransactionNode node){
  //   graph.put(node, null);
  //   visited.put(node, false);
  // }


  // private synchronized void resetVisited(){

  //   for(VDTransactionNode node : visited.keySet())
  //     visited.replace(node, false);
      
  // }

  /**
   * Check for a cycle in the graph
   */
  public boolean isCyclic() {
    
    HashSet<VDTransactionNode> visited = new HashSet<>();
    HashSet<VDTransactionNode> active = new HashSet<>();

    for(VDTransactionNode node : graph.keySet()){
      if(dfsUtil(node, visited, active)){
        // System.out.println(node.getLabel());
        return true;
      }
    }

    return false;
  }

  /**
   * Utility function for DFS traversal
   * @param node
   * @return
   */
  private boolean dfsUtil(VDTransactionNode node, HashSet<VDTransactionNode> visited, HashSet<VDTransactionNode> active){
    // System.out.println("call " + node.getLabel());
    
    if(!visited.contains(node)){  
      HashSet<VDTransactionNode> neighbours = graph.get(node);

      // if(neighbours == null || neighbours.isEmpty())
      //   return false;
      
      visited.add(node);
      active.add(node);

      if(neighbours == null){
        // System.out.println("return false " + node.getLabel());
        active.remove(node);
        return false;
      }

      for(VDTransactionNode neighbour : neighbours ){
        if(!visited.contains(neighbour) && dfsUtil(neighbour, visited, active)){
          // System.out.println("return true " + node.getLabel());
          return true;
        }else if(active.contains(neighbour)){
          // System.out.println("return true " + node.getLabel());
          return true;
        }
      }
    }
    active.remove(node);
    // System.out.println("return false " + node.getLabel());
    return false;
  }

  /**
   * Dump the whole graph into a file in DOT format
   */
  public void dump(){
    
    try {
      File outfile = new File("TXgraph.dot");
      
      if(outfile.exists())      
        outfile.delete();


      if(outfile.createNewFile()){
        System.err.println("File created " + outfile.getName());
      }else{
        System.err.println("Erorr in file creation");
      }
      
      FileWriter fout = new FileWriter(outfile);
      fout.write("digraph G { \n");

      for (Map.Entry<VDTransactionNode, HashSet<VDTransactionNode>> entry : graph.entrySet()) {
        VDTransactionNode node1 = (VDTransactionNode)entry.getKey();
        HashSet<VDTransactionNode> edges = (HashSet<VDTransactionNode>)entry.getValue();

        // fout.write("  " + node1.getLabel() + " [ label = \"" + node1.getMethodName() + "\" ];");
        
        if(edges == null)
          continue;

        for(VDTransactionNode node2 : edges ){
          fout.write("  " + node1.getMethodName() + " -> " + node2.getMethodName() + ";\n");
        }
      }

      fout.write("} \n");

      fout.close();
      
    } catch (IOException e) {
      System.out.println("An error occurred while dumping transaction graph.");
      e.printStackTrace();
    }
  }

}
