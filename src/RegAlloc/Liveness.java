package RegAlloc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import FlowGraph.FlowGraph;
import Graph.Node;
import Graph.NodeInfo;
import Graph.NodeList;
import Temp.Temp;
import Temp.TempList;

public class Liveness extends InterferenceGraph {

	private FlowGraph flowGraph = null;
	private Hashtable<Node, NodeInfo> node2nodeInfo = new Hashtable<Node, NodeInfo>();
	// 用一个哈希表记录每个点的use\def和in\out迭代信息
	private Hashtable<Node, TempList> liveMap = new Hashtable<Node, TempList>();
	// 用一个哈希表记录每个节点和其对应的活跃寄存器
	private Hashtable<Temp, Node> temp2node = new Hashtable<Temp, Node>();
	private Hashtable<Node, Temp> node2temp = new Hashtable<Node, Temp>();

	public Liveness(FlowGraph flowGraph) {
		this.flowGraph = flowGraph;
		initNodeInfo(); // 初始化
		calculateLiveness(); // 计算活性变量
		buildGraph(); // 生成干扰图
	}

	public MoveList moves() {
		return null;
	}

	private void initNodeInfo() {
		// 初始化
		for (NodeList nodes = flowGraph.nodes(); nodes != null; nodes = nodes.tail) {
			// 将每一个点的def与use输入到哈希表中
			NodeInfo ni = new NodeInfo(nodes.head);
			node2nodeInfo.put(nodes.head, ni);
		}
	}

	void calculateLiveness() {
		boolean done = false;
		do {
			done = true;
			for (NodeList node = flowGraph.nodes(); node != null; node = node.tail) {
				NodeInfo inf = node2nodeInfo.get(node.head);

				Set<Temp> in1 = new HashSet<Temp>(inf.out);
				in1.removeAll(inf.def);
				in1.addAll(inf.use);
				if (!in1.equals(inf.in))
					done = false;
				inf.in = in1;

				Set<Temp> out1 = new HashSet<Temp>();
				for (NodeList succ = node.head.succ(); succ != null; succ = succ.tail) {
					NodeInfo i = node2nodeInfo.get(succ.head);
					out1.addAll(i.in);
				}

				if (!out1.equals(inf.out))
					done = false;

				inf.out = out1;
			}
		} while (!done);

		for (NodeList node = flowGraph.nodes(); node != null; node = node.tail) {
			TempList list = null;
			Iterator<Temp> i = ((NodeInfo) node2nodeInfo.get(node.head)).out.iterator();
			while (i.hasNext())
				list = new TempList((Temp) i.next(), list);
			if (list != null)
				liveMap.put(node.head, list);
		}
	}

	public Node tnode(Temp temp) {
		Node n = (Node) temp2node.get(temp);
		if (n == null)
			// 加新节点.
			return newNode(temp);
		else
			return n;
	}

	public Node newNode(Temp t) {
		Node n = new Node(this);
		temp2node.put(t, n);
		return n;
	}

	void add(Node node, Temp temp) {
		// 在冲突图中加边
		temp2node.put(temp, node);
		node2temp.put(node, temp);
	}

	private void buildGraph() {
		Set temps = new HashSet();
		for (NodeList node = flowGraph.nodes(); node != null; node = node.tail) {
			for (TempList t = flowGraph.use(node.head); t != null; t = t.tail)
				temps.add(t.head);
			for (TempList t = flowGraph.def(node.head); t != null; t = t.tail)
				temps.add(t.head);
		}
		Iterator i = temps.iterator();
		while (i.hasNext())
			add(newNode(), (Temp) i.next());
		for (NodeList node = flowGraph.nodes(); node != null; node = node.tail)
			for (TempList t = flowGraph.def(node.head); t != null; t = t.tail)
				for (TempList t1 = (TempList) liveMap.get(node.head); t1 != null; t1 = t1.tail)
					if (t.head != t1.head
							&& !(flowGraph.isMove(node.head) && flowGraph.use(node.head).head == t1.head)) {
						addEdge(tnode(t.head), tnode(t1.head));
						addEdge(tnode(t1.head), tnode(t.head));
					}
	}

	@Override
	public Temp gtemp(Node node) {
		// 由节点查找相对应的变量
		return node2temp.get(node);
	}
}
