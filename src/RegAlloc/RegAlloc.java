package RegAlloc;

import Assem.InstrList;
import FlowGraph.AssemFlowGraph;
import FlowGraph.FlowGraph;
import Frame.Frame;
import Temp.Temp;
import Temp.TempMap;

public class RegAlloc implements TempMap {
	private Assem.InstrList instrs;
	private Color color;

	public RegAlloc(Frame f, InstrList instrs) {
		this.instrs = instrs;
		FlowGraph flowGraph = new AssemFlowGraph(instrs);// ���ݻ��ָ��������ͼ
		InterferenceGraph interGraph = new Liveness(flowGraph);// ���Է�������ͼ
		color = new Color(interGraph, f, f.registers());// ��ɫ������Ĵ���
	}

	@Override
	public String tempMap(Temp t) {
		return color.tempMap(t);
	}
}
