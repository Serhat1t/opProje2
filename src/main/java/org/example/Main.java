package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
        public class Main {
            public static void main(String[] args) {
                String File = null;
                // parse command line arguments
                for (int i = 0; i < args.length; i++) {
                    if ("-i".equals(args[i])) {
                        if (i < args.length - 1) {
                            File = args[i + 1];
                        }
                    }
                }
                // check if input file is provided
                if (File == null) {
                    System.err.println("Input file not provided.");
                    System.exit(1);
                }
                List<Node> nodes = new ArrayList<>();
                Map<String, Node> nodeMap = new HashMap<>();
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(File));
                    String line = reader.readLine();
                    while (line != null) {
                        String[] split = line.split("->");
                        if (split.length == 2) {
                            String[] dependencies = split[0].split(",");
                            String nodeName = split[1];
                            Node node;
                            if (nodeMap.containsKey(nodeName)) {
                                node = nodeMap.get(nodeName);
                            } else {
                                node = new Node(nodeName);
                                nodes.add(node);
                                nodeMap.put(nodeName, node);
                            }
                            for (String dependency : dependencies) {
                                Node depNode;
                                if (nodeMap.containsKey(dependency)) {
                                    depNode = nodeMap.get(dependency);
                                } else {
                                    depNode = new Node(dependency);
                                    nodes.add(depNode);
                                    nodeMap.put(dependency, depNode);
                                }
                                node.addDependency(depNode);
                            }
                        } else if (split.length == 1) {
                            String nodeName = split[0];
                            Node node;
                            if (nodeMap.containsKey(nodeName)) {
                                nodeMap.get(nodeName);
                            } else {
                                node = new Node(nodeName);
                                nodes.add(node);
                                nodeMap.put(nodeName, node);
                            }
                        }
                        line = reader.readLine();
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (Node node : nodes) {
                    node.start();
                }
                boolean allExecuted = false;
                while (!allExecuted) {
                    allExecuted = true;
                    for (Node node : nodes) {
                        if (!node.isExecuted()) {
                            allExecuted = false;
                            break;
                        }
                    }
                    if (!allExecuted) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        class Node extends Thread {
            private final String name;
            private final List<Node> dependencies;
            private boolean executed;
            public Node(String name) {
                this.name = name;
                dependencies = new ArrayList<>();
                executed = false;
            }
            public void addDependency(Node node) {
                dependencies.add(node);
            }
            public void run() {
                synchronized (dependencies) {
                    List<Node> waitingNodes = new ArrayList<>(dependencies);
                    if (!waitingNodes.isEmpty()) {
                        StringBuilder waitingMsg = new StringBuilder("Node" + name + " is waiting for ");
                        for (int i = 0; i < waitingNodes.size(); i++) {
                            Node node = waitingNodes.get(i);
                            if (node.isExecuted()) {
                                waitingMsg.append(node.name).append(" (completed)");
                            } else {

                                waitingMsg.append(node.name);
                                if (i != waitingNodes.size() - 1) {
                                    waitingMsg.append(", ");
                                }
                            }
                        }
                        System.out.println(waitingMsg + ".");
                    }
                    while (!waitingNodes.isEmpty()) {
                        Iterator<Node> iterator = waitingNodes.iterator();
                        while (iterator.hasNext()) {
                            Node node = iterator.next();
                            if (node.isExecuted()) {
                                iterator.remove();
                            } else {
                                try {
                                    node.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                System.out.println("Node" + name + " is being started.");
                perform();
                executed = true;
                synchronized (dependencies) {
                    dependencies.notifyAll();
                }
            }
            public void perform() {
                Random random = new Random();
                int time = random.nextInt(2000);
                try {
                    Thread.sleep(time);
                    System.out.println("Node" + name + " is completed.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            public boolean isExecuted() {
                return executed;
}
        }
