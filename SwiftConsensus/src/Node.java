import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class Node {
	private List<Node> nodes;	// 一开始初始化一堆节点，每次传播时，复制一份
	public int index;
	private int nodeId;
	private List<Integer> adjointNodes;
	public TxPackage tx;	// 本次传递的交易包
	Queue<Node> taskQueue;	// 任务队列
	public Node(List<Node> nodes, int index, int nodeId, Queue<Node> taskQueue) {
		this.nodes = nodes;
		this.index = index;
		this.nodeId = nodeId;
		this.taskQueue = taskQueue;
	}
	
	public void setAdj(List<Integer> adjointNodes) {
		this.adjointNodes = adjointNodes;
	}
	
	public void setTx(TxPackage tx) {
		this.tx = tx;
	}
	
	private void log(String s) {
		/*
		try {
			BufferedWriter out=new BufferedWriter(new FileWriter("log.dat", true));
			out.write(s);
			out.newLine();
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		*/
		//System.out.println(s);
	}
	
	public void transferTx() {
		log("Visit: " + nodeId + " " + index + " ");
		
		tx.getStatistic().addPackage();
		if (tx.getStatistic().nodeFlag[nodeId]) return;
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tx.getDepth(); i++)
			sb.append("\t");
		sb.append(nodeId + " " + tx.getBoundLeft() + "~" + tx.getBoundRight());
		log(sb.toString());
		
		// 设置Flag等
		tx.getStatistic().addData(this);
		int districtNum = Config.distinctNum * Config.distinctNum;
		int leftBound = tx.getBoundLeft();
		int rightBound = tx.getBoundRight();
		List<Pair> optionNodes = new ArrayList<Pair>();
		for (int adjointId : adjointNodes) {
			int index = nodes.get(adjointId).index;
			// 在放进备选队列时，要进行细致的分类，从而实现循环，要进行坐标转换
			if (adjointId != nodeId) {
				if (leftBound < 0 && rightBound < 0) {
					// 坐标在负范围，就添加负坐标
					index -= Config.indexRange;
					if (index >= leftBound && index <= rightBound) 
						optionNodes.add(new Pair(adjointId, index));
				} else if ((leftBound >= 0 && leftBound < Config.indexRange) && 
				(rightBound >= 0 && rightBound < Config.indexRange)) {
					// 坐标都在正常范围内，添加正常nodeId
					if (index >= leftBound && index <= rightBound)
						optionNodes.add(new Pair(adjointId, index));
				} else if (leftBound >= Config.indexRange && rightBound >= Config.indexRange) {
					// 坐标都在右循环范围
					index += Config.indexRange;
					if (index >= leftBound && index <= rightBound)
						optionNodes.add(new Pair(adjointId, index));
				} else if (leftBound < 0 && rightBound >= 0 && rightBound < Config.indexRange) {
					// 左循环，左边负范围，右边正常范围
					if (index - Config.indexRange >= leftBound && index - Config.indexRange < 0)
						optionNodes.add(new Pair(adjointId, index - Config.indexRange));
					else if (index >= 0 && index <= rightBound)
						optionNodes.add(new Pair(adjointId, index));
				} else if (leftBound >= 0 && leftBound < Config.indexRange && rightBound >= Config.indexRange) {
					// 右循环，左边正常范围，右边循环范围
					if (index >= leftBound && index < Config.indexRange)
						optionNodes.add(new Pair(adjointId, index));
					else if (index + Config.indexRange >= Config.indexRange 
							&& index + Config.indexRange <= rightBound)
						optionNodes.add(new Pair(adjointId, Config.indexRange));
				}
			}
		}
		Collections.sort(optionNodes);
		//System.out.println();
		//System.out.print(nodeId + " " + "[" + leftBound + "," + rightBound + "] ");
		if (!optionNodes.isEmpty()) {
			int transferNum = optionNodes.size();
			int partNum = Math.max(transferNum / districtNum, 1);
			int j = 0;
			int l = leftBound;
			int r;
			for (int i = 0; i < districtNum  && j < transferNum; i++) {
				if (i == districtNum - 1 || j == transferNum - partNum)
					r = rightBound;
				else
					r = (optionNodes.get(j).index + optionNodes.get(j + partNum).index) / 2;
				
				int dis = Integer.MAX_VALUE;
				int selectNodeId = -1;
				for (int k = j; k < j + partNum && k < transferNum; k++) {
					int tempDis = Math.abs(optionNodes.get(k).index - (r + l) / 2);
					if (tempDis < dis) {
						dis = tempDis;
						selectNodeId = optionNodes.get(k).nodeId;
					}
				}
				if (selectNodeId != -1) {
					Node n = nodes.get(selectNodeId);
					n.setTx(tx.copy(l, r));
					taskQueue.add(n);
					//System.out.print(n.nodeId + " " + "[" + l + "," + r + "]");
				}
				j += partNum;
				l = r + 1;
			}
			
		}
	}
	
	public int getId() {
		return nodeId;
	}
	
}
