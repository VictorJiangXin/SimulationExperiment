import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 需要统计到达节点
 * @author xinjiang
 *
 */
public class Statistic {
	private int hopCount;
	private int hop;
	public boolean nodeFlag[];
	
	public Statistic() {
		hopCount = 1;
		hop = 1;
		nodeFlag = new boolean[Config.nodeNum];
		Arrays.fill(nodeFlag, false);
	}
	
	public void addData(Node node) {
		nodeFlag[node.getId()] = true;
		hop = Math.max(hop, node.tx.getDepth() + 1);
	}
	
	public void addPackage() {
		hopCount += 1;
	}
	
	public int getHop() {
		return hop;
	}
	
	public int getHopCount() {
		return hopCount;
	}
	
	public int getCoverage() {
		double visitNodeNum = 0;
		for (boolean flag : nodeFlag) {
			if (flag) visitNodeNum += 1;
		}
		visitNodeNum = 100 * visitNodeNum / Config.nodeNum;
		return (int) visitNodeNum;
	}
	
	public String getResult() {
		double visitNodeNum = 0;
		for (boolean flag : nodeFlag) {
			if (flag) visitNodeNum += 1;
		}
		visitNodeNum = 100 * visitNodeNum / Config.nodeNum;
		
		String result = " NodeCount: " + Config.nodeNum + " "
				+ "LinkNum: " + Config.linkNum + " "
				+ "hop count: " + hopCount + " "
				+ "Max hop: " + hop + " "
				+ "coverage: " + visitNodeNum + "%";
		return result;
	}
	
	
}
