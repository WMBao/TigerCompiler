package RegAlloc;

import Graph.*;
import Temp.*;
import java.util.*;

public class Color implements TempMap {
	private Stack selStack = new Stack();
	private Hashtable<Temp, Temp> map = new Hashtable<Temp, Temp>();
	private TempMap init;

	// public TempList spills();
	// 这里对于溢出的情况就不做特别处理了,一般不会溢出,若溢出直接报错
	public String tempMap(Temp t) {
		// 查找tempmap返回变量对应的寄存器的真实名称
		return init.tempMap(map.get(t));
	}

	public Color(InterferenceGraph interGraph, TempMap init, HashSet registers) {
		// 用着色法分配寄存器,最终结果放入tempmap中
		HashSet regs = new HashSet(registers);
		this.init = init;

		int number = 0;
		// 预处理,处理已被分配的寄存器的变量
		for (NodeList nodes = interGraph.nodes(); nodes != null; nodes = nodes.tail) {
			// 遍历每个临时变量结点
			++number;
			Temp temp = interGraph.gtemp(nodes.head);
			/// 得到结点所对应的临时变量 temp
			if (init.tempMap(temp) != null) {
				// 如果 temp 已经被分配了寄存器
				--number;
				selStack.add(nodes.head);
				// 将该节点压栈
				map.put(temp, temp);
				// 放入分配列表 map 中，它们的寄存器就是其本身
				for (NodeList adj = nodes.head.succ(); adj != null; adj = adj.tail)
					interGraph.rmEdge(nodes.head, adj.head);
				// 删除从该结点出发的所有边
			}
		}
		// 处理剩下的number个还未分配寄存器的变量
		for (int i = 0; i < number; ++i) {
			Node node = null;
			int max = -1;

			for (NodeList n = interGraph.nodes(); n != null; n = n.tail)
				// 再次遍历每个临时变量结点
				if (init.tempMap(interGraph.gtemp(n.head)) == null && !selStack.contains(n.head)) {
					// 若没有被分配寄存器且不在堆栈中
					int num = n.head.outDegree(); // 得到节点的出度
					if (max < num && num < regs.size()) {
						// 找到一个度最大的且小于寄存器数目的结点,这样有利于以后的color不会溢出,是一种贪心的做法
						max = num;
						node = n.head;
					}
				}
			if (node == null) {
				// 度大于等于寄存器数目，溢出,这里只是简单地报错
				System.err.println("Color.color() : 溢出");
				break;
			}

			selStack.add(node);
			// 否则继续推入堆栈并移去从不在堆栈中的结点指向该结点的所有边
			for (NodeList adj = node.pred(); adj != null; adj = adj.tail)
				if (!selStack.contains(adj.head))
					interGraph.rmEdge(adj.head, node);
		}
		// 接下来开始分配那 number 个没有分配寄存器的临时变量，它们处在栈顶
		for (int i = 0; i < number; ++i) {
			Node node = (Node) selStack.pop();
			// 弹栈
			Set available = new HashSet(regs);
			// 可供分配的寄存器列表
			for (NodeList adj = node.succ(); adj != null; adj = adj.tail) {
				available.remove(map.get(interGraph.gtemp(adj.head)));
				// 从可供分配的寄存器列表中移除该结点指向的某个结点所代表的寄存器(即与之冲突的变量的寄存器)
			}
			Temp reg = (Temp) available.iterator().next();
			// 取剩下的一个作为寄存器
			map.put(interGraph.gtemp(node), reg);
			// 加入寄存器分配表
		}
	}
}
