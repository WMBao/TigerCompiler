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
		FlowGraph flowGraph = new AssemFlowGraph(instrs);// 根据汇编指令生成流图
		InterferenceGraph interGraph = new Liveness(flowGraph);// 活性分析干扰图
		color = new Color(interGraph, f, f.registers());// 着色法分配寄存器
	}

	@Override
	public String tempMap(Temp t) {
		return color.tempMap(t);
	}
}
