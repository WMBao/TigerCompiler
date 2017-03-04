package RegAlloc;

import Graph.*;
import Temp.*;
import java.util.*;

public class Color implements TempMap {
	private Stack selStack = new Stack();
	private Hashtable<Temp, Temp> map = new Hashtable<Temp, Temp>();
	private TempMap init;

	// public TempList spills();
	// ����������������Ͳ����ر�����,һ�㲻�����,�����ֱ�ӱ���
	public String tempMap(Temp t) {
		// ����tempmap���ر�����Ӧ�ļĴ�������ʵ����
		return init.tempMap(map.get(t));
	}

	public Color(InterferenceGraph interGraph, TempMap init, HashSet registers) {
		// ����ɫ������Ĵ���,���ս������tempmap��
		HashSet regs = new HashSet(registers);
		this.init = init;

		int number = 0;
		// Ԥ����,�����ѱ�����ļĴ����ı���
		for (NodeList nodes = interGraph.nodes(); nodes != null; nodes = nodes.tail) {
			// ����ÿ����ʱ�������
			++number;
			Temp temp = interGraph.gtemp(nodes.head);
			/// �õ��������Ӧ����ʱ���� temp
			if (init.tempMap(temp) != null) {
				// ��� temp �Ѿ��������˼Ĵ���
				--number;
				selStack.add(nodes.head);
				// ���ýڵ�ѹջ
				map.put(temp, temp);
				// ��������б� map �У����ǵļĴ��������䱾��
				for (NodeList adj = nodes.head.succ(); adj != null; adj = adj.tail)
					interGraph.rmEdge(nodes.head, adj.head);
				// ɾ���Ӹý����������б�
			}
		}
		// ����ʣ�µ�number����δ����Ĵ����ı���
		for (int i = 0; i < number; ++i) {
			Node node = null;
			int max = -1;

			for (NodeList n = interGraph.nodes(); n != null; n = n.tail)
				// �ٴα���ÿ����ʱ�������
				if (init.tempMap(interGraph.gtemp(n.head)) == null && !selStack.contains(n.head)) {
					// ��û�б�����Ĵ����Ҳ��ڶ�ջ��
					int num = n.head.outDegree(); // �õ��ڵ�ĳ���
					if (max < num && num < regs.size()) {
						// �ҵ�һ����������С�ڼĴ�����Ŀ�Ľ��,�����������Ժ��color�������,��һ��̰�ĵ�����
						max = num;
						node = n.head;
					}
				}
			if (node == null) {
				// �ȴ��ڵ��ڼĴ�����Ŀ�����,����ֻ�Ǽ򵥵ر���
				System.err.println("Color.color() : ���");
				break;
			}

			selStack.add(node);
			// ������������ջ����ȥ�Ӳ��ڶ�ջ�еĽ��ָ��ý������б�
			for (NodeList adj = node.pred(); adj != null; adj = adj.tail)
				if (!selStack.contains(adj.head))
					interGraph.rmEdge(adj.head, node);
		}
		// ��������ʼ������ number ��û�з���Ĵ�������ʱ���������Ǵ���ջ��
		for (int i = 0; i < number; ++i) {
			Node node = (Node) selStack.pop();
			// ��ջ
			Set available = new HashSet(regs);
			// �ɹ�����ļĴ����б�
			for (NodeList adj = node.succ(); adj != null; adj = adj.tail) {
				available.remove(map.get(interGraph.gtemp(adj.head)));
				// �ӿɹ�����ļĴ����б����Ƴ��ý��ָ���ĳ�����������ļĴ���(����֮��ͻ�ı����ļĴ���)
			}
			Temp reg = (Temp) available.iterator().next();
			// ȡʣ�µ�һ����Ϊ�Ĵ���
			map.put(interGraph.gtemp(node), reg);
			// ����Ĵ��������
		}
	}
}
