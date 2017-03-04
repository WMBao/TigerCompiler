package Graph;

import java.util.HashSet;
import java.util.Set;

import FlowGraph.FlowGraph;
import Graph.Node;
import Temp.Temp;
import Temp.TempList;

public class NodeInfo {
	public Set<Temp> in = new HashSet<Temp>(); // 来指令前的活性变量
	public Set<Temp> out = new HashSet<Temp>(); // 出指令后的活性变量 － 即活跃变量
	public Set<Temp> use = new HashSet<Temp>(); // 某指令使用的变量 - 赋值号右边
	public Set<Temp> def = new HashSet<Temp>(); // 某指令定义的变量 - 赋值号左边

	NodeInfo(TempList u, TempList d) {
		for (TempList t = u; t != null; t = t.tail)
			use.add(t.head);
		for (TempList t = d; t != null; t = t.tail)
			def.add(t.head);
	}

	public NodeInfo(Node n) {
		for (TempList t = ((FlowGraph) n.mygraph).use(n); t != null; t = t.tail)
			use.add(t.head);
		for (TempList t = ((FlowGraph) n.mygraph).def(n); t != null; t = t.tail)
			def.add(t.head);
	}
}