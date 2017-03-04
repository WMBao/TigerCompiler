package FlowGraph;

import java.util.Map;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import Assem.*;
import Graph.*;
import Temp.*;

public class AssemFlowGraph extends FlowGraph {
	Map<Node, Instr> represent = new HashMap<Node, Instr>();

	public AssemFlowGraph(InstrList instrs) {
		Dictionary<Label, Node> labels = new Hashtable<Label, Node>();

		// 把所有指令加为流图节点
		for (InstrList i = instrs; i != null; i = i.tail) {
			Node node = newNode();
			represent.put(node, i.head);
			if (i.head instanceof LABEL)
				labels.put(((LABEL) i.head).label, node);
		}
		// 加边
		for (NodeList node = nodes(); node != null; node = node.tail) {
			Targets next = instr(node.head).jumps();
			if (next == null) {
				if (node.tail != null)
					addEdge(node.head, node.tail.head);
			} else
				for (LabelList l = next.labels; l != null; l = l.tail)
					addEdge(node.head, (Node) labels.get(l.head));
		}

	}

	public Instr instr(Node n) {
		return (Instr) represent.get(n);
	}

	public TempList def(Node node) {
		return instr(node).def();
	}

	public TempList use(Node node) {
		return instr(node).use();
	}

	public boolean isMove(Node node) {
		Instr instr = instr(node);
		return instr.assem.startsWith("move");
	}

}